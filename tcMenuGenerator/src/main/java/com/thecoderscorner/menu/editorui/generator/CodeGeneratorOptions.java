/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator;

import com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.parameters.AuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.EepromDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.NoAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.eeprom.NoEepromDefinition;

import java.util.List;
import java.util.UUID;

public class CodeGeneratorOptions {
    private String embeddedPlatform;
    private String lastDisplayUuid;
    private String lastInputUuid;
    private String lastRemoteUuid;
    private List<String> lastRemoteUuids;
    private String lastThemeUuid;
    private UUID applicationUUID;
    private String applicationName;
    private List<CreatorProperty> lastProperties;
    private boolean namingRecursive;
    private boolean saveToSrc;
    private boolean useCppMain;
    private EepromDefinition eepromDefinition;
    private AuthenticatorDefinition authenticatorDefinition;

    public CodeGeneratorOptions() {
        // for serialisation
    }

    public CodeGeneratorOptions(String embeddedPlatform, String displayTypeId, String inputTypeId, List<String> remoteCapabilities,
                                String themeTypeId, List<CreatorProperty> lastProperties,
                                UUID applicationUUID, String applicationName,
                                EepromDefinition eepromDef, AuthenticatorDefinition authDef,
                                boolean namingRecursive, boolean saveToSrc, boolean useCppMain) {
        this.embeddedPlatform = embeddedPlatform;
        this.lastDisplayUuid = displayTypeId;
        this.lastInputUuid = inputTypeId;
        if(remoteCapabilities != null && !remoteCapabilities.isEmpty()) {
            this.lastRemoteUuids = remoteCapabilities;
            // for backward compatibility as far as possible we save the first in the old format.
            this.lastRemoteUuid = remoteCapabilities.get(0);
        }
        this.lastThemeUuid = themeTypeId;
        this.lastProperties = lastProperties;
        this.applicationUUID = applicationUUID;
        this.applicationName = applicationName;
        this.namingRecursive = namingRecursive;
        this.saveToSrc = saveToSrc;
        this.useCppMain = useCppMain || embeddedPlatform.equals("MBED_RTOS");
        this.eepromDefinition = eepromDef;
        this.authenticatorDefinition = authDef;
    }

    public EepromDefinition getEepromDefinition() {
        if(eepromDefinition == null) return new NoEepromDefinition();
        return eepromDefinition;
    }

    public AuthenticatorDefinition getAuthenticatorDefinition() {
        if(authenticatorDefinition == null) return new NoAuthenticatorDefinition();
        return authenticatorDefinition;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public UUID getApplicationUUID() {
        return applicationUUID;
    }

    public String getEmbeddedPlatform() {
        return embeddedPlatform;
    }

    public String getLastDisplayUuid() {
        return lastDisplayUuid;
    }

    public String getLastInputUuid() {
        return lastInputUuid;
    }

    public String getLastThemeUuid() { return lastThemeUuid; }

    public List<String> getLastRemoteCapabilitiesUuids() {
        if (lastRemoteUuids == null) {
            lastRemoteUuids = List.of(lastRemoteUuid != null ? lastRemoteUuid : CoreCodeGenerator.NO_REMOTE_ID);
        }
        return lastRemoteUuids;
    }

    public List<CreatorProperty> getLastProperties() {
        return lastProperties;
    }

    public boolean isNamingRecursive() {
        return namingRecursive;
    }

    public boolean isSaveToSrc() {
        return saveToSrc;
    }

    public boolean isUseCppMain() {
        return useCppMain;
    }
}
