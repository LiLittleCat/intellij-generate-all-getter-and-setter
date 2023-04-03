package com.lilittlecat.plugin.template;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.*;
import com.lilittlecat.plugin.util.PsiClassUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static com.lilittlecat.plugin.common.Constants.*;
import static com.lilittlecat.plugin.util.PsiClassUtil.*;

/**
 * @author LiLittleCat
 * @since 4/13/2022
 */
public class GenerateAllGetterPostfixTemplate extends BaseGeneratePostfixTemplate {

    public GenerateAllGetterPostfixTemplate(PostfixTemplateProvider provider) {
        super(null, ALL_GETTER_SUFFIX, ALL_GETTER_INFO, provider);
    }

    @Override
    protected String buildTemplateString(@NotNull PsiElement expression,
                                         @NotNull Document document,
                                         @NotNull List<PsiMethod> methods,
                                         @NotNull List<PsiField> fields) {
        StringBuilder builder = new StringBuilder();
        // handle public static field
        for (PsiField field : fields) {
            if (field.hasModifierProperty(PsiModifier.STATIC) && field.hasModifierProperty(PsiModifier.PUBLIC)) {
                builder.append(field.getType().getCanonicalText()).append(" ")
                        .append(field.getName()).append(" = ")
                        .append(Objects.requireNonNull(field.getContainingClass()).getName())
                        .append(".")
                        .append(field.getName()).append(";\n");
            }
        }
        // handle getter method
        for (PsiMethod method : methods) {
            PsiType returnType = method.getReturnType();
            if (returnType == null) {
                continue;
            }
            String methodName = method.getName();
            builder.append(returnType.getCanonicalText()).append(" ")
                    .append(getFieldNameInMethod(method, GET_METHOD_TYPE))
                    .append(" = ").append(expression.getText()).append(".").append(methodName).append("();\n");
        }
        builder.append("$END$");
        return builder.toString();
    }

    @Override
    protected Template modifyTemplate(Template template) {
        return template;
    }

    @Override
    protected List<PsiMethod> getMethods(PsiClass psiClass, List<PsiField> fields) {
        return PsiClassUtil.getMethods(psiClass, method -> isValidGetterMethod(method, fields));
    }
}
