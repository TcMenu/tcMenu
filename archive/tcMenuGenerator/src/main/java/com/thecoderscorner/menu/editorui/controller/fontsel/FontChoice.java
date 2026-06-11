package com.thecoderscorner.menu.editorui.controller.fontsel;

public record FontChoice(String fontName, FontType fontType, boolean local) {
    @Override
    public String toString() {
        return fontName;
    }
}
