package com.lilittlecat.plugin.template;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTypesUtil;
import com.lilittlecat.plugin.util.PsiClassUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.lilittlecat.plugin.common.Constants.*;
import static com.lilittlecat.plugin.util.PsiClassUtil.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Generate all setter methods with chain style (method returns this)
 *
 * @author LiLittleCat
 * @since 2022/4/16
 */
public class GenerateAllSetterChainWithoutDefaultValuePostfixTemplate extends BaseGeneratePostfixTemplate {

    public GenerateAllSetterChainWithoutDefaultValuePostfixTemplate(PostfixTemplateProvider provider) {
        super(null, ALL_SETTER_CHAIN_SUFFIX, ALL_SETTER_CHAIN_INFO, provider);
    }

    /**
     * variables will be added in template.
     */
    private final List<String> variableList = new ArrayList<>();

    /**
     * 储存方法名到变量名的映射，确保每个setter方法都有对应的变量
     */
    private final Map<String, String> methodToVariableMap = new HashMap<>();

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
        // 清除之前的映射和标记
        methodToVariableMap.clear();
        hasOnlyComments = true; // 默认假设只有注释

        if (methods.isEmpty()) {
            builder.append("// No setter methods found in ").append(expression.getText()).append("\n");
            builder.append("$END$");
            return builder.toString();
        }

        // Start with the expression
        String expressionText = expression.getText();

        for (PsiMethod setterMethod : methods) {
            String fieldName = getFieldNameInMethod(setterMethod, SET_METHOD_TYPE);
            String methodName = setterMethod.getName();

            if ("__empty__".equals(fieldName)) {
                // 如果字段名是特殊标记，跳过此方法
                continue;
            }

            if (isNotBlank(fieldName)) {
                boolean variableAdded = false; // 跟踪变量是否已添加

                // 检查参数类型是否包含泛型
                PsiParameter parameter = setterMethod.getParameterList().getParameters()[0];
                PsiType paramType = parameter.getType();
                String paramTypeText = paramType.getCanonicalText();

                if (hasGenericType) {
                    // 首先检查参数是否直接是类型参数(如C, T等)
                    if (paramType instanceof PsiClassType) {
                        PsiClass paramClass = ((PsiClassType) paramType).resolve();
                        if (paramClass instanceof PsiTypeParameter) {
                            Map<String, String> typeMap = genericTypeMap.get(((PsiExpression) expression).getType().getCanonicalText().split("<")[0]);
                            if (typeMap != null && !typeMap.isEmpty()) {
                                String resolvedType = resolveGenericParameterType(paramTypeText, typeMap, (PsiTypeParameter) paramClass);
                                if (!resolvedType.equals(paramTypeText)) {
                                    // 如果成功解析了泛型类型
                                    variableList.add(fieldName + " // Type: " + resolvedType);
                                    variableAdded = true;
                                    if (builder.length() == 0) {
                                        builder.append(expressionText).append(".").append(setterMethod.getName())
                                            .append("($").append(fieldName).append("$)");
                                    } else {
                                        builder.append("\n    .").append(setterMethod.getName())
                                            .append("($").append(fieldName).append("$)");
                                    }
                                    methodToVariableMap.put(methodName, fieldName);
                                    hasOnlyComments = false; // 有真实代码
                                    continue;
                                }
                            }
                        }
                    }

                    // 然后处理包含泛型的参数类型
                    Map<String, String> typeMap = genericTypeMap.get(((PsiExpression) expression).getType().getCanonicalText().split("<")[0]);
                    if (typeMap != null && !typeMap.isEmpty() && paramTypeText.contains("<")) {
                        // 获取类的所有类型参数名称
                        boolean containsClassTypeParameter = false;
                        for (String typeParamName : typeMap.keySet()) {
                            if (paramTypeText.contains("<" + typeParamName + ">") ||
                                paramTypeText.contains("<" + typeParamName + ",") ||
                                paramTypeText.contains(", " + typeParamName + ">") ||
                                paramTypeText.contains(", " + typeParamName + ",")) {
                                containsClassTypeParameter = true;
                                break;
                            }
                        }

                        if (containsClassTypeParameter) {
                            // 包含类的泛型参数但没有类型映射，添加注释
                            builder.append("\n    // Cannot resolve generic types in ").append(paramTypeText)
                                   .append(" that use class type parameters. Please use a parameterized type for ")
                                   .append(expression.getText()).append(".");
                            continue;
                        } else {
                            // 不包含类的泛型参数，按正常方式处理
                            variableList.add(fieldName);
                            variableAdded = true;
                            if (builder.length() == 0) {
                                builder.append(expressionText).append(".").append(setterMethod.getName())
                                    .append("($").append(fieldName).append("$)");
                            } else {
                                builder.append("\n    .").append(setterMethod.getName())
                                    .append("($").append(fieldName).append("$)");
                            }
                            methodToVariableMap.put(methodName, fieldName);
                            hasOnlyComments = false; // 有真实代码
                            continue;
                        }
                    }
                }

                // 确保变量被添加
                if (!variableAdded) {
                    variableList.add(fieldName);
                }
            } else {
                // 如果fieldName为空（但不是__empty__标记），使用一个默认名称确保变量被添加
                variableList.add("value");
                fieldName = "value";
            }

            if (builder.length() == 0) {
                builder.append(expressionText).append(".").append(setterMethod.getName())
                    .append("($").append(fieldName).append("$)");
            } else {
                builder.append("\n    .").append(setterMethod.getName())
                    .append("($").append(fieldName).append("$)");
            }
            // 记录方法名到变量名的映射
            methodToVariableMap.put(methodName, fieldName);
            hasOnlyComments = false; // 有真实代码
        }

        builder.append(";\n$END$");
        return builder.toString();
    }

    @Override
    protected Template modifyTemplate(Template template) {
        try {
            // 如果模板只包含注释，添加一个隐藏的占位变量
            if (hasOnlyComments) {
                // 添加一个隐藏变量，但不会在编辑器中显示
                template.addVariable("_dummy", "\"\"", "\"\"", false);
                // 不要在模板中添加警告，它会使模板非法
                return template;
            }

            // 确保变量列表不为空
            if (variableList.isEmpty() && methodToVariableMap.isEmpty()) {
                template.addVariable("value", "\"\"", "\"\"", true);
            } else {
                // 先处理从variableList中的变量
                for (String variable : variableList) {
                    // 提取没有注释部分的变量名
                    String varName = variable;
                    String varValue = variable;
                    if (variable.contains(" // Type: ")) {
                        varName = variable.substring(0, variable.indexOf(" // Type: "));
                    }
                    template.addVariable(varName, varValue, varValue, true);
                }

                // 再确保methodToVariableMap中的所有变量都添加了
                for (Map.Entry<String, String> entry : methodToVariableMap.entrySet()) {
                    String varValue = entry.getValue();
                    if (varValue != null && !varValue.isEmpty()) {
                        // 如果变量名包含注释，提取真正的变量名
                        final String varName;
                        if (varValue.contains(" // Type: ")) {
                            varName = varValue.substring(0, varValue.indexOf(" // Type: "));
                        } else {
                            varName = varValue;
                        }
                        // 检查该变量是否已添加(避免重复添加)
                        if (!variableList.stream().anyMatch(v ->
                                v.equals(varName) || (v.contains(" // Type: ") && v.startsWith(varName + " // Type: ")))) {
                            template.addVariable(varName, "\"\"", "\"\"", true);
                        }
                    }
                }
            }
        } finally {
            // 清除变量列表，以便下次使用
            variableList.clear();
            methodToVariableMap.clear();
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
}
