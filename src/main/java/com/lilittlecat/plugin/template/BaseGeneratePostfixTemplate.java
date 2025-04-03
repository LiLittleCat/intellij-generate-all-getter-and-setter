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
                String className = type.getCanonicalText();
                int genericIndex = className.indexOf("<");
                String baseClassName = genericIndex > 0 ? className.substring(0, genericIndex) : className;
                PsiClass psiClass = JavaPsiFacade.getInstance(project)
                        .findClass(baseClassName, psiElement.getResolveScope());
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
     * 识别泛型参数的正则表达式，支持任意标识符
     */
    public static final Pattern genericTypePattern = Pattern.compile("<([^<>]+)>");

    protected Boolean hasGenericType = false;

    public static final Map<String, Map<String, String>> genericTypeMap = new HashMap<>();

    /**
     * 解析泛型类型字符串，处理嵌套泛型情况
     * 例如从 List<T> 中提取出 T，即使 T 来自于外层类定义
     * 
     * @param genericText 包含泛型的类型字符串，如 "List<T>"
     * @param typeMap 当前类的泛型映射表
     * @return 处理后的泛型类型字符串，如 "List<String>"
     */
    protected String resolveNestedGenericType(String genericText, Map<String, String> typeMap) {
        if (genericText == null || typeMap == null || typeMap.isEmpty()) {
            return genericText;
        }
        
        // 使用正则表达式查找所有泛型参数
        Matcher matcher = Pattern.compile("<([^<>]+)>").matcher(genericText);
        if (matcher.find()) {
            String genericParams = matcher.group(1);
            // 分割多个泛型参数（例如Map<K,V>）
            String[] params = genericParams.split(",");
            for (int i = 0; i < params.length; i++) {
                String param = params[i].trim();
                // 检查是否是TypeVariable（简单标识符，没有点号和尖括号）
                if (!param.contains(".") && !param.contains("<") && !param.contains(">")) {
                    // 在类型映射中查找替换
                    String actualType = typeMap.get(param);
                    if (actualType != null) {
                        params[i] = actualType;
                    }
                }
            }
            
            // 重新构建泛型类型字符串
            String typeWithoutGeneric = genericText.substring(0, genericText.indexOf("<"));
            StringBuilder newGeneric = new StringBuilder(typeWithoutGeneric).append("<");
            for (int i = 0; i < params.length; i++) {
                if (i > 0) {
                    newGeneric.append(", ");
                }
                newGeneric.append(params[i]);
            }
            newGeneric.append(">");
            return newGeneric.toString();
        }
        
        return genericText;
    }
    
    /**
     * 检查是否应该保留泛型信息
     * 当使用原始类型时（没有指定具体泛型参数），返回不带泛型的类型
     * 
     * @param genericText 可能包含泛型的类型字符串
     * @param typeMap 泛型映射表
     * @return 处理后的类型字符串
     */
    protected String handleRawType(String genericText, Map<String, String> typeMap) {
        if (genericText == null) {
            return null;
        }
        
        // 如果没有泛型，直接返回原文本
        if (!genericText.contains("<")) {
            return genericText;
        }
        
        // 在泛型映射为空的情况下，如果有泛型，则认为是使用了原始类型，返回无泛型版本
        if (typeMap == null || typeMap.isEmpty()) {
            return genericText.substring(0, genericText.indexOf("<"));
        }
        
        // 处理泛型类型
        String resolvedType = resolveNestedGenericType(genericText, typeMap);
        
        // 检查解析后的类型是否包含未解析的类型变量
        Matcher matcher = Pattern.compile("<([^<>]+)>").matcher(resolvedType);
        if (matcher.find()) {
            String genericParams = matcher.group(1);
            String[] params = genericParams.split(",");
            for (String param : params) {
                param = param.trim();
                // 如果参数是类型变量（简单标识符，没有点号和尖括号）并且没有在typeMap中找到对应值
                if (!param.contains(".") && !param.contains("<") && !param.contains(">")) {
                    // 检查是否是没有被替换的类型变量
                    boolean foundInMap = false;
                    for (Map.Entry<String, String> entry : typeMap.entrySet()) {
                        if (param.equals(entry.getKey()) || param.equals(entry.getValue())) {
                            foundInMap = true;
                            break;
                        }
                    }
                    
                    // 如果是未替换的类型变量，则认为是使用了原始类型
                    if (!foundInMap) {
                        return resolvedType.substring(0, resolvedType.indexOf("<"));
                    }
                }
            }
        }
        
        return resolvedType;
    }

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
        
        // 从类名中提取基础类名（不带泛型）
        String baseClassName = className;
        if (matcher.find()) {
            // Has generic types specified in the instance
            String typeArgs = matcher.group(1);
            for (String name : typeArgs.split(",")) {
                genericTypeClassNameList.add(name.trim());
            }
            baseClassName = className.substring(0, className.indexOf("<"));
        }
        
        PsiClass psiClass = JavaPsiFacade.getInstance(expression.getProject()).findClass(baseClassName, expression.getResolveScope());
        assert psiClass != null;
        
        // 检查类定义是否包含泛型参数，无论实例化时是否指定了具体泛型
        hasGenericType = psiClass.getTypeParameters().length > 0;
        
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
