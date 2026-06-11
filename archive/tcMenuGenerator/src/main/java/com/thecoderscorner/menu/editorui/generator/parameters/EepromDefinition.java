package com.thecoderscorner.menu.editorui.generator.parameters;

import com.thecoderscorner.menu.editorui.generator.parameters.eeprom.*;
import com.thecoderscorner.menu.editorui.util.StringHelper;

import java.util.Optional;

public interface EepromDefinition extends CodeGeneratorCapable {
    String writeToProject();

    static EepromDefinition readFromProject(String encoding) {
        if(StringHelper.isStringEmptyOrNull(encoding)) return new NoEepromDefinition();

        try {
            if (encoding.startsWith("avr:")) return new AVREepromDefinition();
            else if (encoding.startsWith("eeprom:")) return new ArduinoClassEepromDefinition();
            else if (encoding.startsWith("prefs:")) {
                String[] parts = encoding.split(":");
                String ns = parts[1];
                int size = Integer.parseInt(parts[2]);
                return new PreferencesEepromDefinition(ns, size);
            }
            else if (encoding.startsWith("bsp:")) {
                int memOffset = Integer.parseInt(encoding.substring(4));
                return new BspStm32EepromDefinition(memOffset);
            } else if (encoding.startsWith("at24:")) {
                String[] parts = encoding.split(":");
                int addr = Integer.parseInt(parts[1]);
                String pageSize = parts[2];
                return new At24EepromDefinition(addr, pageSize);
            }
            else return new NoEepromDefinition();
        }
        catch (Exception ex) {
            return new NoEepromDefinition();
        }
    }

    @Override
    default Optional<String> generateExport() {
        return Optional.empty();
    }
}
