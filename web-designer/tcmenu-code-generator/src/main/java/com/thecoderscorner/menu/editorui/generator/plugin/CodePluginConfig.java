/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.plugin;

import java.nio.file.Path;
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
    private Path path;

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

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getVendorUrl() {
        return vendorUrl;
    }

    public void setVendorUrl(String vendorUrl) {
        this.vendorUrl = vendorUrl;
    }

    public List<CodePluginItem> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<CodePluginItem> plugins) {
        this.plugins = plugins;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
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
