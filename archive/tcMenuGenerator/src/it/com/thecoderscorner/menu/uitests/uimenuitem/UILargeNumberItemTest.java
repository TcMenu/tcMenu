package com.thecoderscorner.menu.uitests.uimenuitem;

import com.thecoderscorner.menu.domain.EditableLargeNumberMenuItem;
import com.thecoderscorner.menu.domain.EditableLargeNumberMenuItemBuilder;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.uimodel.UILargeNumberMenuItem;
import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.TextInputControlMatchers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.testfx.api.FxAssert.verifyThat;

@ExtendWith(ApplicationExtension.class)
public class UILargeNumberItemTest extends UIMenuItemTestBase {
    @Start
    public void setup(Stage stage) {
        init(stage);
    }

    @AfterEach
    protected void closeWindow() {
        Platform.runLater(() -> stage.close());
    }

    @Test
    public void testGeneratingLargeNumberPositiveInt(FxRobot robot) throws Exception {
        var uiLge = generateLargeNumDialog();
        performAllCommonChecks(uiLge.getMenuItem(), true);

        tryToEnterBadValueIntoField(robot, "lgeTotalDigits", "nameField", "99",
                "ERROR Total Digits: Value must be between 4 and 12");
        tryToEnterBadValueIntoField(robot, "lgeDecimalPlaces", "nameField", "99",
                "ERROR Decimal Places: Value must be between 0 and 8");

        writeIntoField(robot, "lgeDecimalPlaces", 0);
        tryToEnterBadValueIntoField(robot, "lgeTotalDigits", "nameField", "10",
                "ERROR Total Digits: Whole part cannot be larger than 9 figures");

        writeIntoField(robot, "lgeTotalDigits", 8);

        verifyThat("#NegAllowCheck", CheckBox::isSelected);
        robot.clickOn("#NegAllowCheck");

        var lgeNum = captureTheLatestNumber();
        assertEquals(0, lgeNum.getDecimalPlaces());
        assertEquals(8, lgeNum.getDigitsAllowed());
        assertFalse(lgeNum.isNegativeAllowed());
    }

    @Test
    public void testGeneratingLargeNumberDecimal(FxRobot robot) throws Exception {
        var uiLge = generateLargeNumDialog();
        performAllCommonChecks(uiLge.getMenuItem(), true);

        writeIntoField(robot, "lgeTotalDigits", 10);
        writeIntoField(robot, "lgeDecimalPlaces", 4);

        verifyThat("#NegAllowCheck", CheckBox::isSelected);
        verifyThat("#defaultValueField", TextInputControlMatchers.hasText("10"));

        writeIntoField(robot, "defaultValueField", "10.42");

        var lgeNum = captureTheLatestNumber();
        assertEquals(4, lgeNum.getDecimalPlaces());
        assertEquals(10, lgeNum.getDigitsAllowed());
        assertEquals(new BigDecimal("10.42"), MenuItemHelper.getValueFor(lgeNum, menuTree, BigDecimal.ZERO));
        assertTrue(lgeNum.isNegativeAllowed());
    }

    private UILargeNumberMenuItem generateLargeNumDialog() throws Exception {
        var largeNumItem = EditableLargeNumberMenuItemBuilder.aLargeNumberItemBuilder()
                .withName("Large Num")
                .withId(222)
                .withEepromAddr(22)
                .withDecimalPlaces(5).withTotalDigits(10)
                .withNegativeAllowed(true)
                .menuItem();
        menuTree.addMenuItem(MenuTree.ROOT, largeNumItem);
        MenuItemHelper.setMenuState(largeNumItem, BigDecimal.TEN, menuTree);
        VariableNameGenerator vng = new VariableNameGenerator(menuTree, false);
        var uiBoolItem = editorUI.createPanelForMenuItem(largeNumItem, menuTree, vng, mockedConsumer);
        if(uiBoolItem.isEmpty()) throw new IllegalArgumentException("No menu item found");
        createMainPanel(uiBoolItem);
        return (UILargeNumberMenuItem) uiBoolItem.get();
    }

    private EditableLargeNumberMenuItem captureTheLatestNumber() {
        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
        Mockito.verify(mockedConsumer, Mockito.atLeastOnce()).accept(any(), captor.capture());
        return (EditableLargeNumberMenuItem) captor.getValue();
    }
}
