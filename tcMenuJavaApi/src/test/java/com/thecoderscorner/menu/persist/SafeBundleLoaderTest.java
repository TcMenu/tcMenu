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
                "food=pizza\n", new String(allBytes));

        var mapOfValues = loader.loadResourceBundleAsMap(Locale.FRENCH);
        assertEquals("hi", mapOfValues.get("welcome"));
        assertEquals("au rivoir", mapOfValues.get("leave"));
        assertEquals("merci", mapOfValues.get("thanks"));
        assertEquals("pizza", mapOfValues.get("food"));
    }

    @Test
    public void testSavingDefaultCompltelyNewFile() throws IOException {
        SafeBundleLoader loader = new SafeBundleLoader(tempDir, "test");
        loader.saveChangesKeepingFormatting(Locale.GERMAN, Map.of("welcome", "hi"));
        var allBytes = Files.readAllBytes(tempDir.resolve("test_de.properties"));
        assertEqualsIgnoringCRLF("# Created by TcMenu to hold menu translations\n" +
                "welcome=hi\n", new String(allBytes));

        loader.saveChangesKeepingFormatting(Locale.ITALIAN, Map.of());
        allBytes = Files.readAllBytes(tempDir.resolve("test_it.properties"));
        assertEqualsIgnoringCRLF("# Created by TcMenu to hold menu translations\n", new String(allBytes));
    }

    @Test
    public void testGettingAndSetting() {
        SafeBundleLoader loader = new SafeBundleLoader(tempDir, "test");
        var handler = new PropertiesLocaleEnabledHandler(loader);
        assertEquals(loader, handler.getSafeLoader());
        assertEquals("test", handler.getSafeLoader().getBaseName());
        assertEquals(tempDir, handler.getSafeLoader().getLocation());
        assertEquals(tempDir.resolve("test_fr.properties"), handler.getSafeLoader().getPathForLocale(Locale.FRENCH));
        assertEquals(tempDir.resolve("test.properties"), handler.getSafeLoader().getPathForLocale(EMPTY_LOCALE));

        var locales = handler.getEnabledLocales();
        assertEquals(2, locales.size());
        assertTrue(locales.contains(Locale.FRENCH));
        assertTrue(locales.contains(EMPTY_LOCALE));
    }

    @Test
    public void testPropertiesLocaleHandler() throws IOException {
        SafeBundleLoader loader = new SafeBundleLoader(tempDir, "test");
        var handler = new PropertiesLocaleEnabledHandler(loader);

        assertTrue(handler.isLocalSupportEnabled());

        handler.changeLocale(Locale.FRENCH);
        assertEquals("bonjour", handler.getLocalSpecificEntry("welcome"));

        handler.changeLocale(EMPTY_LOCALE);
        assertEquals("hello", handler.getLocalSpecificEntry("welcome"));
        handler.setLocalSpecificEntry("welcome", "foo");
        handler.saveChanges();

        var bundle = loader.getBundleForLocale(EMPTY_LOCALE);
        assertEquals("foo", bundle.getString("welcome"));
    }

    public static void assertEqualsIgnoringCRLF(String expected, String actual) {
        expected = expected.replaceAll("\\r\\n", "\n");
        actual = actual.replaceAll("\\r\\n", "\n");
        assertEquals(expected, actual);
    }
}
