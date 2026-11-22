package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.domain.ScrollChoiceMenuItem;
import com.thecoderscorner.menu.domain.ScrollChoiceMenuItemBuilder;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptionsBuilder;
import com.thecoderscorner.menu.editorui.generator.logger.UserFeedbackLogger;
import com.thecoderscorner.menu.editorui.generator.parameters.eeprom.AVREepromDefinition;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CoreCodeGeneratorTest {
    private TestCoreCodeGenerator generator;
    private UserFeedbackLogger feedbackLogger;
    private MenuTree menuTree;
    private CodeGeneratorOptions options;

    static class TestCoreCodeGenerator extends CoreCodeGenerator {
        public TestCoreCodeGenerator(SketchFileAdjuster adjuster, EmbeddedPlatform embeddedPlatform,
                                     ConfigurationStorage configStore, Clock clock,
                                     UserFeedbackLogger feedbackLogger) {
            super(adjuster, embeddedPlatform, configStore, clock, feedbackLogger);
        }

        public void setMenuTree(MenuTree tree) {
            this.menuTree = tree;
        }

        public void setOptions(CodeGeneratorOptions options) {
            this.options = options;
        }

        @Override
        public void doSanityChecks() throws TcMenuConversionException {
            super.doSanityChecks();
        }

        @Override
        protected String platformIncludes() {
            return "";
        }
    }

    @BeforeEach
    public void setUp() {
        feedbackLogger = mock(UserFeedbackLogger.class);
        generator = new TestCoreCodeGenerator(
                mock(SketchFileAdjuster.class),
                EmbeddedPlatform.ARDUINO_AVR,
                mock(ConfigurationStorage.class),
                Clock.systemUTC(),
                feedbackLogger
        );
        menuTree = new MenuTree();
        options = new CodeGeneratorOptionsBuilder()
                .withPlatform(EmbeddedPlatform.ARDUINO_AVR)
                .withEepromDefinition(new AVREepromDefinition())
                .codeOptions();
        generator.setOptions(options);
        generator.setMenuTree(menuTree);
    }

    @Test
    void testScrollChoiceArrayInRamWithNullOrEmptyVariableFailsSanityCheck() {
        var scrollItem = new ScrollChoiceMenuItemBuilder()
                .withId(1)
                .withName("ScrollChoice")
                .withChoiceMode(ScrollChoiceMenuItem.ScrollChoiceMode.ARRAY_IN_RAM)
                .withNumEntries(5)
                .withItemWidth(10)
                .withVariable(null)
                .menuItem();
        menuTree.addMenuItem(MenuTree.ROOT, scrollItem);

        assertThatThrownBy(() -> generator.doSanityChecks())
                .isInstanceOf(TcMenuConversionException.class)
                .hasMessageContaining("Menu sanity checks have failed.");

        verify(feedbackLogger).error("You have at least one scroll choice item that creates a data variable in code, but the variable name for it is unset.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testScrollChoiceArrayInEepromWithNullOrEmptyVariableFailsSanityCheck(String varName) {
        var scrollItem = new ScrollChoiceMenuItemBuilder()
                .withId(1)
                .withName("ScrollChoice")
                .withChoiceMode(ScrollChoiceMenuItem.ScrollChoiceMode.ARRAY_IN_EEPROM)
                .withNumEntries(5)
                .withItemWidth(10)
                .withVariable(varName)
                .menuItem();
        menuTree.addMenuItem(MenuTree.ROOT, scrollItem);

        assertThatThrownBy(() -> generator.doSanityChecks())
                .isInstanceOf(TcMenuConversionException.class)
                .hasMessageContaining("Menu sanity checks have failed.");

        verify(feedbackLogger).error("You have at least one scroll choice item that creates a data variable in code, but the variable name for it is unset.");
    }

    @Test
    void testScrollChoiceArrayInRamWithValidVariablePassesSanityCheck() {
        var scrollItem = new ScrollChoiceMenuItemBuilder()
                .withId(1)
                .withName("ScrollChoice")
                .withChoiceMode(ScrollChoiceMenuItem.ScrollChoiceMode.ARRAY_IN_RAM)
                .withNumEntries(5)
                .withItemWidth(10)
                .withVariable("myCustomRamArray")
                .menuItem();
        menuTree.addMenuItem(MenuTree.ROOT, scrollItem);

        assertDoesNotThrow(() -> generator.doSanityChecks());
    }

    @Test
    void testScrollChoiceCustomRenderFnWithNullVariablePassesSanityCheck() {
        var scrollItem = new ScrollChoiceMenuItemBuilder()
                .withId(1)
                .withName("ScrollChoice")
                .withChoiceMode(ScrollChoiceMenuItem.ScrollChoiceMode.CUSTOM_RENDERFN)
                .withNumEntries(5)
                .withItemWidth(10)
                .withVariable(null)
                .menuItem();
        menuTree.addMenuItem(MenuTree.ROOT, scrollItem);

        assertDoesNotThrow(() -> generator.doSanityChecks());
    }
}
