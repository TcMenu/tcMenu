package com.thecoderscorner.menu.editorui.generator.input;

public class RotaryEncoderInputCreator implements InputCreator {
    @Override
    public String getInputHeaders() {
        return "";
    }

    @Override
    public String getInputGlobals() {
        StringBuilder sb = new StringBuilder(256);
        return sb.append("\n// Definitions for the Encoder A, B and select pins\n")
                .append("#define ENCODER_PIN_A      2\n")
                .append("#define ENCODER_PIN_B      3\n")
                .append("#define ENCODER_BUTTON_PIN 24\n")
                .toString();
    }

    @Override
    public String getInputSetup(String rootItem) {
        StringBuilder sb = new StringBuilder(256);
        return sb.append("    switches.initialise(ioUsingArduino());\n")
                 .append("    menuMgr.initForEncoder(&renderer, &")
                 .append(rootItem)
                 .append(", ENCODER_PIN_A, ENCODER_PIN_B, ENCODER_BUTTON_PIN);")
                 .toString();
    }
}
