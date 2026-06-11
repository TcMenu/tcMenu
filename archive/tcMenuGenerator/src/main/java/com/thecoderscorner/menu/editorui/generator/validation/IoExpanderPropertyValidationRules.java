/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.validation;

import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;

import java.util.Collections;
import java.util.List;

/**
 * A boolean property validator that has choices for true and false. Prefer {@link CannedPropertyValidators} when possible
 */
public class IoExpanderPropertyValidationRules implements PropertyValidationRules {

    private CurrentEditorProject project;

    public void initialise(CurrentEditorProject project) {
        this.project = project;
    }

    @Override
    public boolean isValueValid(String value) {

        try {
            var expanderCollection = project.getGeneratorOptions().getExpanderDefinitions();
            return expanderCollection.getAllExpanders().stream().anyMatch(io -> io.getId().equals(value));
        }
        catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean hasChoices() {
        return false;
    }

    @Override
    public List<ChoiceDescription> choices() {
        return Collections.emptyList();
    }

    @Override
    public ChoiceDescription getChoiceFor(String latestValue) {
        return null;
    }

    @Override
    public String toString() {
        return "IoExpander Validator";
    }

    public String getNameOfCurrentChoice(String latestValue) {
        var expanderCollection = project.getGeneratorOptions().getExpanderDefinitions();
        var expander = expanderCollection.getAllExpanders().stream()
                .filter(io -> io.getId().equals(latestValue))
                .findFirst().orElse(expanderCollection.getInternalExpander());
        return expander.getNicePrintableName();
    }
}
