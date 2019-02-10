/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

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
