package com.thecoderscorner.menu.editorui.generator.parameters.eeprom;

import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.EepromDefinition;

import java.util.Optional;

import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType.GLOBAL;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.PRIORITY_NORMAL;

public class PreferencesEepromDefinition implements EepromDefinition {
    private final String romNamespace;
    private final int size;

    public PreferencesEepromDefinition(String ns, int size) {
        this.romNamespace = ns;
        this.size = size;
    }

    @Override
    public Optional<String> generateCode() {
        return Optional.of("    menuMgr.setEepromRef(&glEspRom);");
    }

    @Override
    public Optional<String> generateGlobal() {
        return Optional.of("EspPreferencesEeprom glEspRom(\"" + romNamespace + "\", " + size + ");");
    }

    @Override
    public Optional<String> generateExport() {
        return Optional.of("extern EspPreferencesEeprom glEspRom;");
    }

    @Override
    public Optional<HeaderDefinition> generateHeader() {
        return Optional.of(new HeaderDefinition("esp32/EspPreferencesEeprom.h", GLOBAL, PRIORITY_NORMAL, new AlwaysApplicable()));
    }

    @Override
    public String writeToProject() {
        return "prefs:" + romNamespace + ":" + size;
    }

    public String getRomNamespace() {
        return romNamespace;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "ESP32 Preferences EEPROM functions";
    }
}
