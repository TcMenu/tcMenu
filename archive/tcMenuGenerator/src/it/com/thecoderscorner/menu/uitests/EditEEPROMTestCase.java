package com.thecoderscorner.menu.uitests;

import com.thecoderscorner.menu.editorui.dialog.SelectEepromTypeDialog;
import com.thecoderscorner.menu.editorui.generator.parameters.eeprom.*;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.TextInputControlMatchers;
import org.testfx.util.WaitForAsyncUtils;

import java.util.function.Predicate;

import static com.thecoderscorner.menu.editorui.controller.EepromTypeSelectionController.ROM_PAGE_SIZES;
import static com.thecoderscorner.menu.editorui.util.TestUtils.selectItemInCombo;
import static com.thecoderscorner.menu.editorui.util.TestUtils.writeIntoField;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;

@ExtendWith(ApplicationExtension.class)
public class EditEEPROMTestCase {
    private Stage stage;
    private SelectEepromTypeDialog dialog;

    @Start
    public void onStart(Stage stage) {
        this.stage = stage;
    }

    @Test
    public void checkAvrRomStartWithNone(FxRobot robot) throws InterruptedException {
        TestUtils.runOnFxThreadAndWait(() -> dialog = new SelectEepromTypeDialog(stage, new NoEepromDefinition(), false));

        verifyThat("#noRomRadio", RadioButton::isSelected);

        robot.clickOn("#avrRomRadio");
        verifyThat("#i2cAddrField", Node::isDisabled);
        verifyThat("#memOffsetField", Node::isDisabled);
        robot.clickOn("#okButton");

        var res = dialog.getResultOrEmpty().orElseThrow();
        assertTrue(res instanceof AVREepromDefinition);

    }

    @Test
    public void checkAt24Eeprom(FxRobot robot) throws InterruptedException {
        TestUtils.runOnFxThreadAndWait(() -> dialog = new SelectEepromTypeDialog(stage, new At24EepromDefinition(0x50, ROM_PAGE_SIZES.get(7).varName()), false));

        verifyThat("#at24Radio", RadioButton::isSelected);
        verifyThat("#i2cAddrField", Predicate.not(Node::isDisabled));
        verifyThat("#i2cAddrField", TextInputControlMatchers.hasText("0x50"));
        verifyThat("#romPageCombo", (ComboBox<?> n) -> n.getSelectionModel().getSelectedIndex() == 7);
        verifyThat("#memOffsetField", Node::isDisabled);
        selectItemInCombo(robot, "#romPageCombo", o -> o.equals(ROM_PAGE_SIZES.get(1)));

        WaitForAsyncUtils.waitForFxEvents(100);
        robot.clickOn("#okButton");
        WaitForAsyncUtils.waitForFxEvents(100);

        var res = dialog.getResultOrEmpty().orElseThrow();
        if(res instanceof At24EepromDefinition at24) {
            assertEquals(0x50, at24.getAddress());
            assertEquals(ROM_PAGE_SIZES.get(1).varName(), at24.getPageSize());

        } else fail("Not AT24 rom");
    }

    @Test
    public void checkPreferences(FxRobot robot) throws InterruptedException {
        TestUtils.runOnFxThreadAndWait(() -> dialog = new SelectEepromTypeDialog(stage, new PreferencesEepromDefinition("TcMenuAbc", 1024), false));

        verifyThat("#prefsRadio", RadioButton::isSelected);
        verifyThat("#prefsNamespace", Predicate.not(Node::isDisabled));
        verifyThat("#prefsNamespace", TextInputControlMatchers.hasText("TcMenuAbc"));
        verifyThat("#prefsSize", Predicate.not(Node::isDisabled));
        verifyThat("#prefsSize", TextInputControlMatchers.hasText("1024"));

        WaitForAsyncUtils.waitForFxEvents(100);
        robot.clickOn("#okButton");
        WaitForAsyncUtils.waitForFxEvents(100);

        var res = dialog.getResultOrEmpty().orElseThrow();
        if(res instanceof PreferencesEepromDefinition prefs) {
            assertEquals("TcMenuAbc", prefs.getRomNamespace());
            assertEquals(1024, prefs.getSize());

        } else fail("Not AT24 rom");
    }

    @Test
    public void testNoEeprom(FxRobot robot) throws InterruptedException {
        TestUtils.runOnFxThreadAndWait(() -> dialog = new SelectEepromTypeDialog(stage, new ArduinoClassEepromDefinition(), false));

        verifyThat("#eepromRadio", RadioButton::isSelected);
        robot.clickOn("#noRomRadio");
        robot.clickOn("#okButton");
        Thread.sleep(250);
        assertTrue(dialog.getResultOrEmpty().orElseThrow() instanceof NoEepromDefinition);
    }

    @Test
    public void testArduinoEeprom(FxRobot robot) throws InterruptedException {
        TestUtils.runOnFxThreadAndWait(() -> dialog = new SelectEepromTypeDialog(stage, new AVREepromDefinition(), false));

        verifyThat("#avrRomRadio", RadioButton::isSelected);
        robot.clickOn("#eepromRadio");
        robot.clickOn("#okButton");
        Thread.sleep(250);
        assertTrue(dialog.getResultOrEmpty().orElseThrow() instanceof ArduinoClassEepromDefinition);
    }

    @Test
    public void testBspEeprom(FxRobot robot) throws InterruptedException {
        TestUtils.runOnFxThreadAndWait(() -> dialog = new SelectEepromTypeDialog(stage, new BspStm32EepromDefinition(22), false));

        verifyThat("#bspStRadio", RadioButton::isSelected);
        verifyThat("#memOffsetField", TextInputControlMatchers.hasText("22"));

        writeIntoField(robot, "#memOffsetField", 100, 5);
        robot.clickOn("#okButton");
        Thread.sleep(250);
        if(dialog.getResultOrEmpty().orElseThrow() instanceof BspStm32EepromDefinition bsp) {
            assertEquals(100, bsp.getOffset());
        } else fail("Not BSP");
    }


}
