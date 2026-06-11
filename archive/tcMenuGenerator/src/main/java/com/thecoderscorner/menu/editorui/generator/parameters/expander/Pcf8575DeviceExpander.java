package com.thecoderscorner.menu.editorui.generator.parameters.expander;

import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinition;

import java.util.Optional;

import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType.GLOBAL;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.PRIORITY_NORMAL;

public class Pcf8575DeviceExpander extends IoExpanderDefinition {
    private final int i2cAddress;
    private final String intPin;
    private final String name;
    private final boolean invertedLogic;

    public Pcf8575DeviceExpander(String name, int i2cAddress, String intPin, boolean inverted) {
        this.i2cAddress = i2cAddress;
        this.intPin = intPin;
        this.name = name;
        this.invertedLogic = inverted;
    }

    @Override
    public String getNicePrintableName() {
        var inv = invertedLogic ? "!" : "";
        return String.format("%sPCF8575(0x%02x, %s)", inv, i2cAddress, intPin);
    }

    @Override
    public String getVariableName() {
        return "ioexp_" + name;
    }

    @Override
    public String getId() {
        return name;
    }

    public int getI2cAddress() {
        return i2cAddress;
    }

    public String getIntPin() {
        return intPin;
    }

    public boolean isInvertedLogic() {
        return invertedLogic;
    }

    @Override
    public String toString() {
        return "pcf8575:" + name + ":" + i2cAddress + ":" + intPin + ":" + invertedLogic;
    }

    @Override
    public Optional<String> generateCode() {
        return Optional.empty();
    }

    @Override
    public Optional<String> generateGlobal() {
        var invertedCode = invertedLogic ? ", true" : "";
        return Optional.of(String.format("IoAbstractionRef ioexp_%s = ioFrom8575(0x%02x, %s%s);", name, i2cAddress, intPin, invertedCode));
    }

    @Override
    public Optional<String> generateExport() {
        return Optional.of(String.format("extern IoAbstractionRef ioexp_%s;", name));
    }

    @Override
    public Optional<HeaderDefinition> generateHeader() {
        return Optional.of(new HeaderDefinition("IoAbstractionWire.h", GLOBAL, PRIORITY_NORMAL, new AlwaysApplicable()));
    }
}
