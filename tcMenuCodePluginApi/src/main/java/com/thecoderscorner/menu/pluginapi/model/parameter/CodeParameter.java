/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi.model.parameter;

/**
 * Within the variable and function builders, the parameters are defined using types that specialise from ones
 * in this package.
 */
public class CodeParameter {
    protected String value;

    /**
     * Define the parameter to have this value.
     * @param value the value to be represented ( toString will be used )
     */
    public CodeParameter(Object value) {
        this.value = value == null ? "NULL" : value.toString();
    }

    /**
     * @param context the context of the code conversion
     * @return the parameter value for this context
     */
    public String getParameterValue(CodeConversionContext context) {
        return value;
    }
}
