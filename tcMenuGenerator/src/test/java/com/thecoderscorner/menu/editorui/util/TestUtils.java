package com.thecoderscorner.menu.editorui.util;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.EmbeddedCodeCreator;
import com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.project.ProjectPersistor;
import org.junit.Assert;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestUtils {
    public static void assertEqualsIgnoringCRLF(String expected, String actual) {
        expected = expected.replaceAll("\\r\\n", "\n");
        actual = actual.replaceAll("\\r\\n", "\n");
        Assert.assertEquals(expected, actual);
    }

    public static CreatorProperty findAndCheckProperty(EmbeddedCodeCreator creator, String name, CreatorProperty.SubSystem subSystem,
                                                 CreatorProperty.PropType type, String newVal) {
        CreatorProperty prop = creator.properties().stream()
                .filter(p -> p.getName().equals(name))
                .findFirst().orElse(null);

        assertNotNull(prop);

        assertEquals(subSystem, prop.getSubsystem());
        assertEquals(type, prop.getPropType());
        prop.getProperty().setValue(newVal);
        assertEquals(newVal, prop.getLatestValue());
        return prop;
    }

    public static CurrentEditorProject makeEditorProject() {
        ProjectPersistor mockedPersistor = Mockito.mock(ProjectPersistor.class);
        CurrentEditorProject project = new CurrentEditorProject(null, mockedPersistor);
        return project;
    }

    public static MenuTree buildSimpleTree() {
        MenuTree tree = new MenuTree();

        AnalogMenuItem item = AnalogMenuItemBuilder.anAnalogMenuItemBuilder()
                .withId(1)
                .withName("test")
                .withFunctionName(null)
                .withEepromAddr(2)
                .withOffset(0)
                .withDivisor(1)
                .withUnit("dB")
                .withMaxValue(100)
                .menuItem();
        AnalogMenuItem item2 = AnalogMenuItemBuilder.anAnalogMenuItemBuilder()
                .withExisting(item)
                .withId(2)
                .withEepromAddr(4)
                .withFunctionName("callback1")
                .menuItem();
        SubMenuItem sub = SubMenuItemBuilder.aSubMenuItemBuilder()
                .withName("sub")
                .withId(100)
                .withEepromAddr(-1)
                .menuItem();
        EnumMenuItem extraItem = EnumMenuItemBuilder.anEnumMenuItemBuilder()
                .withId(20)
                .withName("Extra")
                .withEepromAddr(4)
                .withEnumList(List.of("test"))
                .menuItem();
        tree.addMenuItem(MenuTree.ROOT, extraItem);
        tree.addMenuItem(MenuTree.ROOT, item);
        tree.addMenuItem(MenuTree.ROOT, sub);
        tree.addMenuItem(sub, item2);
        return tree;
    }

}
