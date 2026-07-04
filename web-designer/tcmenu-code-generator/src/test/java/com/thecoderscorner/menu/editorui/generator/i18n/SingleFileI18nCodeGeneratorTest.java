package com.thecoderscorner.menu.editorui.generator.i18n;

import com.thecoderscorner.menu.editorui.generator.logger.UserFeedbackLogger;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;
import com.thecoderscorner.menu.persist.PropertiesLocaleEnabledHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

public class SingleFileI18nCodeGeneratorTest {
    private Path tempDir;
    private Path projectDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("tcMenu");
        projectDir = tempDir.resolve("myProject");
        Files.createDirectories(projectDir);
    }

    @AfterEach
    void tearDown() throws IOException {
        FileSystemUtils.deleteRecursively(tempDir);
    }

    /**
     * Tests for the processLocale method in the SingleFileI18nCodeGenerator class.
     * <p>
     * The processLocale method generates a single locale file based on available enabled locales and logs this process.
     */

    @Test
    public void testProcessLocaleWithValidLocales() throws IOException {
        var feedbackLogger = mock(UserFeedbackLogger.class);
        var localeHandler = mock(LocaleMappingHandler.class);

        Locale previousLocale = Locale.ENGLISH;
        when(localeHandler.getCurrentLocale()).thenReturn(previousLocale);
        when(localeHandler.getEnabledLocales()).thenReturn(List.of(
                Locale.of(""),
                Locale.of("de"),
                Locale.of("fr")));
        when(localeHandler.getUnderlyingMap()).thenReturn(Map.of(
                "menu.hello", "Guten Tag",
                "menu.bye", "Auf Wiedersehen")
        );
        when(localeHandler.getUnderlyingMap()).thenReturn(Map.of(
                "menu.hello", "Bonjour",
                "menu.bye", "Au Revoir")
        );
        when(localeHandler.getUnderlyingMap()).thenReturn(Map.of(
                "menu.hello", "Hello",
                "menu.bye", "Goodbye")
        );

        var generator = new SingleFileI18nCodeGenerator(feedbackLogger, localeHandler);
        assertDoesNotThrow(() -> generator.processLocale(projectDir, false));

        assertThat(Files.readString(projectDir.resolve("myProject_langSelect.h"))).isEqualToIgnoringNewLines("""
                // TcMenu Generated locale header file containing all locale definitions.
                // To enable a particular language set build flag TC_LOCALE_<LANG>
                
                
                // Definitions for locale de
                #if defined(TC_LOCALE_DE)
                #define TC_I18N_MENU_BYE "Goodbye"
                #define TC_I18N_MENU_HELLO "Hello"
                
                // Definitions for locale fr
                #elif defined(TC_LOCALE_FR)
                #define TC_I18N_MENU_BYE "Goodbye"
                #define TC_I18N_MENU_HELLO "Hello"
                
                #else // default locale
                #define TC_I18N_MENU_BYE "Goodbye"
                #define TC_I18N_MENU_HELLO "Hello"
                #endif // locale definitionsEN
                
                
                // Its always better to use getTcLocaleString(string_id) method.
                #define getTcLocaleString(x) (x)
                """);

        verify(localeHandler).changeLocale(Locale.of("de"));
        verify(localeHandler).changeLocale(Locale.of("fr"));
        verify(localeHandler).changeLocale(PropertiesLocaleEnabledHandler.DEFAULT_LOCALE);
        verify(localeHandler).changeLocale(previousLocale);
    }

    @Test
    public void testProcessLocaleWithOnlyDefault() throws IOException {
        var feedbackLogger = mock(UserFeedbackLogger.class);
        var localeHandler = mock(LocaleMappingHandler.class);

        when(localeHandler.getCurrentLocale()).thenReturn(Locale.of(""));
        when(localeHandler.getEnabledLocales()).thenReturn(Collections.singletonList(Locale.of("")));
        when(localeHandler.getUnderlyingMap()).thenReturn(Map.of(
                "voltage.item.name", "Voltage",
                "voltage.item.unit", "V",
                "amps.item.name", "Amps",
                "amps.item.unit", "A"
        ));

        var generator = new SingleFileI18nCodeGenerator(feedbackLogger, localeHandler);

        assertDoesNotThrow(() -> generator.processLocale(projectDir, false));

        verify(feedbackLogger).info("Start locale processing");
        verify(feedbackLogger).info(contains("Wrote all locales to single file"));
        assertThat(Files.readString(projectDir.resolve("myProject_langSelect.h"))).isEqualToIgnoringNewLines("""
                // TcMenu Generated locale header file containing all locale definitions.
                // To enable a particular language set build flag TC_LOCALE_<LANG>
                
                #if defined(NOT_USING_I18N)
                
                #else // default locale
                #define TC_I18N_AMPS_ITEM_NAME "Amps"
                #define TC_I18N_AMPS_ITEM_UNIT "A"
                #define TC_I18N_VOLTAGE_ITEM_NAME "Voltage"
                #define TC_I18N_VOLTAGE_ITEM_UNIT "V"
                #endif // locale definitions
                
                
                // Its always better to use getTcLocaleString(string_id) method.
                #define getTcLocaleString(x) (x)
                """);

        verify(localeHandler, times(2)).changeLocale(PropertiesLocaleEnabledHandler.DEFAULT_LOCALE);
    }
}