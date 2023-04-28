package com.thecoderscorner.menu.editorint.uitests;

import com.thecoderscorner.menu.editorui.MenuEditorApp;
import com.thecoderscorner.menu.editorui.dialog.ChooseFontDialog;
import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.parameters.FontDefinition;
import com.thecoderscorner.menu.editorui.generator.validation.CannedPropertyValidators;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.ComboBoxMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;
import org.testfx.util.WaitForAsyncUtils;

import static com.thecoderscorner.menu.editorui.util.TestUtils.clickOnButtonInDialog;
import static com.thecoderscorner.menu.editorui.util.TestUtils.writeIntoField;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ApplicationExtension.class)
public class EditFontTestCase {
    private Stage stage;

    @Start
    public void onStart(Stage stage) {
        this.stage = stage;
    }

    @Test
    public void checkFontPropertyEditing(FxRobot robot) throws InterruptedException {
        FontDefinition defaultFont = new FontDefinition(FontDefinition.FontMode.DEFAULT_FONT, "", 1);
        CreatorProperty prop = new CreatorProperty("Name", "Desc", "ExtDesc", defaultFont.toString(),
                SubSystem.DISPLAY, CreatorProperty.PropType.TEXTUAL, CannedPropertyValidators.fontValidator(), new AlwaysApplicable());
        var dialogPane = createFontDialog(robot, prop);
        robot.clickOn("#defaultFontSelect");
        writeIntoField(robot, "#fontVarField", "", 10);
        assertTrue(TestUtils.selectItemInCombo(robot, "#sizeCombo", (String val) -> val.equals("1")));
        clickOnButtonInDialog(robot, dialogPane, "Apply");

        // try the numbered x2
        dialogPane = createFontDialog(robot, prop);
        robot.clickOn("#largeNumSelect");
        assertTrue(TestUtils.selectItemInCombo(robot, "#sizeCombo", (String val) -> val.equals("9")));
        writeIntoField(robot, "#fontVarField", "", 10);
        clickOnButtonInDialog(robot, dialogPane, "Apply");

        // try ada font x1
        dialogPane = createFontDialog(robot, prop);
        robot.clickOn("#adafruitFontSel");
        assertTrue(TestUtils.selectItemInCombo(robot, "#sizeCombo", (String val) -> val.equals("2")));
        writeIntoField(robot, "#fontVarField", "myFont", 10);
        clickOnButtonInDialog(robot, dialogPane, "Apply");
    }

    private Node createFontDialog(FxRobot robot, CreatorProperty prop) throws InterruptedException {
        var def = FontDefinition.fromString(prop.getLatestValue()).orElseThrow();
        MenuEditorApp.configureBundle(MenuEditorApp.EMPTY_LOCALE);
        TestUtils.runOnFxThreadAndWait(()->new ChooseFontDialog(stage, def.toString(), false, false));
        WaitForAsyncUtils.waitForFxEvents();

        var radioToCheck = switch (def.fontMode()) {
            case DEFAULT_FONT -> "#defaultFontSelect";
            case ADAFRUIT -> "#adafruitFontSel";
            case ADAFRUIT_LOCAL -> "#adafruitLocalFontSel";
            case AVAILABLE -> "#staticFontSel";
            case NUMBERED -> "#largeNumSelect";
        };
        FxAssert.verifyThat(radioToCheck, RadioButton::isSelected);
        FxAssert.verifyThat("#sizeCombo", ComboBoxMatchers.hasSelectedItem(Integer.toString(def.fontNumber())));
        FxAssert.verifyThat("#fontVarField", TextInputControlMatchers.hasText(def.fontName()));

        return robot.lookup(".fontDialog").query();
    }
}
