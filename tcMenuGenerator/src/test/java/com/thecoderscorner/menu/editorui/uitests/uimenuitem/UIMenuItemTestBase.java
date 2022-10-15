/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uitests.uimenuitem;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.LibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatforms;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
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
import org.testfx.matcher.control.TextInputControlMatchers;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.testfx.api.FxAssert.verifyThat;

public abstract class UIMenuItemTestBase {

    protected CurrentProjectEditorUI editorUI;
    protected MenuTree menuTree;
    protected BiConsumer<MenuItem, MenuItem> mockedConsumer;
    protected Stage stage;
    protected DialogPane dialogPane;
    private CodePluginManager manager;

    @SuppressWarnings("unchecked")
    protected void init(Stage stage) {
        manager = mock(CodePluginManager.class);
        ConfigurationStorage storage = mock(ConfigurationStorage.class);
        editorUI = new CurrentProjectEditorUIImpl(manager, stage, mock(EmbeddedPlatforms.class),
                mock(ArduinoLibraryInstaller.class), storage,
                mock(LibraryVersionDetector.class), System.getProperty("user.home")
        );
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

    protected void writeIntoField(FxRobot robot, String idField, Object value) {
        TestUtils.writeIntoField(robot, "#" + idField, value, 12);
    }

    protected void verifyThatThereAreNoErrorsReported() {
        verifyThat("#uiItemErrors", node -> !node.isVisible());
    }

    protected void performAllCommonChecks(MenuItem item, boolean hasEepromField) {
        verifyThat("#idField", Node::isDisabled);
        verifyThat("#idField", TextInputControlMatchers.hasText(Integer.toString(item.getId())));
        if(hasEepromField) verifyThat("#eepromField", TextInputControlMatchers.hasText(Integer.toString(item.getEepromAddress())));
        verifyThat("#nameField", TextInputControlMatchers.hasText(item.getName()));
        verifyThat("#functionNameTextField", TextInputControlMatchers.hasText(
                item.getFunctionName() == null ? UIMenuItem.NO_FUNCTION_DEFINED : item.getFunctionName()));
        verifyThat("#uiItemErrors", (Node node)->!node.isVisible());
    }

    protected void createMainPanel(Optional<UIMenuItem<?>> uiSubItem) throws InterruptedException {
        assertTrue(uiSubItem.isPresent());

        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            BorderPane borderLayout = new BorderPane();
            borderLayout.setMinSize(500, 500);
            borderLayout.centerProperty().set(uiSubItem.get().initPanel(menuTree));
            dialogPane.getChildren().add(borderLayout);
            stage.show();
            latch.countDown();
        });
        if(!latch.await(2000, TimeUnit.MILLISECONDS)) throw new IllegalStateException("panel timeout");
    }
}
