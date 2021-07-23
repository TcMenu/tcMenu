package com.thecoderscorner.menu.editorui.generator.parameters.eeprom;

import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.EepromDefinition;

import java.util.Optional;

import static com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator.LINE_BREAK;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType.GLOBAL;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.PRIORITY_NORMAL;

public class BspStm32EepromDefinition implements EepromDefinition {
    private final int offset;

    public BspStm32EepromDefinition(int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public Optional<String> generateCode() {
        return Optional.of("    glBspRom.initialise(" + offset + ");" + LINE_BREAK
                + "    menuMgr.setEepromRef(&glBspRom);");
    }

    @Override
    public Optional<String> generateGlobal() {
        return Optional.of("HalStm32EepromAbstraction glBspRom;");
    }

    @Override
    public Optional<HeaderDefinition> generateHeader() {
        return Optional.of(new HeaderDefinition("mbed/HalStm32EepromAbstraction.h", GLOBAL, PRIORITY_NORMAL, new AlwaysApplicable()));
    }

    @Override
    public String writeToProject() {
        return "bsp:" + offset;
    }

    @Override
    public String toString() {
        return "STM32 BSP offset=" + offset;
    }
}
