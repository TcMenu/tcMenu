package com.thecoderscorner.menu.persist;

import java.io.IOException;
import java.util.*;

public class ResourceBundleMappingHandler implements LocaleMappingHandler {
    private final ResourceBundle bundle;

    public ResourceBundleMappingHandler(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public boolean isLocalSupportEnabled() {
        return true;
    }

    @Override
    public String getLocalSpecificEntry(String source) throws IllegalArgumentException {
        return bundle.getString(source);
    }

    @Override
    public void setLocalSpecificEntry(String source, String newValue) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Resource bundles are read only");
    }

    @Override
    public List<Locale> getEnabledLocales() {
        return Collections.singletonList(bundle.getLocale());
    }

    @Override
    public void changeLocale(Locale locale) throws IOException {
        throw new UnsupportedOperationException("Resource bundles are read only");
    }

    @Override
    public void saveChanges() {
        throw new UnsupportedOperationException("Resource bundles are read only");
    }

    @Override
    public Map<String, String> getUnderlyingMap() {
        var keys = bundle.keySet();
        var map = new HashMap<String, String>(keys.size() + 10);
        for(var key : keys) {
            map.put(key, bundle.getString(key));
        }
        return map;
    }

    @Override
    public Locale getCurrentLocale() {
        return bundle.getLocale();
    }
}
