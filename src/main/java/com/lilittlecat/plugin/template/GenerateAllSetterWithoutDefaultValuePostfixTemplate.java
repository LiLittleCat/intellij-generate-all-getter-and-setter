package com.lilittlecat.plugin.template;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.lilittlecat.plugin.util.PsiClassUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.lilittlecat.plugin.common.Constants.ALL_SETTER_INFO;
import static com.lilittlecat.plugin.common.Constants.ALL_SETTER_SUFFIX;
import static com.lilittlecat.plugin.util.PsiClassUtil.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author LiLittleCat
 * @since 4/15/2022
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
                                         @NotNull PsiField[] fields) {
        StringBuilder builder = new StringBuilder();
        for (PsiMethod setterMethod : methods) {
            String fieldName = getFieldNameInMethod(setterMethod, SET);
            if (isNotBlank(fieldName)) {
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
        return template;
    }

    @Override
    protected List<PsiMethod> getMethods(PsiClass psiClass, PsiField[] fields) {
        return PsiClassUtil.getMethods(psiClass, method -> isValidSetterMethod(method, fields));
    }
}
