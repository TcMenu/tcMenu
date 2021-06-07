/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.validation;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A property validator based on a list of choices.
 */
public class ChoicesPropertyValidationRules implements PropertyValidationRules {

    final Map<String, ChoiceDescription> enumValues = new HashMap<>();
    final List<ChoiceDescription> enumNaturalOrder = new ArrayList<>();
    private final String defaultValue;

    /**
     * Create an instance with an array of values, generally from an Enum.
     * @param values the value array
     */
    public ChoicesPropertyValidationRules(Collection<ChoiceDescription> values, String initialValue) {
        defaultValue = initialValue;
        for(var ch : values) {
            enumValues.put(ch.getChoiceValue(), ch);
            enumNaturalOrder.add(ch);
        }
    }

    public ChoicesPropertyValidationRules(String initialValue) {
        defaultValue = initialValue;
    }

    @Override
    public boolean isValueValid(String value) {
        return enumValues.containsKey(value);
    }

    @Override
    public boolean hasChoices() {
        return true;
    }

    @Override
    public List<ChoiceDescription> choices() {
        return new ArrayList<>(List.copyOf(enumNaturalOrder));
    }

    @Override
    public String toString() {
        return "Choice Validator accepting " + enumValues.values().stream()
                .map(ChoiceDescription::getChoiceValue)
                .collect(Collectors.joining(", "));
    }

    public ChoiceDescription getChoiceFor(String val) {
        return enumValues.get(val);
    }
}
