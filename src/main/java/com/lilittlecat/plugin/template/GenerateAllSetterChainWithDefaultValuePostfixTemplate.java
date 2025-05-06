package com.lilittlecat.plugin.template;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.util.PsiTypesUtil;
import com.lilittlecat.plugin.util.PsiClassUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.lilittlecat.plugin.common.Constants.*;
import static com.lilittlecat.plugin.util.PsiClassUtil.*;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Generate all setter methods with default values and chain style (method returns this)
 *
 * @author LiLittleCat
 * @since 2022/4/16
 */
public class GenerateAllSetterChainWithDefaultValuePostfixTemplate extends BaseGeneratePostfixTemplate {

    public GenerateAllSetterChainWithDefaultValuePostfixTemplate(PostfixTemplateProvider provider) {
        super(null, ALL_SETTER_CHAIN_WITH_DEFAULT_VALUE_SUFFIX, ALL_SETTER_CHAIN_WITH_DEFAULT_VALUE_INFO, provider);
    }

    /**
     * new import set
     */
    Set<String> newImportSet = new HashSet<>();

    /**
     * 标记模板是否只包含注释
     */
    private boolean hasOnlyComments = false;

    @Override
    protected String buildTemplateString(@NotNull PsiElement expression,
                                         @NotNull Document document,
                                         @NotNull List<PsiMethod> methods,
                                         @NotNull List<PsiField> fields) {
        StringBuilder builder = new StringBuilder();
        String defaultValue = "";
        // 初始化标记
        hasOnlyComments = true;

        if (methods.isEmpty()) {
            builder.append("// No setter methods found in ").append(expression.getText()).append("\n");
            builder.append("$END$");
            return builder.toString();
        }

        // Start with the expression
        String expressionText = expression.getText();

        for (PsiMethod setterMethod : methods) {
            // 检查是否是set()方法（没有对应字段名）
            String fieldName = getFieldNameInMethod(setterMethod, SET_METHOD_TYPE);
            if ("__empty__".equals(fieldName)) {
                // 如果字段名是特殊标记，跳过此方法
                continue;
            }

            // parameters of setter method.
            PsiParameter parameter = setterMethod.getParameterList().getParameters()[0];
            PsiType parameterType = parameter.getType();

            // 首先检查参数是否直接是类型参数(如C, T等)
            if (hasGenericType) {
                if (parameterType instanceof PsiClassType) {
                    PsiClass paramClass = ((PsiClassType) parameterType).resolve();
                    if (paramClass instanceof PsiTypeParameter) {
                        Map<String, String> typeMap = genericTypeMap.get(((PsiExpression) expression).getType().getCanonicalText().split("<")[0]);
                        if (typeMap != null && !typeMap.isEmpty()) {
                            String resolvedType = resolveGenericParameterType(parameterType.getCanonicalText(), typeMap, (PsiTypeParameter) paramClass);
                            if (!resolvedType.equals(parameterType.getCanonicalText())) {
                                // 如果解析出了具体类型，尝试使用该类型创建默认值
                                if (!resolvedType.contains(".")) {
                                    // 简单类型，如Integer、String等
                                    defaultValue = createBasicTypeDefaultValue(resolvedType);
                                } else {
                                    // 查找是否有预定义的默认值
                                    String staticDefaultValue = DEFAULT_VALUE_MAP.get(resolvedType);
                                    if (isNotBlank(staticDefaultValue)) {
                                        defaultValue = staticDefaultValue;
                                    } else {
                                        // 复杂类型，尝试创建实例
                                        defaultValue = "new " + getClassName(resolvedType) + "()";
                                        newImportSet.add(resolvedType);
                                    }
                                }
                                if (builder.length() == 0) {
                                    builder.append(expressionText).append(".").append(setterMethod.getName())
                                        .append("(").append(defaultValue).append(")");
                                } else {
                                    builder.append("\n    .").append(setterMethod.getName())
                                        .append("(").append(defaultValue).append(")");
                                }
                                hasOnlyComments = false;
                                continue;
                            } else {
                                // 泛型参数未知，添加注释
                                builder.append("\n    // Generic type parameter ").append(parameterType.getCanonicalText())
                                       .append(" is unknown. Please specify the generic type for ")
                                       .append(setterMethod.getName()).append(".");
                                continue;
                            }
                        }
                    }
                }
            }

            if (parameterType instanceof PsiArrayType) {
                // parameter type is array type.
                String text = parameterType.getCanonicalText();
                int i = text.indexOf("<");
                // array type don't need implementation, we can use interface such as "new Map[]",
                // users should adjust it after auto insert set value.
                String qualifiedName;
                if (i == -1) {
                    qualifiedName = text.replace("[]", "");
                } else {
                    // parameter type is generic
                    qualifiedName = text.substring(0, i);
                }
                if (isNotBlank(qualifiedName)) {
                    newImportSet.add(qualifiedName);
                    defaultValue = "new " + getClassName(qualifiedName) + "[0]";
                }
            } else if (parameterType instanceof PsiClassReferenceType) {
                // parameter type is class reference type.
                PsiClass parameterClass = PsiTypesUtil.getPsiClass(parameterType);
                if (parameterClass == null) {
                    continue;
                }
                String qualifiedName = parameterClass.getQualifiedName();
                if (isBlank(qualifiedName)) {
                    continue;
                }
                String text = parameterType.getCanonicalText();
                int i = text.indexOf("<");
                if (i == -1) {
                    newImportSet.add(qualifiedName);
                } else {
                    // parameter type is generic
                    String fieldType = text.substring(0, i);
                    newImportSet.add(getRealImport(fieldType));

                    // 只有当类定义中包含泛型参数时才进行处理
                    if (hasGenericType && text.contains("<")) {
                        Map<String, String> typeMap = genericTypeMap.get(((PsiExpression) expression).getType().getCanonicalText().split("<")[0]);
                        if (typeMap != null && !typeMap.isEmpty()) {
                            // 首先检查类型中是否包含了类定义的泛型参数
                            boolean containsClassTypeParameter = false;

                            // 获取类的所有类型参数名称
                            Set<String> classTypeParamNames = new HashSet<>();
                            if (expression instanceof PsiExpression) {
                                PsiType exprType = ((PsiExpression) expression).getType();
                                if (exprType instanceof PsiClassType) {
                                    PsiClass psiClass = ((PsiClassType) exprType).resolve();
                                    if (psiClass != null) {
                                        PsiTypeParameter[] typeParameters = psiClass.getTypeParameters();
                                        for (PsiTypeParameter tp : typeParameters) {
                                            classTypeParamNames.add(tp.getName());
                                        }
                                    }
                                }
                            }

                            // 检查参数类型是否包含类的类型参数
                            for (String typeParamName : classTypeParamNames) {
                                if (text.contains("<" + typeParamName + ">") ||
                                    text.contains("<" + typeParamName + ",") ||
                                    text.contains(", " + typeParamName + ">") ||
                                    text.contains(", " + typeParamName + ",")) {
                                    containsClassTypeParameter = true;
                                    break;
                                }
                            }

                            if (containsClassTypeParameter) {
                                // 尝试解析泛型类型
                                String resolvedType = resolveNestedGenericType(text, typeMap);
                                if (!resolvedType.equals(text)) {
                                    // 如果成功解析了泛型类型

                                    // 如果处理后类型中不包含泛型，则使用基本的默认值
                                    if (!resolvedType.contains("<")) {
                                        String staticDefaultValue = DEFAULT_VALUE_MAP.get(fieldType);
                                        if (isNotBlank(staticDefaultValue)) {
                                            defaultValue = staticDefaultValue;
                                        } else {
                                            defaultValue = "new " + getClassName(fieldType) + "()";
                                        }
                                    } else {
                                        // 从嵌套泛型中提取实际类型参数
                                        Matcher genericMatcher = genericTypePattern.matcher(resolvedType);
                                        if (genericMatcher.find()) {
                                            String actualTypeParam = genericMatcher.group(1);
                                            // 根据类型创建合适的默认值
                                            String genericDefaultValue = createGenericDefaultValue(fieldType, actualTypeParam);
                                            if (genericDefaultValue != null) {
                                                defaultValue = genericDefaultValue;
                                            }
                                        }
                                    }
                                } else {
                                    // 无法解析泛型类型，添加注释
                                    builder.append("\n    // Cannot resolve generic types in ").append(text)
                                           .append(" that use class type parameters. Please use a parameterized type for ")
                                           .append(expression.getText()).append(".");
                                    continue;
                                }
                            }
                        }
                    }
                }

                if (parameterClass.isEnum()) {
                    // field is enum
                    PsiField[] enumList = parameterClass.getFields();
                    if (enumList.length != 0) {
                        defaultValue = getClassName(qualifiedName) + "." + enumList[0].getName();
                    }
                } else if (parameterClass.isAnnotationType()) {
                    // nothing to do if field is annotation, can this happen?
                } else {
                    // field is class or interface like "java.lang.List" or "java.util.Set"
                    String staticDefaultValue = DEFAULT_VALUE_MAP.get(qualifiedName);
                    if (isNotBlank(staticDefaultValue)) {
                        defaultValue = staticDefaultValue;
                    } else {
                        defaultValue = "new " + getClassName(qualifiedName) + "()";
                    }
                }
            } else if (parameterType instanceof PsiPrimitiveType) {
                // parameter type is primitive type.
                String text = parameterType.getCanonicalText();
                String staticDefaultValue = DEFAULT_VALUE_MAP.get(text);
                if (isNotBlank(staticDefaultValue)) {
                    defaultValue = staticDefaultValue;
                } else {
                    defaultValue = "0";
                }
            } else {
                // nothing to do.
                continue;
            }
            if (builder.length() == 0) {
                builder.append(expressionText).append(".").append(setterMethod.getName())
                    .append("(").append(defaultValue).append(")");
            } else {
                builder.append("\n    .").append(setterMethod.getName())
                    .append("(").append(defaultValue).append(")");
            }
            hasOnlyComments = false;
        }

        builder.append(";\n$END$");

        // import
        importNewClass(expression, document);
        // clear for next time use
        newImportSet.clear();
        return builder.toString();
    }

