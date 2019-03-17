/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.plugin;

import java.util.List;

public class CodePluginConfig {
    private String moduleName;
    private String name;
    private String version;
    private String license;
    private String licenseUrl;
    private String vendor;
    private String vendorUrl;
    private List<CodePluginItem> plugins;

    public CodePluginConfig() {
        // for reflection / serialisation
    }

    public CodePluginConfig(String moduleName, String name, String version, List<CodePluginItem> plugins) {
        this.moduleName = moduleName;
        this.name = name;
        this.version = version;
        this.plugins = plugins;
        this.vendor = "Not set";
        this.vendorUrl = "https://www.thecoderscorner.com";
        this.license= "Unspecified";
        this.licenseUrl = "https://";
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

    public String getLicense() {
        return license;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public String getVendor() {
        return vendor;
    }

    public String getVendorUrl() {
        return vendorUrl;
    }

    @Override
    public String toString() {
        return "CodePluginConfig{" +
                "moduleName='" + moduleName + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", license='" + license + '\'' +
                ", licenseUrl='" + licenseUrl + '\'' +
                ", plugins=" + plugins +
                '}';
    }
}
