package com.thecoderscorner.menu.editorui.uitests.uimenuitem;

import com.thecoderscorner.menu.domain.EditItemType;
import com.thecoderscorner.menu.domain.EditableTextMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.uimodel.UITextMenuItem;
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

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(ApplicationExtension.class)
public class UITextMenuItemTest extends UIMenuItemTestBase {
    @Start
    public void setup(Stage stage) {
        init(stage);
    }

    @AfterEach
    protected void closeWindow() {
        Platform.runLater(() -> stage.close());
    }

    @Test
    public void testGeneratingTextItem(FxRobot robot) throws Exception {
        var ui = generateTextItem();
        performAllCommonChecks(ui.getMenuItem(), true);

        tryToEnterBadValueIntoField(robot, "textLength", "nameField", "1000",
                "MaxLength - Value must be between 1 and 256");

        writeIntoField(robot, "textLength", 10);

        var item = captureTheLatestTextItem();
        assertEquals(10, item.getTextLength());
        assertEquals(EditItemType.PLAIN_TEXT, item.getItemType());
    }

    @Test
    public void testGeneratingEachNonPlainType(FxRobot robot) throws Exception {
        var ui = generateTextItem();
        performAllCommonChecks(ui.getMenuItem(), true);

        var list = new ArrayList<>(Arrays.asList(EditItemType.values()));
        list.remove(EditItemType.PLAIN_TEXT);
        for(var mode : list) {
            TestUtils.selectItemInCombo(robot, "#textEditType", (EditItemType e) -> e == mode);
            var item = captureTheLatestTextItem();
            assertEquals(mode, item.getItemType());
        }
    }

    private UITextMenuItem generateTextItem() throws Exception {
        VariableNameGenerator vng = new VariableNameGenerator(menuTree, false);
        var uiText = editorUI.createPanelForMenuItem(menuTree.getMenuById(5).orElseThrow(), menuTree, vng, mockedConsumer);
        if(uiText.isEmpty()) throw new IllegalArgumentException("No menu item found");
        createMainPanel(uiText);
        return (UITextMenuItem) uiText.get();
    }

    private EditableTextMenuItem captureTheLatestTextItem() {
        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
        Mockito.verify(mockedConsumer, Mockito.atLeastOnce()).accept(any(), captor.capture());
        return (EditableTextMenuItem) captor.getValue();
    }
}
