package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.AnalogMenuItemBuilder;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.SubMenuItemBuilder;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.AbstractCodeCreator;
import com.thecoderscorner.menu.editorui.generator.EmbeddedCodeCreator;
import com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.util.LibraryStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

public class ArduinoGeneratorTest {

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
    public void testArduinoConversion() throws IOException {
        ArduinoSketchFileAdjuster adjuster = Mockito.mock(ArduinoSketchFileAdjuster.class);

        MenuTree tree = buildSimpleTree();
        ArduinoLibraryInstaller installer = Mockito.mock(ArduinoLibraryInstaller.class);
        Mockito.when(installer.statusOfAllLibraries()).thenReturn(new LibraryStatus(true, true, true));
        List<EmbeddedCodeCreator> generators = unitTestGenerator();
        ArduinoGenerator generator = new ArduinoGenerator(dir, generators, tree, adjuster, installer);

        assertTrue(generator.startConversion());

        String cppGenerated = new String(Files.readAllBytes(dir.resolve(dir.getFileName() + ".cpp")));
        String hGenerated = new String(Files.readAllBytes(dir.resolve(dir.getFileName() + ".h")));

        String cppTemplate = new String(getClass().getResourceAsStream("/generator/template.cpp").readAllBytes());
        String hTemplate = new String(getClass().getResourceAsStream("/generator/template.h").readAllBytes());

        cppGenerated = cppGenerated.replaceAll("#include \"tcmenu[^\"]*\"", "replacedInclude");
        cppTemplate = cppTemplate.replaceAll("#include \"tcmenu[^\"]*\"", "replacedInclude");

        // these files should line up. IF they do not because of the change in the ArduinoGenerator,
        // then make sure the change is good before adjusting the templates.
        assertEquals(cppTemplate, cppGenerated);
        assertEquals(hTemplate, hGenerated);


        Mockito.verify(adjuster).makeAdjustments(any(Consumer.class),
                eq(dir.resolve(dir.resolve(dir.getFileName() + ".ino")).toString()),
                eq(dir.getFileName().toString()), eq(List.of("callback1")));
    }

    private List<EmbeddedCodeCreator> unitTestGenerator() {
        EmbeddedCodeCreator gen = new AbstractCodeCreator() {
            @Override
            public List<String> getIncludes() {
                return Collections.singletonList("includes section");
            }

            @Override
            public String getGlobalVariables() {
                return "global vars";
            }

            @Override
            public String getSetupCode(String rootItem) {
                return "setup code";
            }

            @Override
            public List<CreatorProperty> properties() {
                return Collections.emptyList();
            }
        };
        return Collections.singletonList(gen);
    }

    private MenuTree buildSimpleTree() {
        MenuTree tree = new MenuTree();

        AnalogMenuItem item = AnalogMenuItemBuilder.anAnalogMenuItemBuilder()
                .withId(1)
                .withName("test")
                .withFunctionName(null)
                .withEepromAddr(-1)
                .withOffset(0)
                .withDivisor(1)
                .withUnit("dB")
                .withMaxValue(100)
                .menuItem();
        AnalogMenuItem item2 = AnalogMenuItemBuilder.anAnalogMenuItemBuilder()
                .withExisting(item)
                .withId(2)
                .withFunctionName("callback1")
                .menuItem();
        SubMenuItem sub = SubMenuItemBuilder.aSubMenuItemBuilder()
                .withName("sub")
                .withId(100)
                .withEepromAddr(-1)
                .menuItem();
        tree.addMenuItem(MenuTree.ROOT, item);
        tree.addMenuItem(MenuTree.ROOT, sub);
        tree.addMenuItem(sub, item2);
        return tree;
    }
}