package com.thecoderscorner.menu.editorui.generator.input;

public enum InputType {
    ROTARY_ENCODER("Rotary encoder", new RotaryEncoderInputCreator()),
    UP_DOWN_OK_SWITCHES("Up/Down/OK switches", new UpDownOkInputCreator());

    private final String name;
    private final InputCreator creator;

    InputType(String s, InputCreator creator) {
        name = s;
        this.creator = creator;
    }

    public InputCreator getCreator() {
        return creator;
    }

    @Override
    public String toString() {
        return name;
    }
}
