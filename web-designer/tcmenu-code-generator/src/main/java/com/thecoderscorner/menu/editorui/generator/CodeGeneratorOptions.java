/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator;

import com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.parameters.AuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.EepromDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinitionCollection;
import com.thecoderscorner.menu.editorui.generator.parameters.MenuInMenuCollection;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.NoAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.eeprom.NoEepromDefinition;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
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
    private EepromSaveMode eepromSaveMode;
    private EepromDefinition eepromDefinition;
    private AuthenticatorDefinition authenticatorDefinition;
    private IoExpanderDefinitionCollection projectIoExpanders;
    boolean useDynamicMenus;
    private MenuInMenuCollection menuInMenuCollection;
    boolean i18nEnabled;

    public CodeGeneratorOptions() {
        // for serialisation
    }

    public CodeGeneratorOptions(EmbeddedPlatform embeddedPlatform, String displayTypeId, String inputTypeId, List<String> remoteCapabilities,
                                String themeTypeId, List<CreatorProperty> lastProperties,
                                UUID applicationUUID, String applicationName,
                                EepromDefinition eepromDef, AuthenticatorDefinition authDef,
                                IoExpanderDefinitionCollection projectIoExpanders,
                                MenuInMenuCollection menuInMenuCollection, ProjectSaveLocation saveLocation,
                                boolean namingRecursive, boolean useCppMain, EepromSaveMode eepromSaveMode, boolean useDynamicMenus,
                                boolean i18nEnabled) {
        this.embeddedPlatform = embeddedPlatform;
        this.useDynamicMenus = useDynamicMenus;
        this.lastDisplayUuid = displayTypeId;
        this.eepromSaveMode = eepromSaveMode;
        this.lastInputUuid = inputTypeId;
        this.i18nEnabled = i18nEnabled;
        if(remoteCapabilities != null && !remoteCapabilities.isEmpty()) {
            this.lastRemoteUuids = remoteCapabilities;
            // for backward compatibility as far as possible we save the first in the old format.
            this.lastRemoteUuid = remoteCapabilities.getFirst();
        }
        this.projectIoExpanders = projectIoExpanders;
        this.lastThemeUuid = themeTypeId;
        this.lastProperties = lastProperties;
        this.applicationUUID = applicationUUID;
        this.applicationName = applicationName;
        this.namingRecursive = namingRecursive;
        this.saveLocation = saveLocation;
        this.useCppMain = useCppMain || embeddedPlatform.getBoardId().equals("MBED_RTOS");
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


    public List<String> getLastRemoteCapabilitiesUuids() {
        if (lastRemoteUuids == null) {
            lastRemoteUuids = List.of(lastRemoteUuid != null ? lastRemoteUuid : CoreCodeGenerator.NO_REMOTE_ID);
        }
        return lastRemoteUuids;
    }

    public IoExpanderDefinitionCollection getExpanderDefinitions() {
        if(projectIoExpanders == null) return new IoExpanderDefinitionCollection();
        return projectIoExpanders;
    }

    public MenuInMenuCollection getMenuInMenuCollection() {
        if(menuInMenuCollection == null)  menuInMenuCollection = new MenuInMenuCollection();
        return menuInMenuCollection;
    }

    public boolean isUsingSizedEEPROMStorage() {
        return eepromSaveMode == EepromSaveMode.WRITE_BY_POSITION_WITH_SIZE;
    }
}
