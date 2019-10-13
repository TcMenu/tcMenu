/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi;

import com.thecoderscorner.menu.domain.state.MenuTree;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

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
     * @return
     */
    boolean startConversion(Path directory, List<EmbeddedCodeCreator> generators, MenuTree menuTree,
                            NameAndKey nameKey, List<String> previousPluginFiles);

    /**
     * Called before the conversion starts to set the logger to use for the rest of the conversion.
     * @param logLine the logger consumer
     */
    void setLoggerFunction(Consumer<String> logLine);
}