    @Override
    protected Template modifyTemplate(Template template) {
        // 如果模板只包含注释，添加一个隐藏的占位变量
        if (hasOnlyComments) {
            // 添加一个隐藏变量，但不会在编辑器中显示
            template.addVariable("_dummy", "\"\"", "\"\"", false);
        }
        return template;
    }

    @Override
    protected List<PsiMethod> getMethods(PsiClass psiClass, List<PsiField> fields) {
        // Only include setter methods that return the class type (for chaining)
        return PsiClassUtil.getMethods(psiClass, method -> {
            if (!isValidSetterMethod(method, fields)) {
                return false;
            }

            // Check if the method returns the class type (for chaining)
            PsiType returnType = method.getReturnType();
            if (returnType == null) {
                return false;
            }

            String returnTypeName = returnType.getCanonicalText();
            String className = psiClass.getQualifiedName();

            // Check if return type matches the class type (for chaining)
            return returnTypeName.equals(className) ||
                   (returnTypeName.contains("<") && returnTypeName.substring(0, returnTypeName.indexOf("<")).equals(className));
        });
    }

    /**
     * import new class.
     *
     * @param expression expression
     * @param document   document
     */
    private void importNewClass(@NotNull PsiElement expression, @NotNull Document document) {
        newImportSet.removeIf(next -> next.startsWith("java.lang") || !next.contains("."));
        PsiFile containingFile = expression.getContainingFile();
        PsiJavaFile javaFile = (PsiJavaFile) containingFile;
        PsiImportList importList = (javaFile).getImportList();
        if (importList != null) {
            // add new imports
            PsiImportStatement[] importStatements = importList.getImportStatements();
            for (PsiImportStatement importStatement : importStatements) {
                String text = importStatement.getText();
                String qualifiedName = text.replace("import ", "").replace(";", "");
                newImportSet.remove(qualifiedName);
            }
            PsiImportStaticStatement[] importStaticStatements = importList.getImportStaticStatements();
            for (PsiImportStaticStatement importStaticStatement : importStaticStatements) {
                String text = importStaticStatement.getText();
                String qualifiedName = text.replace("import static ", "").replace(";", "");
                newImportSet.remove(qualifiedName);
            }
            if (!newImportSet.isEmpty()) {
                int offset = importList.getTextRange().getEndOffset();
                StringBuilder importText = new StringBuilder();
                for (String qualifiedName : newImportSet) {
                    importText.append("\nimport ").append(qualifiedName).append(";");
                }
                document.insertString(offset, importText.toString());
            }
        }
    }

