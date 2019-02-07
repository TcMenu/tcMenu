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
