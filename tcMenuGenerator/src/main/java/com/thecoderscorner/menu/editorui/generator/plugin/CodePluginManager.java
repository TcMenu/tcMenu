package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.pluginapi.EmbeddedPlatform;
import com.thecoderscorner.menu.pluginapi.SubSystem;
import javafx.scene.image.Image;

import java.util.List;
import java.util.Optional;

public interface CodePluginManager {
    void loadPlugins(String sourceDir) throws Exception;
    List<CodePluginConfig> getLoadedPlugins();
    Optional<Image> getImageForName(String imageName);
    List<CodePluginItem> getPluginsThatMatch(EmbeddedPlatform platform, SubSystem subSystem);
}
