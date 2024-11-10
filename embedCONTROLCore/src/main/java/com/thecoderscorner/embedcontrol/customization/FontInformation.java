package com.thecoderscorner.embedcontrol.customization;

/// Used by component settings to indicate what size of font is needed for a given item, either absolute or percentage
/// of the existing font.
public record FontInformation(int fontSize, SizeMeasurement sizeMeasurement) {
    public enum SizeMeasurement {ABS_SIZE, PERCENT}

    /// Calculates the font size to be used based on the existing font.
    ///
    /// @param size the existing size to base the calculation on
    /// @return the calculated font size, either the absolute size or a percentage-based size
    public int fontSizeFromExisting(int size) {
        return (sizeMeasurement == SizeMeasurement.ABS_SIZE) ? size : (int)((size) * (fontSize / 100.0));
    }

    public String toWire() { return fontSize + ((sizeMeasurement == SizeMeasurement.PERCENT) ? "%" : ""); }

    public static FontInformation fromWire(String wireFormat) {
        if(wireFormat.endsWith("%")) {
            return new FontInformation(Integer.parseInt(wireFormat.substring(0, wireFormat.length() - 1)), SizeMeasurement.PERCENT);
        } else {
            return new FontInformation(Integer.parseInt(wireFormat), SizeMeasurement.ABS_SIZE);
        }
    }
}
