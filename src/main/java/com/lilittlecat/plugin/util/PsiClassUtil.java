package com.lilittlecat.plugin.util;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.lilittlecat.plugin.common.Constants.*;

/**
 * @author LiLittleCat
 * @since 2022/4/16
 */
public class PsiClassUtil {
    private PsiClassUtil() {
    }

    /**
     * judge whether the class is a java system class
     *
     * @param psiClass the class to be judged
     * @return true if the class is a java system class
     */
    public static boolean isSystemClass(PsiClass psiClass) {
        if (psiClass == null) {
            return false;
        }
        return psiClass.getQualifiedName() != null && psiClass.getQualifiedName().startsWith("java.");
    }

    /**
     * judge whether the method is a valid getter method
     *
     * @param method the method to be judged
     * @param fields the normal fields of the class
     * @return true if the method is a valid getter method
     */
    public static boolean isValidGetterMethod(PsiMethod method, List<PsiField> fields) {
        if (method == null) {
            return false;
        }
        return method.hasModifierProperty(PsiModifier.PUBLIC)
                && !method.hasModifierProperty(PsiModifier.STATIC)
                && (method.getName().startsWith(GET) || method.getName().startsWith(IS))
                // getter method should have no parameter
                && method.getParameterList().getParametersCount() == 0
                // getter method should contain the field name in method name
                && fields.stream().filter(PsiClassUtil::isNormalField).anyMatch(
                field -> Objects.equals(field.getName(), getFieldNameInMethod(method, GET_METHOD_TYPE)));
    }

    /**
     * judge whether the method is a valid setter method
     *
     * @param method the method to be judged
     * @param fields the normal fields of the class
     * @return true if the method is a valid setter method
     */
    public static boolean isValidSetterMethod(PsiMethod method, List<PsiField> fields) {
        if (method == null) {
            return false;
        }
        return method.hasModifierProperty(PsiModifier.PUBLIC)
                && !method.hasModifierProperty(PsiModifier.STATIC)
                && (method.getName().startsWith(SET))
                // setter method should have one parameter
                && method.getParameterList().getParametersCount() == 1
                // setter method should contain the field name in method name
                && fields.stream().filter(PsiClassUtil::isNormalField).anyMatch(
                field -> Objects.equals(field.getName(), getFieldNameInMethod(method, SET_METHOD_TYPE)));
    }

    /**
     * judge whether the field is a normal field
     *
     * @param field the field to be judged
     * @return true if the field is a normal field
     */
    public static boolean isNormalField(PsiField field) {
        if (field == null) {
            return false;
        }
        return !(field.hasModifierProperty(PsiModifier.STATIC)
                || field.hasModifierProperty(PsiModifier.FINAL));
    }


    /**
     * get methods of the class
     *
     * @param psiClass  the class
     * @param predicate the predicate to filter methods
     * @return the filtered methods
     */
    public static List<PsiMethod> getMethods(PsiClass psiClass, Predicate<? super PsiMethod> predicate) {
        if (isSystemClass(psiClass)) {
            return new ArrayList<>();
        }
        PsiField[] fields = psiClass.getFields();
        PsiMethod[] methods = psiClass.getMethods();
        if (fields.length == 0 || methods.length == 0) {
            return new ArrayList<>();
        }
        return Arrays.stream(methods).filter(predicate).collect(Collectors.toList());
    }


    public static String getFirstCharUpperCase(String fieldName) {
        return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    public static String getFirstCharLowerCase(String fieldName) {
        return fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
    }

    /**
     * Get field name in method name
     *
     * @param method     method
     * @param methodType method type
     * @return field name
     */
    public static String getFieldNameInMethod(PsiMethod method, Integer methodType) {
        if (method == null) {
            return "";
        }
        String methodName = method.getName();
        if (GET_METHOD_TYPE.equals(methodType)) {
            if (methodName.startsWith(GET)) {
                return getFirstCharLowerCase(methodName.replaceFirst(GET, ""));
            } else if (methodName.startsWith(IS)) {
                return getFirstCharLowerCase(methodName.replaceFirst(IS, ""));
            }
        } else if (SET_METHOD_TYPE.equals(methodType)) {
            return getFirstCharLowerCase(methodName.replaceFirst(SET, ""));
        }
        return "";
    }


    /**
     * get all fields include super classes' field.
     *
     * @param psiClass psiClass
     * @return list of all fields
     */
    public static List<PsiField> getAllFieldsIncludeSuperClass(PsiClass psiClass) {
        List<PsiField> allFields = new ArrayList<>();
        while (!isSystemClass(psiClass)) {
            allFields.addAll(List.of(psiClass.getFields()));
            PsiClass superClass = psiClass.getSuperClass();
            if (superClass == null) {
                break;
            } else {
                psiClass = psiClass.getSuperClass();
            }
        }
        return allFields;
    }


}