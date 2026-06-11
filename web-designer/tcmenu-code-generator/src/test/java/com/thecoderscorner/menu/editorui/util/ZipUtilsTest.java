package com.thecoderscorner.menu.editorui.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

public class ZipUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    public void testCreateZipFileFromExcludesBackupFilesAndDirectories() throws IOException {
        // Given a directory with some regular files, .backup files, and a .backup directory
        Path subDir = tempDir.resolve("subdir");
        Path backupDir = tempDir.resolve(".backup");
        Path nestedBackupDir = subDir.resolve(".backup");
        Files.createDirectories(subDir);
        Files.createDirectories(backupDir);
        Files.createDirectories(nestedBackupDir);
        
        Files.writeString(tempDir.resolve("file1.txt"), "content1");
        Files.writeString(tempDir.resolve("file2.backup"), "backup content");
        Files.writeString(subDir.resolve("file3.java"), "public class Test {}");
        Files.writeString(subDir.resolve("file4.backup"), "another backup");
        Files.writeString(backupDir.resolve("should_be_ignored.txt"), "ignored");
        Files.writeString(nestedBackupDir.resolve("also_ignored.txt"), "ignored");

        // When we create a zip from this directory
        byte[] zipData = ZipUtils.createZipFileFrom(tempDir);

        // Then the zip should contain file1.txt and subdir/file3.java, but not the .backup files or contents of .backup directories
        Set<String> entries = new HashSet<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipData))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entries.add(entry.getName());
                zis.closeEntry();
            }
        }

        assertTrue(entries.contains("file1.txt"));
        assertTrue(entries.contains("subdir/file3.java"));
        assertFalse(entries.contains("file2.backup"));
        assertFalse(entries.contains("subdir/file4.backup"));
        assertFalse(entries.contains(".backup/should_be_ignored.txt"));
        assertFalse(entries.contains("subdir/.backup/also_ignored.txt"));
        assertEquals(2, entries.size());
    }

    @Test
    public void testExtractFilesFromZip() throws IOException {
        // Given a zip file with some content
        Path sourceDir = tempDir.resolve("source");
        Files.createDirectories(sourceDir);
        Files.writeString(sourceDir.resolve("test.txt"), "hello world");
        byte[] zipData = ZipUtils.createZipFileFrom(sourceDir);

        // When we extract it to a new directory
        Path targetDir = tempDir.resolve("target");
        ZipUtils.extractFilesFromZip(targetDir, new ByteArrayInputStream(zipData));

        // Then the files should be present
        assertTrue(Files.exists(targetDir.resolve("test.txt")));
        assertEquals("hello world", Files.readString(targetDir.resolve("test.txt")));
    }
}
