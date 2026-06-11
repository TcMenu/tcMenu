package com.thecoderscorner.menu.editorui.generator.validation;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PropValidationInfo {
    public enum PropertyValidationMode {
        BOOLEAN, FONT, OPTIONAL_PIN, PIN, INT_WITH_RANGE, VARIABLE, TEXT, CHOICES,
        MENUITEM, IO_EXPANDER, RGB, SEPARATOR
    }
    private PropertyValidationMode mode;
    private int min;
    private int max;
    private String menuItemFilter;
    private List<ChoiceDescription> choices;
}