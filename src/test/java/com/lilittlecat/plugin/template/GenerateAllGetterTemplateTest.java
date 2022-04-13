package com.lilittlecat.plugin.template;

import com.intellij.testFramework.LightPlatformCodeInsightTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import com.intellij.javaee.ProjectResources;

/**
 * @author LiLittleCat
 * @since 4/13/2022
 */
class GenerateAllGetterTemplateTest extends LightPlatformCodeInsightFixture4TestCase {
    public void testWrapWithArray() throws Exception {
        assertWrapping("{123}", "123");
        assertWrapping("{null}", "null");
    }

    /**
     * Asserts that the expansion of {@code content} by .o equals {@code expected}.
     */
    private void assertWrapping(String expected, String content) {

    }
}