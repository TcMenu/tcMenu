package com.thecoderscorner.menu.pluginapi;

import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.pluginapi.SubSystem.*;
import static org.junit.jupiter.api.Assertions.*;

class CreatorPropertyTest {
    @Test
    void testCreatorProperty() {
        CreatorProperty prop = new CreatorProperty("name", "desc", "123", INPUT);
        CreatorProperty propCopy = new CreatorProperty("name", "desc", "123", INPUT);
        assertEquals("name", prop.getName());
        assertEquals("desc", prop.getDescription());
        assertEquals("123", prop.getLatestValue());
        assertEquals(INPUT, prop.getSubsystem());
        assertEquals(CreatorProperty.PropType.USE_IN_DEFINE, prop.getPropType());

        assertEquals(prop, propCopy);
        assertEquals(prop.hashCode(), propCopy.hashCode());

        prop.getProperty().setValue("10");
        assertEquals(10, prop.getLatestValueAsInt());

        assertEquals("10", prop.getLatestValue());

        assertNotEquals(prop, propCopy);
    }

    @Test
    void testArduinoPlatformEnum() {
        assertEquals("Arduino - Uno, Mega, 8bit", EmbeddedPlatform.ARDUINO.toString());
    }
}