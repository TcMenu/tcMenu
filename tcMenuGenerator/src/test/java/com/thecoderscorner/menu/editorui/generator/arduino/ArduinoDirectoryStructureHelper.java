package com.thecoderscorner.menu.editorui.generator.arduino;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;

public class ArduinoDirectoryStructureHelper {
    public enum DirectoryPath {TCMENU_DIR, SKETCHES_DIR}

    public static final String INO_FILE = "void setup() {}\nvoid loop();\n";
    public static final String EMF_FILE = "[]";
    private Path dirTmp;
    private Path arduinoSketchesDirTemp;
    private Path tcMenuLibDirTemp;

    public void initialise() throws IOException {
        dirTmp = Files.createTempDirectory("tcmenu");

        arduinoSketchesDirTemp = dirTmp.resolve("Documents/Arduino");
        Files.createDirectories(arduinoSketchesDirTemp);
        tcMenuLibDirTemp= arduinoSketchesDirTemp.resolve("libraries/tcMenu");
        Files.createDirectories(tcMenuLibDirTemp);
        Files.createDirectories(tcMenuLibDirTemp.resolve("examples"));

    }

    public void createSketch(DirectoryPath where, String name, boolean hasEmf) throws IOException {
        var path = where == DirectoryPath.TCMENU_DIR ? tcMenuLibDirTemp.resolve("examples") : arduinoSketchesDirTemp;
        Path sketchDir = path.resolve(name);
        Files.createDirectories(sketchDir);
        String fileNameBase = sketchDir.getFileName().toString();
        Files.write(sketchDir.resolve(fileNameBase + ".INO"), INO_FILE.getBytes());
        if(hasEmf) {
            Files.write(sketchDir.resolve(fileNameBase + ".EMF"), EMF_FILE.getBytes());
        }
    }

    public void cleanUp() throws IOException {
        Files.walk(dirTmp)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    public Optional<Path> getTcMenuPath() {
        return Optional.of(tcMenuLibDirTemp);
    }

    public Optional<Path> getSketchesDir() {
        return Optional.of(arduinoSketchesDirTemp);
    }
}
