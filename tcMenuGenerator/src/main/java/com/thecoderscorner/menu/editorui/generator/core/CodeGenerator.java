/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;

import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * An implementation of CodeGenerator interface is used by the code generator UI once all options
 * are chosen, in order to do the actual code generation for the desired platform. Within the UI
 * there is a map of CodeGenerator objects keyed by platform.
 */
public interface CodeGenerator {
    /**
     * Called in order to perform the actual conversion, given the location, a list of code generators
     * and the menu tree to convert.
     * @param directory the place to store the output files.
     * @param generators the list of generators to use, assumed to be in priority order
     * @param menuTree the tree of menu items to be represented
     * @return true if conversion successful
     */
    boolean startConversion(Path directory, List<CodePluginItem> generators, MenuTree menuTree,
                            List<String> previousPluginFiles, CodeGeneratorOptions options);

    /**
     * Called before the conversion starts to set the logger to use for the rest of the conversion.
     * @param logLine the logger consumer
     */
    void setLoggerFunction(BiConsumer<System.Logger.Level, String> logLine);

    /**
     * Gets the sketch file adjuster that can create and maintain sketch files
     * @return the sketch file adjuster for the build platform.
     */
    SketchFileAdjuster getSketchFileAdjuster();
}
