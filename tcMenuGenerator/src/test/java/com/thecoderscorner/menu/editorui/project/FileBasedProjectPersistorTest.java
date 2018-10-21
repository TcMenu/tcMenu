package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.display.DisplayType;
import com.thecoderscorner.menu.editorui.generator.input.InputType;
import com.thecoderscorner.menu.editorui.generator.remote.RemoteCapabilities;
import com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty.SubSystem.DISPLAY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileBasedProjectPersistorTest {
    private Path dir;

    @Before
    public void setUp() throws Exception {
        dir = Files.createTempDirectory("tcmenu");
    }

    @After
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
        MenuTree tree = TestUtils.buildSimpleTree();
        CodeGeneratorOptions options = new CodeGeneratorOptions(
                EmbeddedPlatform.ARDUINO,
                DisplayType.values.get(1),
                InputType.values.get(1),
                RemoteCapabilities.values.get(1),
                Collections.singletonList(new CreatorProperty("name", "desc", "123", DISPLAY, TEXTUAL))
        );
        persistor.save(projFile.toString(), tree, options);

        MenuTreeWithCodeOptions openResult = persistor.open(projFile.toString());

        assertEquals(openResult.getMenuTree().getMenuById(MenuTree.ROOT, 1), tree.getMenuById(MenuTree.ROOT, 1));
        assertEquals(openResult.getMenuTree().getMenuById(MenuTree.ROOT, 20), tree.getMenuById(MenuTree.ROOT, 20));
        Optional<SubMenuItem> subMenu = openResult.getMenuTree().getSubMenuById(100);
        assertTrue(subMenu.isPresent());
        assertEquals(subMenu, tree.getMenuById(MenuTree.ROOT, 100));
        assertEquals(openResult.getMenuTree().getMenuById(subMenu.get(), 2), tree.getMenuById(subMenu.get(), 2));

        assertEquals(EmbeddedPlatform.ARDUINO, openResult.getOptions().getEmbeddedPlatform());
        assertEquals(1, openResult.getOptions().getLastDisplayType().getKey());
        assertEquals(1, openResult.getOptions().getLastInputType().getKey());
        assertEquals(1, openResult.getOptions().getLastRemoteCapabilities().getKey());

        List<CreatorProperty> returnedProps = openResult.getOptions().getLastProperties();
        assertEquals("123", returnedProps.get(0).getLatestValue());
        assertEquals("name", returnedProps.get(0).getName());
        assertEquals(DISPLAY, returnedProps.get(0).getSubsystem());
    }
}