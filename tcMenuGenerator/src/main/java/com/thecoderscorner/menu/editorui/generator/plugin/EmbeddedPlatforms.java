/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.core.CodeGenerator;

import java.util.List;

/**
 * An embedded platform creator provides both a list of platforms that are available and also is able to provide
 * a code generator for a given platform.
 */
public interface EmbeddedPlatforms {

    /** @return the list of available platforms */
    List<EmbeddedPlatform> getEmbeddedPlatforms();

    /**
     * Given a platform this method will return a ready configured code generator.
     * @param platform the platform
     * @return the generator ready for use.
     */
    CodeGenerator getCodeGeneratorFor(EmbeddedPlatform platform, CodeGeneratorOptions options);

    /**
     * @param id the to be found
     * @return the embedded platform for the given id
     */
    EmbeddedPlatform getEmbeddedPlatformFromId(String id);

    /**
     * @param platform the platform to check
     * @return true if it is an mbed platform
     */
    public boolean isMbed(EmbeddedPlatform platform);

    /**
     * @param platform the platform to check
     * @return true if it is an arduino platform
     */
    public boolean isArduino(EmbeddedPlatform platform);
}
