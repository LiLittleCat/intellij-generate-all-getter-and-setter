package com.lilittlecat.plugin.template;

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateWithExpressionSelector;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import static com.intellij.codeInsight.template.postfix.util.JavaPostfixTemplatesUtils.selectorTopmost;
import static com.lilittlecat.plugin.common.Constants.ALL_GETTER_INFO;
import static com.lilittlecat.plugin.common.Constants.ALL_GETTER_SUFFIX;

/**
 * @author LiLittleCat
 * @since 4/13/2022
 */
public class GenerateAllGetterPostfixTemplate extends PostfixTemplateWithExpressionSelector {

    public GenerateAllGetterPostfixTemplate() {
        super(null, ALL_GETTER_SUFFIX, ALL_GETTER_INFO, selectorTopmost(psiElement -> true), null);
    }


    @Override
    protected void expandForChooseExpression(@NotNull PsiElement expression, @NotNull Editor editor) {

    }
}
