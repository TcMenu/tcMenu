/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.pluginapi.EmbeddedCodeCreator;
import com.thecoderscorner.menu.pluginapi.EmbeddedPlatform;
import com.thecoderscorner.menu.pluginapi.SubSystem;
import javafx.scene.image.Image;

import java.util.List;
import java.util.Optional;

public interface CodePluginManager {
    void loadPlugins(String sourceDir) throws Exception;

    List<CodePluginConfig> getLoadedPlugins();

    Optional<Image> getImageForName(String imageName);

    public Optional<EmbeddedCodeCreator> makeCreator(CodePluginItem item);

    List<CodePluginItem> getPluginsThatMatch(EmbeddedPlatform platform, SubSystem subSystem);

    Optional<CodePluginConfig> getPluginConfigForItem(CodePluginItem item);
}
