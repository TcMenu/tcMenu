package com.thecoderscorner.embedcontrol.customization;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;

import java.util.Optional;

public class NamedColorCustomizable implements ColorCustomizable {
    private final String colorName;
    private Optional<ControlColor> textColor = Optional.empty();
    private Optional<ControlColor> highlighColor = Optional.empty();
    private Optional<ControlColor> buttonColor = Optional.empty();
    private Optional<ControlColor> updateColor = Optional.empty();
    private Optional<ControlColor> pendingColor = Optional.empty();
    private Optional<ControlColor> dialogColor = Optional.empty();

    public NamedColorCustomizable(String colorName) {
        this.colorName = colorName;
    }

    @Override
    public boolean isRepresentingGlobal() {
        return false;
    }

    @Override
    public ColorStatus getColorStatus(ConditionalColoring.ColorComponentType componentType) {
        return switch (componentType) {
            case TEXT_FIELD -> textColor.isPresent() ? ColorStatus.AVAILABLE : ColorStatus.PROVIDED_NOT_AVAILABLE;
            case HIGHLIGHT -> highlighColor.isPresent() ? ColorStatus.AVAILABLE : ColorStatus.PROVIDED_NOT_AVAILABLE;
            case CUSTOM -> updateColor.isPresent() ? ColorStatus.AVAILABLE : ColorStatus.PROVIDED_NOT_AVAILABLE;
            case BUTTON -> buttonColor.isPresent() ? ColorStatus.AVAILABLE : ColorStatus.PROVIDED_NOT_AVAILABLE;
            case PENDING -> pendingColor.isPresent() ? ColorStatus.AVAILABLE : ColorStatus.PROVIDED_NOT_AVAILABLE;
            case DIALOG -> dialogColor.isPresent() ? ColorStatus.AVAILABLE : ColorStatus.PROVIDED_NOT_AVAILABLE;
            default -> ColorStatus.NOT_PROVIDED;
        };
    }

    @Override
    public ControlColor getColorFor(ConditionalColoring.ColorComponentType componentType) {
        return switch (componentType) {
            case TEXT_FIELD ->textColor.orElseThrow();
            case BUTTON -> buttonColor.orElseThrow();
            case HIGHLIGHT -> highlighColor.orElseThrow();
            case CUSTOM -> updateColor.orElseThrow();
            case PENDING -> pendingColor.orElseThrow();
            case DIALOG -> dialogColor.orElseThrow();
            default -> throw new IllegalArgumentException("Invalid field selected " + componentType);
        };
    }

    @Override
    public void setColorFor(ConditionalColoring.ColorComponentType componentType, ControlColor controlColor) {
        switch(componentType) {
            case TEXT_FIELD -> textColor = Optional.of(controlColor);
            case BUTTON -> buttonColor = Optional.of(controlColor);
            case HIGHLIGHT -> highlighColor = Optional.of(controlColor);
            case CUSTOM -> updateColor = Optional.of(controlColor);
            case DIALOG -> dialogColor = Optional.of(controlColor);
            case PENDING -> pendingColor = Optional.of(controlColor);
            default ->  throw new IllegalArgumentException("Invalid field for set color" + componentType);
        }
    }

    @Override
    public void clearColorFor(ConditionalColoring.ColorComponentType componentType) {
        switch (componentType) {
            case TEXT_FIELD -> textColor = Optional.empty();
            case BUTTON -> buttonColor = Optional.empty();
            case HIGHLIGHT -> highlighColor = Optional.empty();
            case CUSTOM -> updateColor = Optional.empty();
            case DIALOG -> dialogColor = Optional.empty();
            case PENDING -> pendingColor = Optional.empty();
            default -> throw new IllegalArgumentException("Invalid field selected " + componentType);
        }
    }

    @Override
    public String toString() {
        return colorName + " settings";
    }

    @Override
    public String getColorSchemeName() {
        return colorName;
    }
}
