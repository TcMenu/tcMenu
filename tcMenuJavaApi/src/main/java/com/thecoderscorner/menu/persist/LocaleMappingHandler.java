package com.thecoderscorner.menu.persist;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public interface LocaleMappingHandler {
    boolean isLocalSupportEnabled();
    String getLocalSpecificEntry(String source) throws IllegalArgumentException;
    void setLocalSpecificEntry(String source, String newValue) throws IllegalArgumentException;
    List<Locale> getEnabledLocales();
    void changeLocale(Locale locale) throws IOException;
    void saveChanges();
}
