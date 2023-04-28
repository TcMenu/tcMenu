/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorint.uitests;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.dialog.EditMenuInMenuDialog;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptionsBuilder;
import com.thecoderscorner.menu.editorui.generator.parameters.MenuInMenuCollection;
import com.thecoderscorner.menu.editorui.generator.parameters.MenuInMenuDefinition;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.util.EnumWithStringValue;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import com.thecoderscorner.menu.mgr.MenuInMenu;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.ComboBoxMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;
import org.testfx.util.WaitForAsyncUtils;

import static com.thecoderscorner.menu.editorui.util.TestUtils.buildSimpleTreeReadOnly;
import static com.thecoderscorner.menu.editorui.util.TestUtils.writeIntoField;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;

@ExtendWith(ApplicationExtension.class)
public class MenuInMenuEditorTestCases {

    private Stage stage;
    private CodeGeneratorOptions options;
    private MenuTree tree;
    private MenuInMenuCollection menuInMenuColections;
    private MenuInMenuDefinition testDef1;
    private MenuInMenuDefinition testDef2;

    @Start
    public void onStart(Stage stage) {
        menuInMenuColections = new MenuInMenuCollection();
        testDef1 = new MenuInMenuDefinition("test1", "COM2", 9600, MenuInMenuDefinition.ConnectionType.SERIAL, MenuInMenu.ReplicationMode.REPLICATE_ADD_STATUS_ITEM, 100, 100000, 51000);
        testDef2 = new MenuInMenuDefinition("test2", "localhost", 3333, MenuInMenuDefinition.ConnectionType.SOCKET, MenuInMenu.ReplicationMode.REPLICATE_SILENTLY, 100, 100000, 51000);
        menuInMenuColections.addDefinition(testDef1);
        menuInMenuColections.addDefinition(testDef2);
        this.stage = stage;
        this.options = new CodeGeneratorOptionsBuilder()
                .withPlatform(EmbeddedPlatform.RASPBERRY_PIJ)
                .withMenuInMenu(menuInMenuColections)
                .codeOptions();
        tree = buildSimpleTreeReadOnly();
        new EditMenuInMenuDialog(stage, options, tree, false);
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        TestUtils.runOnFxThreadAndWait(()-> stage.close());
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testMenuInMenuEdit(FxRobot robot) throws InterruptedException {
        robot.clickOn("#addButton");
        robot.clickOn("#cancelButton");

        Thread.sleep(200);

        assertTrue(TestUtils.selectItemInTable(robot, "#menuInMenuTable",
                (MenuInMenuDefinition def) -> def.getVariableName().equals("test1")));

        robot.clickOn("#editButton");
        verifyThat("#nameField", TextInputControlMatchers.hasText("test1"));
        verifyThat("#hostField", TextInputControlMatchers.hasText("COM2"));
        verifyThat("#portBaudField", TextInputControlMatchers.hasText("9600"));
        verifyThat("#connectionTypeCombo", (ComboBox<EnumWithStringValue<?>> c)->
                c.getSelectionModel().getSelectedItem().stringValue().equals("Serial"));
        robot.clickOn("#nameField");
        writeIntoField(robot, "#nameField", "hello", 6);
        writeIntoField(robot, "#hostField", "newhost", 10);
        writeIntoField(robot, "#portBaudField", "3333", 6);
        writeIntoField(robot, "#offsetSpinner", "123456", 6);
        writeIntoField(robot, "#maxRangeSpinner", "9999", 6);
        assertTrue(TestUtils.selectItemInCombo(robot, "#connectionTypeCombo", (EnumWithStringValue<Enum<?>> conType) ->
                conType.stringValue().equals("Socket")));
        verifyThat("#submenuCombo", ComboBoxMatchers.containsExactlyItems(MenuTree.ROOT, tree.getMenuById(100).orElseThrow()));
        robot.clickOn("#saveButton");

        var item = menuInMenuColections.getAllDefinitions().stream().filter(d -> d.getVariableName().equals("hello"))
                .findFirst().orElseThrow();
        assertEquals("newhost", item.getPortOrIpAddress());
        assertEquals(3333, item.getPortOrBaud());
        assertEquals(123456, item.getIdOffset());
        assertEquals(9999, item.getMaximumRange());
        assertEquals(MenuInMenuDefinition.ConnectionType.SOCKET, item.getConnectionType());
        assertEquals(MenuInMenu.ReplicationMode.REPLICATE_ADD_STATUS_ITEM, item.getReplicationMode());

        assertTrue(TestUtils.selectItemInTable(robot, "#menuInMenuTable", (MenuInMenuDefinition def) -> {
            var name = def.getVariableName();
            return name.equals("hello");
        }));

        robot.clickOn("#removeButton");
        assertThat(menuInMenuColections.getAllDefinitions()).doesNotContain(testDef1);
    }
}
