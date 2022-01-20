/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.validation;

import com.thecoderscorner.menu.editorui.util.StringHelper;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static com.thecoderscorner.menu.editorui.generator.validation.StringPropertyValidationRules.VAR_PATTERN;

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
        if(StringHelper.isStringEmptyOrNull(value)) return false;

        // short cut to allow -1 when it's optional.
        if(optional && value.equals("-1")) return true;

        try {
            var pinInt = Integer.parseInt(value);
            if(pinInt == -1 && !optional) return false;
            return (pinInt >= -1 && pinInt < 256);
        }
        catch(Exception ex) {
            // not an integer
        }

        // lastly is it a variable or pin definition, EG: A0, PE_4 etc.
        return VAR_PATTERN.matcher(value).matches();
    }

    @Override
    public boolean hasChoices() {
        return false;
    }

    @Override
    public List<ChoiceDescription> choices() {
        return Collections.emptyList();
    }

    @Override
    public ChoiceDescription getChoiceFor(String latestValue) {
        return null;
    }

    @Override
    public String toString() {
        return "Arduino Pin Validator";
    }

}
