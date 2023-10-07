package com.thecoderscorner.embedcontrol.customization.customdraw;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.menu.domain.EditableTextMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import javafx.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class StringCustomDrawingConfiguration implements CustomDrawingConfiguration {
    private final Map<String, ControlColor> colorMappings;
    private final String name;

    public StringCustomDrawingConfiguration(Map<String, ControlColor> colorMappings, String name) {
        this.colorMappings = colorMappings;
        this.name = name;
    }

    public StringCustomDrawingConfiguration(List<Pair<String, ControlColor>> ranges, String text) {
        this.colorMappings = ranges.stream().collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        this.name = text;
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
    public Optional<ControlColor> getColorFor(Object value) {
        if(colorMappings.containsKey(value.toString())) {
            return Optional.ofNullable(colorMappings.get(value.toString()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return name + " string map";
    }
    public Map<String, ControlColor> getAllMappings() {
        return colorMappings;
    }
}

