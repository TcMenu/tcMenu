package com.thecoderscorner.menu.pluginapi.model.parameter;

public class CodeParameter {
    protected String value;

    public CodeParameter(Object value) {
        this.value = value == null ? "NULL" : value.toString();
    }

    public String getParameterValue(CodeConversionContext context) {
        return value;
    }
}
