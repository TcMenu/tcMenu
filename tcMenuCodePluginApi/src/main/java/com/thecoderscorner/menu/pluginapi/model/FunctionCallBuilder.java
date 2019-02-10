/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi.model;

import com.thecoderscorner.menu.pluginapi.model.parameter.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Builder pattern way of building function calls that will be made during setup, to avoid writing code in strings.
 */
public class FunctionCallBuilder {
    private Optional<String> objectName = Optional.empty();
    private String functionName;
    private Collection<CodeParameter> params = new ArrayList<>();
    private Set<HeaderDefinition> headers = new HashSet<>();

    /**
     * If this function call is on an object then provide the object definition.
     * @param variable the object to call on
     * @return itself for chaining
     */
    public FunctionCallBuilder objectName(CodeVariableBuilder variable) {
        objectName = Optional.ofNullable(variable.getNameOnly());
        return this;
    }

    /**
     * If this function call is on an object then provide the object name.
     * @param variable the object to call on
     * @return itself for chaining
     */
    public FunctionCallBuilder objectName(String variable) {
        objectName = Optional.ofNullable(variable);
        return this;
    }

    /**
     * The function to call
     * @param name the name of the function
     * @return itself for chaining
     */
    public FunctionCallBuilder functionName(String name) {
        functionName = name;
        return this;
    }

    /**
     * Add a requirement that a header file needs to be included for this to work
     * @param headerName the name of the header including .h
     * @param useQuotes true for quotes, false for triangle brackets.
     * @return
     */
    public FunctionCallBuilder requiresHeader(String headerName, boolean useQuotes) {
        headers.add(new HeaderDefinition(headerName, useQuotes));
        return this;
    }

    /**
     * Provides a parameter to the function, must be called in order.
     * @param param the parameter
     * @return itself for chaining
     */
    public FunctionCallBuilder param(Object param) {
        params.add(new CodeParameter(param));
        return this;
    }

    public FunctionCallBuilder fnparam(String fn) {
        params.add(new FunctionCodeParameter(fn));
        return this;
    }

    /**
     * Provides a parameter to the function that is quoted in double quotes, must be called in order.
     * @param param the parameter
     * @return itself for chaining
     */
    public FunctionCallBuilder quoted(Object param) {
        params.add(new QuotedCodeParameter(param, '\"'));
        return this;
    }

    public FunctionCallBuilder paramFromPropertyWithDefault(String property, String defVal) {
        params.add(new PropertyWithDefaultParameter(property, defVal));
        return this;
    }

    /**
     * Adds the root item variable that defines the top level item in the tree as a parameter.
     * @return this for chaining.
     */
    public FunctionCallBuilder paramMenuRoot() {
        params.add(new RootItemCodeParameter());
        return this;
    }


    /**
     * @return the code that is built from this function builder.
     */
    public String getFunctionCode(CodeConversionContext context) {
        String fn = "    ";
        if(objectName.isPresent()) {
            fn += objectName.get() + ".";
        }
        fn += functionName + "(";
        var parameters = params.stream().map(p -> p.getParameterValue(context)).collect(Collectors.joining(", "));
        fn += parameters;
        fn += ");";
        return fn;
    }

    public Set<HeaderDefinition> getHeaders() {
        return Collections.unmodifiableSet(headers);
    }
}
