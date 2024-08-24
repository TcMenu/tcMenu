package com.thecoderscorner.menu.editorui.generator.parameters;

import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.eeprom.*;
import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator.LINE_BREAK;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType.GLOBAL;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.PRIORITY_NORMAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EepromDefinitionTest {
    @Test
    public void testNoEepromDefinition() {
        var definition = EepromDefinition.readFromProject("");
        assertThat(definition).isInstanceOf(NoEepromDefinition.class);
        assertEquals("", definition.writeToProject());
        assertThat(definition.generateHeader()).isEmpty();
        assertThat(definition.generateGlobal()).isEmpty();
        assertThat(definition.generateCode()).isEmpty();
        assertEquals("No / Custom EEPROM", definition.toString());
    }

    @Test
    public void testAvrEepromDefinition() {
        var definition = EepromDefinition.readFromProject("avr:");
        assertThat(definition).isInstanceOf(AVREepromDefinition.class);
        assertEquals("avr:", definition.writeToProject());
        assertThat(definition.generateHeader()).contains(new HeaderDefinition("EepromAbstraction.h", GLOBAL, PRIORITY_NORMAL, new AlwaysApplicable()));
        assertThat(definition.generateGlobal()).contains("AvrEeprom glAvrRom;");
        assertThat(definition.generateCode()).contains("    menuMgr.setEepromRef(&glAvrRom);");
        assertEquals("Direct AVR EEPROM functions", definition.toString());
    }

    @Test
    public void testArduinoClassEepromDefinition() {
        var definition = EepromDefinition.readFromProject("eeprom:");
        assertThat(definition).isInstanceOf(ArduinoClassEepromDefinition.class);
        assertEquals("eeprom:", definition.writeToProject());
        assertThat(definition.generateHeader()).contains(new HeaderDefinition("ArduinoEEPROMAbstraction.h", GLOBAL, PRIORITY_NORMAL, new AlwaysApplicable()));
        assertThat(definition.generateGlobal()).contains("ArduinoEEPROMAbstraction glArduinoEeprom(&EEPROM);");
        assertThat(definition.generateCode()).contains("    menuMgr.setEepromRef(&glArduinoEeprom);");
        assertEquals("Arduino EEPROM class", definition.toString());
    }

    @Test
    public void testAt24EepromDefinition() {
        At24EepromDefinition definition = (At24EepromDefinition) EepromDefinition.readFromProject("at24:32:PAGESIZE_AT24C256");
        assertThat(definition).isInstanceOf(At24EepromDefinition.class);
        assertEquals("at24:32:PAGESIZE_AT24C256", definition.writeToProject());
        assertEquals(32, definition.getAddress());
        assertEquals("PAGESIZE_AT24C256", definition.getPageSize());
        assertThat(definition.generateHeader()).contains(new HeaderDefinition("EepromAbstractionWire.h", GLOBAL, PRIORITY_NORMAL, new AlwaysApplicable()));
        assertThat(definition.generateGlobal()).contains("I2cAt24Eeprom glI2cRom(0x20, PAGESIZE_AT24C256);");
        assertThat(definition.generateCode()).contains("    menuMgr.setEepromRef(&glI2cRom);");
        assertEquals("I2C AT24 addr=0x20, PAGESIZE_AT24C256", definition.toString());
    }

    @Test
    public void testBspStEepromDefinition() {
        BspStm32EepromDefinition definition = (BspStm32EepromDefinition) EepromDefinition.readFromProject("bsp:256");
        assertThat(definition).isInstanceOf(BspStm32EepromDefinition.class);
        assertEquals("bsp:256", definition.writeToProject());
        assertEquals(256, definition.getOffset());
        assertThat(definition.generateHeader()).contains(new HeaderDefinition("mbed/HalStm32EepromAbstraction.h", GLOBAL, PRIORITY_NORMAL, new AlwaysApplicable()));
        assertThat(definition.generateGlobal()).contains("HalStm32EepromAbstraction glBspRom;");
        assertThat(definition.generateCode()).contains("    glBspRom.initialise(256);" + LINE_BREAK + "    menuMgr.setEepromRef(&glBspRom);");
        assertEquals("STM32 BSP offset=256", definition.toString());
    }
}