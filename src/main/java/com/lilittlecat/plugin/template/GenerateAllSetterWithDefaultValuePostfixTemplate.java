package com.lilittlecat.plugin.template;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.util.PsiTypesUtil;
import com.lilittlecat.plugin.util.PsiClassUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.lilittlecat.plugin.common.Constants.*;
import static com.lilittlecat.plugin.util.PsiClassUtil.isValidSetterMethod;
import static com.lilittlecat.plugin.util.PsiClassUtil.getFieldNameInMethod;
import static com.lilittlecat.plugin.common.Constants.SET_METHOD_TYPE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author LiLittleCat
 * @since 2022/4/16
 */
public class GenerateAllSetterWithDefaultValuePostfixTemplate extends BaseGeneratePostfixTemplate {

    public GenerateAllSetterWithDefaultValuePostfixTemplate(PostfixTemplateProvider provider) {
        super(null, ALL_SETTER_WITH_DEFAULT_VALUE_SUFFIX, ALL_SETTER_WITH_DEFAULT_VALUE_INFO, provider);
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
                                builder.append(expression.getText()).append(".").append(setterMethod.getName())
                                    .append("(").append(defaultValue).append(");\n");
                                hasOnlyComments = false;
                                continue;
                            } else {
                                // 泛型参数未知，添加注释
                                builder.append("// Generic type parameter ").append(parameterType.getCanonicalText())
                                       .append(" is unknown. Please specify the generic type for ")
                                       .append(setterMethod.getName()).append(".\n");
                                continue;
                            }
                        } else {
                            // 未提供泛型类型信息，添加注释
                            builder.append("// Generic type parameter ").append(parameterType.getCanonicalText())
                                   .append(" is not specified. Please use a parameterized type for ")
                                   .append(expression.getText()).append(".\n");
                            continue;
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
                            
                            // 检查参数类型是否使用了类的泛型参数
                            for (String paramName : classTypeParamNames) {
                                if (text.contains("<" + paramName + ">") || 
                                    text.contains("<" + paramName + ",") ||
                                    text.contains(", " + paramName + ">") ||
                                    text.contains(", " + paramName + ",")) {
                                    containsClassTypeParameter = true;
                                    break;
                                }
                            }
                            
                            // 只处理包含类型参数的泛型
                            if (containsClassTypeParameter) {
                                // 首先检查是否是原始类型
                                String resolvedType = handleRawType(text, typeMap);
                                
                                // 检查解析后的类型是否仍包含未解析的泛型参数
                                boolean containsUnresolvedTypeVar = false;
                                if (resolvedType.contains("<")) {
                                    Matcher genericMatcher = Pattern.compile("<([^<>]+)>").matcher(resolvedType);
                                    if (genericMatcher.find()) {
                                        String typeVars = genericMatcher.group(1);
                                        // 检查是否包含未解析的类型变量（单个字母或简单标识符）
                                        containsUnresolvedTypeVar = Arrays.stream(typeVars.split(","))
                                                                     .map(String::trim)
                                                                     .anyMatch(s -> !s.contains(".") && classTypeParamNames.contains(s));
                                    }
                                }
                                
                                if (containsUnresolvedTypeVar) {
                                    // 泛型参数未完全具体化，添加注释
                                    builder.append("// Generic type in ").append(text)
                                           .append(" is not fully specified. Please use a parameterized type for ")
                                           .append(expression.getText()).append(".\n");
                                    continue;
                                }
                                
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
                                // 不包含类的泛型参数，按正常类型处理
                                String staticDefaultValue = DEFAULT_VALUE_MAP.get(fieldType);
                                if (isNotBlank(staticDefaultValue)) {
                                    defaultValue = staticDefaultValue;
                                } else {
                                    defaultValue = "new " + getClassName(fieldType) + "()";
                                }
                                newImportSet.add(fieldType);
                            }
                        } else if (typeMap == null || typeMap.isEmpty()) {
                            // 检查类型中是否包含了类定义的泛型参数
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
                            
                            // 检查参数类型是否使用了类的泛型参数
                            for (String paramName : classTypeParamNames) {
                                if (text.contains("<" + paramName + ">") || 
                                    text.contains("<" + paramName + ",") ||
                                    text.contains(", " + paramName + ">") ||
                                    text.contains(", " + paramName + ",")) {
                                    containsClassTypeParameter = true;
                                    break;
                                }
                            }
                            
                            if (containsClassTypeParameter) {
                                // 未提供泛型类型信息，添加注释
                                builder.append("// Cannot resolve generic types in ").append(text)
                                       .append(" that use class type parameters. Please use a parameterized type for ")
                                       .append(expression.getText()).append(".\n");
                                continue;
                            } else {
                                // 不包含类的泛型参数，按正常类型处理
                                String staticDefaultValue = DEFAULT_VALUE_MAP.get(fieldType);
                                if (isNotBlank(staticDefaultValue)) {
                                    defaultValue = staticDefaultValue;
                                } else {
                                    defaultValue = "new " + getClassName(fieldType) + "()";
                                }
                                newImportSet.add(fieldType);
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
            builder.append(expression.getText()).append(".").append(setterMethod.getName())
                    .append("(").append(defaultValue).append(");\n");
            hasOnlyComments = false;
        }
        builder.append("$END$");

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
        return PsiClassUtil.getMethods(psiClass, method -> isValidSetterMethod(method, fields));
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
                // handle import .*
                if (qualifiedName.endsWith(".*")) {
                    String prefix = qualifiedName.substring(0, qualifiedName.length() - 2);
                    newImportSet.removeIf(next -> next.startsWith(prefix));
                }
                newImportSet.add(qualifiedName);
            }
            Set<String> sortedSet = newImportSet.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new));
            StringBuilder newImportBuilder = new StringBuilder();
            for (String s : sortedSet) {
                newImportBuilder.append("\nimport ").append(s).append(";");
            }
            // keep static imports
            PsiImportStaticStatement[] importStaticStatements = importList.getImportStaticStatements();
            newImportBuilder.append("\n");
            for (PsiImportStaticStatement importStaticStatement : importStaticStatements) {
                newImportBuilder.append("\n").append(importStaticStatement.getText());
            }
            // replace imports
            int start = importList.getTextRange().getStartOffset();
            int end = importList.getTextRange().getEndOffset();
            document.deleteString(start, end);
            document.insertString(start, newImportBuilder.toString().replaceFirst("\n", ""));
        }
    }

    /**
     * get real import, such as: java.util.Map -> java.util.HashMap
     *
     * @param qualifiedName qualified name
     * @return real import
     */
    private String getRealImport(String qualifiedName) {
        String realImport = REAL_IMPORT_MAP.get(qualifiedName);
        if (realImport != null) {
            return realImport;
        } else {
            return qualifiedName;
        }
    }

    /**
     * get class name, such as: java.util.Map -> Map; User -> User
     *
     * @param qualifiedName qualified name
     * @return class name
     */
    private String getClassName(String qualifiedName) {
        int i = qualifiedName.lastIndexOf(".");
        if (i == -1) {
            return qualifiedName;
        }
        return qualifiedName.substring(i + 1);
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
}
