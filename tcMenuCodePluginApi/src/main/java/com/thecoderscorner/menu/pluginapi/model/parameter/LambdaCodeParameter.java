/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi.model.parameter;

import com.thecoderscorner.menu.pluginapi.model.FunctionCallBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * describes a parameter that is provided in the form of a lambda function. In order to define
 * a lambda we provide it's parameters and any function calls within the lambda block.
 */
public class LambdaCodeParameter extends CodeParameter {
    private final List<CodeParameter> parameters = new ArrayList<>();
    private final List<FunctionCallBuilder> functions = new ArrayList<>();

    /**
     * Creates an empty lambda function definition
     */
    public LambdaCodeParameter() {
        super("");
    }

    /**
     * adds a paramter to a lambda function definition
     * @param param the parameter object
     * @return itself for chaining
     */
    public LambdaCodeParameter addParameter(CodeParameter param) {
        parameters.add(param);
        return this;
    }

    /**
     * adds a function to this lambda's code block
     * @param function the function definition
     * @return
     */
    public LambdaCodeParameter addFunctionCall(FunctionCallBuilder function) {
        functions.add(function);
        return this;
    }

    /**
     * @return all added parameters in order of adding
     */
    public List<CodeParameter> getParameters() {
        return parameters;
    }

    /**
     * @return all added functions in order of adding
     */
    public List<FunctionCallBuilder> getFunctions() {
        return functions;
    }
}
