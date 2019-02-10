/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

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
