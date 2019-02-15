/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.pluginapi.EmbeddedCodeCreator;
import com.thecoderscorner.menu.pluginapi.EmbeddedPlatform;
import javafx.stage.Stage;

import java.util.List;

public interface CodeGeneratorRunner {
    void startCodeGeneration(Stage stage, EmbeddedPlatform platform, String  path,
                             List<EmbeddedCodeCreator> creators, boolean modal);
}
