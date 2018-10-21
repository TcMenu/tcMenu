/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.remote;

import com.thecoderscorner.menu.editorui.generator.EmbeddedCodeCreator;
import com.thecoderscorner.menu.editorui.generator.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.EnumWithApplicability;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.thecoderscorner.menu.editorui.generator.EmbeddedPlatformMappings.ALL_ARDUINO_BOARDS;
import static com.thecoderscorner.menu.editorui.generator.EmbeddedPlatformMappings.ALL_DEVICES;

public class RemoteCapabilities extends EnumWithApplicability {
    public static Map<Integer, RemoteCapabilities> values = new HashMap<>();

    static {
        addValue(1, ALL_DEVICES, "No Remote", NoRemoteCapability.class);
        addValue(2, ALL_ARDUINO_BOARDS, "Serial Remote", Rs232RemoteCapabilitiesCreator.class);
        addValue(3, ALL_ARDUINO_BOARDS, "Ethernet Remote", EthernetRemoteCapabilitiesCreator.class);
    }

    private static void addValue(int key, Set<EmbeddedPlatform> applicability, String description,
                                 Class<? extends EmbeddedCodeCreator> creator) {
        values.put(key, new RemoteCapabilities(applicability, description, creator, key));
    }

    RemoteCapabilities(Set<EmbeddedPlatform> platformApplicability, String description,
                       Class<? extends EmbeddedCodeCreator> creator, int key) {
        super(platformApplicability, description, creator, key);
    }

}
