package com.thecoderscorner.menu.pluginapi.util;

import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.EmbeddedCodeCreator;
import com.thecoderscorner.menu.pluginapi.SubSystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestUtils {
    public static void assertEqualsIgnoringCRLF(String expected, String actual) {
        expected = expected.replaceAll("\\r\\n", "\n");
        actual = actual.replaceAll("\\r\\n", "\n");
        assertEquals(expected, actual);
    }

    public static CreatorProperty findAndSetValueOnProperty(EmbeddedCodeCreator creator, String name, SubSystem subSystem,
                                                       CreatorProperty.PropType type, Object newVal) {
        CreatorProperty prop = creator.properties().stream()
                .filter(p -> p.getName().equals(name))
                .findFirst().orElse(null);

        assertNotNull(prop);

        assertEquals(subSystem, prop.getSubsystem());
        assertEquals(type, prop.getPropType());
        String newStr = newVal.toString();
        prop.getProperty().setValue(newStr);
        assertEquals(newStr, prop.getLatestValue());
        return prop;
    }

}
