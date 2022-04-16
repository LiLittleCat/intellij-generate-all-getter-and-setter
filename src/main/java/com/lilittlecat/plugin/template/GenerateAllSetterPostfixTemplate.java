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

import java.util.ArrayList;
import java.util.List;

import static com.intellij.codeInsight.template.postfix.util.JavaPostfixTemplatesUtils.selectorTopmost;
import static com.lilittlecat.plugin.common.Constants.ALL_SETTER_INFO;
import static com.lilittlecat.plugin.common.Constants.ALL_SETTER_SUFFIX;
import static com.lilittlecat.plugin.util.PsiClassUtil.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author LiLittleCat
 * @since 4/15/2022
 */
public class GenerateAllSetterPostfixTemplate extends PostfixTemplateWithExpressionSelector {

    public GenerateAllSetterPostfixTemplate() {
        super(null, ALL_SETTER_SUFFIX, ALL_SETTER_INFO, selectorTopmost(psiElement -> true), null);
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    /**
     * reference:
     * {@link com.intellij.codeInsight.template.postfix.templates.editable.EditablePostfixTemplate#expand(PsiElement, Editor)}
     *
     * @param expression the expression.
     * @param editor     the editor.
     */
    @Override
    protected void expandForChooseExpression(@NotNull PsiElement expression, @NotNull Editor editor) {
        Project project = expression.getProject();
        TemplateManager manager = TemplateManager.getInstance(project);
        Document document = editor.getDocument();
        // delete old text in current row.
        document.deleteString(expression.getTextRange().getStartOffset(), expression.getTextRange().getEndOffset());
        PsiType type = ((PsiExpression) expression).getType();
        if (type == null) {
            return;
        }
        String className = type.getCanonicalText();
        PsiClass psiClass = JavaPsiFacade.getInstance(expression.getProject()).findClass(className, expression.getResolveScope());
        if (psiClass == null) {
            return;
        }
        PsiField[] fields = psiClass.getAllFields();
        List<PsiMethod> setterMethods = PsiClassUtil.getMethods(psiClass, method -> isValidSetterMethod(method, fields));
        // variables will be added in template.
        List<String> variableList = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (PsiMethod setterMethod : setterMethods) {
            String fieldName = getFieldNameInMethod(setterMethod, SET);
            if (isNotBlank(fieldName)) {
                variableList.add(fieldName);
            }
            builder.append(expression.getText()).append(".").append(setterMethod.getName())
                    .append("($").append(fieldName).append("$);\n");
        }
        builder.append("$END$");
        Template template = manager.createTemplate(getId(), "", builder.toString());
        for (String variable : variableList) {
            template.addVariable(variable, variable, variable, true);
        }
        template.setToReformat(true);
        manager.startTemplate(editor, template);
    }
}
