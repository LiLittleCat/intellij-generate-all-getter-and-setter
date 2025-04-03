package com.lilittlecat.plugin.template;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTypesUtil;
import com.lilittlecat.plugin.util.PsiClassUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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

    @Override
    protected String buildTemplateString(@NotNull PsiElement expression,
                                         @NotNull Document document,
                                         @NotNull List<PsiMethod> methods,
                                         @NotNull List<PsiField> fields) {
        StringBuilder builder = new StringBuilder();
        for (PsiMethod setterMethod : methods) {
            String fieldName = getFieldNameInMethod(setterMethod, SET_METHOD_TYPE);
            if (isNotBlank(fieldName)) {
                // 确保方法参数类型的泛型信息得到保留
                if (hasGenericType && setterMethod.getParameterList().getParametersCount() > 0) {
                    PsiParameter parameter = setterMethod.getParameterList().getParameters()[0];
                    PsiType paramType = parameter.getType();
                    String paramTypeText = paramType.getCanonicalText();
                    
                    if (paramTypeText.contains("<")) {
                        // 处理泛型参数
                        Map<String, String> typeMap = genericTypeMap.get(((PsiExpression) expression).getType().getCanonicalText().split("<")[0]);
                        if (typeMap != null && !typeMap.isEmpty()) {
                            // 使用新方法解析嵌套泛型
                            String resolvedGenericText = resolveNestedGenericType(paramTypeText, typeMap);
                            // 添加带解析后泛型类型的变量备注
                            variableList.add(fieldName + " // Type: " + resolvedGenericText);
                            builder.append(expression.getText()).append(".").append(setterMethod.getName())
                                .append("($").append(fieldName).append("$);\n");
                            continue;
                        }
                    }
                }
                variableList.add(fieldName);
            }
            builder.append(expression.getText()).append(".").append(setterMethod.getName())
                    .append("($").append(fieldName).append("$);\n");
        }
        builder.append("$END$");
        return builder.toString();
    }

    @Override
    protected Template modifyTemplate(Template template) {
        for (String variable : variableList) {
            template.addVariable(variable, variable, variable, true);
        }
        // clear for next time use
        variableList.clear();
        return template;
    }

    @Override
    protected List<PsiMethod> getMethods(PsiClass psiClass, List<PsiField> fields) {
        return PsiClassUtil.getMethods(psiClass, method -> isValidSetterMethod(method, fields));
    }
}
