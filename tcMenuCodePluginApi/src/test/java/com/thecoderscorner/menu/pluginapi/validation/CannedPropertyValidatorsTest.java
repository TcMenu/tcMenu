/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CannedPropertyValidatorsTest {

    @Test
    public void testBooleanValidator() {
        BooleanPropertyValidationRules validator = CannedPropertyValidators.boolValidator();
        assertTrue(validator.hasChoices());
        assertThat(validator.choices()).contains("true", "false");
        assertTrue(validator.isValueValid("true"));
        assertTrue(validator.isValueValid("false"));
        assertFalse(validator.isValueValid("invalid"));
    }

    @Test
    public void testPinIntegerValidator() {
        IntegerPropertyValidationRules validator = CannedPropertyValidators.pinValidator();
        assertFalse(validator.hasChoices());
        assertThat(validator.choices()).isEmpty();
        assertTrue(validator.isValueValid("1"));
        assertTrue(validator.isValueValid("254"));
        assertFalse(validator.isValueValid("1024"));
        assertFalse(validator.isValueValid("-1"));

        validator = CannedPropertyValidators.optPinValidator();
        assertTrue(validator.isValueValid("1"));
        assertTrue(validator.isValueValid("254"));
        assertFalse(validator.isValueValid("1024"));
        assertTrue(validator.isValueValid("-1"));
    }

    @Test
    public void testUIntValidator() {
        IntegerPropertyValidationRules validator = CannedPropertyValidators.uintValidator(40);
        assertFalse(validator.hasChoices());
        assertThat(validator.choices()).isEmpty();
        assertTrue(validator.isValueValid("0"));
        assertTrue(validator.isValueValid("40"));
        assertFalse(validator.isValueValid("42"));
        assertFalse(validator.isValueValid("-1"));
        assertTrue(validator.isValueValid("0x20"));
        assertFalse(validator.isValueValid("0x50"));
        assertFalse(validator.isValueValid("0x"));
    }


    @Test
    public void testStringValidator() {
        StringPropertyValidationRules validator = CannedPropertyValidators.textValidator();
        assertFalse(validator.hasChoices());
        assertThat(validator.choices()).isEmpty();
        assertTrue(validator.isValueValid(""));
        assertTrue(validator.isValueValid("This is text"));
        assertFalse(validator.isValueValid("Invalid char \""));
        assertFalse(validator.isValueValid("Invalid because the text is too long for the field"));
    }

    @Test
    public void testVariableValidator() {
        StringPropertyValidationRules validator = CannedPropertyValidators.variableValidator();
        assertFalse(validator.hasChoices());
        assertThat(validator.choices()).isEmpty();
        assertTrue(validator.isValueValid(""));
        assertTrue(validator.isValueValid("variable_name01"));
        assertFalse(validator.isValueValid("Invalid Space"));
        assertFalse(validator.isValueValid("invalid_because_text_is_too_long_for_the_field"));
    }

    @Test
    public void testChoicesValidator() {

    }
}