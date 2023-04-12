package com.thecoderscorner.menu.editorui.util;

import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BackupManagerTest {
    private Path rootDir;
    private ConfigurationStorage mockedStorage;

    @BeforeEach
    public void prepareDir() throws IOException {
        rootDir = Files.createTempDirectory("tcmenutest");
        Files.createDirectories(rootDir);

        Files.writeString(rootDir.resolve("hello.ino"), "Hello world");

        mockedStorage = Mockito.mock(ConfigurationStorage.class);
    }

    @AfterEach
    public void deleteDir() throws IOException {
        Files.walk(rootDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    public void testLegacyBackup() throws IOException {
        Mockito.when(mockedStorage.getNumBackupItems()).thenReturn(0);
        var backupMgr = new BackupManager(mockedStorage);
        backupMgr.backupFile(rootDir, rootDir.resolve("hello.ino"));

        assertFalse(Files.exists(rootDir.resolve(".backup")));
        assertTrue(Files.exists(rootDir.resolve("hello.ino.backup")));

        assertEquals("Hello world", Files.readString(rootDir.resolve("hello.ino.backup"), StandardCharsets.UTF_8));
    }

    @Test
    public void testTimeBasedBackupNoRemovalNeeded() throws IOException {
        Mockito.when(mockedStorage.getNumBackupItems()).thenReturn(3);
        var backupMgr = new BackupManager(mockedStorage);
        backupMgr.backupFile(rootDir, rootDir.resolve("hello.ino"));

        assertTrue(Files.exists(rootDir.resolve(".backup")));
        var backupFile = Files.walk(rootDir)
                .filter(path -> path.getFileName().toString().endsWith(".bak"))
                .findFirst().orElseThrow();
        assertTrue(backupFile.getFileName().toString().startsWith("hello."));
        assertEquals("Hello world", Files.readString(backupFile, StandardCharsets.UTF_8));
        assertEquals(1, Files.list(rootDir.resolve(".backup")).count());

        backupMgr.backupFile(rootDir, rootDir.resolve("file_not_there.ino"));
        assertEquals(1, Files.list(rootDir.resolve(".backup")).count());
    }

    @Test
    public void testTimeBasedBackupWithRemoval() throws IOException, InterruptedException {
        Files.writeString(rootDir.resolve("file2.cpp"), "Hello world1");
        Files.writeString(rootDir.resolve("file3.cpp"), "Hello world2");

        Mockito.when(mockedStorage.getNumBackupItems()).thenReturn(3);
        var backupMgr = new BackupManager(mockedStorage);
        backupMgr.backupFile(rootDir, rootDir.resolve("hello.ino"));
        backupMgr.backupFile(rootDir, rootDir.resolve("hello.ino"));
        backupMgr.backupFile(rootDir, rootDir.resolve("file2.cpp"));
        backupMgr.backupFile(rootDir, rootDir.resolve("file3.cpp"));

        assertTrue(Files.exists(rootDir.resolve(".backup")));

        var backupFiles = Files.list(rootDir.resolve(".backup"))
                .map(path -> path.getFileName().toString())
                .toList();
        assertEquals(3, backupFiles.size());

        assertHasFileContaining(backupFiles, "hello.ino");
        assertHasFileContaining(backupFiles, "file2.cpp");
        assertHasFileContaining(backupFiles, "file3.cpp");
    }

    private void assertHasFileContaining(List<String> backupFiles, String s) {
        assertTrue(backupFiles.stream().anyMatch(p -> p.contains(s)));
    }

}