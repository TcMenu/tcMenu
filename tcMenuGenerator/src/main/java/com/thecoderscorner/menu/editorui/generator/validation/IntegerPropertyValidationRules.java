/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.validation;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * An integer property validator that will validate between a given range of values
 * Prefer {@link CannedPropertyValidators} when possible
 */
public class IntegerPropertyValidationRules implements PropertyValidationRules {

    private final static Pattern INT_MATCHER = Pattern.compile("(-?[\\d]+|0x[\\dABCDEFabcdef]+)");

    private final int minVal;
    private final int maxVal;

    /**
     * Create an integer validator for a range of values.
     * @param minVal min allowed
     * @param maxVal max allowed
     */
    public IntegerPropertyValidationRules(int minVal, int maxVal) {
        this.minVal = minVal;
        this.maxVal = maxVal;
    }

    @Override
    public boolean isValueValid(String value) {
        if(!INT_MATCHER.matcher(value).matches()) return false;

        int i;
        if(value.startsWith("0x")) {
            i = Integer.parseInt(value.substring(2), 16);
        }
        else {
            i = Integer.parseInt(value);
        }
        return i >= minVal && i <= maxVal;
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
        return "Int Validator accepting " + minVal + " to " + maxVal;
    }

    public int getMinVal() {
        return minVal;
    }

    public int getMaxVal() {
        return maxVal;
    }
}
