/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi.model;

import java.util.List;

/**
 * This interface defines a method to map structures into code. This provides plugins with a means of defining variables,
 * functions, exports etc for a specific language.
 * @see CppCode
 */
public interface CodeVariableExtractor {
    String mapFunctions(List<FunctionCallBuilder> functions);
    String mapVariables(List<CodeVariableBuilder> variables);
    String mapExports(List<CodeVariableBuilder> variables);
    String mapDefines();
    String mapIncludes(List<HeaderDefinition> includes);
    String mapStructSource(BuildStructInitializer s);
    String mapStructHeader(BuildStructInitializer s);
}
