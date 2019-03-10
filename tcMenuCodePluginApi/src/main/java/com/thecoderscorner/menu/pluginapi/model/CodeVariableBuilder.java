/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi.model;

import com.thecoderscorner.menu.pluginapi.model.parameter.*;

import java.util.*;

/**
 * Provides a builder pattern for describing variables that need to be created in order for this plugin to work.
 * This allows for describing each needed varaible in the constructor and then allowing the AbstractCodeCreator
 * to do the rest.
 */
public class CodeVariableBuilder {

    enum DefinitionMode { EXPORT_AND_DEFINE, EXPORT_ONLY, VARIABLE_ONLY }
    private DefinitionMode definitionMode = DefinitionMode.VARIABLE_ONLY;
    private String type;
    private String name;
    private boolean byAssignment = false;
    private boolean progmem = false;
    private List<CodeParameter> params = new ArrayList<>();
    private Set<HeaderDefinition> headers = new HashSet<>();

    /**
     * indicates that this variable needs exporting for use in other files.
     * @return this for chaining
     */
    public CodeVariableBuilder exportNeeded() {
        this.definitionMode = DefinitionMode.EXPORT_AND_DEFINE;
        return this;
    }

    public CodeVariableBuilder exportOnly() {
        this.definitionMode = DefinitionMode.EXPORT_ONLY;
        return this;
    }

    public CodeVariableBuilder byAssignment() {
        this.byAssignment = true;
        return this;
    }

    public CodeVariableBuilder progmem() {
        this.progmem = true;
        return this;
    }


    /**
     * Set the variable name for this var.
     * @param  variableName variable name
     * @return this for chaining
     */
    public CodeVariableBuilder variableName(String variableName) {
        this.name = variableName;
        return this;
    }

    /**
     * Set the variable type for this var.
     * @param  variableType variable type
     * @return this for chaining
     */
    public CodeVariableBuilder variableType(String variableType) {
        this.type = variableType;
        return this;
    }

    /**
     * Adds a parameter to this variables construction params, must be called in desired order.
     * @param  param the parameter
     * @return this for chaining
     */
    public CodeVariableBuilder param(Object param) {
        params.add(new CodeParameter(param));
        return this;
    }

    /**
     * Adds a parameter to this variables construction params that is tnen quoted with double quotes, must be called
     * in desired order.
     * @param  param the parameter
     * @return this for chaining
     */
    public CodeVariableBuilder quoted(Object param) {
        params.add(new QuotedCodeParameter(param, '\"'));
        return this;
    }

    /**
     * This parameter will be based on the value of a property, if the property is empty or missing then the replacement
     * default will be used.
     * @param property the property to lookup
     * @param defVal the default if the above is blank
     * @return this for chaining.
     */
    public CodeVariableBuilder paramFromPropertyWithDefault(String property, String defVal) {
        params.add(new PropertyWithDefaultParameter(property, defVal));
        return this;
    }

    /**
     * describes a property in the form of a function with no params, ( ) will be added to the end
     * @param fn the function
     * @return this for chainging.
     */
    public CodeVariableBuilder fnparam(String fn) {
        params.add(new FunctionCodeParameter(fn));
        return this;
    }

    /**
     * describes a property in the form of a reference to an object. For C++ &amp; will be added to the start
     * @param ref the reference parameter
     * @return this for chaining calls.
     */
    public CodeVariableBuilder paramRef(String ref) {
        params.add(new ReferenceCodeParameter(ref));
        return this;
    }


    /**
     * Adds the root item variable that defines the top level item in the tree as a parameter.
     * @return this for chaining.
     */
    public CodeVariableBuilder paramMenuRoot() {
        params.add(new RootItemCodeParameter());
        return this;
    }

    /**
     * @return true if exported, otherwise false
     */
    public boolean isExported() {
        return definitionMode != DefinitionMode.VARIABLE_ONLY;
    }

    public boolean isVariableDefNeeded() {
        return definitionMode != DefinitionMode.EXPORT_ONLY;
    }

    public Set<HeaderDefinition> getHeaders() {
        return Collections.unmodifiableSet(headers);
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public boolean isByAssignment() {
        return byAssignment;
    }

    public boolean isProgmem() {
        return progmem;
    }

    public List<CodeParameter> getParams() {
        return params;
    }

    /**
     * Signals that this header file is needed and should use triangle brackets,
     * indicating that the file is not in the current directory structure.
     * @param headerName the name of the header
     * @param useQuotes indicates the header should use quotes if true
     * @return this object for chaining
     */
    public CodeVariableBuilder requiresHeader(String headerName, boolean useQuotes) {
        headers.add(new HeaderDefinition(headerName, useQuotes, HeaderDefinition.PRIORITY_NORMAL));
        return this;
    }

    /**
     * Signals that this header file is needed and should use triangle brackets,
     * indicating that the file is not in the current directory structure.
     * @param headerName the name of the header
     * @param useQuotes indicates the header should use quotes if true
     * @param priority indicates priority where 0 is highest
     * @see HeaderDefinition
     * @return this object for chaining
     */
    public CodeVariableBuilder requiresHeader(String headerName, boolean useQuotes, int priority) {
        headers.add(new HeaderDefinition(headerName, useQuotes, priority));
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodeVariableBuilder builder = (CodeVariableBuilder) o;
        return Objects.equals(type, builder.type) &&
                Objects.equals(name, builder.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name);
    }
}
