/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.validation;

import com.thecoderscorner.menu.editorui.generator.parameters.FontDefinition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A boolean property validator that has choices for true and false. Prefer {@link CannedPropertyValidators} when possible
 */
public class FontPropertyValidationRules implements PropertyValidationRules {
    @Override
    public boolean isValueValid(String value) {

        try {
            FontDefinition.fromString(value);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean hasChoices() {
        return false;
    }

    @Override
    public List<String> choices() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "Font Validator accepting font definitions";
    }
}
