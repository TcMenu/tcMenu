package com.thecoderscorner.menu.pluginapi.validation;

import java.util.Arrays;
import java.util.Collection;

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
    public Collection<String> choices() {
        return Arrays.asList("true", "false");
    }
}
