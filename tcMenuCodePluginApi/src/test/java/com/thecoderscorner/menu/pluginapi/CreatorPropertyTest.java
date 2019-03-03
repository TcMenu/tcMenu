/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi;

import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.pluginapi.SubSystem.INPUT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
        EmbeddedPlatform emb1 = new EmbeddedPlatform("NotUsedEquality", "ARDUINO");
        EmbeddedPlatform emb2 = new EmbeddedPlatform("NotUsedEquality", "ARDUINO");
        EmbeddedPlatform emb3 = new EmbeddedPlatform("NotUsedEquality", "SAMD");
        assertEquals(emb1, emb2);
        assertEquals(emb1.hashCode(), emb2.hashCode());
        assertNotEquals(emb2, emb3);
    }
}