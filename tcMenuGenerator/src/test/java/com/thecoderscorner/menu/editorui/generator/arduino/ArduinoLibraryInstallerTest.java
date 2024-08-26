/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.editorui.generator.AppVersionDetector;
import com.thecoderscorner.menu.editorui.generator.GitHubAppVersionChecker;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.persist.VersionInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;

import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType.CURRENT_LIB;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArduinoLibraryInstallerTest {
    private ArduinoLibraryInstaller installer;
    private Path dirTmp;
    private Path dirArduino;
    private Path dirArduinoLibs;
    private AppVersionDetector verDetector;

    @BeforeEach
    public void setUp() throws Exception {
        dirTmp = Files.createTempDirectory("tcmenu");

        dirArduino = dirTmp.resolve("Documents/Arduino");
        Files.createDirectories(dirArduino);
        dirArduinoLibs = dirArduino.resolve("libraries");
        // we don't create libraries, make sure the installer can do it.

        verDetector = Mockito.mock(AppVersionDetector.class);

        var prefs = mock(ConfigurationStorage.class);
        when(prefs.isUsingArduinoIDE()).thenReturn(true);
        when(prefs.getArduinoOverrideDirectory()).thenReturn(Optional.of(dirArduino.toString()));
        when(prefs.getArduinoLibrariesOverrideDirectory()).thenReturn(Optional.of(dirArduinoLibs.toString()));

        installer = new ArduinoLibraryInstaller(verDetector, mock(CodePluginManager.class), prefs);
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

        when(verDetector.acquireVersion()).thenReturn(
                new GitHubAppVersionChecker.TcMenuRelease("", VersionInfo.of("1.2.3"), LocalDateTime.now()));

        assertEquals("1.0.1", installer.getVersionOfLibrary("tcMenu", CURRENT_LIB).toString());
        assertEquals("1.2.1", installer.getVersionOfLibrary("IoAbstraction", CURRENT_LIB).toString());
        assertEquals("1.4.1", installer.getVersionOfLibrary("LiquidCrystalIO", CURRENT_LIB).toString());
        assertEquals("1.0.0", installer.getVersionOfLibrary("TaskManagerIO", CURRENT_LIB).toString());
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