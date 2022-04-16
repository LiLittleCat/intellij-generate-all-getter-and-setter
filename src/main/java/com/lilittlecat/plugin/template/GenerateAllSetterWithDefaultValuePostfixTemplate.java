package com.lilittlecat.plugin.template;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateWithExpressionSelector;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.util.PsiTypesUtil;
import com.lilittlecat.plugin.util.PsiClassUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.codeInsight.template.postfix.util.JavaPostfixTemplatesUtils.selectorTopmost;
import static com.intellij.psi.PsiModifier.*;
import static com.lilittlecat.plugin.common.Constants.*;
import static com.lilittlecat.plugin.util.PsiClassUtil.*;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author LiLittleCat
 * @since 2022/4/16
 */
public class GenerateAllSetterWithDefaultValuePostfixTemplate extends PostfixTemplateWithExpressionSelector {
    public GenerateAllSetterWithDefaultValuePostfixTemplate() {
        super(null,
                ALL_SETTER_WITH_DEFAULT_VALUE_SUFFIX,
                ALL_SETTER_WITH_DEFAULT_VALUE_INFO, selectorTopmost(psiElement -> true), null);
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public boolean isEditable() {
        return false;
    }


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
        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(type.getCanonicalText(), expression.getResolveScope());
        if (psiClass == null) {
            return;
        }
        PsiField[] fields = psiClass.getAllFields();
        List<PsiMethod> setterMethods = PsiClassUtil.getMethods(psiClass, method -> isValidSetterMethod(method, fields));
        Set<String> newImportSet = new HashSet<>();
        StringBuilder builder = new StringBuilder();
        String defaultValue = "";
        for (PsiMethod setterMethod : setterMethods) {
            // parameters of setter method.
            PsiParameter parameter = setterMethod.getParameterList().getParameters()[0];
            PsiType parameterType = parameter.getType();
            if (parameterType instanceof PsiArrayType) {
                // parameter type is array type. todo




            } else if (parameterType instanceof PsiClassReferenceType) {
                // parameter type is class reference type.
                PsiClass parameterClass = PsiTypesUtil.getPsiClass(parameterType);
                if (parameterClass == null) {
                    continue;
                }
                String qualifiedName = parameterClass.getQualifiedName();
                if (isBlank(qualifiedName)) {
                    continue;
                }
                String text = parameterType.getCanonicalText();
                String className = "";
                int i = text.indexOf("<");
                if (i == -1) {
                    newImportSet.add(qualifiedName);
                    className = getClassName(qualifiedName);
                } else {
                    StringBuilder all = new StringBuilder();
                    // parameter type is generic
                    String fieldType = text.substring(0, i);
                    newImportSet.add(getRealImport(fieldType));
                    all.append(getClassName(fieldType)).append("<");
                    String genericTypeList = text.substring(i + 1, text.length() - 1);
                    String[] split = genericTypeList.split(",");
                    if (split.length == 1) {
                        newImportSet.add(getRealImport(split[0]));
                        all.append(getClassName(split[0])).append(">");
                    } else {
                        for (int j = 0; j < split.length; j++) {
                            newImportSet.add(getRealImport(split[j]));
                            all.append(getClassName(split[j]));
                            if (j != split.length - 1) {
                                all.append(", ");
                            }
                        }
                        all.append(">");
                    }
                    className = all.toString();
                }

                String staticDefaultValue = DEFAULT_VALUE_MAP.get(qualifiedName);
                if (isNotBlank(staticDefaultValue)) {
                    defaultValue = staticDefaultValue;
                } else {
                    defaultValue = "new " + className + "()";
                }

            } else {
                // nothing to do.
                continue;
            }
            builder.append(expression.getText()).append(".").append(setterMethod.getName())
                    .append("(").append(defaultValue).append(");\n");

        }
        builder.append("$END$");


        // import
        newImportSet.removeIf(next -> next.startsWith("java.lang") || !next.contains("."));
        PsiFile containingFile = expression.getContainingFile();
        PsiJavaFile javaFile = (PsiJavaFile) containingFile;
        PsiImportList importList = (javaFile).getImportList();
        if (importList != null) {
            PsiImportStatement[] importStatements = importList.getImportStatements();
            for (PsiImportStatement importStatement : importStatements) {
                newImportSet.add(importStatement.getQualifiedName());
            }
            Set<String> sortedSet = newImportSet.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new));
            StringBuilder newImportBuilder = new StringBuilder();
            for (String s : sortedSet) {
                newImportBuilder.append("\nimport ").append(s).append(";");
            }
            // replace import
            int start = importList.getTextRange().getStartOffset();
            int end = importList.getTextRange().getEndOffset();
            document.deleteString(start, end);
            document.insertString(start, newImportBuilder.toString());
        }

        Template template = manager.createTemplate(getId(), "", builder.toString());
        template.setToReformat(true);
        manager.startTemplate(editor, template);

    }

    private String getRealImport(String qualifiedName) {
        String defaultImport = DEFAULT_IMPORT_MAP.get(qualifiedName);
        if (defaultImport != null) {
            return defaultImport;
        } else {
            return qualifiedName;
        }
    }


    private String getClassName(String qualifiedName) {
        int i = qualifiedName.lastIndexOf(".");
        if (i == -1) {
            return qualifiedName;
        }
        return qualifiedName.substring(i + 1);
    }
}
