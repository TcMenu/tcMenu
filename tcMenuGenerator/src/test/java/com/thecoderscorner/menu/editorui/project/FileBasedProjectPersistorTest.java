package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.display.DisplayType;
import com.thecoderscorner.menu.editorui.generator.input.InputType;
import com.thecoderscorner.menu.editorui.generator.remote.RemoteCapabilities;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.thecoderscorner.menu.editorui.generator.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.editorui.generator.CreatorProperty.SubSystem.DISPLAY;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileBasedProjectPersistorTest {
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
                EmbeddedPlatform.ARDUINO,
                DisplayType.values.get(1),
                InputType.values.get(1),
                RemoteCapabilities.values.get(1),
                Collections.singletonList(new CreatorProperty("name", "desc", "123", DISPLAY, TEXTUAL))
        );
        persistor.save(projFile.toString(), tree, options);

        MenuTreeWithCodeOptions openResult = persistor.open(projFile.toString());

        compareTrees(tree, openResult.getMenuTree());

        assertEquals(EmbeddedPlatform.ARDUINO, openResult.getOptions().getEmbeddedPlatform());
        assertEquals(1, openResult.getOptions().getLastDisplayType().getKey());
        assertEquals(1, openResult.getOptions().getLastInputType().getKey());
        assertEquals(1, openResult.getOptions().getLastRemoteCapabilities().getKey());

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