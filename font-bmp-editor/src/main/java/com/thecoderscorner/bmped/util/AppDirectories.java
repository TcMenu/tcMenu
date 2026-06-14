package com.thecoderscorner.bmped.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class AppDirectories {
    private AppDirectories() {
    }

    public static Path logDirectory(String appName) throws IOException {
        String os = System.getProperty("os.name", "").toLowerCase();
        Path dir;

        if (os.contains("mac")) {
            dir = Paths.get(
                    System.getProperty("user.home"),
                    "Library",
                    "Logs",
                    appName
            );
        } else if (os.contains("win")) {
            String localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData != null && !localAppData.isBlank()) {
                dir = Paths.get(localAppData, appName, "logs");
            } else {
                dir = Paths.get(System.getProperty("user.home"), "AppData", "Local", appName, "logs");
            }
        } else {
            String xdgStateHome = System.getenv("XDG_STATE_HOME");
            if (xdgStateHome != null && !xdgStateHome.isBlank()) {
                dir = Paths.get(xdgStateHome, appName, "logs");
            } else {
                dir = Paths.get(System.getProperty("user.home"), ".local", "state", appName, "logs");
            }
        }

        Files.createDirectories(dir);
        return dir;
    }
}