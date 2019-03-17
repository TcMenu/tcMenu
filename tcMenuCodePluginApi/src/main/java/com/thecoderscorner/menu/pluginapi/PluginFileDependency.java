/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi;

import java.util.Map;
import java.util.Objects;

/**
 * Defines a file that needs to be copied into the users sketch / project directory in order to provide
 * additional requested capabilities. There are several options, if the file is within the plugin or packaged
 * in the tcMenu library itself, and also if textual replacements are needed for it to work. The usual way they
 * are provided is to ensure the basic script is working for one case, and then a series of replacements to make
 * it work for another sketch.
 */
public class PluginFileDependency {
    public enum PackagingType { WITH_PLUGIN, WITHIN_TCMENU }
    private final String fileName;
    private PackagingType packaging;
    private final Map<String, String> replacements;

    /**
     * Create a new plugin file dependency
     * @param fileName the name of the file to copy (either relative to tcMenu, or relative to root in plugin
     * @param packaging if the plugin should be located in the plugin directory, or within tcMenu
     * @param replacements a map of replacements to be applied.
     */
    public PluginFileDependency(String fileName, PackagingType packaging, Map<String, String> replacements) {
        this.fileName = fileName;
        this.packaging = packaging;
        this.replacements = replacements;
    }

    /**
     * @return the name of the file
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return if packaged with the plugin relative to root, or within tcMenu
     */
    public PackagingType getPackaging() {
        return packaging;
    }

    /**
     * @return a map of replacements where the key is the item to find and the value is the replacement.
     */
    public Map<String, String> getReplacements() {
        return replacements;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginFileDependency that = (PluginFileDependency) o;
        return getPackaging() == that.getPackaging() &&
                Objects.equals(getFileName(), that.getFileName()) &&
                Objects.equals(getReplacements(), that.getReplacements());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFileName(), getPackaging(), getReplacements());
    }

    @Override
    public String toString() {
        return "PluginFileDependency{" +
                "fileName='" + fileName + '\'' +
                ", packaging=" + packaging +
                ", replacements=" + replacements +
                '}';
    }

    /**
     * Creates an instance of the class that indicates the file is within tcMenu itself. Don't use for
     * new plugins, prefer to package arduino code in the plugin or a new library.
     *
     * @param file the file name
     * @return a new instance
     */
    public static PluginFileDependency fileInTcMenu(String file) {
        return new PluginFileDependency(file, PackagingType.WITHIN_TCMENU, Map.of());
    }
}
