/*
 * Copyright (c)  2016-2021 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator;

import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.parameters.AuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.EepromDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CodeGeneratorOptionsBuilder {
    private String embeddedPlatform;
    private String lastDisplayUuid;
    private String lastInputUuid;
    private List<String> lastRemoteUuids;
    private String lastThemeUuid;
    private UUID applicationUUID;
    private String applicationName;
    private List<CreatorProperty> lastProperties;
    private boolean namingRecursive;
    private boolean saveToSrc;
    private boolean useCppMain;
    private EepromDefinition eepromDef;
    private AuthenticatorDefinition authDef;

    public CodeGeneratorOptionsBuilder withExisting(CodeGeneratorOptions other) {
        embeddedPlatform = other.getEmbeddedPlatform();
        lastDisplayUuid = other.getLastDisplayUuid();
        lastInputUuid = other.getLastInputUuid();
        lastRemoteUuids = other.getLastRemoteCapabilitiesUuids();
        lastThemeUuid = other.getLastThemeUuid();
        applicationUUID = other.getApplicationUUID();
        applicationName = other.getApplicationName();
        lastProperties = other.getLastProperties();
        namingRecursive = other.isNamingRecursive();
        saveToSrc = other.isSaveToSrc();
        useCppMain = other.isUseCppMain();
        eepromDef = other.getEepromDefinition();
        authDef = other.getAuthenticatorDefinition();
        return this;
    }

    public CodeGeneratorOptions codeOptions() {
        return new CodeGeneratorOptions(embeddedPlatform, lastDisplayUuid, lastInputUuid, lastRemoteUuids, lastThemeUuid,
                lastProperties, applicationUUID, applicationName, eepromDef, authDef, namingRecursive, saveToSrc, useCppMain);
    }

    public CodeGeneratorOptionsBuilder withRecursiveNaming(Boolean recursive) {
        namingRecursive = recursive;
        return this;
    }

    public CodeGeneratorOptionsBuilder withSaveToSrc(Boolean saveSrc) {
        saveToSrc = saveSrc;
        return this;
    }

    public CodeGeneratorOptionsBuilder withCppMain(Boolean cppMain) {
        useCppMain = cppMain;
        return this;
    }

    public CodeGeneratorOptionsBuilder withNewId(UUID uuid) {
        applicationUUID = uuid;
        return this;
    }

    public CodeGeneratorOptionsBuilder withAppName(String newValue) {
        applicationName = newValue;
        return this;
    }

    public CodeGeneratorOptionsBuilder withEepromDefinition(EepromDefinition eepromDef) {
        this.eepromDef = eepromDef;
        return this;
    }

    public CodeGeneratorOptionsBuilder withPlatform(String boardId) {
        this.embeddedPlatform = boardId;
        return this;
    }

    public CodeGeneratorOptionsBuilder withDisplay(String id) {
        this.lastDisplayUuid = id;
        return this;
    }

    public CodeGeneratorOptionsBuilder withTheme(String id) {
        this.lastThemeUuid = id;
        return this;
    }

    public CodeGeneratorOptionsBuilder withInput(String id) {
        this.lastInputUuid = id;
        return this;
    }
    public CodeGeneratorOptionsBuilder withRemotes(List<String> id) {
        this.lastRemoteUuids = id;
        return this;
    }

    public CodeGeneratorOptionsBuilder withProperties(ArrayList<CreatorProperty> allProps) {
        this.lastProperties = allProps;
        return this;
    }

    public CodeGeneratorOptionsBuilder withAuthenticationDefinition(AuthenticatorDefinition authSel) {
        this.authDef = authSel;
        return this;
    }
}