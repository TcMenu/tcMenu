/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uimodel;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.util.regex.Pattern;

public class TextFormatterUtils {
    public static final Pattern INTEGER_MATCH = Pattern.compile("[-]?[\\d]*");
    public static final Pattern FLOAT_MATCH = Pattern.compile("[-]?[\\de.]*");
    public static final Pattern PORTABLE_COLOR_MATCH = Pattern.compile("#[0-9A-Fa-f]*");

    public static void applyIntegerFormatToField(TextField field) {
        field.setTextFormatter( new TextFormatter<>(c ->
        {
            if (c.getControlNewText().isEmpty()) {
                return c;
            }

            return (INTEGER_MATCH.matcher(c.getControlNewText()).matches()) ? c : null;
        }));
    }

    public static void applyFormatToField(TextField field, Pattern format) {
        field.setTextFormatter( new TextFormatter<>(c ->
        {
            if (c.getControlNewText().isEmpty()) {
                return c;
            }

            return (format.matcher(c.getControlNewText()).matches()) ? c : null;
        }));
    }
}
