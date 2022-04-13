package com.lilittlecat.plugin.provider;

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.lilittlecat.plugin.template.GenerateAllGetterTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * @author LiLittleCat
 * @since 4/13/2022
 */
public class GenerateAllGetterTemplateProvider implements PostfixTemplateProvider {

    private final HashSet<PostfixTemplate> templates;

    public GenerateAllGetterTemplateProvider() {
        templates = new HashSet<>();
        templates.add(new GenerateAllGetterTemplate());
    }

    @Override
    public @NotNull Set<PostfixTemplate> getTemplates() {
        return templates;
    }

    @Override
    public boolean isTerminalSymbol(char currentChar) {
        return '.' == currentChar;
    }

    @Override
    public void preExpand(@NotNull PsiFile file, @NotNull Editor editor) {

    }

    @Override
    public void afterExpand(@NotNull PsiFile file, @NotNull Editor editor) {

    }

    @Override
    public @NotNull PsiFile preCheck(@NotNull PsiFile copyFile, @NotNull Editor realEditor, int currentOffset) {
        return copyFile;
    }
}
