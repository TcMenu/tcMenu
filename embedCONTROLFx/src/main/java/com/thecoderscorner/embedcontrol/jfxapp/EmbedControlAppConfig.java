package com.thecoderscorner.embedcontrol.jfxapp;

import com.thecoderscorner.embedcontrol.core.service.CoreControlAppConfig;

public class EmbedControlAppConfig extends CoreControlAppConfig {

    private final VersionHelper helper;
    private final RemoteUiEmbedControlContext remoteContext;

    public EmbedControlAppConfig() {
        helper = new VersionHelper();
        remoteContext = new RemoteUiEmbedControlContext(executor, serializer, serialFactory, ecDataStore, globalSettings, helper);
    }

    public VersionHelper getHelper() {
        return helper;
    }

    public RemoteUiEmbedControlContext getRemoteContext() {
        return remoteContext;
    }
}