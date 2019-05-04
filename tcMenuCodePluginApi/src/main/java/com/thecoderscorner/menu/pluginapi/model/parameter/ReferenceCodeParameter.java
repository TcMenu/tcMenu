/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi.model.parameter;

/**
 * A reference code parameter
 */
public class ReferenceCodeParameter extends CodeParameter{

    public ReferenceCodeParameter(Object value) {
        super(value);
    }

    public String getParameterValue(CodeConversionContext context) {
        if(value.equals("NULL")) return value;
        else return "&" + value;
    }
}
