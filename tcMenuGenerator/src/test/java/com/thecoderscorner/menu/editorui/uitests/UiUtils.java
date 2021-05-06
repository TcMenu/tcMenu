/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uitests;

import javafx.scene.control.Labeled;
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

    public static Predicate<Labeled> labeledFieldHasValue(String expected) {
        return (Labeled textField) -> textField.getText().equals(expected);
    }

    public static Predicate<TextField> textFieldHasValue(String expected) {
        return (TextField textField) -> textField.getText().equals(expected);
    }

}
