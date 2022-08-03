/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.ScrollChoiceMenuItem;
import com.thecoderscorner.menu.domain.ScrollChoiceMenuItemBuilder;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.util.TestUtils;
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
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


public class ArduinoSketchFileAdjusterTest {
    private Path dir;
    private MenuTree tree;
    private ArduinoSketchFileAdjuster adjuster;
    private List<CallbackRequirement> callbacks;
    private ScrollChoiceMenuItem scrollChoice;
    private Path inoFile;
    private BiConsumer<System.Logger.Level, String> emptyLogger;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUp() throws Exception {
        dir = Files.createTempDirectory("tcmenu");
        tree = TestUtils.buildCompleteTree();
        inoFile = dir.resolve("superProject.ino");
        emptyLogger = Mockito.mock(BiConsumer.class);
        adjuster = new ArduinoSketchFileAdjuster(new CodeGeneratorOptions());

        scrollChoice = new ScrollChoiceMenuItemBuilder()
                .withId(1111)
                .withName("HelloScroll")
                .withVariable("helloWorldScrollVar")
                .withChoiceMode(ScrollChoiceMenuItem.ScrollChoiceMode.ARRAY_IN_RAM)
                .withNumEntries(10).withItemWidth(10)
                .menuItem();
        tree.addMenuItem(MenuTree.ROOT, scrollChoice);

        scrollChoice = new ScrollChoiceMenuItemBuilder()
                .withId(1111)
                .withName("HelloScrollII")
                .withVariable("@scrollVarNotThere")
                .withChoiceMode(ScrollChoiceMenuItem.ScrollChoiceMode.ARRAY_IN_RAM)
                .withNumEntries(9).withItemWidth(22)
                .menuItem();
        tree.addMenuItem(MenuTree.ROOT, scrollChoice);

        MenuItem itemId6 = tree.getMenuById(6).orElseThrow();
        MenuItem itemId8 = tree.getMenuById(8).orElseThrow();
        MenuItem itemId9 = tree.getMenuById(9).orElseThrow();
        MenuItem itemId10 = tree.getMenuById(10).orElseThrow();
        VariableNameGenerator generator = Mockito.mock(VariableNameGenerator.class);
        when(generator.makeRtFunctionName(any())).thenReturn("fnListRtCall");
        when(generator.makeNameToVar(itemId8)).thenReturn("ActionTest");
        when(generator.makeNameToVar(itemId9)).thenReturn("SubnetMask");
        when(generator.makeNameToVar(itemId10)).thenReturn("List");
        callbacks = List.of(
                new CallbackRequirement(generator, "callback", itemId8),
                new CallbackRequirement(generator, "fnListRtCall", itemId10),
                new CallbackRequirement(generator, "onIpChange", itemId9),
                new CallbackRequirement(generator, "@includeOnly", itemId6)
        );
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
        adjuster.makeAdjustments(emptyLogger, inoFile.toString(), "superProject", callbacks, tree);

        List<String> lines = Files.readAllLines(inoFile);

        // we should have an include, only once.
        ensureLinesContaining(lines, "#include \"superProject_menu.h\"");

        // we should have a basic set up and loop method ready prepared
        ensureLinesContaining(lines, "void setup() {",
                "setupMenu();",
                "}");
        ensureLinesContaining(lines, "void loop() {",
                "taskManager.runLoop();",
                "}");

        ensureLinesContaining(lines, "char* helloWorldScrollVar = \"1\\0        2\\0        3\\0        4\\0        5\\0        6\\0        7\\0        8\\0        9\\0        10\\0       ~\";");

        // we should have both callbacks created.
        ensureLinesContaining(lines,
                "void CALLBACK_FUNCTION callback(int id) {",
                "// TODO - your menu change code",
                "}");

        ensureLinesContaining(lines,
                "void CALLBACK_FUNCTION onIpChange(int id) {",
                "// TODO - your menu change code",
                "}");
    }

    @Test
    public void testUpdatingFileThatPartlyExists() throws IOException {
        String inoContent = "#include \"superProject_menu.h\"\n\n"
                + "void setup() {\n"
                + "  superObj.init();\n"
                + "}\n\n"
                + "void loop() {\n"
                + "}\n\n"
                + "void CALLBACK_FUNCTION callback(int id) {\n"
                + "  superObj.doIt();\n"
                + "}\n\n";
        Files.write(inoFile, inoContent.getBytes());

        adjuster.makeAdjustments(emptyLogger, inoFile.toString(), "superProject", callbacks, tree);

        List<String> lines = Files.readAllLines(inoFile);

        // we should have an include, only once.
        ensureLinesContaining(lines, "#include \"superProject_menu.h\"");

        // we should have a basic set up and loop method ready prepared
        ensureLinesContaining(lines, "void setup() {", "setupMenu();", "superObj.init();", "}");
        ensureLinesContaining(lines, "void loop() {", "taskManager.runLoop();", "}");

        // we should have both callbacks created.
        ensureLinesContaining(lines, "void CALLBACK_FUNCTION callback(int id) {",
                "superObj.doIt();",
                "}");
        ensureLinesContaining(lines, "void CALLBACK_FUNCTION onIpChange(int id) {",
                "// TODO - your menu change code",
                "}");
        ensureDoesNotContainLine(lines, "void CALLBACK_FUNCTION includeOnly(int id)");

        // and very importantly, make sure the backup is made
        Path backup = inoFile.resolveSibling("superProject.ino.backup");
        assertTrue(Files.exists(backup));
        assertEquals(inoContent, new String(Files.readAllBytes(backup)));
    }


    @Test
    public void testWhereNoChangesNeeded() throws IOException {
        String inoContent = """
                #include "superProject_menu.h"

                char helloWorldScrollVar[] = "sdfkjdsfkjsdfj";
                
                void setup() {
                  superObj.init();
                  setupMenu();}

                void loop() {
                  taskManager.runLoop();}

                void CALLBACK_FUNCTION callback(int id) {
                  superObj.doIt();
                }

                void CALLBACK_FUNCTION onIpChange(int id) {
                  superObj.doIt();
                }

                int CALLBACK_FUNCTION fnListRtCall(RuntimeMenuItem* item, uint8_t row, RenderFnMode mode, char* , int ) {
                   switch(mode) {
                    case RENDERFN_INVOKE:
                        return true;
                    case RENDERFN_NAME:
                        return true;
                    }
                }
                """;
        Files.write(inoFile, inoContent.getBytes());

        adjuster.makeAdjustments(emptyLogger, inoFile.toString(), "superProject", callbacks, tree);

        // Shouldn't do anything this time around.
        Path backup = inoFile.resolveSibling("superProject.ino.backup");
        assertFalse(Files.exists(backup));
    }

    private boolean ensureDoesNotContainLine(List<String> lines, String search) {
        return lines.stream().noneMatch(line -> line.contains(search));
    }

    private void ensureLinesContaining(List<String> lines, String search, String... next) {
        boolean foundSearch = false;
        int currentNext = 0;
        for (String line : lines) {
            if (!foundSearch) {
                foundSearch = line.trim().equals(search);
            } else if (!line.trim().isEmpty() && currentNext < next.length) {
                // we must find the next string
                assertEquals(next[currentNext], line.trim(), "Expected items not in order");
                currentNext++;
            } else {
                // we must not find the search string more than once.
                assertFalse(line.contains(search), "Item to search duplicated");
            }
        }
        assertTrue(foundSearch, "Search item not found");
    }
}