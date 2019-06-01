/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.thecoderscorner.menu.pluginapi.EmbeddedPlatform.ARDUINO_AVR;
import static com.thecoderscorner.menu.pluginapi.SubSystem.DISPLAY;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileBasedProjectPersistorTest {
    public static final UUID APPLICATION_UUID = UUID.randomUUID();
    private Path dir;

    @BeforeEach
    public void setUp() throws Exception {
        dir = Files.createTempDirectory("tcmenu");
    }

    @AfterEach
    public void tearDown() throws Exception {
        Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    public void testSaveThenLoad() throws IOException {

        Path projFile = dir.resolve("projectSave.emf");

        FileBasedProjectPersistor persistor = new FileBasedProjectPersistor();
        MenuTree tree = TestUtils.buildCompleteTree();
        CodeGeneratorOptions options = new CodeGeneratorOptions(
                ARDUINO_AVR.getBoardId(),
                "uuid1",
                "uuid2",
                "uuid3",
                Collections.singletonList(new CreatorProperty("name", "desc", "123", DISPLAY)),
                APPLICATION_UUID, "app name"
        );
        persistor.save(projFile.toString(), tree, options);

        MenuTreeWithCodeOptions openResult = persistor.open(projFile.toString());

        compareTrees(tree, openResult.getMenuTree());


        assertEquals(ARDUINO_AVR.getBoardId(), openResult.getOptions().getEmbeddedPlatform());
        assertEquals("uuid1", openResult.getOptions().getLastDisplayUuid());
        assertEquals("uuid2", openResult.getOptions().getLastInputUuid());
        assertEquals("uuid3", openResult.getOptions().getLastRemoteCapabilitiesUuid());
        assertEquals("app name", openResult.getOptions().getApplicationName());
        assertEquals("app name", openResult.getOptions().getApplicationName());
        assertEquals(APPLICATION_UUID, openResult.getOptions().getApplicationUUID());

        List<CreatorProperty> returnedProps = openResult.getOptions().getLastProperties();
        assertEquals("123", returnedProps.get(0).getLatestValue());
        assertEquals("name", returnedProps.get(0).getName());
        assertEquals(DISPLAY, returnedProps.get(0).getSubsystem());
    }

    private void compareTrees(MenuTree sourceTree, MenuTree compTree) {
        Set<MenuItem> srcSubs = sourceTree.getAllSubMenus();
        Set<MenuItem> dstSubs = compTree.getAllSubMenus();

        assertEquals(dstSubs, srcSubs);

        srcSubs.forEach(subMenu -> {
            List<MenuItem> srcItems = sourceTree.getMenuItems(subMenu);
            List<MenuItem> dstItems = compTree.getMenuItems(subMenu);

            assertEquals(dstItems, srcItems);
        });
    }
}