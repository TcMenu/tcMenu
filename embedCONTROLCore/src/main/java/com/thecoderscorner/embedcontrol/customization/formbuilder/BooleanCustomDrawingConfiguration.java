package com.thecoderscorner.embedcontrol.customization.formbuilder;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.menu.domain.ActionMenuItem;
import com.thecoderscorner.menu.domain.BooleanMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;

import java.util.Optional;

public class BooleanCustomDrawingConfiguration implements CustomDrawingConfiguration<Boolean> {
    private String name;
    private ControlColor yesColor;
    private ControlColor noColor;
    private ImageDefinition yesImage;
    private ImageDefinition noImage;

    public BooleanCustomDrawingConfiguration(String name, ControlColor yesColor, ControlColor noColor) {
        this.name = name;
        this.yesColor = yesColor;
        this.noColor = noColor;
    }

    public BooleanCustomDrawingConfiguration(String name, ControlColor yesColor, ControlColor noColor, ImageDefinition yesImage, ImageDefinition noImage) {
        this.name = name;
        this.yesColor = yesColor;
        this.noColor = noColor;
        this.yesImage = yesImage;
        this.noImage = noImage;
    }

    @Override
    public boolean isSupportedFor(MenuItem item) {
        return item instanceof BooleanMenuItem || item instanceof SubMenuItem || item instanceof ActionMenuItem;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<ControlColor> getColorFor(Boolean val) {
        return Optional.ofNullable(val ? yesColor : noColor);
    }

    public ImageDefinition getImageFor(boolean val) {
        return val ? yesImage : noImage;
    }

    public boolean hasImages() {
        return yesImage != null || noImage != null;
    }

    @Override
    public String toString() {
        return "Bool Custom " + name;
    }

    public enum ImageLocation { NO_IMAGE, SVG_EMBEDDED_BASE64, PNG_EMBEDDED_BASE64, NETWORK_URL}
    public record ImageDefinition(ImageLocation imageType, String urlOrName) { }
}
