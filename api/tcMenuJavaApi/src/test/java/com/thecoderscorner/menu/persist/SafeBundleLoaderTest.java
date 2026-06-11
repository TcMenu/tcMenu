package com.thecoderscorner.menu.persist;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SafeBundleLoaderTest {
    public static final Locale EMPTY_LOCALE = new Locale("");
    private Path tempDir;

    @BeforeEach
    public void setup() throws IOException {
        tempDir = Files.createTempDirectory("safeloader");
        Files.write(tempDir.resolve("test.properties"), getClass().getResource("/testBundle/test.properties").openStream().readAllBytes());
        Files.write(tempDir.resolve("test_fr.properties"), getClass().getResource("/testBundle/test_fr.properties").openStream().readAllBytes());
        Files.write(tempDir.resolve("test_fr_CA.properties"), getClass().getResource("/testBundle/test_fr_CA.properties").openStream().readAllBytes());
    }

    @AfterEach
    public void cleanUp() throws IOException {
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }


    @Test
    public void testLoadingBundle() {
        SafeBundleLoader loader = new SafeBundleLoader(tempDir, "test");
        var bundle = loader.getBundleForLocale(Locale.ENGLISH);
        assertEquals("hello", bundle.getString("welcome"));
        assertEquals("goodbye", bundle.getString("leave"));
        assertEquals("thank you", bundle.getString("thanks"));

        bundle = loader.getBundleForLocale(Locale.FRENCH);
        assertEquals("bonjour", bundle.getString("welcome"));
        assertEquals("au rivoir", bundle.getString("leave"));
        assertEquals("merci", bundle.getString("thanks"));

        bundle = loader.getBundleForLocale(Locale.CANADA_FRENCH);
        assertEquals("bonjourCA", bundle.getString("welcome"));
    }

    @Test
    public void testLoadingBundleFailsWhenUnsafe() throws IOException {
        Files.write(tempDir.resolve("dodgy.content"), "hello".getBytes());
        assertThrows(IllegalArgumentException.class, () -> new SafeBundleLoader(tempDir, "test"));
    }
    @Test
    public void testSavingDefaultLocalePropertyToFile() throws IOException {
        SafeBundleLoader loader = new SafeBundleLoader(tempDir, "test");
        loader.saveChangesKeepingFormatting(EMPTY_LOCALE, Map.of("food", "pizza", "welcome", "hi"));
        var allBytes = Files.readAllBytes(tempDir.resolve("test.properties"));
        assertEqualsIgnoringCRLF("# comment at top\n" +
                "welcome=hi\n" +
                "\n" +
                "# blank line above, then comment.\n" +
                "leave=goodbye\n" +
                "\n" +
                "\n" +
                "# two blank lines and comment\n" +
                "thanks=thank you\n" +
                "\n" +
                "menu.5.name=Menu 5 name\n" +
                "menu.3.enum.1=Menu 3 enum1\n" +
                "\n" +
                "root.only.entry=1234\n" +
                "\n" +
                "# and a blank entry\n" +
                "menu.2.unit=\n" +
                "food=pizza\n", new String(allBytes));
    }

    @Test
    public void testSavingNewPropertyToFile() throws IOException {
        SafeBundleLoader loader = new SafeBundleLoader(tempDir, "test");
        loader.saveChangesKeepingFormatting(Locale.FRENCH, Map.of("food", "pizza", "welcome", "hi"));
        var allBytes = Files.readAllBytes(tempDir.resolve("test_fr.properties"));
        assertEqualsIgnoringCRLF("# comment on line 1\n" +
                "welcome=hi\n" +
                "\n" +
                "# comment and blank line\n" +
                "leave=au rivoir\n" +
                "\n" +
                "\n" +
                "# two blank lines and comment\n" +
                "thanks=merci\n" +
                "\n" +
                "# utf8 should be maintained in the file and not lost\n" +
                "menu.3.enum.1=Pates\n" +
                "food=pizza\n", new String(allBytes));

        var mapOfValues = loader.loadResourceBundleAsMap(Locale.FRENCH);
        assertEquals("hi", mapOfValues.get("welcome"));
        assertEquals("au rivoir", mapOfValues.get("leave"));
        assertEquals("merci", mapOfValues.get("thanks"));
        assertEquals("pizza", mapOfValues.get("food"));
    }

    @Test
    public void testSavingDefaultCompletelyNewFileLocaleOnly() throws IOException {
        SafeBundleLoader loader = new SafeBundleLoader(tempDir, "test");
        loader.saveChangesKeepingFormatting(Locale.GERMAN, Map.of("welcome", "hi"));
        var allBytes = Files.readAllBytes(tempDir.resolve("test_de.properties"));
        assertEqualsIgnoringCRLF("# Created by TcMenu to hold menu translations, will always be written in UTF-8\n" +
                "welcome=hi\n", new String(allBytes));

        loader.saveChangesKeepingFormatting(Locale.ITALIAN, Map.of());
        allBytes = Files.readAllBytes(tempDir.resolve("test_it.properties"));
        assertEqualsIgnoringCRLF("# Created by TcMenu to hold menu translations, will always be written in UTF-8\n", new String(allBytes));
    }

    @Test
    public void testSavingDefaultCompletelyNewFileLocaleCountry() throws IOException {
        var loader = new SafeBundleLoader(tempDir, "test");
        var handler = new PropertiesLocaleEnabledHandler(loader);

        // create a completely new locale of language and country and add a property to it.
        handler.changeLocale(new Locale("de", "CH"));
        handler.getWithLocaleInitIfNeeded("%extra.prop.name", "123"); // will only be in default
        handler.setLocalSpecificEntry("welcome", "Guten Tag"); // will only be this value in DE
        handler.saveChanges();

        // read it back and it should have the property after saving, the property should also be in the root locale
        var allBytes = Files.readAllBytes(tempDir.resolve("test_de_CH.properties"));
        assertEqualsIgnoringCRLF("# Created by TcMenu to hold menu translations, will always be written in UTF-8\n" +
                "welcome=Guten Tag\n", new String(allBytes));
        allBytes = Files.readAllBytes(tempDir.resolve("test.properties"));
        assertTrue(new String(allBytes).contains("extra.prop.name=123"));
        assertTrue(new String(allBytes).contains("welcome=hello"));
        assertFalse(new String(allBytes).contains("welcome=Guten Tag"));

        // now create a completely empty new file.
        loader.saveChangesKeepingFormatting(Locale.ITALIAN, Map.of());
        allBytes = Files.readAllBytes(tempDir.resolve("test_it.properties"));
        assertEqualsIgnoringCRLF("# Created by TcMenu to hold menu translations, will always be written in UTF-8\n", new String(allBytes));
    }

    @Test
    public void testGettingAndSetting() {
        SafeBundleLoader loader = new SafeBundleLoader(tempDir, "test");
        var handler = new PropertiesLocaleEnabledHandler(loader);
        assertEquals(loader, handler.getSafeLoader());
        assertEquals("test", handler.getSafeLoader().getBaseName());
        assertEquals(tempDir, handler.getSafeLoader().getLocation());
        assertEquals(tempDir.resolve("test_fr.properties"), handler.getSafeLoader().getPathForLocale(Locale.FRENCH));
        assertEquals(tempDir.resolve("test_fr_CA.properties"), handler.getSafeLoader().getPathForLocale(Locale.CANADA_FRENCH));
        assertEquals(tempDir.resolve("test.properties"), handler.getSafeLoader().getPathForLocale(EMPTY_LOCALE));

        var locales = handler.getEnabledLocales();
        assertEquals(3, locales.size());
        assertTrue(locales.contains(Locale.FRENCH));
        assertTrue(locales.contains(new Locale("fr", "CA")));
        assertTrue(locales.contains(EMPTY_LOCALE));
    }

    @Test
    public void testPropertiesLocaleHandleLanguageCountry() throws IOException {
        SafeBundleLoader loader = new SafeBundleLoader(tempDir, "test");
        var handler = new PropertiesLocaleEnabledHandler(loader);

        assertTrue(handler.isLocalSupportEnabled());
        handler.changeLocale(new Locale("fr", "CA"));

        // test that overrides work
        assertEquals("bonjourCA", handler.getLocalSpecificEntry("welcome"));
        assertEquals("Salade", handler.getFromLocaleWithDefault("%menu.3.enum.1", "non"));
        assertEquals("Paramètres des pâtes", handler.getFromLocaleWithDefault("%menu.5.name", "non"));

        // test that defaulting is back to language level and not root.
        assertEquals("au rivoir", handler.getFromLocaleWithDefault("%leave", "non"));
        assertEquals("merci", handler.getFromLocaleWithDefault("%thanks", "non"));

        // this should default back to the default locale
        assertEquals("1234", handler.getFromLocaleWithDefault("%root.only.entry", "non"));

        var underlyingMap = handler.getUnderlyingMap();
        assertEquals(7, underlyingMap.size());
        assertEquals("bonjourCA", underlyingMap.get("welcome"));
        assertEquals("Salade", underlyingMap.get("menu.3.enum.1"));
        assertEquals("Paramètres des pâtes", underlyingMap.get("menu.5.name"));

        // test that defaulting is back to language level and not root.
        assertEquals("au rivoir", underlyingMap.get("leave"));
        assertEquals("merci", underlyingMap.get("thanks"));

        // test an entry that only exists at the root level.
        assertEquals("1234", underlyingMap.get("root.only.entry"));
    }

    @Test
    public void testPropertiesLocaleHandler() throws IOException {
        SafeBundleLoader loader = new SafeBundleLoader(tempDir, "test");
        var handler = new PropertiesLocaleEnabledHandler(loader);

        assertTrue(handler.isLocalSupportEnabled());

        handler.changeLocale(Locale.FRENCH);
        assertEquals("bonjour", handler.getLocalSpecificEntry("welcome"));

        var underlyingMap = handler.getUnderlyingMap();
        assertEquals(7, underlyingMap.size());
        assertEquals("bonjour", underlyingMap.get("welcome"));
        assertEquals("Pates", handler.getFromLocaleWithDefault("%menu.3.enum.1", "non"));
        assertEquals("", handler.getFromLocaleWithDefault("%menu.2.unit", "non"));

        assertEquals(Locale.FRENCH, handler.getCurrentLocale());

        handler.changeLocale(EMPTY_LOCALE);
        assertEquals("hello", handler.getLocalSpecificEntry("welcome"));
        handler.setLocalSpecificEntry("welcome", "foo");
        handler.saveChanges();

        assertEquals("foo", handler.getFromLocaleWithDefault("%welcome", "non"));
        assertEquals("non", handler.getFromLocaleWithDefault("welcome", "non"));
        assertEquals("non", handler.getFromLocaleWithDefault("%another", "non"));

        var bundle = loader.getBundleForLocale(EMPTY_LOCALE);
        assertEquals("foo", bundle.getString("welcome"));
    }

    public static void assertEqualsIgnoringCRLF(String expected, String actual) {
        expected = expected.replaceAll("\\r\\n", "\n");
        actual = actual.replaceAll("\\r\\n", "\n");
        assertEquals(expected, actual);
    }
}
