package com.thecoderscorner.menu.editorui.generator.plugin;

import java.util.List;

public class CodePluginConfig {
    private String moduleName;
    private String name;
    private String version;
    private List<CodePluginItem> plugins;

    public CodePluginConfig() {
        // for reflection / serialisation
    }

    public CodePluginConfig(String moduleName, String name, String version, List<CodePluginItem> plugins) {
        this.moduleName = moduleName;
        this.name = name;
        this.version = version;
        this.plugins = plugins;
    }

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
