package com.lilittlecat.plugin.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LiLittleCat
 * @since 4/13/2022
 */
public class Constants {
    private Constants() {
    }

    /**
     * all getter, use double 'a' for quick input
     */
    public static final String ALL_GETTER_SUFFIX = "getaa";
    /**
     * all setter, use double 'a' for quick input
     */
    public static final String ALL_SETTER_SUFFIX = "setaa";
    /**
     * all setter with default value, use triple 'a' for quick input
     */
    public static final String ALL_SETTER_WITH_DEFAULT_VALUE_SUFFIX = "setaaa";
    public static final String ALL_GETTER_INFO = "Generate all getter()";
    public static final String ALL_SETTER_INFO = "Generate all setter()";
    public static final String ALL_SETTER_WITH_DEFAULT_VALUE_INFO = "Generate all setter() with default value";

    public static final Map<String, String> DEFAULT_VALUE_MAP = new HashMap<>() {
        {
            put("boolean", "false");
            put("java.lang.Boolean", "false");
            put("int", "0");
            put("byte", "(byte)  0");
            put("java.lang.Byte", "(byte) 0");
            put("java.lang.Integer", "0");
            put("java.lang.String", "\"\"");
            put("java.math.BigDecimal", "new BigDecimal(\"0\")");
            put("java.lang.Long", "0L");
            put("long", "0L");
            put("short", "(short) 0");
            put("java.lang.Short", "(short) 0");
            put("java.util.Date", "new Date()");
            put("float", "0.0F");
            put("java.lang.Float", "0.0F");
            put("double", "0.0D");
            put("java.lang.Double", "0.0D");
            put("java.lang.Character", "'\\u0000'");
            put("char", "'\\u0000'");
            put("java.time.LocalDateTime", "LocalDateTime.now()");
            put("java.time.LocalDate", "LocalDate.now()");
            put("java.time.OffsetDateTime", "OffsetDateTime.now()");
            put("java.util.Optional", "Optional.empty()");
            put("java.util.List", "new ArrayList<>()");
            put("java.util.ArrayList", "new ArrayList<>()");
            put("java.util.Collection", "new ArrayList<>()");
            put("java.util.Set", "new HashSet<>()");
            put("java.util.HashSet", "new HashSet<>()");
            put("java.util.Map", "new HashMap<>()");
            put("java.util.HashMap", "new HashMap<>()");
        }
    };

    private static List<String> BASIC_TYPE_LIST = new ArrayList<>() {
        {
            add("boolean");
            add("byte");
            add("short");
            add("int");
            add("long");
            add("float");
            add("double");
            add("char");
        }
    };

    public static Map<String, String> DEFAULT_IMPORT_MAP = new HashMap<>() {
        {
            put("java.util.List", "java.util.ArrayList");
            put("java.util.Map", "java.util.HashMap");
            put("java.util.Set", "java.util.HashSet");
            put("java.util.Collection", "java.util.HashSet");
        }
    };


}
