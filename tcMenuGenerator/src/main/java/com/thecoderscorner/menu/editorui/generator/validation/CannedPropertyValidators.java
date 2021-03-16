/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.validation;

import com.thecoderscorner.menu.domain.MenuItem;

import java.util.Collection;

/**
 * A helper class with static methods that provides short cuts for commonly used property validators.
 */
public class CannedPropertyValidators {
    /**
     * @return a standard boolean validator with choices for true and false.
     */
    public static BooleanPropertyValidationRules boolValidator() {
        return new BooleanPropertyValidationRules();
    }

    /**
     * @return a validator useful for arduino pins
     */
    public static PinPropertyValidationRules pinValidator() {
        return new PinPropertyValidationRules(false);
    }

    /**
     * @return a pin validator that allows -1 to indicate optionality
     */
    public static PinPropertyValidationRules optPinValidator() {
        return new PinPropertyValidationRules(true);
    }

    /**
     * @param max the largest value
     * @return a zero based unsigned int validator
     */
    public static IntegerPropertyValidationRules uintValidator(int max) {
        return new IntegerPropertyValidationRules(0, max);
    }

    /**
     * @return a validator useful for checking variable names
     */
    public static StringPropertyValidationRules variableValidator() {
        return new StringPropertyValidationRules(true, 64);
    }

    /**
     * @return a general purpose string validator
     */
    public static StringPropertyValidationRules textValidator() {
        return new StringPropertyValidationRules(false, 32);
    }

    /**
     * This method returns a choices validator based on a java enum. It takes the values from such an item and converts
     * them into string choices in a combo. It also validates against these choices.
     *
     * @return a choices validator based on an enum
     * @param values the values that will both be validated against, and displayed in the combo.
     */
    public static ChoicesPropertyValidationRules choicesValidator(Collection<String> values) {
        return new ChoicesPropertyValidationRules(values);
    }

    public static FontPropertyValidationRules fontValidator() {
        return new FontPropertyValidationRules();
    }

    /**
     * This method returns a validator based on menu items, in this case all menu items
     * @return a new validator to validate on menu items.
     */
    public static MenuItemValidationRules menuItemValidatorForAllItems() {
        return new MenuItemValidationRules((i) -> true);
    }

    /**
     * This method returns a validator based on menu items, in this case menu items that match the class passed in.
     * @param cls the class to match. EG SubMenuItem.class for all sub menus.
     * @return a new validator to validate on menu items.
     */    public static MenuItemValidationRules menuItemValidatorForSpecifcType(Class<? extends MenuItem> cls) {
        return new MenuItemValidationRules((i) -> cls.equals(i.getClass()));
    }
}
