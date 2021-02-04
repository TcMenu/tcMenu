/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.editorui.controller.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.generator.LibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.util.LibraryStatus;
import com.thecoderscorner.menu.editorui.generator.util.VersionInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArduinoLibraryInstallerTest {
    private ArduinoLibraryInstaller installer;
    private Path dirTmp;
    private Path dirArduino;
    private Path dirArduinoLibs;
    private LibraryVersionDetector verDetector;

    @BeforeEach
    public void setUp() throws Exception {
        dirTmp = Files.createTempDirectory("tcmenu");

        dirArduino = dirTmp.resolve("Documents/Arduino");
        Files.createDirectories(dirArduino);
        dirArduinoLibs = dirArduino.resolve("libraries");
        // we don't create libraries, make sure the installer can do it.

        verDetector = Mockito.mock(LibraryVersionDetector.class);

        var prefs = mock(ConfigurationStorage.class);
        when(prefs.isUsingArduinoIDE()).thenReturn(true);
        when(prefs.getArduinoOverrideDirectory()).thenReturn(Optional.empty());

        installer = new ArduinoLibraryInstaller(dirTmp.toString(), verDetector, mock(CodePluginManager.class), prefs, false);
    }

    @AfterEach
    public void tearDown() throws Exception {
        Files.walk(dirTmp)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    public void testGetAllLibraryStatus() throws IOException {
        // put libs in the embedded source at 1.0.1, newer than arduino copy
        putLibraryInPlace(dirArduinoLibs, "tcMenu", "1.0.1");
        putLibraryInPlace(dirArduinoLibs, "IoAbstraction", "1.2.1");
        putLibraryInPlace(dirArduinoLibs, "LiquidCrystalIO", "1.4.1");
        putLibraryInPlace(dirArduinoLibs, "TaskManagerIO", "1.0.0");

        var versions = Map.of(
                "tcMenu/Library", new VersionInfo("1.0.0"),
                "IoAbstraction/Library", new VersionInfo("1.2.1"),
                "LiquidCrystalIO/Library", new VersionInfo("1.5.1"),
                "TaskManagerIO/Library", new VersionInfo("1.0.1"),
                "xyz/Plugin", new VersionInfo("7.8.9")
        );
        when(verDetector.acquireVersions()).thenReturn(versions);

        LibraryStatus libraryStatus = installer.statusOfAllLibraries();
        assertFalse(libraryStatus.isUpToDate());
        assertTrue(libraryStatus.isIoAbstractionUpToDate());
        assertTrue(libraryStatus.isTcMenuUpToDate());
        assertFalse(libraryStatus.isLiquidCrystalIoUpToDate());

        assertEquals("1.0.1", installer.getVersionOfLibrary("tcMenu", CURRENT_LIB).toString());
        assertEquals("1.2.1", installer.getVersionOfLibrary("IoAbstraction", CURRENT_LIB).toString());
        assertEquals("1.4.1", installer.getVersionOfLibrary("LiquidCrystalIO", CURRENT_LIB).toString());
        assertEquals("1.0.0", installer.getVersionOfLibrary("TaskManagerIO", CURRENT_LIB).toString());

        assertEquals("1.0.1", installer.getVersionOfLibrary("TaskManagerIO", AVAILABLE_LIB).toString());
        assertEquals("1.0.0", installer.getVersionOfLibrary("tcMenu", AVAILABLE_LIB).toString());
        assertEquals("1.2.1", installer.getVersionOfLibrary("IoAbstraction", AVAILABLE_LIB).toString());
        assertEquals("1.5.1", installer.getVersionOfLibrary("LiquidCrystalIO", AVAILABLE_LIB).toString());
        assertEquals("7.8.9", installer.getVersionOfLibrary("xyz", AVAILABLE_PLUGIN).toString());
    }

    private void putLibraryInPlace(Path location, String name, String version) throws IOException {
        if(!Files.exists(dirArduinoLibs)) Files.createDirectory(dirArduinoLibs);

        Path tcMenuDir = Files.createDirectory(location.resolve(name));
        Path libProps = tcMenuDir.resolve("library.properties");
        String contents = "version=" + version + "\r\n";
        Files.write(libProps, contents.getBytes());
        Path src = tcMenuDir.resolve("src");
        Files.createDirectory(src);
        Files.write(src.resolve("afile.txt"), "This is some text to copy".getBytes());
    }
}