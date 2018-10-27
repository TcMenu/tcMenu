package com.thecoderscorner.menu.editorui.uitests;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.testfx.api.FxRobot;

import java.util.function.Predicate;

import static javafx.scene.input.KeyCombination.ModifierValue.DOWN;
import static javafx.scene.input.KeyCombination.ModifierValue.UP;

public class UiUtils {

    public static void pushCtrlAndKey(FxRobot robot, KeyCode code) {
        robot.push(new KeyCodeCombination(code, UP, UP, UP, UP, DOWN));

    }

    public static Predicate<TextField> textFieldHasValue(String expected) {
        return (TextField textField) -> textField.getText().equals(expected);
    }

}
