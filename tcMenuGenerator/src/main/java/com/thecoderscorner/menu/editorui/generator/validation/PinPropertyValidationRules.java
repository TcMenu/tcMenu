/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.validation;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * An integer pin property validator that will validate that the value is a pin number or pin variable.
 * Prefer {@link CannedPropertyValidators} when possible
 */
public class PinPropertyValidationRules implements PropertyValidationRules {

    private final static Pattern INT_MATCHER = Pattern.compile("A?([\\d]+)");
    private final boolean optional;

    /**
     * Create an integer validator for a range of values.
     * @param optional true if the pin is optional otherwise false
     */
    public PinPropertyValidationRules(boolean optional) {
        this.optional = optional;
    }

    @Override
    public boolean isValueValid(String value) {
        // short cut to allow -1 when it's optional.
        if(optional && value.equals("-1")) return true;

        // otherwise we must match the pattern and the pin be in range.
        var matcher = INT_MATCHER.matcher(value);
        if(matcher.matches()) {
            var match = Integer.parseInt(matcher.group(1));
            return match < 255;
        }

        return false;
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
        return "Arduino Pin Validator";
    }

}
