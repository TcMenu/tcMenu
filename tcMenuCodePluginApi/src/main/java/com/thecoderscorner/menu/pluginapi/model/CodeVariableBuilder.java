package com.thecoderscorner.menu.pluginapi.model;

import java.util.*;

import static com.thecoderscorner.menu.pluginapi.AbstractCodeCreator.ROOT_ITEM_REPLACEMENT;

/**
 * Provides a builder pattern for describing variables that need to be created in order for this plugin to work.
 * This allows for describing each needed varaible in the constructor and then allowing the AbstractCodeCreator
 * to do the rest.
 */
public class CodeVariableBuilder {
    private boolean export = false;
    private String type;
    private String name;
    private Collection<String> params = new ArrayList<>();
    private Set<HeaderDefinition> headers = new HashSet<>();

    /**
     * indicates that this variable needs exporting for use in other files.
     * @return this for chaining
     */
    public CodeVariableBuilder exportNeeded() {
        this.export = true;
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
        params.add(param == null ? "NULL" : param.toString());
        return this;
    }

    /**
     * Adds a parameter to this variables construction params that is tnen quoted with double quotes, must be called
     * in desired order.
     * @param  param the parameter
     * @return this for chaining
     */
    public CodeVariableBuilder quoted(Object param) {
        if(param == null) {
            params.add("NULL");
        }
        else {
            params.add("\"" + param.toString() + "\"");
        }
        return this;
    }

    /**
     * gets the variable code from this builder.
     * @return the variable code
     */
    public String getVariable() {
        return type + " " + name + "(" + String.join(", ", params) + ");";
    }

    /**
     * gets the export code from this variable
     * @return the export code.
     */
    public String getExport() {
        return "extern " + type + " " + name + ";";
    }

    /**
     * @return true if exported, otherwise false
     */
    public boolean isExported() {
        return export;
    }

    public Set<HeaderDefinition> getHeaders() {
        return Collections.unmodifiableSet(headers);
    }

    /**
     * Signals that this header file is needed and should use triangle brackets,
     * indicating that the file is not in the current directory structure.
     * @param headerName the name of the header
     * @return this object for chaining
     */
    public CodeVariableBuilder requiresHeader(String headerName, boolean useQuotes) {
        headers.add(new HeaderDefinition(headerName, useQuotes));
        return this;
    }

    public String getNameOnly() {
        return name;
    }
}
