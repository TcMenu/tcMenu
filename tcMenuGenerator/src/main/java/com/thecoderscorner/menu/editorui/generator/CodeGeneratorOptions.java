/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator;

import com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.parameters.*;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.NoAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.eeprom.NoEepromDefinition;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CodeGeneratorOptions {
    private EmbeddedPlatform embeddedPlatform;
    private String lastDisplayUuid;
    private String lastInputUuid;
    private String lastRemoteUuid;
    private List<String> lastRemoteUuids;
    private String lastThemeUuid;
    private UUID applicationUUID;
    private String applicationName;
    private List<CreatorProperty> lastProperties;
    private boolean namingRecursive;
    private ProjectSaveLocation saveLocation;
    private boolean useCppMain;
    private boolean usingSizedEEPROMStorage;
    private EepromDefinition eepromDefinition;
    private AuthenticatorDefinition authenticatorDefinition;
    private IoExpanderDefinitionCollection projectIoExpanders;
    private List<String> listOfEmbeddedForms;

    private MenuInMenuCollection menuInMenuCollection;
    private String packageNamespace;
    private boolean appIsModular;

    public CodeGeneratorOptions() {
        // for serialisation
    }

    public CodeGeneratorOptions(EmbeddedPlatform embeddedPlatform, String displayTypeId, String inputTypeId, List<String> remoteCapabilities,
                                String themeTypeId, List<CreatorProperty> lastProperties,
                                UUID applicationUUID, String applicationName, String packageNamespace,
                                EepromDefinition eepromDef, AuthenticatorDefinition authDef,
                                IoExpanderDefinitionCollection projectIoExpanders, List<String> listOfEmbeddedForms,
                                MenuInMenuCollection menuInMenuCollection, ProjectSaveLocation saveLocation,
                                boolean appIsModular, boolean namingRecursive, boolean useCppMain, boolean sizeBasedRom) {
        this.embeddedPlatform = embeddedPlatform;
        this.lastDisplayUuid = displayTypeId;
        this.lastInputUuid = inputTypeId;
        this.listOfEmbeddedForms = listOfEmbeddedForms;
        if(remoteCapabilities != null && !remoteCapabilities.isEmpty()) {
            this.lastRemoteUuids = remoteCapabilities;
            // for backward compatibility as far as possible we save the first in the old format.
            this.lastRemoteUuid = remoteCapabilities.get(0);
        }
        this.projectIoExpanders = projectIoExpanders;
        this.lastThemeUuid = themeTypeId;
        this.lastProperties = lastProperties;
        this.applicationUUID = applicationUUID;
        this.applicationName = applicationName;
        this.packageNamespace = packageNamespace;
        this.namingRecursive = namingRecursive;
        this.appIsModular = appIsModular;
        this.saveLocation = saveLocation;
        this.useCppMain = useCppMain || embeddedPlatform.equals("MBED_RTOS");
        this.usingSizedEEPROMStorage = sizeBasedRom;
        this.eepromDefinition = eepromDef;
        this.authenticatorDefinition = authDef;
        this.menuInMenuCollection = menuInMenuCollection;
    }

    public EepromDefinition getEepromDefinition() {
        if(eepromDefinition == null) return new NoEepromDefinition();
        return eepromDefinition;
    }

    public AuthenticatorDefinition getAuthenticatorDefinition() {
        if(authenticatorDefinition == null) return new NoAuthenticatorDefinition();
        return authenticatorDefinition;
    }

    public List<String> getListOfEmbeddedForms() {
        if(listOfEmbeddedForms == null) listOfEmbeddedForms = new ArrayList<>();
        return listOfEmbeddedForms;
    }

    public String getPackageNamespace() { return packageNamespace; }

    public String getApplicationName() {
        return applicationName;
    }

    public UUID getApplicationUUID() {
        return applicationUUID;
    }

    public EmbeddedPlatform getEmbeddedPlatform() {
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

    public ProjectSaveLocation getSaveLocation() {
        return saveLocation;
    }

    public boolean isUseCppMain() {
        return useCppMain;
    }

    public IoExpanderDefinitionCollection getExpanderDefinitions() {
        if(projectIoExpanders == null) return new IoExpanderDefinitionCollection();
        return projectIoExpanders;
    }

    public MenuInMenuCollection getMenuInMenuCollection() {
        if(menuInMenuCollection == null)  menuInMenuCollection = new MenuInMenuCollection();
        return menuInMenuCollection;
    }

    public boolean isModularApp() {
        return appIsModular;
    }

    public boolean isUsingSizedEEPROMStorage() {
        return usingSizedEEPROMStorage;
    }
}
