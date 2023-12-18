package com.thecoderscorner.embedcontrol.customization;

public record FontInformation(int fontSize, SizeMeasurement sizeMeasurement) {
    public enum SizeMeasurement {ABS_SIZE, PERCENT}

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
