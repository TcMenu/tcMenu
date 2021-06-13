package com.thecoderscorner.menu.editorui.util;

import com.thecoderscorner.menu.editorui.generator.LibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.util.VersionInfo;
import javafx.stage.Stage;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType.AVAILABLE_PLUGIN;
import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType.CURRENT_PLUGIN;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class PluginUpgradeTask implements Runnable {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());

    private final CodePluginManager pluginManager;
    private final ArduinoLibraryInstaller installer;
    private final LibraryVersionDetector detector;
    private final AtomicBoolean runningAlready = new AtomicBoolean(false);
    private UpdateProgressGlassPane updatePopup = null;
    private Optional<VersionInfo> maybeVersion;
    private Set<String> pluginsToUpdate;
    private Optional<Runnable> onCompleteTask = Optional.empty();

    public PluginUpgradeTask(CodePluginManager pluginManager, ArduinoLibraryInstaller installer, LibraryVersionDetector detector) {
        this.pluginManager = pluginManager;
        this.installer = installer;
        this.detector = detector;
    }

    public synchronized void startUpdateProcedure(Stage theStage, Collection<String> pluginsToUpdate, Optional<VersionInfo> maybeVersion) {
        if(runningAlready.get()) return;

        this.maybeVersion = maybeVersion;
        updatePopup = new UpdateProgressGlassPane();
        updatePopup.show(theStage);
        this.pluginsToUpdate = Set.copyOf(pluginsToUpdate);

        Thread th = new Thread(this);
        runningAlready.set(true);
        th.start();
    }

    @Override
    public void run() {
        try {
            double currentPercentage = 0.0;
            double percentagePerTask = 1.0 / (double)(pluginsToUpdate.size() + 1);
            for(var pluginName : pluginsToUpdate) {
                var availableVersion = maybeVersion.orElse(installer.getVersionOfLibrary(pluginName, AVAILABLE_PLUGIN));
                if(availableVersion != null) {
                    var installedVersion = installer.getVersionOfLibrary(pluginName, CURRENT_PLUGIN);
                    if (!installedVersion.equals(availableVersion)) {
                        updatePopup.updateProgress(currentPercentage, "Updating plugin " + pluginName);
                        currentPercentage += percentagePerTask;
                        logger.log(INFO, "Updating " + pluginName);
                        detector.upgradePlugin(pluginName, availableVersion);
                    }
                }
            }
            updatePopup.updateProgress(currentPercentage, "Refreshing plugins");
            pluginManager.reload();
            updatePopup.completed(true, pluginsToUpdate.size() + " plugins updated successfully");
        } catch (Exception e) {
            updatePopup.completed(false, "Failed: " + e.getMessage());
            logger.log(ERROR, "Update failed with exception", e);
        }

        runningAlready.set(false);

        onCompleteTask.ifPresent(Runnable::run);
        onCompleteTask = Optional.empty();
    }

    public void onCompleted(Runnable onCompleteTask) {
        this.onCompleteTask = Optional.of(onCompleteTask);
    }
}

