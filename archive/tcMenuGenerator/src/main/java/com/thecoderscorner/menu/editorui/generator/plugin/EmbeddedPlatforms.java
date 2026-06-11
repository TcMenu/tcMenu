/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.plugin;

import java.util.List;

/**
 * An embedded platform creator provides both a list of platforms that are available and also is able to provide
 * a code generator for a given platform.
 */
public interface EmbeddedPlatforms {
    String CPP_PLATFORM_GROUP_STR = "TrueCpp";
    String JAVA_PLATFORM_GROUP_STR = "EmbJava";
    String ARDUINO_PLATFORM_GROUP_STR = "Arduino";

    /** @return the list of available platforms */
    List<EmbeddedPlatform> getEmbeddedPlatforms();

    /**
     * @param id the to be found
     * @return the embedded platform for the given id
     */
    EmbeddedPlatform getEmbeddedPlatformFromId(String id);

    /**
     * @param platform the platform to check
     * @return true if it is an mbed platform
     */
    boolean isNativeCpp(EmbeddedPlatform platform);

    /**
     * @param platform the platform to check
     * @return true if it is an arduino platform
     */
    boolean isArduino(EmbeddedPlatform platform);

    /**
     * @param platform the platform to check
     * @return true if it is a Java platform such as Raspberry PI
     */
    boolean isJava(EmbeddedPlatform platform);

    List<EmbeddedPlatform> getEmbeddedPlatformsFromGroup(String group);
}
