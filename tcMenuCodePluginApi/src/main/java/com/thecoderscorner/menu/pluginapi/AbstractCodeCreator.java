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

/**
 * AbstractCodeCreator is the base class that should be used for nearly all code conversion plug-ins.
 * It is much easier to work with that to use the interface directly.
 *
 * This class provides a way to provide variables, function calls, export definitions and includes in
 * a platform independent way. It is normally used to represent a specific display, input or remote
 * driver and the code needed to initialise and work with it.
 *
 * It may also need extra files to be copied into the users sketch, in that case they can be either
 * provided relative to the tcMenu library or relative to the arduino directory at the top level of
 * the JAR.
 *
 * Each code creator has a list of properties, these properties are provided by the properties method
 * which should be overridden to provide this list. The properties can be referred to during code
 * conversion, and they are set by the user during code creation.
 */
public abstract class AbstractCodeCreator implements EmbeddedCodeCreator {
    public static final String LINE_BREAK = System.getProperty("line.separator");
    private static final CreatorProperty EMPTY = new CreatorProperty("", "", "-1", INPUT);

    private List<FunctionCallBuilder> functionCalls = new ArrayList<>();
    private List<CodeVariableBuilder> variables = new ArrayList<>();
    private List<PluginFileDependency> libraryFiles = new ArrayList<>();
    private List<HeaderDefinition> headerDefinitions = new ArrayList<>();

    /**
     * Do not override this, use initCreator
     * @param root the first menu item in the tree
     */
    @Override
    public void initialise(String root) {
        functionCalls.clear();
        variables.clear();
        initCreator(root);
    }

    /**
     * The usual way to work with this class is to build all the required structures within
     * this method. Using the addVariable, addFunction etc.
     * @param root the top level menu name
     */
    protected abstract void initCreator(String root);

    /**
     * @return a list of variables previously added
     */
    @Override
    public List<CodeVariableBuilder> getVariables() {
        return variables;
    }

    /**
     * @return a list of includes to be added
     */
    @Override
    public List<HeaderDefinition> getIncludes() {
        var allHeaders = new ArrayList<>(headerDefinitions);
        allHeaders.addAll(variables.stream()
                .flatMap(var -> var.getHeaders().stream()).collect(Collectors.toList()));

        allHeaders.addAll(functionCalls.stream()
                .flatMap(fn -> fn.getHeaders().stream()).collect(Collectors.toList()));

        return allHeaders.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Adds a variable for export only when a property is present
     * @param variable the variable name (and also the expected property value)
     * @param typeName the type name
     */
    protected void addExportVariableIfPresent(String variable, String typeName) {
        String expVar = findPropertyValue(variable).getLatestValue();
        if(expVar != null && !expVar.isEmpty()) {
            addVariable(new CodeVariableBuilder().exportOnly().variableType(typeName).variableName(expVar));
        }
    }

    /**
     * @return a list of function calls previously added
     */
    @Override
    public List<FunctionCallBuilder> getFunctionCalls() {
        return functionCalls;
    }

    /**
     * adds requirements on one or more files that must be copied into the project,
     * this is only for files that already exist in tcMenu library, prefer to use the
     * other version that takes `PluiginFileDependency`
     * @see PluginFileDependency
     * @param files the list of library files.
     */
    protected void addLibraryFiles(String... files) {
        libraryFiles.addAll(Arrays.stream(files)
                .map(PluginFileDependency::fileInTcMenu)
                .collect(Collectors.toList())
        );
    }

    /**
     * adds requirements on one or more files describing if they are located in
     * the library or plugin, and a map of possible replacements.
     * @param files the list of dependencies
     */
    protected void addLibraryFiles(PluginFileDependency... files) {
        libraryFiles.addAll(Arrays.asList(files));
    }

    /**
     * Adds a variable that must be added to the users menu definition files.
     * @param variable the variable definition
     */
    protected void addVariable(CodeVariableBuilder variable) {
        variables.add(variable);
    }

    /**
     * Adds a function call that must be added to the users menu definition files.
     * @param call the function definition
     */
    protected void addFunctionCall(FunctionCallBuilder call) {
        functionCalls.add(call);
    }

    /**
     * Adds the requirement on a header file.
     * @param headerDefinition the header file to add.
     */
    protected void addHeader(HeaderDefinition headerDefinition) {
        headerDefinitions.add(headerDefinition);
    }

    /**
     * @return the list of required files
     */
    @Override
    public List<PluginFileDependency> getRequiredFiles() {
        return Collections.unmodifiableList(libraryFiles);
    }

    /**
     * Gets a property by it's name
     * @param name the name of the property to find
     * @return the property or EMPTY
     */
    public CreatorProperty findPropertyValue(String name) {
        return properties().stream().filter(p->name.equals(p.getName())).findFirst().orElse(EMPTY);
    }

    /**
     * Find the integer value of a property or a default if it is not set
     * @param name the property name
     * @param defVal the default value
     * @return the properties integer value or the default
     */
    public int findPropertyValueAsIntWithDefault(String name, int defVal) {
        return properties().stream()
                .filter(p->name.equals(p.getName()))
                .map(CreatorProperty::getLatestValueAsInt)
                .findFirst().orElse(defVal);
    }

    /**
     * Returns the boolean value of a property
     * @param propName the name of the property
     * @return boolean value of property
     */
    protected boolean getBooleanFromProperty(String propName) {
        CreatorProperty prop = findPropertyValue(propName);
        return Boolean.valueOf(prop.getLatestValue());
    }
}
