package com.thecoderscorner.embedcontrol.customization.formbuilder;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.menu.domain.EditableTextMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;

import java.util.Map;
import java.util.Optional;

public class StringCustomDrawingConfiguration implements CustomDrawingConfiguration<String> {
    private Map<String, ControlColor> colorMappings;
    private String name;

    public StringCustomDrawingConfiguration(Map<String, ControlColor> colorMappings, String name) {
        this.colorMappings = colorMappings;
        this.name = name;
    }

    @Override
    public boolean isSupportedFor(MenuItem item) {
        return item instanceof EditableTextMenuItem;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<ControlColor> getColorFor(String value) {
        if(colorMappings.containsKey(value)) {
            return Optional.ofNullable(colorMappings.get(value));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return "String Custom " + name;
    }

    public Map<String, ControlColor> getAllMappings() {
        return colorMappings;
    }
}

