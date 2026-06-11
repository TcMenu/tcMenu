package com.thecoderscorner.embedcontrol.core.service;

import com.thecoderscorner.embedcontrol.core.util.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@TableMapping(tableName = "TC_CONNECTION", uniqueKeyField = "LOCAL_ID")
public class TcMenuPersistedConnection {
    private final System.Logger logger = System.getLogger(TcMenuPersistedConnection.class.getSimpleName());
    public enum StoreConnectionType { MANUAL_SOCKET, SERIAL_CONNECTION, SIMULATOR }

    @FieldMapping(fieldName = "LOCAL_ID", primaryKey = true, fieldType = FieldType.INTEGER)
    private int localId;
    @FieldMapping(fieldName = "CONNECTION_NAME", fieldType = FieldType.VARCHAR)
    private String name;
    @FieldMapping(fieldName = "CONNECTION_UUID", fieldType = FieldType.VARCHAR)
    private String uuid;
    @FieldMapping(fieldName = "SELECTED_FORM", fieldType = FieldType.VARCHAR)
    private String formName;
    @FieldMapping(fieldName = "CONNECTION_TYPE", fieldType = FieldType.ENUM)
    private StoreConnectionType connectionType;
    @FieldMapping(fieldName = "HOST_OR_SERIAL_ID", fieldType = FieldType.VARCHAR)
    private String hostOrSerialId;
    @FieldMapping(fieldName = "PORT_OR_BAUD", fieldType = FieldType.VARCHAR)
    private String portOrBaud;
    @FieldMapping(fieldName = "EXTRA_DATA", fieldType = FieldType.VARCHAR)
    private String extraData;
    @FieldMapping(fieldName = "LAST_MODIFIED", fieldType = FieldType.ISO_DATE)
    private LocalDateTime lastModified;
    @ProvideStore
    private TccDatabaseUtilities dataStore;

    public TcMenuPersistedConnection(int localId, String name, String uuid, String formName, StoreConnectionType conType,
                                     String hostOrSerialId, String portOrBaud, String extraData, TccDatabaseUtilities dataStore) {
        this.localId = localId;
        this.name = name;
        this.uuid = uuid;
        this.formName = formName;
        this.connectionType = conType;
        this.hostOrSerialId = hostOrSerialId;
        this.portOrBaud = portOrBaud;
        this.extraData = extraData;
        this.dataStore = dataStore;
        this.lastModified = LocalDateTime.now();
    }

    public TcMenuPersistedConnection() {
    }

    public TcMenuPersistedConnection withUuid(UUID uuid) {
        return new TcMenuPersistedConnection(localId, name, uuid.toString(), formName, connectionType, hostOrSerialId, portOrBaud, extraData, dataStore);
    }


    public TcMenuPersistedConnection withNewLocalId(int localId) {
        return new TcMenuPersistedConnection(localId, name, uuid, formName, connectionType, hostOrSerialId, portOrBaud, extraData, dataStore);
    }

    public TcMenuPersistedConnection withFormChange(String formName) {
        return new TcMenuPersistedConnection(localId, name, uuid, formName, connectionType, hostOrSerialId, portOrBaud, extraData, dataStore);
    }

    public int getLocalId() {
        return localId;
    }

    public StoreConnectionType getConnectionType() {
        return connectionType;
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public String getFormName() {
        return formName;
    }

    public String getHostOrSerialId() {
        return hostOrSerialId;
    }

    public String getPortOrBaud() {
        return portOrBaud;
    }

    public String getExtraData() {
        return extraData;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TcMenuPersistedConnection) obj;
        return this.localId == that.localId &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.uuid, that.uuid) &&
                Objects.equals(this.formName, that.formName) &&
                Objects.equals(this.hostOrSerialId, that.hostOrSerialId) &&
                Objects.equals(this.portOrBaud, that.portOrBaud) &&
                Objects.equals(this.extraData, that.extraData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localId, name, uuid, formName, hostOrSerialId, portOrBaud, extraData);
    }

    @Override
    public String toString() {
        return "TcMenuPersistedConnection[" +
                "localId=" + localId + ", " +
                "name=" + name + ", " +
                "uuid=" + uuid + ", " +
                "formName=" + formName + ", " +
                "connectionType=" + connectionType + ", " +
                "hostOrSerialId=" + hostOrSerialId + ", " +
                "portOrBaud=" + portOrBaud + ", " +
                "LastModified=" + lastModified + ']';
    }

}
