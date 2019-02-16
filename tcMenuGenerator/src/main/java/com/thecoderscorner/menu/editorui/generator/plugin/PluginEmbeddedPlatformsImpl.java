/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoGenerator;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoSketchFileAdjuster;
import com.thecoderscorner.menu.pluginapi.CodeGenerator;
import com.thecoderscorner.menu.pluginapi.EmbeddedPlatform;

import java.util.List;

/**
 * This implementation of the embedded platforms creator has now been broken out in such a way that as there
 * are other board types, it will be easier to support them by means of plugins. In this release only arduino is
 * supported, and it is still wired into the main application. However, moving it into a plugin if needed is now
 * trivial.
 *
 */
public class PluginEmbeddedPlatformsImpl implements EmbeddedPlatforms {
    private final List<EmbeddedPlatform> platforms = List.of(DEFAULT);

    @Override
    public List<EmbeddedPlatform> getEmbeddedPlatforms() {
        return platforms;
    }

    @Override
    public CodeGenerator getCodeGeneratorFor(EmbeddedPlatform platform) {
        if(platform.equals(DEFAULT)) {
            return new ArduinoGenerator(new ArduinoSketchFileAdjuster(), new ArduinoLibraryInstaller());
        }
        else {
            throw new IllegalArgumentException("No such platform " + platform);
        }
    }

    @Override
    public EmbeddedPlatform getEmbeddedPlatformFromId(String id) {
        if(!DEFAULT.getBoardId().equals(id)) throw new IllegalArgumentException("Invalid platform " + id);
        return DEFAULT;
    }
}
