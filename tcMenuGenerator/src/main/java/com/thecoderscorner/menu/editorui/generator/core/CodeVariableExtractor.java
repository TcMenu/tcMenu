/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.editorui.generator.plugin.CodeVariable;
import com.thecoderscorner.menu.editorui.generator.plugin.FunctionDefinition;

import java.util.List;

/**
 * This interface defines a method to map structures into code. This provides plugins with a means of defining variables,
 * functions, exports etc for a specific language. For example the {@link CodeVariableCppExtractor} is the C++ extractor
 * that can convert the below structures into C++ code.
 */
public interface CodeVariableExtractor {
    String mapFunctions(List<FunctionDefinition> functions);
    String mapVariables(List<CodeVariable> variables);
    String mapExports(List<CodeVariable> variables);
    String mapDefines();
    String mapIncludes(List<HeaderDefinition> includes);
    String mapStructSource(BuildStructInitializer s);
    String mapStructHeader(BuildStructInitializer s);
}
