package com.thecoderscorner.menu.editorui.project;

import javafx.application.Platform;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.Adler32;

import static java.lang.System.Logger.Level.*;
import static java.nio.file.StandardWatchEventKinds.*;

public class TccProjectWatcherImpl implements TccProjectWatcher {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final Thread watcherThread;
    private final AtomicReference<WatchService> watchService = new AtomicReference<>();
    private final AtomicReference<ProjectDirectories> projectDirs = new AtomicReference<>();
    private final AtomicReference<ProjectWatchListener> watchListener = new AtomicReference<>();
    private Map<Path, Long> checksumsByFileName = new ConcurrentHashMap<>();

    public TccProjectWatcherImpl() {
        watcherThread = new Thread(() -> {
            try {
                logger.log(INFO, "Watch Thread starting");
                while (!Thread.currentThread().isInterrupted()) {
                    var watcher = watchService.get();
                    var emfHandler = watchListener.get();
                    var projDir = projectDirs.get();
                    if (watcher == null || emfHandler == null || projDir == null) continue;

                    try {
                        var keys = watcher.poll(500, TimeUnit.MILLISECONDS);
                        if (keys == null) continue;
                        for (var key : keys.pollEvents()) {
                            String context = key.context().toString();
                            if (context.equalsIgnoreCase(Paths.get(projectDirs.get().emfName()).getFileName().toString())) {
                                if(!hasFileChecksumChanged(context)) return;
                                // only the project has changed, reload the project file.
                                Platform.runLater(emfHandler::externalChangeToProject);
                            }
                            if (context.startsWith("project-lang") && context.endsWith(".properties")) {
                                if(!hasFileChecksumChanged(context)) return;
                                Platform.runLater(() -> {
                                    // update any changed properties files, and then completely reload the project
                                    // this makes double sure that everything is reloaded and updated.
                                    emfHandler.i18nFileUpdated(context);
                                });

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

    private boolean hasFileChecksumChanged(String context) {
        Path key = Paths.get(context);
        var f = (context.endsWith(".properties")) ? Paths.get("i18n", context) : key;
        var completeFileName = projectDirs.get().projectDir.resolve(f);
        if(!checksumsByFileName.containsKey(key)) return true;

        try {
            var lastChecksum = checksumsByFileName.get(key);
            var fileContents = Files.readString(completeFileName);
            var checksum = new Adler32();
            checksum.update(fileContents.getBytes(StandardCharsets.UTF_8));
            if(checksum.getValue() == lastChecksum) return false;

            // update the checksum
            checksumsByFileName.put(key, checksum.getValue());
        } catch (IOException e) {
            logger.log(ERROR, "Unable to read back changed file " + context, e);
        }
        return true;
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
    public void registerWatchListener(ProjectWatchListener listener) {
        watchListener.set(listener);
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

    @Override
    public void fileWasSaved(Path path, String data) {
        var checksum = new Adler32();
        checksum.update(data.getBytes(StandardCharsets.UTF_8));
        checksumsByFileName.put(path.getFileName(), checksum.getValue());
    }

    record ProjectDirectories(String emfName, Path projectDir, Path i18nPath, boolean i18nExists) {
    }
}
