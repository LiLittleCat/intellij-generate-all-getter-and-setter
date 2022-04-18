package com.lilittlecat.plugin.template;

import com.intellij.codeInsight.template.Template;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.util.PsiTypesUtil;
import com.lilittlecat.plugin.util.PsiClassUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.lilittlecat.plugin.common.Constants.*;
import static com.lilittlecat.plugin.util.PsiClassUtil.isValidSetterMethod;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author LiLittleCat
 * @since 2022/4/16
 */
public class GenerateAllSetterWithDefaultValuePostfixTemplate extends BaseGeneratePostfixTemplate {

    public GenerateAllSetterWithDefaultValuePostfixTemplate() {
        super(null, ALL_SETTER_WITH_DEFAULT_VALUE_SUFFIX, ALL_SETTER_WITH_DEFAULT_VALUE_INFO, null);
    }

    /**
     * new import set
     */
    Set<String> newImportSet = new HashSet<>();

    @Override
    protected String buildTemplateString(@NotNull PsiElement expression,
                                         @NotNull Document document,
                                         @NotNull List<PsiMethod> methods,
                                         @NotNull PsiField[] fields) {
        StringBuilder builder = new StringBuilder();
        String defaultValue = "";
        for (PsiMethod setterMethod : methods) {
            // parameters of setter method.
            PsiParameter parameter = setterMethod.getParameterList().getParameters()[0];
            PsiType parameterType = parameter.getType();
            if (parameterType instanceof PsiArrayType) {
                // parameter type is array type.
                String text = parameterType.getCanonicalText();
                int i = text.indexOf("<");
                // array type don't need implementation, we can use interface such as "new Map[]",
                // users should adjust it after auto insert set value.
                String qualifiedName;
                if (i == -1) {
                    qualifiedName = text.replace("[]", "");
                } else {
                    // parameter type is generic
                    qualifiedName = text.substring(0, i);
                }
                if (isNotBlank(qualifiedName)) {
                    newImportSet.add(qualifiedName);
                    defaultValue = "new " + getClassName(qualifiedName) + "[0]";
                }
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
                int i = text.indexOf("<");
                if (i == -1) {
                    newImportSet.add(qualifiedName);
                } else {
                    // parameter type is generic
                    String fieldType = text.substring(0, i);
                    newImportSet.add(getRealImport(fieldType));
                }
                String staticDefaultValue = DEFAULT_VALUE_MAP.get(qualifiedName);
                if (isNotBlank(staticDefaultValue)) {
                    defaultValue = staticDefaultValue;
                } else {
                    defaultValue = "new " + getClassName(qualifiedName) + "()";
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
        importNewClass(expression, document);

        return builder.toString();
    }

    @Override
    protected Template modifyTemplate(Template template) {
        return template;
    }

    @Override
    protected List<PsiMethod> getMethods(PsiClass psiClass, PsiField[] fields) {
        return PsiClassUtil.getMethods(psiClass, method -> isValidSetterMethod(method, fields));
    }

    /**
     * import new class.
     *
     * @param expression expression
     * @param document   document
     */
    private void importNewClass(@NotNull PsiElement expression, @NotNull Document document) {
        newImportSet.removeIf(next -> next.startsWith("java.lang") || !next.contains("."));
        PsiFile containingFile = expression.getContainingFile();
        PsiJavaFile javaFile = (PsiJavaFile) containingFile;
        PsiImportList importList = (javaFile).getImportList();
        if (importList != null) {
            PsiImportStatement[] importStatements = importList.getImportStatements();
            for (PsiImportStatement importStatement : importStatements) {
                String text = importStatement.getText();
                String qualifiedName = text.replace("import ", "").replace(";", "");
                // handle import .*
                if (qualifiedName.endsWith(".*")) {
                    String prefix = qualifiedName.substring(0, qualifiedName.length() - 2);
                    newImportSet.removeIf(next -> next.startsWith(prefix));
                }
                newImportSet.add(qualifiedName);
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
            document.insertString(start, newImportBuilder.toString().replaceFirst("\n", ""));
        }
    }

    /**
     * get real import, such as: java.util.Map -> java.util.HashMap
     *
     * @param qualifiedName qualified name
     * @return real import
     */
    private String getRealImport(String qualifiedName) {
        String defaultImport = DEFAULT_IMPORT_MAP.get(qualifiedName);
        if (defaultImport != null) {
            return defaultImport;
        } else {
            return qualifiedName;
        }
    }

    /**
     * get class name, such as: java.util.Map -> Map; User -> User
     *
     * @param qualifiedName qualified name
     * @return class name
     */
    private String getClassName(String qualifiedName) {
        int i = qualifiedName.lastIndexOf(".");
        if (i == -1) {
            return qualifiedName;
        }
        return qualifiedName.substring(i + 1);
    }
}
