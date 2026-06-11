package com.thecoderscorner.menu.editorui.generator.parameters.eeprom;

import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.EepromDefinition;

import java.util.Optional;

public class NoEepromDefinition implements EepromDefinition {
    @Override
    public Optional<String> generateCode() {
        return Optional.empty();
    }

    @Override
    public Optional<String> generateGlobal() {
        return Optional.empty();
    }

    @Override
    public Optional<HeaderDefinition> generateHeader() {
        return Optional.empty();
    }

    @Override
    public String writeToProject() {
        return "";
    }

    @Override
    public String toString() {
        return "No / Custom EEPROM";
    }
}
