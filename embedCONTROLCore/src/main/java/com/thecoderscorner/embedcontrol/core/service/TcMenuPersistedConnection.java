package com.thecoderscorner.embedcontrol.core.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class TcMenuPersistedConnection {
    public enum StoreConnectionType { MANUAL_SOCKET, SERIAL_CONNECTION, SIMULATOR }
    private final int localId;
    private final String name;
    private final String uuid;
    private final String formName;
    private final StoreConnectionType connectionType;
    private final String hostOrSerialId;
    private final String portOrBaud;
    private final String extraData;
    private final AppDataStore dataStore;
    private List<TcMenuFormPersistence> attachedForms = null;
    private final LocalDateTime lastModified;

    public TcMenuPersistedConnection(int localId, String name, String uuid, String formName, StoreConnectionType conType,
                                     String hostOrSerialId, String portOrBaud, String extraData, String lastModified,
                                     AppDataStore dataStore) {
        this.localId = localId;
        this.name = name;
        this.uuid = uuid;
        this.formName = formName;
        this.connectionType = conType;
        this.hostOrSerialId = hostOrSerialId;
        this.portOrBaud = portOrBaud;
        this.extraData = extraData;
        this.dataStore = dataStore;
        this.lastModified = LocalDateTime.parse(lastModified, DateTimeFormatter.ISO_DATE_TIME);
    }

    public TcMenuPersistedConnection(int localId, String name, String uuid, String formName, StoreConnectionType conType,
                                     String hostOrSerialId, String portOrBaud, String extraData, AppDataStore dataStore) {
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

    public TcMenuPersistedConnection withUuid(UUID uuid) {
        return new TcMenuPersistedConnection(localId, name, uuid.toString(), formName, connectionType, hostOrSerialId, portOrBaud, extraData, dataStore);
    }


    public TcMenuPersistedConnection withNewLocalId(int localId) {
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

    public Optional<TcMenuFormPersistence> getSelectedForm() {
        ensurePopulated();
        return attachedForms.stream().filter(form -> form.formName().equals(formName)).findFirst();
    }

    public List<TcMenuFormPersistence> getAllForms() {
        ensurePopulated();
        return attachedForms;
    }

    private void ensurePopulated() {
        if(attachedForms == null) attachedForms = List.copyOf(dataStore.getAllFormsForConnection(localId));
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

    public String getLastModifiedText() {
        return lastModified.format(DateTimeFormatter.ISO_DATE_TIME);
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
                Objects.equals(this.extraData, that.extraData) &&
                Objects.equals(this.attachedForms, that.attachedForms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localId, name, uuid, formName, hostOrSerialId, portOrBaud, extraData, attachedForms);
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
