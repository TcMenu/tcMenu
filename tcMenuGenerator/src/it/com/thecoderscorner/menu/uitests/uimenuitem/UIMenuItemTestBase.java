/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.uitests.uimenuitem;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.core.util.TccDatabaseUtilities;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.MenuEditorApp;
import com.thecoderscorner.menu.editorui.generator.AppVersionDetector;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorSupplier;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.plugin.PluginEmbeddedPlatformsImpl;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUIImpl;
import com.thecoderscorner.menu.editorui.uimodel.UIMenuItem;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.testfx.api.FxRobot;
import org.testfx.matcher.control.TextInputControlMatchers;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static com.thecoderscorner.menu.editorui.MenuEditorApp.EMPTY_LOCALE;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.testfx.api.FxAssert.verifyThat;

public abstract class UIMenuItemTestBase {

    protected CurrentProjectEditorUIImpl editorUI;
    protected MenuTree menuTree;
    protected BiConsumer<MenuItem, MenuItem> mockedConsumer;
    protected Stage stage;
    protected Pane dialogPane;
    private CodePluginManager manager;

    @SuppressWarnings("unchecked")
    protected void init(Stage stage) {
        manager = mock(CodePluginManager.class);
        ConfigurationStorage storage = mock(ConfigurationStorage.class);
        var bundle = MenuEditorApp.configureBundle(EMPTY_LOCALE);
        editorUI = new CurrentProjectEditorUIImpl(manager, new PluginEmbeddedPlatformsImpl(),
                mock(ArduinoLibraryInstaller.class), storage, mock(AppVersionDetector.class),
                mock(CodeGeneratorSupplier.class), mock(TccDatabaseUtilities.class), mock(GlobalSettings.class), System.getProperty("user.home"));
        editorUI.setStage(stage, bundle);
        menuTree = TestUtils.buildCompleteTree();
        mockedConsumer = mock(BiConsumer.class);
        this.stage = stage;

        dialogPane = new VBox();
        dialogPane.setMinSize(500, 800);
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

    protected void writeIntoField(FxRobot robot, String idField, Object value, int amountToErase) {
        TestUtils.writeIntoField(robot, "#" + idField, value, amountToErase);
    }

    protected void verifyThatThereAreNoErrorsReported() {
        verifyThat("#uiItemErrors", node -> !node.isVisible());
    }

    protected void performAllCommonChecks(MenuItem item, boolean hasEepromField) {
        performAllCommonChecks(item, hasEepromField, true);
    }

    protected void performAllCommonChecks(MenuItem item, boolean hasEepromField, boolean hasFunctionField) {
        verifyThat("#idField", Predicate.not(TextField::isEditable));
        verifyThat("#idField", TextInputControlMatchers.hasText(Integer.toString(item.getId())));
        if(hasEepromField) verifyThat("#eepromField", TextInputControlMatchers.hasText(Integer.toString(item.getEepromAddress())));
        verifyThat("#nameField", TextInputControlMatchers.hasText(item.getName()));
        if(hasFunctionField) {
            verifyThat("#functionNameTextField", TextInputControlMatchers.hasText(
                    item.getFunctionName() == null ? UIMenuItem.NO_FUNCTION_DEFINED : item.getFunctionName()));
        }
        verifyThat("#uiItemErrors", (Node node)->!node.isVisible());
    }

    protected void createMainPanel(Optional<UIMenuItem<?>> uiSubItem) throws InterruptedException {
        assertTrue(uiSubItem.isPresent());

        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            BorderPane borderLayout = new BorderPane();
            borderLayout.setMinSize(500, 500);
            borderLayout.centerProperty().set(uiSubItem.get().initPanel(menuTree, getTestLocaleHandler()));
            dialogPane.getChildren().add(borderLayout);
            stage.show();
            latch.countDown();
        });
        if(!latch.await(2000, TimeUnit.MILLISECONDS)) throw new IllegalStateException("panel timeout");
    }

    protected LocaleMappingHandler getTestLocaleHandler() {
        return LocaleMappingHandler.NOOP_IMPLEMENTATION;
    }
}
