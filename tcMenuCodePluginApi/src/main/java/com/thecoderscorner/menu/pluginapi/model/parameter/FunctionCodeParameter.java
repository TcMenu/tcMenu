package com.thecoderscorner.menu.pluginapi.model.parameter;

public class FunctionCodeParameter extends CodeParameter{

    public FunctionCodeParameter(Object value) {
        super(value);
    }

    public String getParameterValue(CodeConversionContext context) {
        return value + "()";
    }
}
