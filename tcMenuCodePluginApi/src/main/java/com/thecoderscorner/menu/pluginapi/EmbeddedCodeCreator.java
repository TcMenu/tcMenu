/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi;

import com.thecoderscorner.menu.pluginapi.model.CodeVariableBuilder;
import com.thecoderscorner.menu.pluginapi.model.FunctionCallBuilder;
import com.thecoderscorner.menu.pluginapi.model.HeaderDefinition;

import java.util.List;

/**
 * Only use this class when {@link AbstractCodeCreator} doesnt work for you. It is the interface underneath
 * the aforementioned class.
 */
public interface EmbeddedCodeCreator {
    /**
     * Called at the very beginning of conversion to allow the creator to prepare any late bound fields and variables.
     * @param root the first menu item in the tree
     */
    void initialise(String root);

    /**
     * @return a list of header definitions that will be turned into items to include.
     */
    List<HeaderDefinition> getIncludes();

    /**
     * @return a list of variables that will be added to the sketch as globals
     */
    List<CodeVariableBuilder> getVariables();

    /**
     * @return a list of function calls that need to be made during setup
     */
    List<FunctionCallBuilder> getFunctionCalls();

    /**
     * @return a list of file dependencies or plugins needed for this creator to function
     */
    List<PluginFileDependency> getRequiredFiles();

    /**
     * Each code creator has a list of properties, these properties are provided by the properties method
     * which should be overridden to provide this list. The properties can be referred to during code
     * conversion, and they are set by the user during code creation.
     *
     * @return a list of properties, these should not change during object lifetime
     */
    List<CreatorProperty> properties();
}
