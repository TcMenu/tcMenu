package com.thecoderscorner.embedcontrol.core.util;

import java.time.LocalDateTime;

@TableMapping(tableName = "PERSIST_CHECK", uniqueKeyField = "PERSIST_ID")
public class PersistenceTestObj {
    public enum PersistType { DISK_PERSIST, MEMORY_PERSIST, FLASH_PERSIST }

    @FieldMapping(fieldName = "PERSIST_ID", fieldType = FieldType.INTEGER, primaryKey = true)
    private int persistId;
    @FieldMapping(fieldName = "PERSIST_NAME", fieldType = FieldType.VARCHAR)
    private String persistName;
    @FieldMapping(fieldName = "PERSIST_TYPE", fieldType = FieldType.ENUM)
    private PersistType persistType;
    @FieldMapping(fieldName = "LAST_MODIFIED", fieldType = FieldType.ISO_DATE)
    private LocalDateTime lastModified;

    public PersistenceTestObj() {
    }

    public PersistenceTestObj(int persistId, String persistName, PersistType persistType, LocalDateTime lastModified) {
        this.persistId = persistId;
        this.persistName = persistName;
        this.persistType = persistType;
        this.lastModified = lastModified;
    }

    public int getPersistId() {
        return persistId;
    }

    public void setPersistId(int persistId) {
        this.persistId = persistId;
    }

    public String getPersistName() {
        return persistName;
    }

    public void setPersistName(String persistName) {
        this.persistName = persistName;
    }

    public PersistType getPersistType() {
        return persistType;
    }

    public void setPersistType(PersistType persistType) {
        this.persistType = persistType;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }
}
