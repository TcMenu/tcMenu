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
 * Only use this class when {@link AbstractCodeCreator} doesnt work for you. Purposely not documented,
 * see the abstract class linked above instead.
 */
public interface EmbeddedCodeCreator {
    /**
     * Called at the very beginning of conversion to allow the creator to prepare any late bound fields and variables.
     * @param root the first menu item in the tree
     */
    void initialise(String root);

    List<HeaderDefinition> getIncludes();
    List<CodeVariableBuilder> getVariables();
    List<FunctionCallBuilder> getFunctionCalls();
    List<String> getRequiredFiles();

    /**
     * Each code creator has a list of properties, these properties are provided by the properties method
     * which should be overridden to provide this list. The properties can be referred to during code
     * conversion, and they are set by the user during code creation.
     *
     * @return a list of properties, these should not change during object lifetime
     */
    List<CreatorProperty> properties();
}
