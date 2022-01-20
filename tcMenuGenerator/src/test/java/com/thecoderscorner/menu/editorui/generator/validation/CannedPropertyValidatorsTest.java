/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.validation;

import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptionsBuilder;
import com.thecoderscorner.menu.editorui.generator.parameters.FontDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinitionCollection;
import com.thecoderscorner.menu.editorui.generator.parameters.expander.CustomDeviceExpander;
import com.thecoderscorner.menu.editorui.generator.parameters.expander.InternalDeviceExpander;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.generator.parameters.FontDefinition.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CannedPropertyValidatorsTest {

    @Test
    public void testBooleanValidator() {
        BooleanPropertyValidationRules validator = CannedPropertyValidators.boolValidator();
        assertTrue(validator.hasChoices());
        assertThat(validator.choices()).containsExactlyInAnyOrder(new ChoiceDescription("true"), new ChoiceDescription("false"));
        assertTrue(validator.isValueValid("true"));
        assertTrue(validator.isValueValid("false"));
        assertFalse(validator.isValueValid("invalid"));
    }

    @Test
    public void testPinIntegerValidator() {
        PinPropertyValidationRules validator = CannedPropertyValidators.pinValidator();
        assertFalse(validator.hasChoices());
        assertThat(validator.choices()).isEmpty();
        assertTrue(validator.isValueValid("1"));
        assertTrue(validator.isValueValid("254"));
        assertTrue(validator.isValueValid("A0"));
        assertFalse(validator.isValueValid("1024"));
        assertFalse(validator.isValueValid("-1"));

        validator = CannedPropertyValidators.optPinValidator();
        assertTrue(validator.isValueValid("-1"));
        assertTrue(validator.isValueValid("1"));
        assertTrue(validator.isValueValid("254"));
        assertFalse(validator.isValueValid("1024"));
        assertTrue(validator.isValueValid("A12"));
        assertTrue(validator.isValueValid("A1200"));
        assertTrue(validator.isValueValid("PF_12"));
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
        assertFalse(validator.isValueValid("invalid_because_text_is_too_long_for_the_field_and_really_must_be_very_long_indeed_to_fail"));
    }

    @Test
    public void testChoicesValidator() {
        ChoicesPropertyValidationRules validator = CannedPropertyValidators.choicesValidator(List.of(
                new ChoiceDescription("VALUE_1"),
                new ChoiceDescription("VALUE_2", "value 2")
        ), "VALUE_1");

        assertTrue(validator.hasChoices());
        assertThat(validator.choices().stream()
                .map(ChoiceDescription::getChoiceValue)
                .collect(Collectors.toList())).containsExactly("VALUE_1", "VALUE_2");

        assertFalse(validator.isValueValid(""));
        assertFalse(validator.isValueValid("value 2"));
        assertTrue(validator.isValueValid("VALUE_1"));
        assertTrue(validator.isValueValid("VALUE_2"));

        assertEquals("VALUE_1", validator.choices().get(0).getChoiceValue());
        assertEquals("VALUE_1", validator.choices().get(0).getChoiceDesc());
        assertEquals("VALUE_2", validator.choices().get(1).getChoiceValue());
        assertEquals("value 2", validator.choices().get(1).getChoiceDesc());
    }

    @Test
    public void testFontValidator() {
        FontPropertyValidationRules validator = CannedPropertyValidators.fontValidator();

        assertFalse(validator.hasChoices());
        assertThat(validator.choices()).isEmpty();
        assertFalse(validator.isValueValid("asdfasdf"));
        assertTrue(validator.isValueValid(new FontDefinition(FontMode.DEFAULT_FONT, "", 0).toString()));
    }

    @Test
    public void testIoExpanderValidator() {
        var collection = new IoExpanderDefinitionCollection(List.of(new CustomDeviceExpander("helloWorld")));
        var codeOptions = new CodeGeneratorOptionsBuilder().withExpanderDefinitions(collection).codeOptions();
        var project = mock(CurrentEditorProject.class);
        when(project.getGeneratorOptions()).thenReturn(codeOptions);

        var validator = CannedPropertyValidators.ioExpanderValidator();
        validator.initialise(project);

        assertFalse(validator.hasChoices());
        assertThat(validator.choices()).isEmpty();
        assertFalse(validator.isValueValid("notFound"));
        assertTrue(validator.isValueValid(InternalDeviceExpander.DEVICE_ID));
        assertTrue(validator.isValueValid("helloWorld"));
        assertEquals("Device pins", validator.getNameOfCurrentChoice(InternalDeviceExpander.DEVICE_ID));
        assertEquals("IoExpander Validator", validator.toString());
    }
}