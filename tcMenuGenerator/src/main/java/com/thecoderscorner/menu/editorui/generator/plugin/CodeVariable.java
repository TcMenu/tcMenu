/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.editorui.generator.applicability.CodeApplicability;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeParameter;

import java.util.List;
import java.util.Objects;

public class CodeVariable {
    private final String variableName;
    private final String objectName;
    private final VariableDefinitionMode definitionMode;
    private final boolean progMem;
    private final boolean inContext;
    private final List<CodeParameter> parameterList;
    private final CodeApplicability applicability;

    public CodeVariable(String variableName, String objectName, VariableDefinitionMode definitionMode,
                        boolean progMem, boolean inContext, List<CodeParameter> parameterList,
                        CodeApplicability applicability) {
        this.variableName = variableName;
        this.objectName = objectName;
        this.definitionMode = definitionMode;
        this.progMem = progMem;
        this.inContext = inContext;
        this.parameterList = parameterList;
        this.applicability = applicability;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodeVariable that = (CodeVariable) o;
        return progMem == that.progMem && inContext == that.inContext && Objects.equals(variableName, that.variableName) &&
                Objects.equals(objectName, that.objectName) && definitionMode == that.definitionMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(variableName, objectName, definitionMode, progMem, inContext);
    }

    public String getVariableName() {
        return variableName;
    }

    public String getObjectName() {
        return objectName;
    }

    public VariableDefinitionMode getDefinitionMode() {
        return definitionMode;
    }

    public boolean isProgMem() {
        return progMem;
    }

    public boolean isInContext() { return inContext; }

    public List<CodeParameter> getParameterList() {
        return parameterList;
    }

    public CodeApplicability getApplicability() {
        return applicability;
    }

    @Override
    public String toString() {
        return "CodeVariable{" +
                "variableName='" + variableName + '\'' +
                ", objectName='" + objectName + '\'' +
                ", definitionMode=" + definitionMode +
                ", progMem=" + progMem +
                ", parameterList=" + parameterList +
                ", applicability=" + applicability +
                '}';
    }

    public boolean isExported() {
        return definitionMode == VariableDefinitionMode.EXPORT_ONLY || definitionMode == VariableDefinitionMode.VARIABLE_AND_EXPORT
                || definitionMode == VariableDefinitionMode.FONT_EXPORT;
    }

    public boolean isVariableDefNeeded() {
        return definitionMode == VariableDefinitionMode.VARIABLE_AND_EXPORT || definitionMode == VariableDefinitionMode.VARIABLE_ONLY;
    }
}
