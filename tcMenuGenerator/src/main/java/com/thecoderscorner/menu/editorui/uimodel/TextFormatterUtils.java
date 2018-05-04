/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.uimodel;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.util.regex.Pattern;

public class TextFormatterUtils {
    private static final Pattern INTEGER_MATCH = Pattern.compile("[-]?[\\d]*");
    private static final Pattern VARIABLE_NAME_MATCH = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

    public static void applyIntegerFormatToField(TextField field) {
        field.setTextFormatter( new TextFormatter<>(c ->
        {
            if (c.getControlNewText().isEmpty()) {
                return c;
            }

            return (INTEGER_MATCH.matcher(c.getControlNewText()).matches()) ? c : null;
        }));
    }

    public static void applyVariableNameFormatToField(TextField field) {
        field.setTextFormatter( new TextFormatter<>(c ->
        {
            if (c.getControlNewText().isEmpty()) {
                return c;
            }

            return (VARIABLE_NAME_MATCH.matcher(c.getControlNewText()).matches()) ? c : null;
        }));
    }
}
