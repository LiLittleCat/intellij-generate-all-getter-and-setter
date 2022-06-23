package com.lilittlecat.plugin.template;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateWithExpressionSelector;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.*;
import com.lilittlecat.plugin.util.PsiClassUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.codeInsight.template.postfix.util.JavaPostfixTemplatesUtils.selectorTopmost;

/**
 * @author LiLittleCat
 * @since 2022/4/17
 */
public abstract class BaseGeneratePostfixTemplate extends PostfixTemplateWithExpressionSelector {

    protected BaseGeneratePostfixTemplate(@Nullable String id,
                                          @NotNull String name,
                                          @NotNull String example,
                                          @Nullable PostfixTemplateProvider provider) {
        super(id, name, example, selectorTopmost(IS_OK), provider);
    }

    /**
     * which condition can use this template.
     */
    public static final Condition<PsiElement> IS_OK =
            psiElement -> {
                Project project = psiElement.getProject();
                PsiType type = ((PsiExpression) psiElement).getType();
                if (type == null) {
                    return false;
                }
                PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(type.getCanonicalText(), psiElement.getResolveScope());
                return psiClass != null;
            };

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
     * com.intellij.codeInsight.template.postfix.templates.editable.EditablePostfixTemplate#expandForChooseExpression(PsiElement, Editor)
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
        assert type != null;
        String className = type.getCanonicalText();
        PsiClass psiClass = JavaPsiFacade.getInstance(expression.getProject()).findClass(className, expression.getResolveScope());
        assert psiClass != null;
        List<PsiField> fields = PsiClassUtil.getAllFieldsIncludeSuperClass(psiClass);
        // get all methods from class include super classes.
        List<PsiMethod> methods = new ArrayList<>();
        while (!PsiClassUtil.isSystemClass(psiClass)) {
            methods.addAll(getMethods(psiClass, fields));
            PsiClass superClass = psiClass.getSuperClass();
            if (superClass == null) {
                break;
            } else {
                psiClass = psiClass.getSuperClass();
            }
        }
        Template template = manager.createTemplate(getId(), "", buildTemplateString(expression, document, methods, fields));
        template = modifyTemplate(template);
        template.setToReformat(true);
        manager.startTemplate(editor, template);

    }

    /**
     * get methods from class.
     *
     * @param psiClass the class.
     * @param fields   the fields.
     * @return the valid methods in class.
     */
    protected abstract List<PsiMethod> getMethods(PsiClass psiClass, List<PsiField> fields);


    /**
     * build template string.
     *
     * @param expression the expression.
     * @param document   the document.
     * @param methods    the methods.
     * @param fields     the fields.
     * @return the template string.
     */
    protected abstract String buildTemplateString(@NotNull PsiElement expression,
                                                  @NotNull Document document,
                                                  @NotNull List<PsiMethod> methods,
                                                  @NotNull List<PsiField> fields);

    /**
     * modify template.
     *
     * @param template the template.
     * @return the modified template.
     */
    protected abstract Template modifyTemplate(Template template);
}
