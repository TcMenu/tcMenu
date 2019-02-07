package com.thecoderscorner.menu.pluginapi.model.parameter;

public class RootItemCodeParameter extends CodeParameter {

    public RootItemCodeParameter() {
        super("");
    }

    @Override
    public String getParameterValue(CodeConversionContext context) {
        return "&" + context.getRootObject();
    }
}
