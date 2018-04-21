package com.thecoderscorner.menu.editorui.generator.input;

public class UpDownOkInputCreator implements InputCreator {
    @Override
    public String getInputHeaders() {
        return "";
    }

    @Override
    public String getInputGlobals() {
        StringBuilder sb = new  StringBuilder(256);
        return sb.append("// Set the pins used by up down and OK\n")
                .append("#define ENCODER_PIN_UP     22\n")
                .append("#define ENCODER_PIN_DOWN   23\n")
                .append("#define ENCODER_BUTTON_PIN 24\n")
                .toString();

    }

    @Override
    public String getInputSetup(String rootItem) {
        StringBuilder sb = new  StringBuilder(256);
        return sb.append("    switches.initialise(ioUsingArduino());\n")
                 .append("    menuMgr.initForUpDownOk(&renderer, &")
                 .append(rootItem)
                 .append(", ENCODER_PIN_UP, ENCODER_PIN_DOWN, ENCODER_BUTTON_PIN);")
                 .toString();
    }
}
