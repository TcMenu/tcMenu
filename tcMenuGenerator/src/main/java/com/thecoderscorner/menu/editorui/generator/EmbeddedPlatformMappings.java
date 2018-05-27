/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator;

import com.google.common.collect.Sets;

import java.util.Set;

public interface EmbeddedPlatformMappings {
    Set<EmbeddedPlatform> ALL_ARDUINO_BOARDS = Sets.newHashSet(EmbeddedPlatform.ARDUINO_8BIT);
    Set<EmbeddedPlatform> ALL_DEVICES = Sets.newHashSet(EmbeddedPlatform.ARDUINO_8BIT);
}
