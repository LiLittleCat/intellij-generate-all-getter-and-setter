package com.lilittlecat.plugin.template;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateWithExpressionSelector;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.lilittlecat.plugin.util.PsiClassUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.intellij.codeInsight.template.postfix.util.JavaPostfixTemplatesUtils.selectorTopmost;
import static com.lilittlecat.plugin.common.Constants.ALL_GETTER_INFO;
import static com.lilittlecat.plugin.common.Constants.ALL_GETTER_SUFFIX;
import static com.lilittlecat.plugin.util.PsiClassUtil.*;

/**
 * @author LiLittleCat
 * @since 4/13/2022
 */
public class GenerateAllGetterPostfixTemplate extends PostfixTemplateWithExpressionSelector {

    public GenerateAllGetterPostfixTemplate() {
        // todo when can use this postfix template.
        super(null, ALL_GETTER_SUFFIX, ALL_GETTER_INFO, selectorTopmost(psiElement -> true), null);
    }


    @Override
    protected void expandForChooseExpression(@NotNull PsiElement expression, @NotNull Editor editor) {

        Project project = expression.getProject();
        TemplateManager manager = TemplateManager.getInstance(project);
        Document document = editor.getDocument();
        // delete old text in current row.
        document.deleteString(expression.getTextRange().getStartOffset(), expression.getTextRange().getEndOffset());
        PsiType type = ((PsiExpression) expression).getType();
        assert type != null;
        String className = type.getCanonicalText();
        PsiClass psiClass = JavaPsiFacade.getInstance(expression.getProject()).findClass(className, expression.getResolveScope());
        assert psiClass != null;
        PsiField[] fields = psiClass.getFields();
        List<PsiMethod> methods = PsiClassUtil.getMethods(psiClass, method -> isValidGetterMethod(method, fields));

        StringBuilder builder = new StringBuilder();
        for (PsiMethod method : methods) {
            PsiType returnType = method.getReturnType();
            if (returnType == null) {
                continue;
            }
            String methodName = method.getName();
            builder.append(returnType.getCanonicalText()).append(" ")
                    .append(getFieldNameInMethod(method, GET))
                    .append(" = ").append(expression.getText()).append(".").append(methodName).append("();\n");
        }
        builder.append("$END$");
        Template template = manager.createTemplate(getId(), "", builder.toString());
        template.setToReformat(true);
        manager.startTemplate(editor, template);

    }
}
