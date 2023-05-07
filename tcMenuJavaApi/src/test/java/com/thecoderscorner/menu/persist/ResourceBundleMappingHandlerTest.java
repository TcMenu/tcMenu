package com.thecoderscorner.menu.persist;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

class ResourceBundleMappingHandlerTest {

    @Test
    public void testResourceBundleLoadingDefault() {
        var bundle = ResourceBundle.getBundle("testBundle.test");
        var handler = new ResourceBundleMappingHandler(bundle);
        assertEquals(PropertiesLocaleEnabledHandler.DEFAULT_LOCALE, handler.getCurrentLocale());
        assertEquals("hello", handler.getFromLocaleWithDefault("%welcome", "abc"));
        assertEquals("abc", handler.getFromLocaleWithDefault("welcome", "abc"));
    }

    @Test
    public void testFrenchTranslation() {
        var bundle = ResourceBundle.getBundle("testBundle.test", Locale.FRENCH);
        var handler = new ResourceBundleMappingHandler(bundle);
        assertEquals(Locale.FRENCH, handler.getCurrentLocale());
        assertEquals("bonjour", handler.getFromLocaleWithDefault("%welcome", "abc"));
        assertEquals("abc", handler.getFromLocaleWithDefault("welcome", "abc"));

        assertThrows(UnsupportedOperationException.class, () -> handler.saveChanges());
        assertThrows(UnsupportedOperationException.class, () -> handler.setLocalSpecificEntry("a", "b"));
        assertThrows(UnsupportedOperationException.class, () -> handler.changeLocale(Locale.CANADA_FRENCH));
    }

    @Test
    public void textGetUnderlyingMap() {
        var bundle = ResourceBundle.getBundle("testBundle.test", Locale.FRENCH);
        var handler = new ResourceBundleMappingHandler(bundle);
        var map = handler.getUnderlyingMap();
        assertEquals(7, map.size());
        assertEquals("bonjour", map.get("welcome"));
        assertEquals("au rivoir", map.get("leave"));
        assertEquals("merci", map.get("thanks"));
        assertEquals("Paramètres", map.get("menu.5.name"));
        assertEquals("1234", map.get("root.only.entry"));
        assertEquals("", map.get("menu.2.unit"));
    }
}