package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeGeneratorCapable;

import java.util.Optional;

import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.*;

public class SizeBasedEEPROMCodeCapable implements CodeGeneratorCapable {
    private final boolean sizeEnabled;

    public SizeBasedEEPROMCodeCapable(boolean sizeEnabled) {
        this.sizeEnabled = sizeEnabled;
    }

    @Override
    public Optional<String> generateCode() {
        return Optional.of("    setSizeBasedEEPROMStorageEnabled(" + sizeEnabled + ");");
    }

    @Override
    public Optional<String> generateGlobal() {
        return Optional.empty();
    }

    @Override
    public Optional<String> generateExport() {
        return Optional.empty();
    }

    @Override
    public Optional<HeaderDefinition> generateHeader() {
        return Optional.of(new HeaderDefinition("EepromItemStorage.h", HeaderType.GLOBAL, PRIORITY_NORMAL, new AlwaysApplicable()));
    }
}
