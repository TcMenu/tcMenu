/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;


public class ArduinoSketchFileAdjusterTest {
    private Path dir;
    private ArduinoSketchFileAdjuster adjuster;
    private List<String> callbacks = List.of("callback1", "callback2");
    private Path inoFile;
    private Consumer<String> emptyLogger;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUp() throws Exception {
        dir = Files.createTempDirectory("tcmenu");
        inoFile = dir.resolve("superProject.ino");
        emptyLogger = Mockito.mock(Consumer.class);
        adjuster = new ArduinoSketchFileAdjuster();
    }

    @AfterEach
    public void tearDown() throws Exception {
        Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    public void testCreatingFileFromScratch() throws IOException {
        adjuster.makeAdjustments(emptyLogger, inoFile.toString(), "superProject", callbacks);

        List<String> lines = Files.readAllLines(inoFile);

        // we should have an include, only once.
        ensureLinesContaining(lines,"#include \"superProject_menu.h\"");

        // we should have a basic set up and loop method ready prepared
        ensureLinesContaining(lines,"void setup() {", "setupMenu();", "}");
        ensureLinesContaining(lines,"void loop() {", "taskManager.runLoop();", "}");

        // we should have both callbacks created.
        ensureLinesContaining(lines,"void CALLBACK_FUNCTION callback1(int id) {", "// TODO - your menu change code", "}");
        ensureLinesContaining(lines,"void CALLBACK_FUNCTION callback2(int id) {", "// TODO - your menu change code", "}");
    }

    @Test
    public void testUpdatingFileThatPartlyExists() throws IOException {
        String inoContent = "#include \"superProject_menu.h\"\n\n"
                + "void setup() {\n"
                + "  superObj.init();\n"
                + "}\n\n"
                + "void loop() {\n"
                + "}\n\n"
                + "void CALLBACK_FUNCTION callback1(int id) {\n"
                + "  superObj.doIt();\n"
                + "}\n\n";
        Files.write(inoFile, inoContent.getBytes());

        adjuster.makeAdjustments(emptyLogger, inoFile.toString(), "superProject", callbacks);

        List<String> lines = Files.readAllLines(inoFile);

        // we should have an include, only once.
        ensureLinesContaining(lines,"#include \"superProject.h\"");

        // we should have a basic set up and loop method ready prepared
        ensureLinesContaining(lines,"void setup() {", "setupMenu();", "superObj.init();", "}");
        ensureLinesContaining(lines,"void loop() {", "taskManager.runLoop();", "}");

        // we should have both callbacks created.
        ensureLinesContaining(lines,"void CALLBACK_FUNCTION callback1(int id) {", "superObj.doIt();", "}");
        ensureLinesContaining(lines,"void CALLBACK_FUNCTION callback2(int id) {", "// TODO - your menu change code", "}");

        // and very importantly, make sure the backup is made
        Path backup = inoFile.resolveSibling("superProject.ino.backup");
        assertTrue(Files.exists(backup));
        assertEquals(inoContent, new String(Files.readAllBytes(backup)));
    }


    @Test
    public void testWhereNoChangesNeeded() throws IOException {
        String inoContent = "#include \"superProject_menu.h\"\n\n"
                + "void setup() {\n"
                + "  superObj.init();\n"
                + "  setupMenu();"
                + "}\n\n"
                + "void loop() {\n"
                + "  taskManager.runLoop();"
                + "}\n\n"
                + "void CALLBACK_FUNCTION callback1(int id) {\n"
                + "  superObj.doIt();\n"
                + "}\n\n"
                + "void CALLBACK_FUNCTION callback2(int id) {\n"
                + "  superObj.doIt();\n"
                + "}\n\n";
        Files.write(inoFile, inoContent.getBytes());

        adjuster.makeAdjustments(emptyLogger, inoFile.toString(), "superProject", callbacks);

        // Shouldn't do anything this time around.
        Path backup = inoFile.resolveSibling("superProject.ino.backup");
        assertFalse(Files.exists(backup));
    }

    private void ensureLinesContaining(List<String> lines, String search, String... next) {
        boolean foundSearch = false;
        int currentNext = 0;
        for (String line : lines) {
            if(!foundSearch) {
                foundSearch = line.trim().equals(search);
            }
            else if(!line.trim().isEmpty() && currentNext < next.length) {
                // we must find the next string
                assertEquals(next[currentNext], line.trim(), "Expected items not in order");
                currentNext++;
            }
            else {
                // we must not find the search string more than once.
                assertFalse(line.contains(search), "Item to search duplicated");
            }
        }
        assertTrue(foundSearch, "Search item not found");
    }
}