package com.thecoderscorner.menu.persist;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class PropertiesLocaleEnabledHandler implements LocaleMappingHandler {
    private final SafeBundleLoader bundleLoader;
    private final Object localeLock = new Object();
    private Locale currentLocale;
    private Map<String, String> cachedEntries;
    private boolean needsSave = false;

    public PropertiesLocaleEnabledHandler(SafeBundleLoader bundleLoader) {
        this.bundleLoader = bundleLoader;
        try {
            changeLocale(Locale.of(""));
        } catch(Exception ex) {
            throw new UnsupportedOperationException("Default Locale not available", ex);
        }
    }

    @Override
    public boolean isLocalSupportEnabled() {
        return true;
    }

    @Override
    public String getLocalSpecificEntry(String source) throws IllegalArgumentException {
        synchronized (localeLock) {
            var ret = cachedEntries.get(source);
            if(ret == null) ret = "";
            return ret;
        }
    }

    @Override
    public void setLocalSpecificEntry(String source, String newValue) throws IllegalArgumentException {
        synchronized (localeLock) {
            cachedEntries.put(source, newValue);
            needsSave = true;
        }
    }

    @Override
    public List<Locale> getEnabledLocales() {
        try {
            return Files.walk(bundleLoader.getLocation(), 2, FileVisitOption.FOLLOW_LINKS)
                    .filter(p -> p.toString().endsWith(".properties"))
                    .map(p -> p.getFileName().toString().replace(bundleLoader.getBaseName(), ""))
                    .map(s -> s.replace(".properties", ""))
                    .map(s -> s.startsWith("_") ? s.substring(1) : s)
                    .map(Locale::of)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return List.of();
        }
    }

    @Override
    public void changeLocale(Locale locale) throws IOException {
        synchronized (localeLock) {
            saveChanges();
            currentLocale = locale;
            if(locale.getLanguage().equals("--")) {
                cachedEntries = new HashMap<>();
            } else {
                cachedEntries = bundleLoader.loadResourceBundleAsMap(currentLocale);
            }
            needsSave = false;
        }
    }

    @Override
    public void saveChanges() {
        synchronized (localeLock) {
            if(needsSave) {
                needsSave = false;
                bundleLoader.saveChangesKeepingFormatting(currentLocale, cachedEntries);
            }
        }
    }

    @Override
    public Map<String, String> getUnderlyingMap() {
        synchronized (localeLock) {
            return Map.copyOf(cachedEntries);
        }
    }

    public SafeBundleLoader getSafeLoader() {
        return bundleLoader;
    }

    @Override
    public Locale getCurrentLocale() {
        synchronized (localeLock) {
            return currentLocale;
        }
    }
}
