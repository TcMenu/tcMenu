/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.pluginapi.CodeGenerator;
import com.thecoderscorner.menu.pluginapi.EmbeddedPlatform;

import java.util.List;

/**
 * An embedded platform creator provides both a list of platforms that are available and also is able to provide
 * a code generator for a given platform.
 */
public interface EmbeddedPlatforms {
    EmbeddedPlatform ARDUINO_AVR = new EmbeddedPlatform("Arduino AVR/Uno/Mega", "ARDUINO");
    EmbeddedPlatform ARDUINO32 = new EmbeddedPlatform("Arduino SAMD/ESP", "ARDUINO32");

    /** @return the list of available platforms */
    List<EmbeddedPlatform> getEmbeddedPlatforms();

    /**
     * Given a platform this method will return a ready configured code generator.
     * @param platform the platform
     * @return the generator ready for use.
     */
    CodeGenerator getCodeGeneratorFor(EmbeddedPlatform platform);

    /**
     * @param id the to be found
     * @return the embedded platform for the given id
     */
    EmbeddedPlatform getEmbeddedPlatformFromId(String id);
}
