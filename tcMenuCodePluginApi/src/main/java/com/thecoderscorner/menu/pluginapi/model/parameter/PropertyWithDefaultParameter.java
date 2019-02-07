package com.thecoderscorner.menu.pluginapi.model.parameter;

import com.thecoderscorner.menu.pluginapi.CreatorProperty;

public class PropertyWithDefaultParameter extends CodeParameter {

    private String defVal;

    public PropertyWithDefaultParameter(String value, String defVal) {
        super(value);
        this.defVal = defVal;
    }

    @Override
    public String getParameterValue(CodeConversionContext context) {
        return context.getProperties().stream()
                .filter(prop -> prop.getName().equals(value))
                .map(CreatorProperty::getLatestValue)
                .filter(val -> val != null && !val.isEmpty())
                .findFirst()
                .orElse(defVal);
    }
}
