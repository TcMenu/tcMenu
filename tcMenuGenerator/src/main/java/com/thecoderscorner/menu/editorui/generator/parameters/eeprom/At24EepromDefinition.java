package com.thecoderscorner.menu.editorui.generator.parameters.eeprom;

import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.EepromDefinition;

import java.util.Optional;

import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.*;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType.*;

public class At24EepromDefinition implements EepromDefinition {
    private final int address;
    private final String pageSize;

    public At24EepromDefinition(int address, String pageSize) {
        this.address = address;
        this.pageSize = pageSize;
    }

    public int getAddress() {
        return address;
    }

    public String getPageSize() {
        return pageSize;
    }

    @Override
    public Optional<String> generateCode() {
        return Optional.of("    menuMgr.setEepromRef(&glI2cRom);");
    }

    @Override
    public Optional<String> generateGlobal() {
        return Optional.of(String.format("I2cAt24Eeprom glI2cRom(0x%02x, %s);", address, pageSize));
    }

    @Override
    public Optional<String> generateExport() {
        return Optional.empty();
    }

    @Override
    public Optional<HeaderDefinition> generateHeader() {
        return Optional.of(new HeaderDefinition("EepromAbstractionWire.h", GLOBAL, PRIORITY_NORMAL, new AlwaysApplicable()));
    }

    @Override
    public String writeToProject() {
        return String.format("at24:%d:%s", address, pageSize);
    }

    @Override
    public String toString() {
        return String.format("I2C AT24 addr=0x%02x, %s", address, pageSize);
    }
}
