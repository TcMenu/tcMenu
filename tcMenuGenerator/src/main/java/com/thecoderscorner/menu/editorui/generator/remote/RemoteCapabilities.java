/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.remote;

import com.thecoderscorner.menu.pluginapi.EmbeddedCodeCreator;
import com.thecoderscorner.menu.pluginapi.EmbeddedPlatform;
import com.thecoderscorner.menu.pluginapi.EnumWithApplicability;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class RemoteCapabilities extends EnumWithApplicability {
    public static Map<Integer, RemoteCapabilities> values = new HashMap<>();

    static {
        addValue(1, Set.of(EmbeddedPlatform.ARDUINO), "No Remote", NoRemoteCapability.class);
        addValue(2, Set.of(EmbeddedPlatform.ARDUINO), "Serial Remote", Rs232RemoteCapabilitiesCreator.class);
        addValue(3, Set.of(EmbeddedPlatform.ARDUINO), "Ethernet Remote", EthernetRemoteCapabilitiesCreator.class);
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
