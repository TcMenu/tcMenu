package com.thecoderscorner.menu.editorui.generator.parameters;

import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.expander.CustomDeviceExpander;
import com.thecoderscorner.menu.editorui.generator.parameters.expander.InternalDeviceExpander;
import com.thecoderscorner.menu.editorui.generator.parameters.expander.Mcp23017DeviceExpander;
import com.thecoderscorner.menu.editorui.generator.parameters.expander.Pcf8574DeviceExpander;
import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType.GLOBAL;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.PRIORITY_NORMAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class IoExpanderDefinitionTest {
    @Test
    public void testCustomDeviceExpander() {
        var device = IoExpanderDefinition.fromString("customIO:superDuper").orElseThrow();
        assertThat(device).isInstanceOf(CustomDeviceExpander.class);
        var custom = (CustomDeviceExpander)device;
        assertEquals("superDuper", custom.getVariableName());
        assertEquals("superDuper", custom.getId());
        assertEquals("Custom IO: superDuper", custom.getNicePrintableName());
        assertThat(custom.generateCode()).isEmpty();
        assertThat(custom.generateGlobal()).isEmpty();
        assertEquals("extern IoAbstractionRef superDuper;", custom.generateExport().orElseThrow());

        assertThat(custom.generateHeader()).contains(new HeaderDefinition("IoAbstraction.h", GLOBAL, PRIORITY_NORMAL, new AlwaysApplicable()));
        assertEquals("customIO:superDuper", custom.toString());

        var device2 = IoExpanderDefinition.fromString("anotherExpander").orElseThrow();
        assertThat(device2).isInstanceOf(CustomDeviceExpander.class);
        assertEquals("anotherExpander", device2.getVariableName());
        assertNotEquals(custom, device2);

        var device3 = IoExpanderDefinition.fromString("customIO:superDuper").orElseThrow();
        assertEquals(custom, device3);

    }

    @Test
    public void testInternalDeviceExpander() {
        var device = IoExpanderDefinition.fromString("deviceIO:").orElseThrow();
        assertThat(device).isInstanceOf(InternalDeviceExpander.class);
        var custom = (InternalDeviceExpander)device;
        assertEquals("internalDigitalIo()", custom.getVariableName());
        assertEquals("devicePins", custom.getId());
        assertEquals("Device pins", custom.getNicePrintableName());
        assertThat(custom.generateCode()).isEmpty();
        assertThat(custom.generateGlobal()).isEmpty();
        assertEquals("IoAbstraction.h", custom.generateHeader().orElseThrow().getHeaderName());
        assertEquals(GLOBAL, custom.generateHeader().orElseThrow().getHeaderType());
        assertEquals("deviceIO:", custom.toString());

        var device2 = IoExpanderDefinition.fromString("").orElseThrow();
        assertThat(device2).isInstanceOf(InternalDeviceExpander.class);
        assertEquals(custom, device2);
    }

    @Test
    public void testPcf8574Expander() {
        var device = IoExpanderDefinition.fromString("pcf8574:io8574:32:-1").orElseThrow();
        assertThat(device).isInstanceOf(Pcf8574DeviceExpander.class);
        var custom = (Pcf8574DeviceExpander)device;
        assertEquals("ioexp_io8574", custom.getVariableName());
        assertEquals("io8574", custom.getId());
        assertEquals("PCF8574(0x20, -1)", custom.getNicePrintableName());
        assertThat(custom.generateCode()).isEmpty();
        assertEquals("IoAbstractionRef ioexp_io8574 = ioFrom8574(0x20, -1);", custom.generateGlobal().orElseThrow());
        assertEquals("extern IoAbstractionRef ioexp_io8574;", custom.generateExport().orElseThrow());

        assertEquals("IoAbstractionWire.h", custom.generateHeader().orElseThrow().getHeaderName());
        assertEquals(GLOBAL, custom.generateHeader().orElseThrow().getHeaderType());
        assertEquals("pcf8574:io8574:32:-1", custom.toString());
    }

    @Test
    public void testMcp23017Expander() {
        var device = IoExpanderDefinition.fromString("mcp23017:io23017:32:77").orElseThrow();
        assertThat(device).isInstanceOf(Mcp23017DeviceExpander.class);
        var custom = (Mcp23017DeviceExpander)device;
        assertEquals("ioexp_io23017", custom.getVariableName());
        assertEquals("io23017", custom.getId());
        assertEquals("MCP23017(0x20, 77)", custom.getNicePrintableName());
        assertThat(custom.generateCode()).isEmpty();
        assertEquals("IoAbstractionRef ioexp_io23017 = ioFrom23017(0x20, ACTIVE_LOW_OPEN, 77);", custom.generateGlobal().orElseThrow());
        assertEquals("extern IoAbstractionRef ioexp_io23017;", custom.generateExport().orElseThrow());
        assertEquals("IoAbstractionWire.h", custom.generateHeader().orElseThrow().getHeaderName());
        assertEquals(GLOBAL, custom.generateHeader().orElseThrow().getHeaderType());
        assertEquals("mcp23017:io23017:32:77", custom.toString());
    }}