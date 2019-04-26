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
    protected String type;
    protected boolean paramUsed;

    /**
     * Define the parameter to have this value. The value can be null, and it will be converted to "NULL"
     * @param value the value to be represented ( toString will be used )
     */
    public CodeParameter(Object value) {
        this.value = value == null ? "NULL" : value.toString();
    }

    /**
     * Define a parameter and it's type, only used by the lambda code parameter type.
     * In this case, value must not be null.
     * @param value the name of the parameter
     * @param type the type of the parameter
     * @param used if the value is used within it's context.
     */
    public CodeParameter(String value, String type, boolean used) {
        this.type = type;
        this.value = value;
        this.paramUsed = used;
    }

    /**
     * @param context the context of the code conversion
     * @return the parameter value for this context
     */
    public String getParameterValue(CodeConversionContext context) {
        return value;
    }

    /**
     * @return the type associated with this parameter, or null if it was not defined.
     */
    public String getType() {
        return type;
    }

    /**
     * @return true if the parameter is marked as used, otherwise false.
     */
    public boolean isParamUsed() {
        return paramUsed;
    }
}
