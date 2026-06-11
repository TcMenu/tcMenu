package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.editorui.generator.EepromSaveMode;
import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeGeneratorCapable;

import java.util.Optional;

import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.*;

public class EepromStorageModeCodeCapable implements CodeGeneratorCapable {

    private final EepromSaveMode eepromSaveMode;

    public EepromStorageModeCodeCapable(EepromSaveMode eepromSaveMode) {
        this.eepromSaveMode = eepromSaveMode;
    }

    @Override
    public Optional<String> generateCode() {
        return Optional.of("    setEepromStorageMode(" + asCppDef(eepromSaveMode) + ");");
    }

    private String asCppDef(EepromSaveMode mode) {
        return switch (mode) {
            case LEGACY_WRITE_BY_POSITION -> "TC_STORE_ROM_LEGACY";
            case WRITE_BY_POSITION_WITH_SIZE -> "TC_STORE_ROM_WITH_SIZE";
            case DYNAMIC_WRITE_BY_ID -> "TC_STORE_ROM_DYNAMIC";
        };
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
