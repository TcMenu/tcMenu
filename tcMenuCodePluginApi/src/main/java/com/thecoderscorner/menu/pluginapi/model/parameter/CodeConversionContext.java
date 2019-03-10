/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi.model.parameter;

import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.EmbeddedPlatform;

import java.util.Collection;

/**
 * When code is being converted we need to know the context of the conversion, this context should contain all the
 * properties of the conversion and the name of the root item in the tree. It also contains the platform.
 */
public class CodeConversionContext {
    private final String rootObject;
    private final Collection<CreatorProperty> properties;
    private final EmbeddedPlatform platform;

    public CodeConversionContext(EmbeddedPlatform platform, String rootObject, Collection<CreatorProperty> properties) {
        this.rootObject = rootObject;
        this.properties = properties;
        this.platform = platform;
    }

    public String getRootObject() {
        return rootObject;
    }

    public Collection<CreatorProperty> getProperties() {
        return properties;
    }

    public EmbeddedPlatform getPlatform() {
        return platform;
    }
}
