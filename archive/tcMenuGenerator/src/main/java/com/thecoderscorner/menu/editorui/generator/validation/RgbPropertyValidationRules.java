package com.thecoderscorner.menu.editorui.generator.validation;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Ensures that RGB values are either a direct RGB(r,g,b) value or a variable name.
 */
public class RgbPropertyValidationRules implements PropertyValidationRules {
    private final static Pattern RGB_PATTERN = Pattern.compile("^RGB\\([0-9]{1,3},[0-9]{1,3},[0-9]{1,3}\\)$");
    public final static Pattern VAR_PATTERN = Pattern.compile("^[\\p{L}\\p{N}_]*$");

    @Override
    public boolean isValueValid(String value) {
        return RGB_PATTERN.matcher(value).matches() || VAR_PATTERN.matcher(value).matches();
    }

    @Override
    public boolean hasChoices() {
        return false;
    }

    @Override
    public List<ChoiceDescription> choices() {
        return List.of();
    }

    @Override
    public ChoiceDescription getChoiceFor(String latestValue) {
        return null;
    }

    @Override
    public String toString() {
        return "RGB Validator";
    }
}
