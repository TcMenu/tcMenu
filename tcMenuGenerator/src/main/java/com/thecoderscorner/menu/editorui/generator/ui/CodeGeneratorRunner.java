/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.editorui.generator.parameters.CodeGeneratorCapable;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import javafx.stage.Stage;

import java.util.List;

public interface CodeGeneratorRunner {
    void startCodeGeneration(Stage stage,
                             EmbeddedPlatform platform,
                             String  path,
                             List<CodePluginItem> creators,
                             List<String> previousPlugins,
                             boolean modal);
}
