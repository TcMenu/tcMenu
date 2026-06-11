/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.editorui.generator.AppVersionDetector;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.persist.VersionInfo;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType.*;

/**
 * This class is responsible for dealing with the Arduino embedded libraries, it has methods to help
 * identifying which version of a library is installed, if it is up-to-date against the packaged copy,
 * and lastly also copying the library over from the package if needed.
 */
public class ArduinoLibraryInstaller {

    public enum InstallationType { CURRENT_LIB, CURRENT_PLUGIN, AVAILABLE_APP, CURRENT_APP }

    /**
     * the name of the library properties file
     */
    public static final String LIBRARY_PROPERTIES_NAME = "library.properties";

    private final AppVersionDetector versionDetector;
    private final CodePluginManager pluginManager;
    private final ConfigurationStorage configStore;

    /**
     * normally used by unit testing to override the path so that the code can be tested better
     *
     */
    public ArduinoLibraryInstaller(AppVersionDetector detector, CodePluginManager manager, ConfigurationStorage configStore) {
        this.versionDetector = detector;
        this.pluginManager = manager;
        this.configStore = configStore;
    }

    /**
     * Finds and then caches the arduino directory on the file system, looking in the usual places. Should
     * it fail, it will pop up a dialog asking for the directory.
     *
     * @return the arduino directory.
     */
    public Optional<Path> getArduinoDirectory() {
        if(!configStore.isUsingArduinoIDE() || configStore.getArduinoOverrideDirectory().isEmpty()) return Optional.empty();
        return Optional.of(Paths.get(configStore.getArduinoOverrideDirectory().get()));
    }

    /**
     * Find the library installation of a specific named library using standard conventions
     *
     * @param libraryName the name of the library
     * @return an optional wrapped path to the library, may be empty if not found.
     */
    public Optional<Path> findLibraryInstall(String libraryName) {
        var globalLibs = configStore.getArduinoLibrariesOverrideDirectory();
        if(!configStore.isUsingArduinoIDE() || globalLibs.isEmpty()) {
            return Optional.empty();
        }

        Path libsDir = Paths.get(globalLibs.get(), libraryName);
        if (Files.exists(libsDir)) {
            return Optional.of(libsDir);
        }
        else return Optional.empty();
    }

    /**
     * Get the version of a library, either from the packaged version or currently installed depending
     * how it's called.
     * @param name the library name
     * @param installationType what we are looking for, eg, installed or available, plugin or lib.
     * @return the version of the library or 0.0.0 if it cannot be found.
     * @throws IOException in the event of an unexpected IO issue. Not finding the library is not an IO issue.
     */
    public VersionInfo getVersionOfLibrary(String name, InstallationType installationType) throws IOException {
        Path startPath;

        if(installationType == AVAILABLE_APP) {
            var relData = versionDetector.acquireVersion();
            return relData.version();
        }
        else if(installationType == CURRENT_APP) {
            var version = configStore.getVersion();
            return new VersionInfo(version);
        }
        else if(installationType == CURRENT_LIB){
            Optional<String> libsDir = configStore.getArduinoLibrariesOverrideDirectory();
            if(!configStore.isUsingArduinoIDE() || libsDir.isEmpty()) {
                return VersionInfo.ERROR_VERSION;
            }

            startPath = Paths.get(libsDir.get(), name);
            Path libProps = startPath.resolve(LIBRARY_PROPERTIES_NAME);

            if(!Files.exists(libProps)) {
                return new VersionInfo("0.0.0");
            }

            Properties propsSrc = new Properties();
            try(FileReader reader = new FileReader(libProps.toFile())) {
                propsSrc.load(reader);
            }
            return new VersionInfo(propsSrc.getProperty("version", "0.0.0"));
        }
        else {
            return pluginManager.getLoadedPlugins().stream()
                    .filter(pl -> pl.getModuleName().equals(name))
                    .map(pl -> new VersionInfo(pl.getVersion()))
                    .findFirst().orElse(new VersionInfo("0.0.0"));
        }
    }
}