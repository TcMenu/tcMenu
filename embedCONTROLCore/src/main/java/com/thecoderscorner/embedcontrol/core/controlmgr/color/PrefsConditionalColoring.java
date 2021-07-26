package com.thecoderscorner.embedcontrol.core.controlmgr.color;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.menu.domain.state.PortableColor;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.RenderingStatus;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.ColorComponentType.*;

public class PrefsConditionalColoring implements ConditionalColoring {
    private final GlobalSettings settings;

    public PrefsConditionalColoring(GlobalSettings settings) {
        this.settings = settings;
    }

    @Override
    public PortableColor backgroundFor(RenderingStatus status, ColorComponentType compType) {
        switch (status) {
            case RECENT_UPDATE:
                return settings.getUpdateColor().getBg();
            case EDIT_IN_PROGRESS:
                return settings.getPendingColor().getBg();
            case CORRELATION_ERROR:
                return settings.getErrorColor().getBg();
            default:
                return ColorCompForType(compType).getBg();
        }
    }

    private ControlColor ColorCompForType(ColorComponentType compType) {
        switch (compType) {
            case TEXT_FIELD:
                return settings.getTextColor();
            case BUTTON:
                return settings.getButtonColor();
            case HIGHLIGHT:
            case CUSTOM:
                return settings.getHighlightColor();
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public PortableColor foregroundFor(RenderingStatus status, ColorComponentType compType) {
        switch (status) {
            case RECENT_UPDATE:
                return settings.getUpdateColor().getBg();
            case EDIT_IN_PROGRESS:
                return settings.getPendingColor().getFg();
            case CORRELATION_ERROR:
                return settings.getErrorColor().getFg();
            default:
                return ColorCompForType(compType).getFg();
        }
    }
}