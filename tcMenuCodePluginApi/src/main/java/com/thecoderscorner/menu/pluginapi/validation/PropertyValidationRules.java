package com.thecoderscorner.menu.pluginapi.validation;

import java.util.Collection;

public interface PropertyValidationRules {
    boolean isValueValid(String value);

    boolean hasChoices();
    Collection<String> choices();
}
