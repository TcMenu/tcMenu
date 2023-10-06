package com.thecoderscorner.embedcontrol.customization.customdraw;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.menu.domain.*;

import java.util.List;
import java.util.Optional;

public class NumberCustomDrawingConfiguration implements CustomDrawingConfiguration<Number> {
    List<NumericColorRange> colorRanges;
    private String name;

    public NumberCustomDrawingConfiguration(List<NumericColorRange> colorRanges, String name) {
        this.colorRanges = colorRanges;
        this.name = name;
    }

    public boolean isSupportedFor(MenuItem item) {
        return item instanceof AnalogMenuItem || item instanceof EnumMenuItem || item instanceof ScrollChoiceMenuItem ||
                item instanceof FloatMenuItem;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " number range";
    }


    @Override
    public Optional<ControlColor> getColorFor(Number value) {
        double v = value.doubleValue();
        for(var range : colorRanges) {
            if(v > range.start() && v < range.end()) {
                return Optional.of(new ControlColor(range.fg(), range.bg()));
            }
        }

        return Optional.empty();
    }

    public List<NumericColorRange> getColorRanges() {
        return List.copyOf(colorRanges);
    }

    public void setColorRangeAt(int idx, NumericColorRange colorRange) {
        colorRanges.set(idx, colorRange);
    }

    public void addColorRange(NumericColorRange colorRange) {
        colorRanges.add(colorRange);
    }
}
