package com.thecoderscorner.menu.editorui.controller;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ConfigureLocalesController {
    private List<Locale> activeLocales;
    private Optional<List<Locale>> result = Optional.empty();

    public void initialise(List<Locale> activeLocales) {
        this.activeLocales = activeLocales;
    }

    public Optional<List<Locale>> getResult() {
        return result;
    }
}
