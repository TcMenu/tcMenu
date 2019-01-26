package com.thecoderscorner.menu.editorui.uitests.uimenuitem;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUIImpl;
import com.thecoderscorner.menu.editorui.uimodel.UIMenuItem;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.testfx.api.FxRobot;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import static com.thecoderscorner.menu.editorui.uitests.UiUtils.textFieldHasValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.testfx.api.FxAssert.verifyThat;

public abstract class UIMenuItemTestBase {

    protected CurrentProjectEditorUI editorUI;
    protected MenuTree menuTree;
    protected BiConsumer<MenuItem, MenuItem> mockedConsumer;
    protected Stage stage;
    protected DialogPane dialogPane;

    @SuppressWarnings("unchecked")
    protected void init(Stage stage) {
        editorUI = new CurrentProjectEditorUIImpl(stage);
        menuTree = TestUtils.buildCompleteTree();
        mockedConsumer = mock(BiConsumer.class);
        this.stage = stage;

        dialogPane = new DialogPane();
        dialogPane.setMinSize(500, 500);
        stage.setScene(new Scene(dialogPane));
    }

    protected void tryToEnterLettersIntoNumericField(FxRobot robot, String field) {
        robot.clickOn("#" + field);
        robot.eraseText(10);
        robot.write("abc");
        verifyThat("#" + field, (TextField f) -> f.getText().isEmpty());
    }

    protected void tryToEnterBadValueIntoField(FxRobot robot, String idField, String idOtherField, String badValue, String errorText) {
        // now enter a value that is too large into the eeprom field
        robot.clickOn("#" + idField);
        robot.eraseText(12);
        robot.write(badValue);
        robot.clickOn("#" + idOtherField);
        // and verify an error is displayed
        verifyThat("#uiItemErrors", (Label l)-> l.getText().contains(errorText) && l.isVisible());
    }

    protected void verifyThatThereAreNoErrorsReported() {
        verifyThat("#uiItemErrors", node -> !node.isVisible());
    }

    protected void performAllCommonChecks(MenuItem item) {
        verifyThat("#idField", Node::isDisabled);
        verifyThat("#idField", textFieldHasValue(Integer.toString(item.getId())));
        verifyThat("#eepromField", textFieldHasValue(Integer.toString(item.getEepromAddress())));
        verifyThat("#nameField", textFieldHasValue(item.getName()));
        verifyThat("#functionNameTextField", textFieldHasValue(
                item.getFunctionName() == null ? UIMenuItem.NO_FUNCTION_DEFINED : item.getFunctionName()));
        verifyThat("#uiItemErrors", (Node node)->!node.isVisible());
    }

    protected void createMainPanel(Optional<UIMenuItem> uiSubItem) throws InterruptedException {
        assertTrue(uiSubItem.isPresent());

        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            BorderPane borderLayout = new BorderPane();
            borderLayout.setMinSize(500, 500);
            borderLayout.centerProperty().set(uiSubItem.get().initPanel());
            dialogPane.getChildren().add(borderLayout);
            stage.show();
            latch.countDown();
        });
        latch.await(2000, TimeUnit.MILLISECONDS);
    }
}
