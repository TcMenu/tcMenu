/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.validation;

import java.util.Arrays;
import java.util.List;

/**
 * A boolean property validator that has choices for true and false. Prefer {@link CannedPropertyValidators} when possible
 */
public class BooleanPropertyValidationRules implements PropertyValidationRules {
    @Override
    public boolean isValueValid(String value) {
        return value.equals("true") || value.equals("false");
    }

    @Override
    public boolean hasChoices() {
        return true;
    }

    @Override
    public List<ChoiceDescription> choices() {
        return Arrays.asList(new ChoiceDescription("true"), new ChoiceDescription("false"));
    }

    public ChoiceDescription getChoiceFor(String latestValue) {
        return new ChoiceDescription(latestValue);
    }


    @Override
    public String toString() {
        return "Boolean Validator accepting true, false";
    }
}
