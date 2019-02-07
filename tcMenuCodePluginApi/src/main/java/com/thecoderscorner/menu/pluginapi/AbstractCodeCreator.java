/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.pluginapi;

import com.thecoderscorner.menu.pluginapi.model.CodeVariableBuilder;
import com.thecoderscorner.menu.pluginapi.model.FunctionCallBuilder;
import com.thecoderscorner.menu.pluginapi.model.HeaderDefinition;
import com.thecoderscorner.menu.pluginapi.model.parameter.CodeConversionContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.pluginapi.SubSystem.INPUT;

public abstract class AbstractCodeCreator implements EmbeddedCodeCreator {
    public static final String LINE_BREAK = System.getProperty("line.separator");
    private static final CreatorProperty EMPTY = new CreatorProperty("", "", "-1", INPUT);
    public static final String ROOT_ITEM_REPLACEMENT = "$ROOT$";

    private List<FunctionCallBuilder> functionCalls = new ArrayList<>();
    private List<CodeVariableBuilder> variables = new ArrayList<>();
    private List<String> libraryFiles = new ArrayList<>();

    @Override
    public String getExportDefinitions() {
        var props = properties().stream()
                .filter(prop -> prop.getPropType() == CreatorProperty.PropType.USE_IN_DEFINE)
                .map(prop -> ("#define " + prop.getName() + " " + prop.getLatestValue()))
                .collect(Collectors.joining(LINE_BREAK));

        var exports = variables.stream()
                .filter(CodeVariableBuilder::isExported)
                .map(CodeVariableBuilder::getExport)
                .collect(Collectors.joining(LINE_BREAK));

        // don't output two blank lines basically when empty.
        if(props.isEmpty() && exports.isEmpty()) return "";

        return props + (props.isEmpty()?"":LINE_BREAK) +  exports + (exports.isEmpty()?"":LINE_BREAK);
    }

    @Override
    public List<String> getIncludes() {
        var allHeaders = new ArrayList<HeaderDefinition>();
        allHeaders.addAll(variables.stream()
                .flatMap(var -> var.getHeaders().stream()).collect(Collectors.toList()));

        allHeaders.addAll(functionCalls.stream()
                .flatMap(fn -> fn.getHeaders().stream()).collect(Collectors.toList()));

        return allHeaders.stream().distinct().map(HeaderDefinition::getHeaderCode).collect(Collectors.toList());
    }

    @Override
    public String getGlobalVariables() {
        CodeConversionContext context = new CodeConversionContext(null, properties());

        var output = variables.stream()
                .map(v -> v.getVariable(context))
                .collect(Collectors.joining(LINE_BREAK));

        if(output.isEmpty()) return "";

        return output + LINE_BREAK;
    }

    @Override
    public String getSetupCode(String rootItem) {
        CodeConversionContext context = new CodeConversionContext(rootItem, properties());

        return functionCalls.stream()
                .map(f -> f.getFunctionCode(context))
                .map(s -> s.equals(ROOT_ITEM_REPLACEMENT) ? "&" + rootItem : s)
                .collect(Collectors.joining(LINE_BREAK)) + LINE_BREAK;
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
