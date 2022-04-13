package com.lilittlecat.plugin.template;

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.lilittlecat.plugin.common.Constants.ALL_GETTER_INFO;
import static com.lilittlecat.plugin.common.Constants.ALL_GETTER_SUFFIX;

/**
 * @author LiLittleCat
 * @since 4/13/2022
 */
public class GenerateAllGetterTemplate extends PostfixTemplate {

    public GenerateAllGetterTemplate() {
        super(null, ALL_GETTER_SUFFIX, ALL_GETTER_INFO, null);
    }

    @Override
    public boolean isApplicable(@NotNull PsiElement context, @NotNull Document copyDocument, int newOffset) {
        return context instanceof Object || context.getParent() instanceof Object;
    }

    @Override
    public void expand(@NotNull PsiElement context, @NotNull Editor editor) {
        System.out.println(context);
    }
}
