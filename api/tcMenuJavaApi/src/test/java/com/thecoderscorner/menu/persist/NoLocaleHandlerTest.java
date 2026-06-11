package com.thecoderscorner.menu.persist;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class NoLocaleHandlerTest {
    @Test
    public void testNoLocaleCase() {
        var locale = new NoLocaleEnabledLocalHandler();
        assertFalse(locale.isLocalSupportEnabled());
        assertEquals(0, locale.getEnabledLocales().size());
        assertEquals("123", locale.getLocalSpecificEntry("123"));
        assertEquals(Map.of(), locale.getUnderlyingMap());
        assertEquals(new Locale("--"), locale.getCurrentLocale());
        assertThrows(IllegalArgumentException.class,  () -> locale.changeLocale(Locale.FRENCH));
        assertThrows(IllegalArgumentException.class,  () -> locale.setLocalSpecificEntry("a", "b"));
        locale.saveChanges(); // should do nothing.
    }
}
