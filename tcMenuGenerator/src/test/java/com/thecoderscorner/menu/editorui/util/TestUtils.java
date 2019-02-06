package com.thecoderscorner.menu.editorui.util;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.EmbeddedCodeCreator;
import com.thecoderscorner.menu.pluginapi.SubSystem;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import org.testfx.api.FxRobot;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class TestUtils {
    public static void assertEqualsIgnoringCRLF(String expected, String actual) {
        expected = expected.replaceAll("\\r\\n", "\n");
        actual = actual.replaceAll("\\r\\n", "\n");
        assertEquals(expected, actual);
    }

    public static CreatorProperty findAndCheckProperty(EmbeddedCodeCreator creator, String name, SubSystem subSystem,
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

    public static void runOnFxThreadAndWait(Runnable runnable) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(()-> {
            runnable.run();
            latch.countDown();
        });
        latch.await(5000, TimeUnit.MILLISECONDS);
    }

    public static <T> void selectItemInCombo(FxRobot robot, String query, T value) throws InterruptedException {
        runOnFxThreadAndWait(()-> {
            ComboBox<T> combo = robot.lookup(query).queryComboBox();
            combo.getSelectionModel().select(value);
        });
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
                .withEepromAddr(5)
                .withEnumList(List.of("test"))
                .menuItem();
        tree.addMenuItem(MenuTree.ROOT, extraItem);
        tree.addMenuItem(MenuTree.ROOT, item);
        tree.addMenuItem(MenuTree.ROOT, sub);
        tree.addMenuItem(sub, item2);
        return tree;
    }

    public static MenuTree buildCompleteTree() {
        MenuTree tree = buildSimpleTree();

        BooleanMenuItem boolItem = BooleanMenuItemBuilder.aBooleanMenuItemBuilder()
                .withId(4)
                .withNaming(BooleanMenuItem.BooleanNaming.ON_OFF)
                .withName("BoolTest")
                .menuItem();
        tree.addMenuItem(MenuTree.ROOT, boolItem);

        TextMenuItem textItem = TextMenuItemBuilder.aTextMenuItemBuilder()
                .withId(5)
                .withLength(10)
                .withName("TextTest")
                .menuItem();
        tree.addMenuItem(MenuTree.ROOT, textItem);

        FloatMenuItem floatItem = FloatMenuItemBuilder.aFloatMenuItemBuilder()
                .withId(6)
                .withDecimalPlaces(4)
                .withName("FloatTest")
                .menuItem();
        tree.addMenuItem(MenuTree.ROOT, floatItem);

        RemoteMenuItem remoteItem = RemoteMenuItemBuilder.aRemoteMenuItemBuilder()
                .withId(7)
                .withRemoteNo(2)
                .withName("RemoteTest")
                .menuItem();
        tree.addMenuItem(MenuTree.ROOT, remoteItem);

        ActionMenuItem actionItem = ActionMenuItemBuilder.anActionMenuItemBuilder()
                .withId(8)
                .withFunctionName("callback")
                .withName("ActionTest")
                .menuItem();
        tree.addMenuItem(MenuTree.ROOT, actionItem);

        return tree;
    }
}