    /**
     * Get class name from qualified name.
     *
     * @param qualifiedName qualified name
     * @return class name
     */
    private String getClassName(String qualifiedName) {
        if (isBlank(qualifiedName)) {
            return "";
        }
        int i = qualifiedName.lastIndexOf(".");
        if (i == -1) {
            return qualifiedName;
        }
        return qualifiedName.substring(i + 1);
    }

    /**
     * Get real import from interface.
     *
     * @param interfaceName interface name
     * @return real import
     */
    private String getRealImport(String interfaceName) {
        String realImport = REAL_IMPORT_MAP.get(interfaceName);
        if (isNotBlank(realImport)) {
            return realImport;
        }
        return interfaceName;
    }

    /**
     * 为基本类型创建默认值
     *
     * @param typeName 类型名称
     * @return 默认值
     */
    private String createBasicTypeDefaultValue(String typeName) {
        switch (typeName) {
            case "Integer":
                return "0";
            case "Long":
                return "0L";
            case "Double":
                return "0.0";
            case "Float":
                return "0.0f";
            case "Boolean":
                return "false";
            case "String":
                return "\"\"";
            case "Character":
                return "'\\u0000'";
            case "Byte":
                return "(byte) 0";
            case "Short":
                return "(short) 0";
            default:
                return "null";
        }
    }

    /**
     * 为泛型类型创建包含实际类型的默认值
     *
     * @param containerType 容器类型 (如 java.util.List)
     * @param genericType 泛型类型 (如 Animal)
     * @return 适合该泛型类型的默认值
     */
    private String createGenericDefaultValue(String containerType, String genericType) {
        if (containerType == null || genericType == null) {
            return null;
        }

        if (containerType.contains("List") || containerType.contains("ArrayList")) {
            // 对于List类型
            return "new ArrayList<" + getClassName(genericType) + ">()";
        } else if (containerType.contains("Set") || containerType.contains("HashSet")) {
            // 对于Set类型
            return "new HashSet<" + getClassName(genericType) + ">()";
        } else if (containerType.contains("Map") || containerType.contains("HashMap")) {
            // 对于Map类型
            return "new HashMap<>()";
        }

        // 默认返回null，使用原有的默认值生成逻辑
        return null;
    }
}
