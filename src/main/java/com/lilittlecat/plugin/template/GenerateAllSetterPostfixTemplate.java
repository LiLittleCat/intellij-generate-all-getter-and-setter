package com.lilittlecat.plugin.template;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateWithExpressionSelector;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.codeInsight.template.postfix.util.JavaPostfixTemplatesUtils.selectorTopmost;
import static com.intellij.psi.PsiModifier.*;
import static com.lilittlecat.plugin.common.Constants.*;

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
        try {
            PsiType type = ((PsiExpression) expression).getType();
            assert type != null;
            String className = type.getCanonicalText();
            PsiClass psiClass = JavaPsiFacade.getInstance(expression.getProject()).findClass(className, expression.getResolveScope());
            assert psiClass != null;
            PsiField[] fieldList = psiClass.getAllFields();
            // variables will be added in template.
            List<String> variableList = new ArrayList<>();
            StringBuilder builder = new StringBuilder();
            for (PsiField field : fieldList) {
                String fieldName = field.getName();
                assert fieldName != null;
                variableList.add(fieldName);
                PsiModifierList modifierList = field.getModifierList();
                // check if field is static or final or transient or null.
                if (modifierList == null
                        || modifierList.hasModifierProperty(FINAL)
                        || modifierList.hasModifierProperty(STATIC)
                        || modifierList.hasModifierProperty(SYNCHRONIZED)) {
                    continue;
                }
                builder.append(expression.getText()).append(".set").append(getFirstCharUpperCase(fieldName))
                        .append("($").append(fieldName).append("$);\n");
            }
            builder.append("$END$");
            Template template = manager.createTemplate(getId(), "", builder.toString());
            for (String variable : variableList) {
                template.addVariable(variable, variable, variable, true);
            }
            template.setToReformat(true);
            manager.startTemplate(editor, template);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String getFirstCharUpperCase(String fieldName) {
        return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }
}
