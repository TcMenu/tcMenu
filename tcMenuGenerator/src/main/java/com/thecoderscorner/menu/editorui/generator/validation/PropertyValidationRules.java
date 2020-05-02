/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.validation;

import java.util.List;

/**
 * These rules define how a given {@link com.thecoderscorner.menu.pluginapi.CreatorProperty} can be modified within
 * the designer UI, by restricting the values that can be input. If there are choices then a combo will be displayed
 * instead of a text field.
 */
public interface PropertyValidationRules {
    /**
     * Checks if the value is valid
     * @param value the value to check
     * @return if it is valid
     */
    boolean isValueValid(String value);

    /**
     * @return true if this is a choice property
     */
    boolean hasChoices();

    /**
     * @return a list of choices that will be presented in a combo.
     */
    List<String> choices();
}
