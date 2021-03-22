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
 * A property validator that can validate string fields, prefer using {@link CannedPropertyValidators} when possible
 */
public class StringPropertyValidationRules implements PropertyValidationRules {
    public final static Pattern STR_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\s\\-_*%()]*$");
    public final static Pattern VAR_PATTERN = Pattern.compile("^[\\p{L}\\p{N}_]*$");

    private boolean variable;
    private int maxLen;

    /**
     * Create a string validator that can also be used for variables
     * @param variable true if a variable, otherwise false
     * @param maxLen the maximum allowed length
     */
    public StringPropertyValidationRules(boolean variable, int maxLen) {
        this.variable = variable;
        this.maxLen = maxLen;
    }

    @Override
    public boolean isValueValid(String value) {
        if(value.length() > maxLen) return false;

        if(variable)
            return VAR_PATTERN.matcher(value).matches();
        else
            return STR_PATTERN.matcher(value).matches();

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
        return (variable ? "Variable name ":"String ") + "validation (max length " + maxLen + ")";
    }
}
