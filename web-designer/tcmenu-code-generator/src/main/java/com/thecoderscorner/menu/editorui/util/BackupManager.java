package com.thecoderscorner.menu.editorui.util;

import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Collectors;

public class BackupManager {
    private static final System.Logger logger = System.getLogger(BackupManager.class.getSimpleName());
    public static final String BACKUP_DIRECTORY = ".backup";
    private final ConfigurationStorage config;
    private final DateTimeFormatter backupFmt = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSS");

    public BackupManager(ConfigurationStorage config) {
        this.config = config;
    }

    public void backupFile(Path projectDir, Path fileToBackup) throws IOException {
        if(!Files.exists(fileToBackup)) return;

        if(config.getNumBackupItems() > 0) {
            Path backupDir = projectDir.resolve(BACKUP_DIRECTORY);
            if(!Files.exists(backupDir)) {
                Files.createDirectory(backupDir);
            }

            var ts = backupFmt.format(LocalDateTime.now());
            var fileName = fileToBackup.getFileName() + "_" + ts + ".bak";
            var backupFile = backupDir.resolve(fileName);
            Files.copy(fileToBackup, backupFile, StandardCopyOption.REPLACE_EXISTING);

            trimBackupDirectoryToLimit(config.getNumBackupItems(), backupDir);
        } else {
            // just put the backup in the same directory
            var backupFile = Paths.get(fileToBackup.toString() + BACKUP_DIRECTORY);
            Files.copy(fileToBackup, backupFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void trimBackupDirectoryToLimit(int allowedItems, Path dirPath) {
        try(var filesList = Files.list(dirPath)) {
            var allFilesWithTime = filesList.map(path -> {
                try {
                    return new FileWithTime(path, Files.getLastModifiedTime(path).toInstant());
                } catch (IOException ex) {
                    logger.log(System.Logger.Level.ERROR, "File exception while checking old backups", ex);
                    return null;
                }
            }).filter(Objects::nonNull).toList();
            var sortedList = allFilesWithTime.stream().sorted(Comparator.comparing(FileWithTime::time))
                    .filter(f -> f.file().toString().endsWith(".bak"))
                    .collect(Collectors.toCollection(LinkedList::new));
            while(sortedList.size() > allowedItems) {
                var item = sortedList.removeFirst();
                try {
                    Files.delete(item.file());
                } catch(IOException ex) {
                    logger.log(System.Logger.Level.ERROR, "File exception while deleting old backups", ex);
                }
            }
        } catch (IOException ex) {
            logger.log(System.Logger.Level.ERROR, "File exception managing old backups", ex);
        }
    }

    record FileWithTime(Path file, Instant time) {}
}
