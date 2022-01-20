/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.parameters;

import com.thecoderscorner.menu.editorui.generator.applicability.CodeApplicability;
import com.thecoderscorner.menu.editorui.generator.plugin.FunctionDefinition;

import java.util.List;

public class LambdaDefinition {
    private final String name;
    private final List<CodeParameter> params;
    private final List<com.thecoderscorner.menu.editorui.generator.plugin.FunctionDefinition> functionDefinitions;
    private final CodeApplicability applicability;

    public LambdaDefinition(String name, List<CodeParameter> params, List<FunctionDefinition> functionDefinitions, CodeApplicability applicability) {
        this.name = name;
        this.params = params;
        this.functionDefinitions = functionDefinitions;
        this.applicability = applicability;
    }

    public String getName() {
        return name;
    }

    public List<CodeParameter> getParams() {
        return params;
    }

    public List<FunctionDefinition> getFunctionDefinitions() {
        return functionDefinitions;
    }

    public CodeApplicability getApplicability() {
        return applicability;
    }
}
