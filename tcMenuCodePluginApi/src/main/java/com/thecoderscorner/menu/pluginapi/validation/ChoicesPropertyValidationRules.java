/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi.validation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class ChoicesPropertyValidationRules implements PropertyValidationRules {

    final private Set<String> enumValues;

    public <T extends Enum> ChoicesPropertyValidationRules(T[] values) {
        enumValues = Arrays.stream(values)
                .map(Enum::toString).collect(Collectors.toSet());
    }

    @Override
    public boolean isValueValid(String value) {
        return enumValues.contains(value);
    }

    @Override
    public boolean hasChoices() {
        return true;
    }

    @Override
    public Collection<String> choices() {
        return enumValues;
    }
}
