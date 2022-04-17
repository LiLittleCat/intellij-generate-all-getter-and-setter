package com.lilittlecat.plugin.template;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.lilittlecat.plugin.common.Constants;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Ignore;


/**
 * @author LiLittleCat
 * @since 4/13/2022
 */
@Ignore
public class GenerateAllPostfixTemplateTest extends LightJavaCodeInsightFixtureTestCase {

//    @Override
//    protected String getTestDataPath() {
//        return "test/com/lilittlecat/plugin/entity";
//    }

    @NotNull
    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new ProjectDescriptor(LanguageLevel.JDK_11, true) {
            @Override
            public Sdk getSdk() {
                return JavaSdk.getInstance().createJdk(System.getProperty("java.specification.version"),
                        System.getProperty("java.home"), false);
            }
        };
    }

    public void testAllSetterWithDefaultValue() throws Exception {
        assertExpand("        son.setTestString(\"\");\n" +
                        "        son.setTestInteger(0);\n" +
                        "        son.setTestBigDecimal(new BigDecimal(\"0\"));\n" +
                        "        son.setTestObject(new Father());\n" +
                        "        son.setTestList(new ArrayList<>());\n" +
                        "        son.setTestSet(new HashSet<>());\n" +
                        "        son.setTestMap(new HashMap<>());\n" +
                        "        son.setTestMapArray(new Map[0]);\n" +
                        "        son.setTestObjectArray(new Father[0]);\n" +
                        "        son.setTestDoubleArray(new double[0]);\n" +
                        "        \n" +
                        "        ",
                "son" + "." + Constants.ALL_SETTER_WITH_DEFAULT_VALUE_SUFFIX + "\t");
    }

    public void testAllSetterWithoutDefaultValue() throws Exception {
        assertExpand("        son.setTestString(a);\n" +
                        "        son.setTestInteger(a);\n" +
                        "        son.setTestBigDecimal(a);\n" +
                        "        son.setTestObject(a);\n" +
                        "        son.setTestList(a);\n" +
                        "        son.setTestSet(a);\n" +
                        "        son.setTestMap(a);\n" +
                        "        son.setTestMapArray(a);\n" +
                        "        son.setTestObjectArray(a);\n" +
                        "        son.setTestInt(a);\n" +
                        "        son.setTestDoubleArray(a);\n" +
                        "        \n" +
                        "        ",
                "son" + "." + Constants.ALL_SETTER_SUFFIX + "\t");
    }

    public void testAllGetter() throws Exception {
        assertExpand("        String testString = son.getTestString();\n" +
                        "        Integer testInteger = son.getTestInteger();\n" +
                        "        BigDecimal testBigDecimal = son.getTestBigDecimal();\n" +
                        "        Father testObject = son.getTestObject();\n" +
                        "        List<String> testList = son.getTestList();\n" +
                        "        Set<Float> testSet = son.getTestSet();\n" +
                        "        Map<String, BigDecimal> testMap = son.getTestMap();\n" +
                        "        Map<String, BigDecimal>[] testMapArray = son.getTestMapArray();\n" +
                        "        Father[] testObjectArray = son.getTestObjectArray();\n" +
                        "        int testInt = son.getTestInt();\n" +
                        "        double[] testDoubleArray = son.getTestDoubleArray();\n" +
                        "        \n" +
                        "        ",
                "son" + "." + Constants.ALL_GETTER_SUFFIX + "\t");
    }

    private void assertExpand(String expected, String content) {
        String s = "package com.lilittlecat.plugin.entity;\n" +
                "\n" +
                "import java.math.BigDecimal;\n" +
                "import java.util.*;\n" +
                "\n" +
                "/**\n" +
                " * @author LiLittleCat\n" +
                " * @since 2022/4/17\n" +
                " */\n" +
                "public class Main {\n" +
                "\n" +
                "    static class Father {\n" +
                "        private String name;\n" +
                "        private Integer age;\n" +
                "\n" +
                "        public String getName() {\n" +
                "            return name;\n" +
                "        }\n" +
                "\n" +
                "        public void setName(String name) {\n" +
                "            this.name = name;\n" +
                "        }\n" +
                "\n" +
                "        public Integer getAge() {\n" +
                "            return age;\n" +
                "        }\n" +
                "\n" +
                "        public void setAge(Integer age) {\n" +
                "            this.age = age;\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    static class Son {\n" +
                "        private String testString;\n" +
                "        private Integer testInteger;\n" +
                "        private BigDecimal testBigDecimal;\n" +
                "        private Father testObject;\n" +
                "        private List<String> testList;\n" +
                "        private Set<Float> testSet;\n" +
                "        private Map<String, BigDecimal> testMap;\n" +
                "        private Map<String, BigDecimal>[] testMapArray;\n" +
                "        private Father[] testObjectArray;\n" +
                "        private int testInt;\n" +
                "        private double[] testDoubleArray;\n" +
                "\n" +
                "        public String getTestString() {\n" +
                "            return testString;\n" +
                "        }\n" +
                "\n" +
                "        public void setTestString(String testString) {\n" +
                "            this.testString = testString;\n" +
                "        }\n" +
                "\n" +
                "        public Integer getTestInteger() {\n" +
                "            return testInteger;\n" +
                "        }\n" +
                "\n" +
                "        public void setTestInteger(Integer testInteger) {\n" +
                "            this.testInteger = testInteger;\n" +
                "        }\n" +
                "\n" +
                "        public BigDecimal getTestBigDecimal() {\n" +
                "            return testBigDecimal;\n" +
                "        }\n" +
                "\n" +
                "        public void setTestBigDecimal(BigDecimal testBigDecimal) {\n" +
                "            this.testBigDecimal = testBigDecimal;\n" +
                "        }\n" +
                "\n" +
                "        public Father getTestObject() {\n" +
                "            return testObject;\n" +
                "        }\n" +
                "\n" +
                "        public void setTestObject(Father testObject) {\n" +
                "            this.testObject = testObject;\n" +
                "        }\n" +
                "\n" +
                "        public List<String> getTestList() {\n" +
                "            return testList;\n" +
                "        }\n" +
                "\n" +
                "        public void setTestList(List<String> testList) {\n" +
                "            this.testList = testList;\n" +
                "        }\n" +
                "\n" +
                "        public Set<Float> getTestSet() {\n" +
                "            return testSet;\n" +
                "        }\n" +
                "\n" +
                "        public void setTestSet(Set<Float> testSet) {\n" +
                "            this.testSet = testSet;\n" +
                "        }\n" +
                "\n" +
                "        public Map<String, BigDecimal> getTestMap() {\n" +
                "            return testMap;\n" +
                "        }\n" +
                "\n" +
                "        public void setTestMap(Map<String, BigDecimal> testMap) {\n" +
                "            this.testMap = testMap;\n" +
                "        }\n" +
                "\n" +
                "        public Map<String, BigDecimal>[] getTestMapArray() {\n" +
                "            return testMapArray;\n" +
                "        }\n" +
                "\n" +
                "        public void setTestMapArray(Map<String, BigDecimal>[] testMapArray) {\n" +
                "            this.testMapArray = testMapArray;\n" +
                "        }\n" +
                "\n" +
                "        public Father[] getTestObjectArray() {\n" +
                "            return testObjectArray;\n" +
                "        }\n" +
                "\n" +
                "        public void setTestObjectArray(Father[] testObjectArray) {\n" +
                "            this.testObjectArray = testObjectArray;\n" +
                "        }\n" +
                "\n" +
                "        public int getTestInt() {\n" +
                "            return testInt;\n" +
                "        }\n" +
                "\n" +
                "        public void setTestInt(int testInt) {\n" +
                "            this.testInt = testInt;\n" +
                "        }\n" +
                "\n" +
                "        public double[] getTestDoubleArray() {\n" +
                "            return testDoubleArray;\n" +
                "        }\n" +
                "\n" +
                "        public void setTestDoubleArray(double[] testDoubleArray) {\n" +
                "            this.testDoubleArray = testDoubleArray;\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    public void main(String[] args) {\n" +
                "        Son son = new Son();\n" +
                "        //begin\n" +
                "        <caret>\n" +
                "        //end\n" +
                "    }\n" +
                "}\n";
        PsiFile file = myFixture.configureByText(JavaFileType.INSTANCE, s);
        myFixture.type(content);
        System.out.println(file.getFileType());

        String between = subBetween(file.getText(), "//begin\n", "//end\n");
        Assert.assertEquals(expected, between);
        Assert.assertNotNull(file.getText());
    }

    public static String subBetween(CharSequence str, CharSequence before, CharSequence after) {
        if (str != null && before != null && after != null) {
            String str2 = str.toString();
            String before2 = before.toString();
            String after2 = after.toString();
            int start = str2.indexOf(before2);
            if (start != -1) {
                int end = str2.indexOf(after2, start + before2.length());
                if (end != -1) {
                    return str2.substring(start + before2.length(), end);
                }
            }
        }
        return null;
    }

}
