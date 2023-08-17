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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * only when can find the instance's class can use this template，
     */
    public static final Condition<PsiElement> IS_OK =
            psiElement -> {
                Project project = psiElement.getProject();
                PsiType type = ((PsiExpression) psiElement).getType();
                if (type == null) {
                    return false;
                }
                // type.getCanonicalText()
                String className = type.getCanonicalText();
                PsiClass psiClass = JavaPsiFacade.getInstance(project)
                        .findClass(className.substring(0, className.indexOf("<")), psiElement.getResolveScope());
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

    public static final Pattern genericTypePattern = Pattern.compile("<(.*?)>");

    protected Boolean hasGenericType = false;

    public static final Map<String, Map<String, String>> genericTypeMap = new HashMap<>();

    protected void clearState() {
        hasGenericType = false;
        genericTypeMap.clear();
    }

    /**
     * reference:
     * {@link com.intellij.codeInsight.template.postfix.templates.editable.EditablePostfixTemplate#expandForChooseExpression(PsiElement, Editor)}
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
        // Gets the generic type of the instance
        List<String> genericTypeClassNameList = new ArrayList<>();
        Pattern pattern = Pattern.compile("^[^<]*<(.*)>");
        Matcher matcher = pattern.matcher(className);
        if (matcher.find()) {
            hasGenericType = true;
            // Has generic types
            String typeArgs = matcher.group(1);
            for (String name : typeArgs.split(",")) {
                genericTypeClassNameList.add(name.trim());
            }
            className = className.substring(0, className.indexOf("<"));
        }
        PsiClass psiClass = JavaPsiFacade.getInstance(expression.getProject()).findClass(className, expression.getResolveScope());
        assert psiClass != null;
        // Gets the generic placeholder in the class
        PsiTypeParameter[] typeParameters = psiClass.getTypeParameters();
        if (!genericTypeClassNameList.isEmpty() && typeParameters.length == genericTypeClassNameList.size()) {
            for (int i = 0; i < typeParameters.length; i++) {
                Map<String, String> map = genericTypeMap.computeIfAbsent(psiClass.getQualifiedName(), k -> new HashMap<>());
                map.put(typeParameters[i].getName(), genericTypeClassNameList.get(i));
            }
        }
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
        clearState();
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
