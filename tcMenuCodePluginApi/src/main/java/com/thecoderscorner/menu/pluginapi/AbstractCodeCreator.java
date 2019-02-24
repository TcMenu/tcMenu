/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi;

import com.thecoderscorner.menu.pluginapi.model.CodeVariableBuilder;
import com.thecoderscorner.menu.pluginapi.model.FunctionCallBuilder;
import com.thecoderscorner.menu.pluginapi.model.HeaderDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.pluginapi.SubSystem.INPUT;

public abstract class AbstractCodeCreator implements EmbeddedCodeCreator {
    public static final String LINE_BREAK = System.getProperty("line.separator");
    private static final CreatorProperty EMPTY = new CreatorProperty("", "", "-1", INPUT);

    private List<FunctionCallBuilder> functionCalls = new ArrayList<>();
    private List<CodeVariableBuilder> variables = new ArrayList<>();
    private List<String> libraryFiles = new ArrayList<>();

    @Override
    public void initialise(String root) {
        functionCalls.clear();
        variables.clear();
        initCreator(root);
    }

    protected abstract void initCreator(String root);

    @Override
    public List<CodeVariableBuilder> getVariables() {
        return variables;
    }

    @Override
    public List<HeaderDefinition> getIncludes() {
        var allHeaders = new ArrayList<HeaderDefinition>();
        allHeaders.addAll(variables.stream()
                .flatMap(var -> var.getHeaders().stream()).collect(Collectors.toList()));

        allHeaders.addAll(functionCalls.stream()
                .flatMap(fn -> fn.getHeaders().stream()).collect(Collectors.toList()));

        return allHeaders.stream().distinct().collect(Collectors.toList());
    }

    protected void addExportVariableIfPresent(String variable, String typeName) {
        String expVar = findPropertyValue(variable).getLatestValue();
        if(expVar != null && !expVar.isEmpty()) {
            addVariable(new CodeVariableBuilder().exportOnly().variableType(typeName).variableName(expVar));
        }
    }

    @Override
    public List<FunctionCallBuilder> getFunctionCalls() {
        return functionCalls;
    }

    protected void addLibraryFiles(String... files) {
        libraryFiles.addAll(Arrays.asList(files));
    }

    protected void addVariable(CodeVariableBuilder variable) {
        variables.add(variable);
    }

    protected void addFunctionCall(FunctionCallBuilder call) {
        functionCalls.add(call);
    }

    @Override
    public List<String> getRequiredFiles() {
        return Collections.unmodifiableList(libraryFiles);
    }

    public CreatorProperty findPropertyValue(String name) {
        return properties().stream().filter(p->name.equals(p.getName())).findFirst().orElse(EMPTY);
    }

    public int findPropertyValueAsIntWithDefault(String name, int defVal) {
        return properties().stream()
                .filter(p->name.equals(p.getName()))
                .map(CreatorProperty::getLatestValueAsInt)
                .findFirst().orElse(defVal);
    }

    protected boolean getBooleanFromProperty(String propName) {
        CreatorProperty prop = findPropertyValue(propName);
        return Boolean.valueOf(prop.getLatestValue());
    }
}
