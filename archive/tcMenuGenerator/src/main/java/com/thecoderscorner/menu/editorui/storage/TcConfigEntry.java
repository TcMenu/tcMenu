package com.thecoderscorner.menu.editorui.storage;

import com.thecoderscorner.embedcontrol.core.util.FieldMapping;
import com.thecoderscorner.embedcontrol.core.util.FieldType;
import com.thecoderscorner.embedcontrol.core.util.TableMapping;

import java.time.LocalDateTime;

@TableMapping(tableName = "TC_CONFIG_ENTRY", uniqueKeyField = "PARAM_KEY")
public class TcConfigEntry {
    private boolean changed = false;

    @FieldMapping(fieldName = "PRIMARY_ID", primaryKey = true, fieldType = FieldType.INTEGER)
    private int primaryId;
    @FieldMapping(fieldName = "PARAM_KEY", fieldType = FieldType.ENUM)
    private TcConfigEntryType paramKey;
    @FieldMapping(fieldName = "PARAM_VAL", fieldType = FieldType.VARCHAR)
    private String paramVal;
    @FieldMapping(fieldName = "LAST_MOD", fieldType = FieldType.ISO_DATE)
    private LocalDateTime lastMod;
    @FieldMapping(fieldName = "ACTIVE", fieldType = FieldType.BOOLEAN)
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
