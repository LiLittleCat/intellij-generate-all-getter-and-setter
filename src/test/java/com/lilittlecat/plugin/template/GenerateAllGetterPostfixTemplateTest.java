package com.lilittlecat.plugin.template;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import org.junit.Assert;

/**
 * @author LiLittleCat
 * @since 4/13/2022
 */
public class GenerateAllGetterPostfixTemplateTest extends LightJavaCodeInsightFixtureTestCase {

//    @Override
//    protected String getTestDataPath() {
//        return "test/src-test";
//    }
//
//    ProjectDescriptor projectDescriptor = new ProjectDescriptor(LanguageLevel.JDK_1_8, true) {
//        @Override
//        public Sdk getSdk() {
//            return JavaSdk.getInstance().createJdk("1.8", "/usr/lib/jvm/java-8-oracle/jre");
//            //return super.getSdk();
//        }
//    };

    public void testWrapWithArray() throws Exception {
        assertWrapping("test",
                "user.getaa\t");
    }

    /**
     * Asserts that the expansion of {@code content} by .o equals {@code expected}.
     */
    private void assertWrapping(String expected, String content) {
        String s = "import java.util.List;\n" +
                "\n" +
                "/**\n" +
                " * @author LiLittleCat\n" +
                " * @since 4/13/2022\n" +
                " */\n" +
                "public class User {\n" +
                "    private String name;\n" +
                "    private Integer age;\n" +
                "    private List<String> nickNames;\n" +
                "//    private List<T> things;\n" +
                "\n" +
                "    public String getName() {\n" +
                "        return name;\n" +
                "    }\n" +
                "\n" +
                "    public void setName(String name) {\n" +
                "        this.name = name;\n" +
                "    }\n" +
                "\n" +
                "    public Integer getAge() {\n" +
                "        return age;\n" +
                "    }\n" +
                "\n" +
                "    public void setAge(Integer age) {\n" +
                "        this.age = age;\n" +
                "    }\n" +
                "\n" +
                "    public List<String> getNickNames() {\n" +
                "        return nickNames;\n" +
                "    }\n" +
                "\n" +
                "    public void setNickNames(List<String> nickNames) {\n" +
                "        this.nickNames = nickNames;\n" +
                "    }\n" +
                "\n" +
                "//    public List<T> getThings() {\n" +
                "//        return things;\n" +
                "//    }\n" +
                "//\n" +
                "//    public void setThings(List<T> things) {\n" +
                "//        this.things = things;\n" +
                "//    }\n" +
                "\n" +
                "    public static void main(String[] args) {\n" +
                "        User user = new User();\n" +
                "        <caret>\n" +
                "    }\n" +
                "}";
        PsiFile file = myFixture.configureByText(JavaFileType.INSTANCE, s);
        myFixture.type(content);
        System.out.println(file.getFileType());

        Assert.assertEquals(expected, file.getText());
    }

}
