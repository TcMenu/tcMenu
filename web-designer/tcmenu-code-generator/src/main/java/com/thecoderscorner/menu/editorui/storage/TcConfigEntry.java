package com.thecoderscorner.menu.editorui.storage;

import java.time.LocalDateTime;

public class TcConfigEntry {
    private boolean changed = false;

    private int primaryId;
    private TcConfigEntryType paramKey;
    private String paramVal;
    private LocalDateTime lastMod;
    private boolean active;

    public TcConfigEntry() {
    }

    public TcConfigEntry(TcConfigEntryType paramKey, String paramVal) {
        this.paramKey = paramKey;
        this.primaryId = paramKey.getPk();
        this.paramVal = paramVal;
        this.active = true;
        this.lastMod = LocalDateTime.now();
    }

    public TcConfigEntryType getParamKey() {
        return paramKey;
    }

    public String getParamVal() {
        return paramVal;
    }

    public void setParamVal(String paramVal) {
        changed = paramVal.equals(this.paramVal);
        this.paramVal = paramVal;
    }

    public LocalDateTime getLastMod() {
        return lastMod;
    }

    public boolean isActive() {
        return active;
    }

    void saved() {
        changed = false;
    }

    public boolean isChanged() {
        return changed;
    }
}
