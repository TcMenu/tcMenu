/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.editorui.generator.applicability.CodeApplicability;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeParameter;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

import static com.thecoderscorner.menu.editorui.generator.plugin.JavaPluginItem.ALWAYS_APPLICABLE;

public class FunctionDefinition {
    private final String functionName;
    private final String objectName;
    private final boolean objectPointer;
    private final boolean infiniteLoop;
    @Getter
    private final boolean builderSyntax;
    private final List<CodeParameter> parameters;
    private final CodeApplicability applicability;

    public FunctionDefinition(String functionName, String objectName, boolean objectPointer, boolean infiniteLoop,
                              boolean builderSyntax, List<CodeParameter> parameters, CodeApplicability applicability) {
        this.functionName = functionName;
        this.objectName = objectName;
        this.objectPointer = objectPointer;
        this.builderSyntax = builderSyntax;
        this.parameters = parameters;
        this.applicability = applicability;
        this.infiniteLoop = infiniteLoop;
    }

    public static FunctionDefinition ofRegCpp(String fn, String obj, List<CodeParameter> params) {
        return new FunctionDefinition(fn, obj, false, false, false, params, ALWAYS_APPLICABLE);
    }

    public static FunctionDefinition ofBuilderDeclaration(String fn, String obj, List<CodeParameter> buildSteps) {
        return new FunctionDefinition(fn, obj, false, false, true, buildSteps, ALWAYS_APPLICABLE);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionDefinition that = (FunctionDefinition) o;
        return objectPointer == that.objectPointer && infiniteLoop == that.infiniteLoop &&
                Objects.equals(functionName, that.functionName) && Objects.equals(objectName, that.objectName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(functionName, objectName, objectPointer, infiniteLoop);
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

    public boolean isInfiniteLoopFn() {
        return infiniteLoop;
    }

}
