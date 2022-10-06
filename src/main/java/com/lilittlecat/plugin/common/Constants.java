package com.lilittlecat.plugin.common;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LiLittleCat
 * @since 4/13/2022
 */
public class Constants {
    private Constants() {
    }

    public static final String ALL_GETTER_SUFFIX = "allget";
    public static final String ALL_SETTER_SUFFIX = "allset";
    public static final String ALL_SETTER_WITH_DEFAULT_VALUE_SUFFIX = "allsetv";
    public static final String ALL_GETTER_INFO = "Generate all getter()";
    public static final String ALL_SETTER_INFO = "Generate all setter()";
    public static final String ALL_SETTER_WITH_DEFAULT_VALUE_INFO = "Generate all setter() with default value";
    public static final String GET = "get";
    public static final String IS = "is";
    public static final String SET = "set";
    public static final Integer GET_METHOD_TYPE = 0;
    public static final Integer SET_METHOD_TYPE = 1;

    public static final Map<String, String> DEFAULT_VALUE_MAP = new HashMap<>() {
        {
            put("char", "'\\u0000'");
            put("java.lang.Character", "'\\u0000'");
            put("boolean", "false");
            put("java.lang.Boolean", "false");
            put("byte", "(byte)  0");
            put("java.lang.Byte", "(byte) 0");
            put("short", "(short) 0");
            put("java.lang.Short", "(short) 0");
            put("int", "0");
            put("java.lang.Integer", "0");
            put("long", "0L");
            put("java.lang.Long", "0L");
            put("float", "0.0F");
            put("java.lang.Float", "0.0F");
            put("double", "0.0D");
            put("java.lang.Double", "0.0D");
            put("java.lang.String", "\"\"");
            put("java.math.BigDecimal", "new BigDecimal(\"0\")");
            put("java.util.Date", "new Date()");
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
            put("java.util.UUID", "UUID.randomUUID()");
        }
    };

    public static Map<String, String> REAL_IMPORT_MAP = new HashMap<>() {
        {
            put("java.util.List", "java.util.ArrayList");
            put("java.util.Map", "java.util.HashMap");
            put("java.util.Set", "java.util.HashSet");
            put("java.util.Collection", "java.util.HashSet");
        }
    };


}