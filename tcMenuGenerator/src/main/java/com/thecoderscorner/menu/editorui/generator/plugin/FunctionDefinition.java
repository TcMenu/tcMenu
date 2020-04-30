/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.editorui.generator.applicability.CodeApplicability;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeParameter;

import java.util.List;

public class FunctionDefinition {
    private final String functionName;
    private final String objectName;
    private final boolean objectPointer;
    private final List<CodeParameter> parameters;
    private final CodeApplicability applicability;

    public FunctionDefinition(String functionName, String objectName, boolean objectPointer,
                              List<CodeParameter> parameters, CodeApplicability applicability) {
        this.functionName = functionName;
        this.objectName = objectName;
        this.objectPointer = objectPointer;
        this.parameters = parameters;
        this.applicability = applicability;
    }

    @Override
    public String toString() {
        return "FunctionDefinition{" +
                "functionName='" + functionName + '\'' +
                ", objectName='" + objectName + '\'' +
                ", objectPointer=" + objectPointer +
                ", parameters=" + parameters +
                ", applicability=" + applicability +
                '}';
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getObjectName() {
        return objectName;
    }

    public boolean isObjectPointer() {
        return objectPointer;
    }

    public List<CodeParameter> getParameters() {
        return parameters;
    }

    public CodeApplicability getApplicability() {
        return applicability;
    }
}
