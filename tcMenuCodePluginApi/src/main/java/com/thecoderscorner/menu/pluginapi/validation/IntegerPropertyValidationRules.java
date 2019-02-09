package com.thecoderscorner.menu.pluginapi.validation;

import java.util.Collection;
import java.util.Collections;
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
    public Collection<String> choices() {
        return Collections.emptyList();
    }
}
