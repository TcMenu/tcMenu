/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi.model.parameter;

import com.thecoderscorner.menu.pluginapi.CreatorProperty;

import java.util.Collection;

public class CodeConversionContext {
    private final String rootObject;
    private final Collection<CreatorProperty> properties;

    public CodeConversionContext(String rootObject, Collection<CreatorProperty> properties) {
        this.rootObject = rootObject;
        this.properties = properties;
    }

    public String getRootObject() {
        return rootObject;
    }

    public Collection<CreatorProperty> getProperties() {
        return properties;
    }
}
