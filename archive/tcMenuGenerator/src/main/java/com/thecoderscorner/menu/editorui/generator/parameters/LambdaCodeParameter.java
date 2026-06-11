/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.parameters;

import com.thecoderscorner.menu.editorui.generator.plugin.FunctionDefinition;

/**
 * describes a parameter that is provided in the form of a lambda function. In order to define
 * a lambda we provide it's parameters and any function calls within the lambda block.
 */
public class LambdaCodeParameter extends CodeParameter {
    private final LambdaDefinition lambda;

    public LambdaCodeParameter(LambdaDefinition lambda) {
        super(CodeParameter.NO_TYPE, null, true, "");
        this.lambda = lambda;
    }

    public LambdaDefinition getLambda() {
        return lambda;
    }
}