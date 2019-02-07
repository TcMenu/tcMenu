package com.thecoderscorner.menu.pluginapi.model.parameter;

public class QuotedCodeParameter extends CodeParameter {
    private final char quoteCharacter;

    public QuotedCodeParameter(Object value, char quoteCharacter) {
        super(value);
        this.quoteCharacter = quoteCharacter;
    }

    @Override
    public String getParameterValue(CodeConversionContext context) {
        if(value.equals("NULL")) return "NULL";

        return quoteCharacter + value + quoteCharacter;
    }
}
