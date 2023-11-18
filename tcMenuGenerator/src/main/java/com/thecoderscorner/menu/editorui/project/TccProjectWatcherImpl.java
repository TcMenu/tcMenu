package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.persist.LocaleMappingHandler;
import javafx.application.Platform;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static java.lang.System.Logger.Level.*;
import static java.nio.file.StandardWatchEventKinds.*;

public class TccProjectWatcherImpl implements TccProjectWatcher {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final Thread watcherThread;
    private final AtomicReference<WatchService> watchService = new AtomicReference<>();
    private final AtomicReference<ProjectDirectories> projectDirs = new AtomicReference<>();
    private final AtomicReference<Consumer<String>> emfAdjustmentHandler = new AtomicReference<>();
    private final AtomicReference<LocaleMappingHandler> localeHandler = new AtomicReference<>();

    public TccProjectWatcherImpl() {
        watcherThread = new Thread(() -> {
            try {
                logger.log(INFO, "Watch Thread starting");
                while (!Thread.currentThread().isInterrupted()) {
                    var watcher = watchService.get();
                    var emfHandler = emfAdjustmentHandler.get();
                    var locHandler = localeHandler.get();
                    var projDir = projectDirs.get();
                    if (watcher == null || emfHandler == null || locHandler == null || projDir == null) continue;

                    try {
                        var keys = watcher.poll(500, TimeUnit.MILLISECONDS);
                        if (keys == null) continue;
                        for (var key : keys.pollEvents()) {
                            String context = key.context().toString();
                            if (context.equalsIgnoreCase(Paths.get(projectDirs.get().emfName()).getFileName().toString())) {
                                Platform.runLater(() -> emfHandler.accept(context));
                            }
                            if (context.startsWith("i18n") && context.endsWith(".properties") && localeHandler.get().isLocalSupportEnabled()) {
                                var propertiesFile = Paths.get(context).getFileName().toString();
                                Platform.runLater(() -> locHandler.reportLocaleChange(propertiesFile));
                            }
                        }
                        keys.reset();
                    } catch (InterruptedException|ClosedWatchServiceException e) {
                        logger.log(ERROR, "Watch Thread interrupted, exiting");
                        Thread.currentThread().interrupt();
                    } catch (Exception ex) {
                        logger.log(ERROR, "Change handler is closing down " + projDir.emfName() + " " + ex.getMessage());
                    }
                }
                logger.log(INFO, "Watch Thread exiting");
            } catch (Exception ex) {
                logger.log(ERROR, "Severe failure of polling thread, exiting", ex);
            }
        });
        watcherThread.start();

    }

    @Override
    public void setProjectName(Path emfFile) {
        try {
            var pd = projectDirs.get();
            if (pd != null && pd.projectDir().equals(emfFile.getParent())) {
                logger.log(DEBUG, "SetProjectName called on same project" + emfFile);
                return;
            }

            clear();

            logger.log(INFO, "Start watching project " + emfFile);
            watchService.set(FileSystems.getDefault().newWatchService());
            var emf = emfFile.getFileName().toString();
            var dir = emfFile.getParent();
            var i18n = dir.resolve("i18n");
            boolean i18nInUse = Files.exists(i18n);
            projectDirs.set(new ProjectDirectories(emf, dir, i18n, i18nInUse));

            dir.register(watchService.get(), ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
            if (i18nInUse) {
                i18n.register(watchService.get(), ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
            }
        } catch (IOException e) {
            logger.log(ERROR, "File watcher registration failed " + emfFile, e);
        }
    }

    public void clear() {
        if (watchService.get() != null) {
            try {
                logger.log(INFO, "Clear watch service down");
                watchService.get().close();
                watchService.set(null);
                projectDirs.set(null);
            } catch (IOException e) {
                logger.log(ERROR, "File watcher close failed " + projectDirs.get(), e);
            }
        }
    }

    @Override
    public void registerNotifiers(Consumer<String> emfHandler, LocaleMappingHandler handler) {
        emfAdjustmentHandler.set(emfHandler);
        localeHandler.set(handler);
    }

    @Override
    public void close() {
        var w = watchService.get();
        if(w != null) {
            try {
                w.close();
            } catch (IOException e) {
                logger.log(ERROR, "Exception closing watcher");
            }
        }
        watcherThread.interrupt();
    }

    record ProjectDirectories(String emfName, Path projectDir, Path i18nPath, boolean i18nExists) {
    }
}
