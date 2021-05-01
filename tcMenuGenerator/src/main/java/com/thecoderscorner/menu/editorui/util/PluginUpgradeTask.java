package com.thecoderscorner.menu.editorui.util;

import com.thecoderscorner.menu.editorui.generator.LibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginConfig;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import javafx.application.Platform;
import javafx.scene.control.Labeled;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType.AVAILABLE_PLUGIN;
import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType.CURRENT_PLUGIN;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class PluginUpgradeTask implements Runnable {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());

    private Labeled textField;
    private final CodePluginManager pluginManager;
    private final ArduinoLibraryInstaller installer;
    private final LibraryVersionDetector detector;
    private final AtomicBoolean runningAlready = new AtomicBoolean(false);

    public PluginUpgradeTask(CodePluginManager pluginManager, ArduinoLibraryInstaller installer, LibraryVersionDetector detector) {
        this.pluginManager = pluginManager;
        this.installer = installer;
        this.detector = detector;
    }

    public synchronized void startUpdateProcedure(Labeled toUpdate) {
        if(runningAlready.get()) return;
        Thread th = new Thread(this);
        runningAlready.set(true);
        textField = toUpdate;
        th.start();
    }

    private void updateUI(String status, boolean success) {
        Platform.runLater(() -> {
            textField.setText(status);
            if(success)
                textField.setStyle("-fx-background-color: green;-fx-text-fill: white;");
            else
                textField.setStyle("-fx-background-color: red;-fx-text-fill: white;");
        });
    }

    @Override
    public void run() {
        try {
            Set<String> allPlugins = new HashSet<>(List.of("core-display", "core-remote", "core-themes"));
            var installedPlugins = pluginManager.getLoadedPlugins().stream()
                    .map(CodePluginConfig::getModuleName)
                    .collect(Collectors.toList());
            allPlugins.addAll(installedPlugins);

            for(var pluginName : allPlugins) {
                var availableVersion = installer.getVersionOfLibrary(pluginName, AVAILABLE_PLUGIN);
                if(availableVersion != null) {
                    var installedVersion = installer.getVersionOfLibrary(pluginName, CURRENT_PLUGIN);
                    if (!installedVersion.equals(availableVersion)) {
                        updateUI("Updating plugin " + pluginName, true);
                        logger.log(INFO, "Updating " + pluginName);
                        detector.upgradePlugin(pluginName, availableVersion);
                    }
                }
            }
            updateUI("Refreshing plugins", true);
            pluginManager.reload();
            updateUI("Plugins reloaded", true);
            runningAlready.set(false);

        } catch (Exception e) {
            updateUI("Failed to update: " + e.getMessage(), false);
            logger.log(ERROR, "Update failed with exception", e);
        }
    }
}

