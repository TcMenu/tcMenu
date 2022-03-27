package com.thecoderscorner.embedcontrol.customization;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.menu.domain.state.PortableColor;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.RenderingStatus;

public class ConfigurableConditionalColoring implements ConditionalColoring {
    private final GlobalSettings settings;
    private final ControlColor textColor;
    private final ControlColor buttonColor;
    private final ControlColor updateColor;
    private final ControlColor highlightColor;

    public ConfigurableConditionalColoring(GlobalSettings settings, ControlColor textColor, ControlColor buttonColor,
                                           ControlColor updateColor, ControlColor highlightColor) {
        this.settings = settings;
        this.textColor = textColor;
        this.buttonColor = buttonColor;
        this.updateColor = updateColor;
        this.highlightColor = highlightColor;
    }

    @Override
    public PortableColor backgroundFor(RenderingStatus status, ColorComponentType compType) {
        return switch (status) {
            case RECENT_UPDATE -> updateColor.getBg();
            case EDIT_IN_PROGRESS -> settings.getPendingColor().getBg();
            case CORRELATION_ERROR -> settings.getErrorColor().getBg();
            default -> colorCompForType(compType).getBg();
        };
    }

    private ControlColor colorCompForType(ColorComponentType compType) {
        return switch (compType) {
            case TEXT_FIELD -> textColor;
            case BUTTON -> buttonColor;
            case HIGHLIGHT, CUSTOM -> highlightColor;
            case ERROR -> settings.getErrorColor();
            case DIALOG -> settings.getDialogColor();
            case PENDING -> settings.getPendingColor();
        };
    }

    @Override
    public PortableColor foregroundFor(RenderingStatus status, ColorComponentType compType) {
        return switch (status) {
            case RECENT_UPDATE -> updateColor.getFg();
            case EDIT_IN_PROGRESS -> settings.getPendingColor().getFg();
            case CORRELATION_ERROR -> settings.getErrorColor().getFg();
            default -> colorCompForType(compType).getFg();
        };
    }
}