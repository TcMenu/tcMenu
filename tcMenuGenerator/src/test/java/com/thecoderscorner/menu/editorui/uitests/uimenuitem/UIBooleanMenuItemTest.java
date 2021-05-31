package com.thecoderscorner.menu.editorui.uitests.uimenuitem;

import com.thecoderscorner.menu.domain.BooleanMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.ScrollChoiceMenuItem;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.uimodel.UIBooleanMenuItem;
import com.thecoderscorner.menu.editorui.uimodel.UIMenuItem;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.Optional;

import static com.thecoderscorner.menu.domain.BooleanMenuItem.BooleanNaming.*;
import static com.thecoderscorner.menu.editorui.uimodel.UIBooleanMenuItem.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(ApplicationExtension.class)
public class UIBooleanMenuItemTest extends UIMenuItemTestBase {
    @Start
    public void setup(Stage stage) {
        init(stage);
    }

    @AfterEach
    protected void closeWindow() {
        Platform.runLater(() -> stage.close());
    }

    @Test
    public void testBooleanOnOff(FxRobot robot) throws Exception {
        var uiItem = generateBooleanDialog();
        performAllCommonChecks(uiItem.getMenuItem(), true);

        writeIntoField(robot, "nameField", "helloBoolean");

        var capturedItem = captureTheLatestBoolean();
        assertEquals(ON_OFF, capturedItem.getNaming());
    }

    @Test
    public void testBooleanYesNoAndTrueFalse(FxRobot robot) throws Exception {
        var uiItem = generateBooleanDialog();
        performAllCommonChecks(uiItem.getMenuItem(), true);

        TestUtils.selectItemInCombo(robot, "#booleanNamingCombo", (TidyBooleanNaming n) -> n.naming() == YES_NO);

        var capturedItem = captureTheLatestBoolean();
        assertEquals(YES_NO, capturedItem.getNaming());

        TestUtils.selectItemInCombo(robot, "#booleanNamingCombo", (TidyBooleanNaming n) -> n.naming() == TRUE_FALSE);

        capturedItem = captureTheLatestBoolean();
        assertEquals(TRUE_FALSE, capturedItem.getNaming());
    }

    private BooleanMenuItem captureTheLatestBoolean() {
        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
        Mockito.verify(mockedConsumer, Mockito.atLeastOnce()).accept(any(), captor.capture());
        BooleanMenuItem boolItem = (BooleanMenuItem) captor.getValue();
        return boolItem;
    }

    private UIBooleanMenuItem generateBooleanDialog() throws Exception {
        MenuItem analogItem = menuTree.getMenuById(4).orElseThrow();
        VariableNameGenerator vng = new VariableNameGenerator(menuTree, false);
        var uiBoolItem = editorUI.createPanelForMenuItem(analogItem, menuTree, vng, mockedConsumer);
        if(uiBoolItem.isEmpty()) throw new IllegalArgumentException("No menu item found");
        createMainPanel(uiBoolItem);
        return (UIBooleanMenuItem) uiBoolItem.get();
    }
}
