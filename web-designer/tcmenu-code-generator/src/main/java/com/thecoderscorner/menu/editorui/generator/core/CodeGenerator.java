/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;

import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * An implementation of CodeGenerator interface that is used by the code generator UI once all options
 * are chosen, in order to do the actual code generation for the desired platform. Within the UI
 * there is a factory that creates CodeGenerator objects keyed by platform.
 *
 * An instance should be created for every generate action and is not thread safe.
 */
public interface CodeGenerator {
    /**
     * Called in order to perform the actual conversion, given the location, a list of code generators
     * and the menu tree to convert.
     * @param directory the place to store the output files.
     * @param generators the list of generators to use, assumed to be in priority order
     * @param menuTree the tree of menu items to be represented
     * @param prevPluginFiles the previous plugin files as a list of strings
     * @param options the options from the current project
     * @param handler the locale handler containing translations (could be the no-op handler)
     * @return true if conversion successful
     */
    boolean startConversion(Path directory, List<CodePluginItem> generators, MenuTree menuTree,
                            List<String> prevPluginFiles, CodeGeneratorOptions options, LocaleMappingHandler handler,
                            List<CreatorProperty> allProps);

    /**
     * Gets the sketch file adjuster that can create and maintain sketch files
     * @return the sketch file adjuster for the build platform.
     */
    SketchFileAdjuster getSketchFileAdjuster();
}
