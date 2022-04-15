package com.lilittlecat.plugin.provider;

import com.intellij.codeInsight.template.postfix.templates.JavaPostfixTemplateProvider;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate;
import com.lilittlecat.plugin.template.GenerateAllGetterPostfixTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * @author LiLittleCat
 * @since 4/13/2022
 */
public class GenerateAllGetterAndSetterPostfixTemplateProvider extends JavaPostfixTemplateProvider {

    private final HashSet<PostfixTemplate> templates;

    public GenerateAllGetterAndSetterPostfixTemplateProvider() {
        templates = new HashSet<>();
        templates.add(new GenerateAllGetterPostfixTemplate());
    }

    @Override
    public @NotNull Set<PostfixTemplate> getTemplates() {
        return templates;
    }

}
