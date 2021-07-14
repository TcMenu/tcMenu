package com.thecoderscorner.menu.editorui.generator.parameters.eeprom;

import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.EepromDefinition;

import java.util.Optional;

import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.*;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType.*;

public class AVREepromDefinition implements EepromDefinition {
    @Override
    public Optional<String> generateCode() {
        return Optional.of("    menuMgr.setEepromRef(&glAvrRom);");
    }

    @Override
    public Optional<String> generateGlobal() {
        return Optional.of("AvrEeprom glAvrRom;");
    }

    @Override
    public Optional<HeaderDefinition> generateHeader() {
        return Optional.of(new HeaderDefinition("EepromAbstraction.h", GLOBAL, PRIORITY_NORMAL, new AlwaysApplicable()));
    }

    @Override
    public String writeToProject() {
        return "avr:";
    }

    @Override
    public String toString() {
        return "Direct AVR EEPROM functions";
    }
}
