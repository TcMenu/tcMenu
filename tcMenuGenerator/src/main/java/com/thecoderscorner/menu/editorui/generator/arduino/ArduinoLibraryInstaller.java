/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.editorui.generator.util.LibraryStatus;
import com.thecoderscorner.menu.editorui.generator.util.VersionInfo;
import javafx.scene.control.TextInputDialog;

import java.io.FileReader;
import java.io.IOException;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * This class is responsible for dealing with the Arduino embedded libraries, it has methods to help
 * identifying which version of a library is installed, if it is up-to-date against the packaged copy,
 * and lastly also copying the library over from the package if needed.
 */
public class ArduinoLibraryInstaller {
    /**
     * storage path of custom library directory, for when a non standard structure is detected only
     */
    public static final String ARDUINO_CUSTOM_PATH = "ArduinoCustomPath";
    /**
     * the name of the library properties file
     */
    public static final String LIBRARY_PROPERTIES_NAME = "library.properties";
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());

    /**
     * the home directory
     */
    private final String homeDirectory;

    /**
     * the embedded directory that will be used as the source - default to embedded
     */
    private final String embeddedDirectory;

    /**
     * the directory located for arduino library storage
     */
    private String arduinoDirectory;


    /**
     * normally used by unit testing to override the path so that the code can be tested better
     *
     * @param homeDirectory     the path to hardwire for the arduino directory
     * @param embeddedDirectory the embedded source directory to override.
     */
    public ArduinoLibraryInstaller(String homeDirectory, String embeddedDirectory) {
        this.homeDirectory = homeDirectory;
        this.embeddedDirectory = embeddedDirectory;
    }

    /**
     * This is the standard, autodetecting version of construction, that should be used in nearly all cases.
     */
    public ArduinoLibraryInstaller() {
        this.arduinoDirectory = null;
        this.homeDirectory = System.getProperty("homeDirectoryOverride", System.getProperty("user.home"));
        this.embeddedDirectory = "embedded";
    }

    /**
     * Finds and then caches the arduino directory on the file system, looking in the usual places. Should
     * it fail, it will pop up a dialog asking for the directory.
     *
     * @return the arduino directory.
     */
    public Optional<Path> getArduinoDirectory() {
        if (arduinoDirectory != null) {
            return Optional.ofNullable(Paths.get(arduinoDirectory));
        }

        logger.log(Level.INFO, "Looking for Árduino directory");

        Path arduinoPath = Paths.get(homeDirectory, "Documents/Arduino");
        if (!Files.exists(arduinoPath)) {
            logger.log(Level.INFO, "Not found in " + arduinoPath);

            // try again in the onedrive folder, noticed it there on several windows machines
            arduinoPath = Paths.get(homeDirectory, "OneDrive/Documents/Arduino");
        }
        if (!Files.exists(arduinoPath)) {
            logger.log(Level.INFO, "Not found in " + arduinoPath);

            Optional<String> path = getArduinoPathWithDialog();
            if (path.isPresent()) {
                logger.log(Level.INFO, "Finally found in  " + path);
                arduinoPath = Paths.get(path.get());
            }
        }

        if (!Files.exists(arduinoPath)) return Optional.empty();

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
    private Optional<String> getArduinoPathWithDialog() {
        String savedPath = Preferences.userNodeForPackage(ArduinoLibraryInstaller.class)
                .get(ARDUINO_CUSTOM_PATH, homeDirectory);

        Path libsPath = Paths.get(savedPath, "libraries");
        if (Files.exists(libsPath)) return Optional.of(savedPath);

        TextInputDialog dialog = new TextInputDialog(savedPath);
        dialog.setTitle("Manually enter Arduino Path");
        dialog.setHeaderText("Please manually enter the full path to the Arduino folder");
        dialog.setContentText("Arduino Path");
        Optional<String> path = dialog.showAndWait();
        path.ifPresent((p) -> Preferences.userNodeForPackage(ArduinoLibraryInstaller.class).put(ARDUINO_CUSTOM_PATH, p));
        return path;
    }

    /**
     * Find the library installation of a specific named library using standard conventions
     *
     * @param libraryName the name of the library
     * @return an optional wrapped path to the library, may be empty if not found.
     */
    public Optional<Path> findLibraryInstall(String libraryName) {
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
                isLibraryUpToDate("LiquidCrystalIO")
        );
    }

    /**
     * Get the version of a library, either from the packaged version or currently installed depending
     * how it's called.
     * @param name the library name
     * @param inEmbeddedDir true for the packaged version, false for the installed version
     * @return the version of the library or 0.0.0 if it cannot be found.
     * @throws IOException in the event of an unexpected IO issue. Not finding the library is not an IO issue.
     */
    public VersionInfo getVersionOfLibrary(String name, boolean inEmbeddedDir) throws IOException {
        Path startPath;

        if(inEmbeddedDir) {
            startPath = Paths.get(embeddedDirectory, name);
        }
        else {
            Path ardDir = getArduinoDirectory().orElseThrow(IOException::new);
            startPath = ardDir.resolve("libraries").resolve(name);
        }
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

    /**
     * Check if the named library is the same version or new than the packaged version
     * @param name the library name
     * @return true if newer or the same, false otherwise
     */
    public boolean isLibraryUpToDate(String name) {
        Optional<Path> libInst = findLibraryInstall(name);
        if (!libInst.isPresent()) return false; // can we even find it on the system.

        try {
            VersionInfo srcVer = getVersionOfLibrary(name, true);
            VersionInfo dstVer = getVersionOfLibrary(name, false);
            return dstVer.isSameOrNewerThan(srcVer);
        } catch (IOException e) {
            return false; // Library is somehow not good. Certainly not the same!
        }
    }

    /**
     * Copies the library from the packaged version into the installation directory.
     * @param libraryName the library to copy
     * @throws IOException if the copy could not complete.
     */
    public void copyLibraryFromPackage(String libraryName) throws IOException {
        Path ardDir = getArduinoDirectory().orElseThrow(IOException::new);
        Path source = Paths.get(embeddedDirectory).resolve(libraryName);

        Path dest = ardDir.resolve("libraries/" + libraryName);
        if(!Files.exists(dest)) {
            Files.createDirectory(dest);
        }

        Path gitRepoDir = dest.resolve(".git");
        if(Files.exists(gitRepoDir)) {
            throw new IOException("Git repository inside " + libraryName+ "! Not proceeding to update path : " + dest);
        }

        copyLibraryRecursive(source, dest);
    }

    /**
     * Recursive copier for the above copy method. It calls recursively for subdirectories to ensure a full copy
     * @param input the directory to copy from
     * @param output the directory to copy to
     * @throws IOException if the copy could not complete.
     */
    private void copyLibraryRecursive(Path input, Path output) throws IOException {
        for (Path dirItem : Files.list(input).collect(Collectors.toList())) {
            Path outputName = output.resolve(dirItem.getFileName());
            if (Files.isDirectory(dirItem)) {
                if(!Files.exists(outputName)) Files.createDirectory(outputName);
                copyLibraryRecursive(dirItem, outputName);
            } else {
                Files.copy(dirItem, outputName, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}