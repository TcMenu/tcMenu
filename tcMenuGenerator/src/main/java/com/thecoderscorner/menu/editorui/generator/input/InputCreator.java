package com.thecoderscorner.menu.editorui.generator.input;

public interface InputCreator {
    String getInputHeaders();
    String getInputGlobals();
    String getInputSetup(String rootItem);
}
