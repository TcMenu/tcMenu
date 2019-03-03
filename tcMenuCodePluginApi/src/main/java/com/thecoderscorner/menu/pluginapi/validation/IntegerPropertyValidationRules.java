/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi.validation;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class IntegerPropertyValidationRules implements PropertyValidationRules {

    private final static Pattern INT_MATCHER = Pattern.compile("(-?[\\d]+|0x[\\dABCDEFabcdef]+)");

    private final int minVal;
    private final int maxVal;

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
    public List<String> choices() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "Int Validator accepting " + minVal + " to " + maxVal;
    }

}
