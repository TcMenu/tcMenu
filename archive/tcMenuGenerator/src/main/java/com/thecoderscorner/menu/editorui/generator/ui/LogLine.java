package com.thecoderscorner.menu.editorui.generator.ui;

public class LogLine {
    private String text;
    private System.Logger.Level level;

    public LogLine(String text, System.Logger.Level level) {
        this.text = text;
        this.level = level;
    }

    public String getText() {
        return text;
    }

    public System.Logger.Level getLevel() {
        return level;
    }
}
