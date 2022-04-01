package com.thecoderscorner.embedcontrol.customization;

import com.thecoderscorner.embedcontrol.core.controlmgr.*;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.menu.domain.SubMenuItem;

import java.util.Optional;

public class LayoutBasedItemColorCustomizable implements ComponentSettingsCustomizer {
    private final int menuId;
    private final String itemName;
    private final ScreenLayoutPersistence layout;
    private final SubMenuItem par;
    private Optional<ControlColor> textColor;
    private Optional<ControlColor> highlighColor;
    private Optional<ControlColor> buttonColor;
    private Optional<ControlColor> updateColor;
    private int fontSize;
    private final ComponentSettings initialSettings;

    public LayoutBasedItemColorCustomizable(String itemName, SubMenuItem par, ComponentSettingsWithMenuId override, ScreenLayoutPersistence layout) {
        this.layout = layout;
        this.itemName = itemName;
        this.initialSettings = override.settings();
        this.par = par;
        this.menuId = override.menuId();
        fontSize = override.settings().getFontSize();
        this.textColor = override.textColor();
        this.buttonColor = override.buttonColor();
        this.updateColor = override.updateColor();
        this.highlighColor = override.highlightColor();
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
            default -> throw new IllegalArgumentException("Invalid field selected " + componentType);
        };
    }

    @Override
    public void setColorFor(ConditionalColoring.ColorComponentType componentType, ControlColor controlColor) {

    }

    @Override
    public void clearColorFor(ConditionalColoring.ColorComponentType componentType) {
        switch (componentType) {
            case TEXT_FIELD -> textColor = Optional.empty();
            case BUTTON -> buttonColor = Optional.empty();
            case HIGHLIGHT -> highlighColor = Optional.empty();
            case CUSTOM -> updateColor = Optional.empty();
            default -> throw new IllegalArgumentException("Invalid field selected " + componentType);
        }
    }

    @Override
    public int getFontSize() {
        return fontSize;
    }

    @Override
    public void setFontSize(int size) {
        fontSize = size;
    }

    @Override
    public boolean isRecursiveRender() {
        return false;
    }

    @Override
    public void setRecursiveRender(boolean recursiveRender) {
        // ignored
    }

    @Override
    public String toString() {
        return itemName + " settings";
    }

    @Override
    public ComponentSettings getInitialSettings() {
        return initialSettings;
    }

    @Override
    public void acceptSettingChange(int id, ComponentPositioning positioning, RedrawingMode drawingMode, EditorComponent.PortableAlignment alignment,
                                    ControlType controlType, int fontSize) throws InvalidItemChangeException {
        layout.replaceItemOverride(par, menuId, new ComponentSettingsWithMenuId(menuId, new ComponentSettings(
                layout.provideConditionalColorComponent(par, id), fontSize, alignment, positioning, drawingMode, controlType, true),
                textColor, buttonColor, updateColor, highlighColor));
    }

    @Override
    public void removeOverride(int id) throws InvalidItemChangeException {
        layout.removeItemOverride(par, menuId);
    }
}
