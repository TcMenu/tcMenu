package com.thecoderscorner.menu.editorui.generator.plugin.display;

import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginConfig;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.plugin.JavaPluginGroup;
import com.thecoderscorner.menu.editorui.generator.plugin.JavaPluginItem;
import com.thecoderscorner.menu.persist.VersionInfo;

import java.util.ArrayList;

public class InbuiltDisplayInputPlugins implements JavaPluginGroup {
    private final CodePluginConfig config;

    public InbuiltDisplayInputPlugins(CodePluginManager manager, VersionInfo tcMenuVersion) {
        config = new CodePluginConfig(
                "inbuilt-display", "Inbuilt display and input", tcMenuVersion.toString(), new ArrayList<>());
        config.setLicense("Apache 2.0");
        config.setLicenseUrl("https://www.apache.org/licenses/LICENSE-2.0");
        config.setVendor("The CodersCorner");
        config.setVendorUrl("https://www.coderscorner.com");

        addPlugin(new GxEPD2SimplePluginImpl(this, manager));
        addPlugin(new DfRobotDisplayPluginImpl(this, manager));
    }

    public void addPlugin(JavaPluginItem plugin) {
        config.getPlugins().add(plugin.getPlugin());
    }

    @Override
    public CodePluginConfig getConfig() {
        return config;
    }
}
