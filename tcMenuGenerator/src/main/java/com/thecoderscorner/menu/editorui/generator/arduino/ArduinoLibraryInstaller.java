/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.editorui.controller.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.generator.LibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.util.LibraryStatus;
import com.thecoderscorner.menu.editorui.generator.util.VersionInfo;
import javafx.scene.control.TextInputDialog;

import javax.swing.filechooser.FileSystemView;
import java.io.FileReader;
import java.io.IOException;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

/**
 * This class is responsible for dealing with the Arduino embedded libraries, it has methods to help
 * identifying which version of a library is installed, if it is up-to-date against the packaged copy,
 * and lastly also copying the library over from the package if needed.
 */
public class ArduinoLibraryInstaller {

    public enum InstallationType { AVAILABLE_LIB, CURRENT_LIB, AVAILABLE_PLUGIN, CURRENT_PLUGIN }
    /**
     * the name of the library properties file
     */
    public static final String LIBRARY_PROPERTIES_NAME = "library.properties";
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());

    private final LibraryVersionDetector versionDetector;
    private final CodePluginManager pluginManager;
    private final ConfigurationStorage configStore;
    private final boolean useOverride;

    /**
     * the home directory
     */
    private final String homeDirectory;

    /**
     * the directory located for arduino library storage
     */
    private String arduinoDirectory;


    /**
     * normally used by unit testing to override the path so that the code can be tested better
     *
     * @param homeDirectory     the path to hardwire for the arduino directory
     */
    public ArduinoLibraryInstaller(String homeDirectory, LibraryVersionDetector detector, CodePluginManager manager, ConfigurationStorage configStore, boolean useOverride) {
        this.homeDirectory = homeDirectory;
        this.versionDetector = detector;
        this.pluginManager = manager;
        this.configStore = configStore;
        this.useOverride = useOverride;
    }

    /**
     * Finds and then caches the arduino directory on the file system, looking in the usual places. Should
     * it fail, it will pop up a dialog asking for the directory.
     *
     * @return the arduino directory.
     */
    public Optional<Path> getArduinoDirectory() {
        if(!configStore.isUsingArduinoIDE()) return Optional.empty();

        var override = configStore.getArduinoOverrideDirectory();
        if(override.isPresent() && Files.exists(Path.of(override.get())) && useOverride) {
            return Optional.of(Path.of(override.get()));
        }

        if (arduinoDirectory != null) {
            return Optional.of(Paths.get(arduinoDirectory));
        }

        logger.log(Level.INFO, "Looking for Árduino directory");

        Path arduinoPath = Paths.get(homeDirectory, "Documents/Arduino");
        if (!Files.exists(arduinoPath)) {
            logger.log(Level.INFO, "Not found in " + arduinoPath);
            // On Linux, Arduino directory defaults to home.
            arduinoPath = Paths.get(homeDirectory, "Arduino");
        }
        if (!Files.exists(arduinoPath)) {
            logger.log(Level.INFO, "Not found in " + arduinoPath);
            // try again in the onedrive folder, noticed it there on several windows machines
            arduinoPath = Paths.get(homeDirectory, "OneDrive/Documents/Arduino");
        }
        if (!Files.exists(arduinoPath)) {
            logger.log(Level.INFO, "Not found in " + arduinoPath);
        }

        if (!Files.exists(arduinoPath)) return Optional.empty();
        logger.log(Level.INFO, "Arduino directory found at " + arduinoPath);

        logger.log(Level.INFO, "looking for libraries");

        // there was an arduino install without a libraries directory - add it.
        Path libsPath = arduinoPath.resolve("libraries");
        if (!Files.exists(libsPath)) {
            try {
                logger.log(Level.INFO, "Creating libraries folder");
                Files.createDirectory(libsPath);
            } catch (IOException e) {
                return Optional.empty();
            }
        }

        arduinoDirectory = arduinoPath.toString();

        return Optional.of(arduinoPath);
    }

    /**
     * This method will be called internally by the above method when a non standard layout is detected.
     *
     * @return the arduino path wrapped in an optional, or nothing if cancel is pressed.
     */
    public void manuallySetArduinoPath() {
        var dir = getArduinoDirectory().orElseGet(() -> Path.of(FileSystemView.getFileSystemView().getDefaultDirectory().getPath()));
        TextInputDialog dialog = new TextInputDialog(dir.toString());
        dialog.setTitle("Manually enter Arduino Path");
        dialog.setHeaderText("Please manually enter the full path to the Arduino folder, leave blank to clear the path override");
        dialog.setContentText("Arduino Path");
        Optional<String> path = dialog.showAndWait();
        path.ifPresent(configStore::setArduinoOverrideDirectory);
        arduinoDirectory = null;
    }

    /**
     * Find the library installation of a specific named library using standard conventions
     *
     * @param libraryName the name of the library
     * @return an optional wrapped path to the library, may be empty if not found.
     */
    public Optional<Path> findLibraryInstall(String libraryName) {
        if(!configStore.isUsingArduinoIDE()) return Optional.empty();
        return getArduinoDirectory().map(path -> {
            Path libsDir = path.resolve("libraries");
            Path tcMenuDir = libsDir.resolve(libraryName);
            if (Files.exists(tcMenuDir)) {
                return tcMenuDir;
            }
            return null;
        });
    }

    /**
     * Checks if all the core TcMenu libraries are up to date on the system, IE same or newer version as those
     * shipped with the UI Generator.
     *
     * @return true if the libraries are the same or newer.
     */
    public LibraryStatus statusOfAllLibraries() {
        return new LibraryStatus(
                isLibraryUpToDate("tcMenu"),
                isLibraryUpToDate("IoAbstraction"),
                isLibraryUpToDate("LiquidCrystalIO"),
                isLibraryUpToDate("TaskManagerIO")
        );
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

        if(installationType == InstallationType.AVAILABLE_LIB || installationType == InstallationType.AVAILABLE_PLUGIN) {
            var versions = versionDetector.acquireVersions();
            return versions.get(name + ((installationType == InstallationType.AVAILABLE_LIB) ? "/Library" : "/Plugin"));
        }
        else if(installationType == InstallationType.CURRENT_LIB){
            if(!configStore.isUsingArduinoIDE() || getArduinoDirectory().isEmpty()) return VersionInfo.ERROR_VERSION;
            startPath = getArduinoDirectory().get().resolve("libraries").resolve(name);
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

    /**
     * Check if the named library is the same version or new than the packaged version
     * @param name the library name
     * @return true if newer or the same, false otherwise
     */
    public boolean isLibraryUpToDate(String name) {
        if(!configStore.isUsingArduinoIDE()) return true;

        Optional<Path> libInst = findLibraryInstall(name);
        if (libInst.isEmpty()) return false; // can we even find it on the system.

        try {
            VersionInfo srcVer = getVersionOfLibrary(name, InstallationType.AVAILABLE_LIB);
            VersionInfo dstVer = getVersionOfLibrary(name, InstallationType.CURRENT_LIB);
            if(srcVer == null || dstVer.equals(VersionInfo.ERROR_VERSION)) return true;
            return dstVer.isSameOrNewerThan(srcVer);
        } catch (IOException e) {
            return false; // Library is somehow not good. Certainly not the same!
        }
    }
}