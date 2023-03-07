package com.thecoderscorner.menu.persist;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NoLocaleEnabledLocalHandler implements LocaleMappingHandler {
    @Override
    public boolean isLocalSupportEnabled() {
        return false;
    }

    @Override
    public String getLocalSpecificEntry(String source) throws IllegalArgumentException {
        return source;
    }

    @Override
    public void setLocalSpecificEntry(String source, String newValue) throws IllegalArgumentException {
        throw new IllegalArgumentException("Set locale not supported in no locale mode");
    }

    @Override
    public List<Locale> getEnabledLocales() {
        return List.of();
    }

    @Override
    public void changeLocale(Locale locale) {
        throw new IllegalArgumentException("Cannot change locale in no locale mode");
    }

    @Override
    public void saveChanges() {
    }

    @Override
    public Map<String, String> getUnderlyingMap() {
        return Map.of();
    }

    @Override
    public Locale getCurrentLocale() {
        return new Locale("--");
    }
}
