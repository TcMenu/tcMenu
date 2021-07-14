package com.thecoderscorner.menu.editorui.generator.parameters.eeprom;

import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.EepromDefinition;

import java.util.Optional;

import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.*;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType.*;

public class ArduinoClassEepromDefinition implements EepromDefinition {
    @Override
    public Optional<String> generateCode() {
        return Optional.of("    menuMgr.setEepromRef(&glArduinoEeprom);");
    }

    @Override
    public Optional<String> generateGlobal() {
        return Optional.of("ArduinoEEPROMAbstraction glArduinoEeprom(&EEPROM);");
    }

    @Override
    public Optional<HeaderDefinition> generateHeader() {
        return Optional.of(new HeaderDefinition(("ArduinoEEPROMAbstraction.h"), GLOBAL, PRIORITY_NORMAL, new AlwaysApplicable()));
    }

    @Override
    public String writeToProject() {
        return "eeprom:";
    }

    @Override
    public String toString() {
        return "Arduino EEPROM class";
    }
}
