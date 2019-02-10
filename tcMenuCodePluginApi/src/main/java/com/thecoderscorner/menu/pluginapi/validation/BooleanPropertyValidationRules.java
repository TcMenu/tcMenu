/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

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
