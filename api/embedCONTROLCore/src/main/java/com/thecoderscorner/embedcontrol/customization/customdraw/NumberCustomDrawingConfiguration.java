package com.thecoderscorner.embedcontrol.customization.customdraw;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.menu.domain.*;

import java.util.List;
import java.util.Optional;

/// An implementation of custom drawing that supports numeric ranges. Imagine for example a battery indicator showing
/// the various states of charge of a battery, when it is below 20%, we may want to show that part of the range in red,
/// when it is between 20% and 30% we may present that in orange, and 30% to 100% in green. An example follows that
/// would be provided to component settings:
///
/// ```
///         var batteryIndicatorCustom = new NumberCustomDrawingConfiguration(List.of(
///                 new NumericColorRange(0.0, 20.0, fromFxColor(Color.RED), fromFxColor(Color.WHITE)),
///                 new NumericColorRange(20.0, 30.0, fromFxColor(Color.ORANGE), fromFxColor(Color.WHITE)),
///                 new NumericColorRange(40.0, 100.0, fromFxColor(Color.GREEN), fromFxColor(Color.WHITE))
///         ));
/// ```
///
/// @see ControlColor
/// @see com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettingsBuilder
public class NumberCustomDrawingConfiguration implements CustomDrawingConfiguration {
    List<NumericColorRange> colorRanges;
    private String name;

    public NumberCustomDrawingConfiguration(List<NumericColorRange> colorRanges, String name) {
        this.colorRanges = colorRanges;
        this.name = name;
    }

    public NumberCustomDrawingConfiguration(List<NumericColorRange> colorRanges) {
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
    public Optional<ControlColor> getColorFor(Object value) {
        if(!(value instanceof  Number num)) return Optional.empty();
        double v = num.doubleValue();
        for(var range : colorRanges) {
            if(v > range.start() && v <= range.end()) {
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
