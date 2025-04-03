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
 * @author LiLittleCat
 * @since 2022/4/16
 */
public class GenerateAllSetterWithoutDefaultValuePostfixTemplate extends BaseGeneratePostfixTemplate {

    public GenerateAllSetterWithoutDefaultValuePostfixTemplate(PostfixTemplateProvider provider) {
        super(null, ALL_SETTER_SUFFIX, ALL_SETTER_INFO, provider);
    }

    /**
     * variables will be added in template.
     */
    private final List<String> variableList = new ArrayList<>();
    
    /**
     * 储存方法名到变量名的映射，确保每个setter方法都有对应的变量
     */
    private final Map<String, String> methodToVariableMap = new HashMap<>();

    @Override
    protected String buildTemplateString(@NotNull PsiElement expression,
                                         @NotNull Document document,
                                         @NotNull List<PsiMethod> methods,
                                         @NotNull List<PsiField> fields) {
        StringBuilder builder = new StringBuilder();
        // 清除之前的映射
        methodToVariableMap.clear();
        
        for (PsiMethod setterMethod : methods) {
            String fieldName = getFieldNameInMethod(setterMethod, SET_METHOD_TYPE);
            String methodName = setterMethod.getName();
            
            if (isNotBlank(fieldName)) {
                boolean variableAdded = false; // 跟踪变量是否已添加
                
                // 确保方法参数类型的泛型信息得到保留
                if (hasGenericType && setterMethod.getParameterList().getParametersCount() > 0) {
                    PsiParameter parameter = setterMethod.getParameterList().getParameters()[0];
                    PsiType paramType = parameter.getType();
                    String paramTypeText = paramType.getCanonicalText();
                    
                    // 首先检查参数是否直接是类型参数(如C, T等)
                    if (paramType instanceof PsiClassType) {
                        PsiClass paramClass = ((PsiClassType) paramType).resolve();
                        if (paramClass instanceof PsiTypeParameter) {
                            Map<String, String> typeMap = genericTypeMap.get(((PsiExpression) expression).getType().getCanonicalText().split("<")[0]);
                            if (typeMap != null && !typeMap.isEmpty()) {
                                String resolvedType = resolveGenericParameterType(paramTypeText, typeMap, (PsiTypeParameter) paramClass);
                                if (!resolvedType.equals(paramTypeText)) {
                                    variableList.add(fieldName + " // Type: " + resolvedType);
                                    variableAdded = true;
                                    builder.append(expression.getText()).append(".").append(setterMethod.getName())
                                        .append("($").append(fieldName).append("$);\n");
                                    methodToVariableMap.put(methodName, fieldName);
                                    continue;
                                }
                            }
                        }
                    }
                    
                    // 然后处理包含泛型的参数类型
                    Map<String, String> typeMap = genericTypeMap.get(((PsiExpression) expression).getType().getCanonicalText().split("<")[0]);
                    if (typeMap != null && !typeMap.isEmpty() && paramTypeText.contains("<")) {
                        // 首先检查是否是原始类型
                        String resolvedType = handleRawType(paramTypeText, typeMap);
                        // 如果仍然包含泛型标记，则保留类型信息
                        if (resolvedType.contains("<")) {
                            variableList.add(fieldName + " // Type: " + resolvedType);
                        } else {
                            // 原始类型，不添加泛型信息
                            variableList.add(fieldName);
                        }
                        variableAdded = true;
                        builder.append(expression.getText()).append(".").append(setterMethod.getName())
                            .append("($").append(fieldName).append("$);\n");
                        methodToVariableMap.put(methodName, fieldName);
                        continue;
                    }
                }
                
                // 确保变量被添加
                if (!variableAdded) {
                    variableList.add(fieldName);
                }
            } else {
                // 如果fieldName为空，使用一个默认名称确保变量被添加
                variableList.add("value");
                fieldName = "value";
            }
            builder.append(expression.getText()).append(".").append(setterMethod.getName())
                    .append("($").append(fieldName).append("$);\n");
            // 记录方法名到变量名的映射
            methodToVariableMap.put(methodName, fieldName);
        }
        builder.append("$END$");
        return builder.toString();
    }

    @Override
    protected Template modifyTemplate(Template template) {
        try {
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
        return PsiClassUtil.getMethods(psiClass, method -> isValidSetterMethod(method, fields));
    }
}
