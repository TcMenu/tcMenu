package com.thecoderscorner.embedcontrol.customization;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;

import java.util.List;
import java.util.Optional;

public class LayoutBasedSubColorCustomizable implements ColorCustomizable {
    private Optional<ControlColor> btnColor;
    private Optional<ControlColor> txtColor;
    private Optional<ControlColor> updColor;
    private Optional<ControlColor> hiColor;
    private final String layoutName;
    private final ScreenLayoutPersistence layoutPersistence;
    private final int rootId;
    private boolean recursive;
    private int fontSize;

    public LayoutBasedSubColorCustomizable(String layoutName, ScreenLayoutPersistence layoutPersistence, ComponentSettingsForSub cs) {
        this.layoutName = layoutName;
        this.layoutPersistence = layoutPersistence;
        rootId = cs.rootItemId();
        btnColor = cs.buttonColor();
        txtColor = cs.textColor();
        hiColor = cs.highlightColor();
        updColor = cs.updateColor();
        fontSize = cs.fontSize();
        recursive = cs.recursive();
    }

    @Override
    public boolean isRepresentingGlobal() {
        return false;
    }

    @Override
    public ColorStatus getColorStatus(ConditionalColoring.ColorComponentType componentType) {
        return switch (componentType) {
            case BUTTON -> getStatusForKnownColor(btnColor);
            case TEXT_FIELD -> getStatusForKnownColor(txtColor);
            case HIGHLIGHT -> getStatusForKnownColor(hiColor);
            case CUSTOM -> getStatusForKnownColor(updColor);
            default -> ColorStatus.NOT_PROVIDED;
        };
    }

    private ColorStatus getStatusForKnownColor(Optional<ControlColor> updColor) {
        return this.updColor.isPresent() ? ColorStatus.AVAILABLE : ColorStatus.PROVIDED_NOT_AVAILABLE;
    }

    @Override
    public ControlColor getColorFor(ConditionalColoring.ColorComponentType componentType) {
        return switch (componentType) {
            case BUTTON -> btnColor.orElseThrow();
            case TEXT_FIELD -> txtColor.orElseThrow();
            case HIGHLIGHT -> hiColor.orElseThrow();
            case CUSTOM -> updColor.orElseThrow();
            default -> null;
        };
    }

    @Override
    public void setColorFor(ConditionalColoring.ColorComponentType componentType, ControlColor controlColor) {
        switch (componentType) {
            case BUTTON -> btnColor = Optional.of(controlColor);
            case TEXT_FIELD -> txtColor = Optional.of(controlColor);
            case HIGHLIGHT -> hiColor = Optional.of(controlColor);
            case CUSTOM -> updColor = Optional.of(controlColor);
            default -> throw new IllegalArgumentException("Unsupported type for item " + componentType);
        };
        saveBack();
    }

    private void saveBack() {
        layoutPersistence.replaceSubLevelOverride(new ComponentSettingsForSub(
                rootId, recursive, txtColor, btnColor, updColor, hiColor, fontSize, List.of()
        ), ItemOverrideMode.TOP_LEVEL_PROPERTIES_ONLY);
    }

    @Override
    public void clearColorFor(ConditionalColoring.ColorComponentType componentType) {
        switch (componentType) {
            case BUTTON -> btnColor = Optional.empty();
            case TEXT_FIELD -> txtColor = Optional.empty();
            case HIGHLIGHT -> hiColor = Optional.empty();
            case CUSTOM -> updColor = Optional.empty();
            default -> throw new IllegalArgumentException("Unsupported type for item " + componentType);
        };
        saveBack();
    }

    @Override
    public int getFontSize() {
        return fontSize;
    }

    @Override
    public void setFontSize(int size) {
        fontSize = size;
        saveBack();
    }

    @Override
    public boolean isRecursiveRender() {
        return recursive;
    }

    @Override
    public void setRecursiveRender(boolean recursiveRender) {
        recursive = recursiveRender;
        saveBack();
    }

    @Override
    public String toString() {
        return layoutName;
    }
}
