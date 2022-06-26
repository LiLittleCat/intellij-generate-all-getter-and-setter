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

    public static final String AMPERSAND = "&";
    public static final String AND = "and";
    public static final String AT = "@";
    public static final String ASTERISK = "*";
    public static final String STAR = "*";
    public static final String BACK_SLASH = "\\";
    public static final String COLON = ":";
    public static final String COMMA = ",";
    public static final String DASH = "-";
    public static final String DOLLAR = "$";
    public static final String DOT = ".";
    public static final String DOTDOT = "..";
    public static final String DOT_CLASS = ".class";
    public static final String DOT_JAVA = ".java";
    public static final String DOT_XML = ".xml";
    public static final String EMPTY = "";
    public static final String EQUALS = "=";
    public static final String FALSE = "false";
    public static final String SLASH = "/";
    public static final String HASH = "#";
    public static final String HAT = "^";
    public static final String LEFT_BRACE = "{";
    public static final String LEFT_BRACKET = "(";
    public static final String LEFT_CHEV = "<";
    public static final String DOT_NEWLINE = ",\n";
    public static final String NEWLINE = "\n";
    public static final String N = "n";
    public static final String NO = "no";
    public static final String NULL = "null";
    public static final String OFF = "off";
    public static final String ON = "on";
    public static final String PERCENT = "%";
    public static final String PIPE = "|";
    public static final String PLUS = "+";
    public static final String QUESTION_MARK = "?";
    public static final String EXCLAMATION_MARK = "!";
    public static final String QUOTE = "\"";
    public static final String RETURN = "\r";
    public static final String TAB = "\t";
    public static final String RIGHT_BRACE = "}";
    public static final String RIGHT_BRACKET = ")";
    public static final String RIGHT_CHEV = ">";
    public static final String SEMICOLON = ";";
    public static final String SINGLE_QUOTE = "'";
    public static final String BACKTICK = "`";
    public static final String SPACE = " ";
    public static final String TILDA = "~";
    public static final String LEFT_SQ_BRACKET = "[";
    public static final String RIGHT_SQ_BRACKET = "]";
    public static final String TRUE = "true";
    public static final String UNDERSCORE = "_";
    public static final String UTF_8 = "UTF-8";
    public static final String US_ASCII = "US-ASCII";
    public static final String ISO_8859_1 = "ISO-8859-1";
    public static final String Y = "y";
    public static final String YES = "yes";
    public static final String ONE = "1";
    public static final String ZERO = "0";
    public static final String DOLLAR_LEFT_BRACE = "${";
    public static final String HASH_LEFT_BRACE = "#{";
    public static final String CRLF = "\r\n";
    public static final String HTML_NBSP = "&nbsp;";
    public static final String HTML_AMP = "&amp";
    public static final String HTML_QUOTE = "&quot;";
    public static final String HTML_LT = "&lt;";
    public static final String HTML_GT = "&gt;";
    public static final String[] EMPTY_ARRAY = new String[0];
    public static final byte[] BYTES_NEW_LINE = "\n".getBytes();
    public static final String NEW = "new";
    public static final String BLANK = " ";
    public static final String NEW_BLANK = NEW + BLANK;


}
