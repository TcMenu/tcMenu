package com.thecoderscorner.menu.pluginapi.validation;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

public class StringPropertyValidationRules implements PropertyValidationRules {
    private final static Pattern STR_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\s\\-_*%()]*$");
    private final static Pattern VAR_PATTERN = Pattern.compile("^[\\p{L}\\p{N}_]*$");

    private boolean variable;
    private int maxLen;

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
    public Collection<String> choices() {
        return Collections.emptyList();
    }
}
