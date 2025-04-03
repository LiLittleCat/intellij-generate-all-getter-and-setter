package com.lilittlecat.plugin.template;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTypesUtil;
import com.lilittlecat.plugin.util.PsiClassUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.lilittlecat.plugin.common.Constants.*;
import static com.lilittlecat.plugin.util.PsiClassUtil.*;

/**
 * @author LiLittleCat
 * @since 4/13/2022
 */
public class GenerateAllGetterPostfixTemplate extends BaseGeneratePostfixTemplate {

    public GenerateAllGetterPostfixTemplate(PostfixTemplateProvider provider) {
        super(null, ALL_GETTER_SUFFIX, ALL_GETTER_INFO, provider);
    }

    @Override
    protected String buildTemplateString(@NotNull PsiElement expression,
                                         @NotNull Document document,
                                         @NotNull List<PsiMethod> methods,
                                         @NotNull List<PsiField> fields) {
        StringBuilder builder = new StringBuilder();
        // handle public static field
        for (PsiField field : fields) {
            if (field.hasModifierProperty(PsiModifier.STATIC) && field.hasModifierProperty(PsiModifier.PUBLIC)) {
                builder.append(field.getType().getCanonicalText()).append(" ")
                        .append(field.getName()).append(" = ")
                        .append(Objects.requireNonNull(field.getContainingClass()).getName())
                        .append(".")
                        .append(field.getName()).append(";\n");
            }
        }
        // handle getter method
        for (PsiMethod method : methods) {
            PsiType returnType = method.getReturnType();
            if (returnType == null) {
                continue;
            }
            String methodName = method.getName();
            
            // 解析返回类型（包括泛型）
            String returnTypeText = returnType.getCanonicalText();
            String fieldName = getFieldNameInMethod(method, GET_METHOD_TYPE);
            
            // 如果字段名是特殊标记，跳过此方法
            if ("__empty__".equals(fieldName)) {
                continue;
            }
            
            // 处理返回类型中的泛型
            if (hasGenericType && returnType instanceof PsiClassType) {
                // 获取类的所有类型参数名称
                Set<String> classTypeParamNames = new HashSet<>();
                if (expression instanceof PsiExpression) {
                    PsiType exprType = ((PsiExpression) expression).getType();
                    if (exprType instanceof PsiClassType) {
                        PsiClass psiClass = ((PsiClassType) exprType).resolve();
                        if (psiClass != null) {
                            PsiTypeParameter[] typeParameters = psiClass.getTypeParameters();
                            for (PsiTypeParameter tp : typeParameters) {
                                classTypeParamNames.add(tp.getName());
                            }
                        }
                    }
                }
                
                // 检查返回类型是否使用了类的泛型参数
                boolean containsClassTypeParameter = false;
                
                // 针对返回类型是类型参数的情况
                PsiClass returnClass = ((PsiClassType) returnType).resolve();
                if (returnClass instanceof PsiTypeParameter) {
                    String typeName = returnClass.getName();
                    if (classTypeParamNames.contains(typeName)) {
                        containsClassTypeParameter = true;
                    }
                }
                
                // 针对返回类型包含泛型的情况
                if (returnTypeText.contains("<")) {
                    for (String paramName : classTypeParamNames) {
                        if (returnTypeText.contains("<" + paramName + ">") || 
                            returnTypeText.contains("<" + paramName + ",") ||
                            returnTypeText.contains(", " + paramName + ">") ||
                            returnTypeText.contains(", " + paramName + ",")) {
                            containsClassTypeParameter = true;
                            break;
                        }
                    }
                }
                
                // 只有当返回类型包含类的泛型参数时，才需要特殊处理
                if (containsClassTypeParameter) {
                    Map<String, String> typeMap = genericTypeMap.get(((PsiExpression) expression).getType().getCanonicalText().split("<")[0]);
                    if (typeMap != null && !typeMap.isEmpty()) {
                        if (returnTypeText.contains("<")) {
                            // 处理返回类型中包含泛型的情况
                            returnTypeText = handleRawType(returnTypeText, typeMap);
                        } else if (returnClass instanceof PsiTypeParameter) {
                            // 返回类型是类型参数
                            returnTypeText = resolveGenericParameterType(returnTypeText, typeMap, (PsiTypeParameter) returnClass);
                        } else if (returnType instanceof PsiWildcardType) {
                            // 处理通配符类型如 ? extends T
                            PsiType boundType = ((PsiWildcardType) returnType).getBound();
                            if (boundType != null) {
                                String boundText = boundType.getCanonicalText();
                                // 检查边界类型是否是类型参数
                                if (!boundText.contains(".") && !boundText.contains("<")) {
                                    String resolvedBound = resolveGenericParameterType(boundText, typeMap, null);
                                    if (!boundText.equals(resolvedBound)) {
                                        returnTypeText = ((PsiWildcardType) returnType).isExtends() 
                                            ? "? extends " + resolvedBound 
                                            : "? super " + resolvedBound;
                                    }
                                }
                            }
                        } else {
                            // 处理其他简单类型参数如 T
                            returnTypeText = resolveGenericParameterType(returnTypeText, typeMap, null);
                        }
                    } else {
                        // 包含类泛型参数但没有类型映射，添加注释
                        builder.append("// Cannot resolve generic types in ").append(returnTypeText)
                               .append(" that use class type parameters. Please use a parameterized type for ")
                               .append(expression.getText()).append(".\n");
                        continue;
                    }
                }
            }
            
            builder.append(returnTypeText).append(" ")
                    .append(fieldName)
                    .append(" = ").append(expression.getText()).append(".").append(methodName).append("();\n");
        }
        builder.append("$END$");
        return builder.toString();
    }

    @Override
    protected Template modifyTemplate(Template template) {
        return template;
    }

    @Override
    protected List<PsiMethod> getMethods(PsiClass psiClass, List<PsiField> fields) {
        return PsiClassUtil.getMethods(psiClass, method -> isValidGetterMethod(method, fields));
    }
}
