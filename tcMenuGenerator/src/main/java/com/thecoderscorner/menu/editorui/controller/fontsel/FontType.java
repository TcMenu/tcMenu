package com.thecoderscorner.menu.editorui.controller.fontsel;

public enum FontType {
    ADAFRUIT("Adafruit"), TC_UNICODE("TcUnicode"), U8G2("U8G2");

    private final String text;

    FontType(String name) {
        this.text = name;
    }

    public String textName() {
        return text;
    }
}
