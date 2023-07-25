package com.thecoderscorner.embedcontrol.core.service;

import java.util.List;

public interface AppDataStore {
    TcMenuPersistedConnection getConnectionById(int id);
    List<TcMenuPersistedConnection> getAllConnections();
    int updateConnection(TcMenuPersistedConnection connection);
    void deleteConnection(TcMenuPersistedConnection connection);
    GlobalSettings getGlobalSettings();
    void updateGlobalSettings(GlobalSettings settings);

    List<TcMenuFormPersistence> getAllFormsForConnection(int localId);
    void updateForm(TcMenuFormPersistence form);
    void deleteForm(TcMenuFormPersistence form);
}
