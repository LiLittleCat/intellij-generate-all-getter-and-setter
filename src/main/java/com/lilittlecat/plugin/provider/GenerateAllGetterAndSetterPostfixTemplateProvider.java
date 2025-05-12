package com.lilittlecat.plugin.provider;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.codeInsight.actions.OptimizeImportsProcessor;
import com.lilittlecat.plugin.template.GenerateAllGetterPostfixTemplate;
import com.lilittlecat.plugin.template.GenerateAllSetterWithoutDefaultValuePostfixTemplate;
import com.lilittlecat.plugin.template.GenerateAllSetterWithDefaultValuePostfixTemplate;
import com.lilittlecat.plugin.template.GenerateAllSetterChainWithoutDefaultValuePostfixTemplate;
import com.lilittlecat.plugin.template.GenerateAllSetterChainWithDefaultValuePostfixTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * @author LiLittleCat
 * @since 4/13/2022
 */
public class GenerateAllGetterAndSetterPostfixTemplateProvider implements PostfixTemplateProvider {

    private final HashSet<PostfixTemplate> templates = ContainerUtil.newHashSet(
            new GenerateAllGetterPostfixTemplate(this),
            new GenerateAllSetterWithoutDefaultValuePostfixTemplate(this),
            new GenerateAllSetterWithDefaultValuePostfixTemplate(this),
            new GenerateAllSetterChainWithoutDefaultValuePostfixTemplate(this),
            new GenerateAllSetterChainWithDefaultValuePostfixTemplate(this)
    );

    @Override
    public @NotNull Set<PostfixTemplate> getTemplates() {
        return templates;
    }

    @Override
    public boolean isTerminalSymbol(char currentChar) {
        return currentChar == '.';
    }

    @Override
    public void preExpand(@NotNull PsiFile file, @NotNull Editor editor) {
        // 无需预处理
    }

    @Override
    public void afterExpand(@NotNull PsiFile file, @NotNull Editor editor) {
//        new OptimizeImportsProcessor(file.getProject(), file).run();
        new ReformatCodeProcessor(file.getProject(), true).run();
    }

    @NotNull
    @Override
    public PsiFile preCheck(@NotNull PsiFile copyFile, @NotNull Editor realEditor, int currentOffset) {
        return copyFile;
    }

}
