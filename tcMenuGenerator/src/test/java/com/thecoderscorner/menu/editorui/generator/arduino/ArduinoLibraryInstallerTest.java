package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.editorui.generator.util.LibraryStatus;
import com.thecoderscorner.menu.editorui.generator.util.VersionInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;

import static org.junit.Assert.*;

public class ArduinoLibraryInstallerTest {
    private ArduinoLibraryInstaller installer;
    private Path dirTmp;
    private Path dirArduino;
    private Path dirArduinoLibs;
    private Path dirEmbedded;

    @Before
    public void setUp() throws Exception {
        dirTmp = Files.createTempDirectory("tcmenu");

        dirArduino = dirTmp.resolve("Documents/Arduino");
        Files.createDirectories(dirArduino);
        dirArduinoLibs = dirArduino.resolve("libraries");
        // we don't create libraries, make sure the installer can do it.

        dirEmbedded = dirTmp.resolve("Embedded");
        Files.createDirectory(dirEmbedded);

        installer = new ArduinoLibraryInstaller(dirTmp.toString(), dirEmbedded.toString());
    }

    @After
    public void tearDown() throws Exception {
        Files.walk(dirTmp)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    public void testInstaller() throws IOException {
        // first there should be nothing there, blank created directory
        assertEquals(Optional.empty(), installer.findLibraryInstall("tcMenu"));

        // then we put tcMenu library in place.
        putLibraryInPlace(dirArduinoLibs, "tcMenu", "1.0.0");

        // now put tcMenu in the arduino libraries at V1.0
        Path tcMenuPath = installer.findLibraryInstall("tcMenu").get();

        // we should get it back
        assertEquals(dirArduinoLibs.resolve("tcMenu"), tcMenuPath);

        // now put tcMenu in the embedded source at 1.0.1, newer than arduino copy
        putLibraryInPlace(dirEmbedded, "tcMenu", "1.0.1");

        // the library is old and should not be marked up to date
        assertFalse(installer.isLibraryUpToDate("tcMenu"));

        // now lets update the library
        installer.copyLibraryFromPackage("tcMenu");

        // and on checking again it should now be up to date.
        assertTrue(installer.isLibraryUpToDate("tcMenu"));
        assertTrue(Files.exists(dirArduinoLibs.resolve("tcMenu/src")));
        assertTrue(Files.exists(dirArduinoLibs.resolve("tcMenu/src/afile.txt")));
    }

    @Test
    public void testCopyLibraryWithNoExistingLibrary() throws IOException {
        // put only the tcMenu library in place.
        putLibraryInPlace(dirEmbedded, "tcMenu", "1.0.0");

        // certainly not up-to-date, not even there
        assertFalse(installer.isLibraryUpToDate("tcMenu"));

        // check get version doesnt crash when no lib present.
        assertEquals(new VersionInfo("0.0.0"), installer.getVersionOfLibrary("tcMenu", false));

        // now lets update the library
        installer.copyLibraryFromPackage("tcMenu");

        // and on checking again it should now be up to date.
        assertTrue(installer.isLibraryUpToDate("tcMenu"));
        assertTrue(Files.exists(dirArduinoLibs.resolve("tcMenu/src")));
        assertTrue(Files.exists(dirArduinoLibs.resolve("tcMenu/src/afile.txt")));
    }

    @Test
    public void testGetAllLibraryStatus() throws IOException {
        // put libs in the embedded source at 1.0.1, newer than arduino copy
        putLibraryInPlace(dirEmbedded, "tcMenu", "1.0.1");
        putLibraryInPlace(dirEmbedded, "IoAbstraction", "1.0.0");
        putLibraryInPlace(dirEmbedded, "LiquidCrystalIO", "1.0.2");

        putLibraryInPlace(dirArduinoLibs, "tcMenu", "1.0.1");
        putLibraryInPlace(dirArduinoLibs, "IoAbstraction", "1.0.1");
        putLibraryInPlace(dirArduinoLibs, "LiquidCrystalIO", "1.0.1");

        LibraryStatus libraryStatus = installer.statusOfAllLibraries();
        assertFalse(libraryStatus.isUpToDate());
        assertTrue(libraryStatus.isIoAbstractionUpToDate());
        assertTrue(libraryStatus.isTcMenuUpToDate());
        assertFalse(libraryStatus.isLiquidCrystalIoUpToDate());
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