package com.thecoderscorner.menu.persist;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class PropertiesLocaleEnabledHandler implements LocaleMappingHandler {
    public static final Locale DEFAULT_LOCALE = new Locale("");

    private final SafeBundleLoader bundleLoader;
    private final Object localeLock = new Object();
    private Locale currentLocale;
    private Map<String, String> cachedEntries;
    private Map<String, String> defaultCachedEntries;
    private boolean needsSave = false;
    private boolean defaultNeedsSave = false;

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
            if(ret == null) {
                ret = defaultCachedEntries.get(source);
            }
            return ret;
        }
    }

    @Override
    public void setLocalSpecificEntry(String source, String newValue) throws IllegalArgumentException {
        synchronized (localeLock) {
            if(!defaultCachedEntries.containsKey(source)) {
                defaultCachedEntries.put(source, newValue);
                defaultNeedsSave = true;
            }
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
                    .map(this::makeLocale)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return List.of();
        }
    }

    private Locale makeLocale(String s) {
        var localeStr = (s.startsWith("_")) ? s.substring(1) : s;
        if(localeStr.isEmpty() || localeStr.equals("==")) {
            return new Locale(localeStr);
        }
        if(localeStr.length() == 2) {
            return new Locale(localeStr);
        } else if(localeStr.matches("\\w\\w_\\w\\w")) {
            return new Locale(localeStr.substring(0, 2), localeStr.substring(3));
        } else throw new UnsupportedOperationException("Unsupported type of locale only language or languageCountry" + localeStr);
    }

    @Override
    public void changeLocale(Locale locale) throws IOException {
        synchronized (localeLock) {
            // if we haven't yet loaded the defaults, load them now
            if(defaultCachedEntries == null) {
                defaultCachedEntries = bundleLoader.loadResourceBundleAsMap(DEFAULT_LOCALE);
            }
            saveChanges();
            currentLocale = locale;
            if(locale.getLanguage().equals("--")) {
                cachedEntries = new HashMap<>();
            } else if(locale.getLanguage().isEmpty()) {
                cachedEntries = defaultCachedEntries;
            } else {
                cachedEntries = bundleLoader.loadResourceBundleAsMap(currentLocale);
            }
            needsSave = false;
        }
    }

    @Override
    public void saveChanges() {
        synchronized (localeLock) {
            if(defaultNeedsSave) {
                defaultNeedsSave = false;
                bundleLoader.saveChangesKeepingFormatting(DEFAULT_LOCALE, defaultCachedEntries);
            }
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
