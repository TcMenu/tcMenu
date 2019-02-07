package com.thecoderscorner.menu.editorui.generator.plugin;

import java.util.List;

public class CodePluginConfig {
    private String moduleName;
    private String name;
    private String version;
    private List<CodePluginItem> plugins;

    public String getModuleName() {
        return moduleName;
    }

    public String getName() {
        return name;
    }


    public String getVersion() {
        return version;
    }

    public List<CodePluginItem> getPlugins() {
        return plugins;
    }

    @Override
    public String toString() {
        return "CodePluginConfig{" +
                "moduleName='" + moduleName + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", plugins=" + plugins +
                '}';
    }
}
