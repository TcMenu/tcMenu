/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class ArduinoLibraryInstaller {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void tryToInstallLibrary() {
        findTcMenuInstall().ifPresentOrElse((installLoc) -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("TcMenu Already installed");
            alert.setHeaderText("tcMenu is already installed on your system");
            alert.setContentText("The menu library is installed in: " + installLoc);
            alert.showAndWait();
        }, ()-> {
            if(getArduinoDirectory().isPresent()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("TcMenu Not installed");
                alert.setHeaderText("TcMenu needs to be installed, do it now?");
                alert.setContentText("TcMenu will be installed into " + getArduinoDirectory().get() + " libraries sub folder.");
                Optional<ButtonType> confirm = alert.showAndWait();
                if(confirm.isPresent() && confirm.get() == ButtonType.OK) {
                    installTcMenuLibrary();
                }
            }
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Arduino directory not located");
                alert.setHeaderText("Arduino directory not located");
                alert.setContentText("Please copy the library manually from the embedded folder in the distribution to the Arduino -> libraries directory");
                alert.showAndWait();
            }
        });
    }

    private void installTcMenuLibrary() {
        getArduinoDirectory().ifPresent(dir -> {
            try {
                Path libDir = dir.resolve("libraries");
                Files.createDirectories(libDir);
                Path src = Paths.get("./embedded");
                traverseDir(src, libDir);
            } catch (IOException e) {
                installFailure(e);
            }
        });
    }

    private void traverseDir(Path files, Path dir) throws IOException {
        logger.info("Traverse files in {} to {}", files, dir);
        Files.list(files).forEach(path -> {
            try {
                Path outFile = dir.resolve(path.getFileName());
                if(Files.isDirectory(path) && !Files.isHidden(path)) {
                    Files.createDirectory(outFile);
                    traverseDir(path, outFile);
                }
                else if(!Files.isDirectory(outFile)) {
                    logger.info("Copying from {} to {}", path, outFile);
                    Files.copy(path, outFile, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                installFailure(e);
            }
        });
    }

    private void installFailure(IOException e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Installation error");
        alert.setHeaderText("Installation of TcMenu embedded failed");
        logger.error("Could not copy the library", e);
    }


    public static Optional<Path> getArduinoDirectory() {
        String userDir = System.getProperty("user.home");

        Path docsPath = Paths.get(userDir, "Documents");
        if(!Files.exists(docsPath)) return Optional.empty();

        Path arduinoPath = docsPath.resolve("Arduino");
        if(!Files.exists(arduinoPath)) {
            // try again in the onedrive folder, noticed it there on several windows machines
            docsPath = Paths.get(userDir, "OneDrive/Documents");
            if(!Files.exists(docsPath)) {
                return Optional.empty();
            }
            arduinoPath = docsPath.resolve("Arduino");
            if(!Files.exists(arduinoPath)) {
                return Optional.empty();
            }
        }

        Path libsPath = arduinoPath.resolve("libraries");
        if(!Files.exists(libsPath)) return Optional.empty();

        return Optional.of(arduinoPath);
    }

    public static Optional<Path> findTcMenuInstall() {
        return getArduinoDirectory().map(path -> {
            Path libsDir = path.resolve("libraries");
            Path tcMenuDir = libsDir.resolve("tcMenu");
            if(Files.exists(tcMenuDir)) {
                return tcMenuDir;
            }
            return null;
        });
    }
}
