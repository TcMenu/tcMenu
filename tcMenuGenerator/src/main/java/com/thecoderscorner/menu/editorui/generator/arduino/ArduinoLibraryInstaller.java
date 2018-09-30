/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import javafx.scene.control.TextInputDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.prefs.Preferences;

public class ArduinoLibraryInstaller {
    public static final String ARDUINO_CUSTOM_PATH = "ArduinoCustomPath";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static Optional<Path> getArduinoDirectory() {
        String userDir = System.getProperty("user.home");

        Path arduinoPath = Paths.get(userDir, "Documents/Arduino");
        if (!Files.exists(arduinoPath)) {
            // try again in the onedrive folder, noticed it there on several windows machines
            arduinoPath = Paths.get(userDir, "OneDrive/Documents/Arduino");
        }
        if(!Files.exists(arduinoPath)) {
            Optional<String> path = getArduinoPathWithDialog();
            if(path.isPresent()) {
                arduinoPath = Paths.get(path.get());
            }
        }

        if(!Files.exists(arduinoPath)) return Optional.empty();

        Path libsPath = arduinoPath.resolve("libraries");
        if (!Files.exists(libsPath)) return Optional.empty();

        return Optional.of(arduinoPath);
    }

    public static Optional<Path> findTcMenuInstall() {
        return getArduinoDirectory().map(path -> {
            Path libsDir = path.resolve("libraries");
            Path tcMenuDir = libsDir.resolve("tcMenu");
            if (Files.exists(tcMenuDir)) {
                return tcMenuDir;
            }
            return null;
        });
    }

    private static Optional<String> getArduinoPathWithDialog() {
        String savedPath = Preferences.userNodeForPackage(ArduinoLibraryInstaller.class)
                .get(ARDUINO_CUSTOM_PATH, System.getProperty("user.home"));

        Path libsPath = Paths.get(savedPath, "libraries");
        if (Files.exists(libsPath)) return Optional.of(savedPath);

        TextInputDialog dialog = new TextInputDialog(savedPath);
        dialog.setTitle("Manually enter Arduino Path");
        dialog.setHeaderText("Please manually enter the Arduino folder");
        dialog.setContentText("Arduino Path");
        Optional<String> path = dialog.showAndWait();
        path.ifPresent((p)->Preferences.userNodeForPackage(ArduinoLibraryInstaller.class).put(ARDUINO_CUSTOM_PATH, p));
        return path;
    }
}
