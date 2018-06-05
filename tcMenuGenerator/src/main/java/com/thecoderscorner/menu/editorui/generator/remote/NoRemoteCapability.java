/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.remote;

import com.thecoderscorner.menu.editorui.generator.EmbeddedCodeCreator;
import com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty;

import java.util.Collections;
import java.util.List;

public class NoRemoteCapability implements EmbeddedCodeCreator {
    @Override
    public List<String> getIncludes() {
        return Collections.singletonList("#include \"RemoteConnector.h\"");
    }

    @Override
    public String getGlobalVariables() {
        return "";
    }

    @Override
    public String getExportDefinitions() {
        return "";
    }

    @Override
    public String getSetupCode(String rootItem) {
        return "";
    }

    @Override
    public List<CreatorProperty> properties() {
        return Collections.emptyList();
    }
}
