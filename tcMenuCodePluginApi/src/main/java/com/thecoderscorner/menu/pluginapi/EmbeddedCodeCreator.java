/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.pluginapi;

import java.util.List;

public interface EmbeddedCodeCreator {
    /**
     * Called at the very beginning of conversion to allow the creator to prepare any late bound fields and variables.
     * @param root the first menu item in the tree
     */
    void initialise(String root);

    List<String> getIncludes();
    String getGlobalVariables();
    String getExportDefinitions();
    String getSetupCode(String rootItem);
    List<String> getRequiredFiles();
    List<CreatorProperty> properties();
}
