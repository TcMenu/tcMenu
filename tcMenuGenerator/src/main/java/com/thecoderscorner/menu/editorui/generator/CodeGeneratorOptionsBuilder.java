/*
 * Copyright (c)  2016-2021 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator;

import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.parameters.AuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.EepromDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinitionCollection;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.NoAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.eeprom.NoEepromDefinition;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatforms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.thecoderscorner.menu.editorui.project.CurrentEditorProject.NO_CREATOR_SELECTED;

public class CodeGeneratorOptionsBuilder {
    private String embeddedPlatform = EmbeddedPlatform.ARDUINO32.getBoardId();
    private String lastDisplayUuid = NO_CREATOR_SELECTED;
    private String lastInputUuid = NO_CREATOR_SELECTED;
    private List<String> lastRemoteUuids = List.of(NO_CREATOR_SELECTED);
    private String lastThemeUuid = NO_CREATOR_SELECTED;
    private UUID applicationUUID = UUID.randomUUID();
    private String applicationName = "New Device";
    private List<CreatorProperty> lastProperties = List.of();
    private IoExpanderDefinitionCollection expanderDefinitions = new IoExpanderDefinitionCollection();
    private boolean namingRecursive = false;
    private boolean saveToSrc = false;
    private boolean useCppMain = false;
    private EepromDefinition eepromDef = new NoEepromDefinition();
    private AuthenticatorDefinition authDef = new NoAuthenticatorDefinition();

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
        expanderDefinitions = other.getExpanderDefinitions();
        return this;
    }

    public CodeGeneratorOptions codeOptions() {
        return new CodeGeneratorOptions(embeddedPlatform, lastDisplayUuid, lastInputUuid, lastRemoteUuids, lastThemeUuid,
                lastProperties, applicationUUID, applicationName, eepromDef, authDef, expanderDefinitions,
                namingRecursive, saveToSrc, useCppMain);
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

    public CodeGeneratorOptionsBuilder withExpanderDefinitions(IoExpanderDefinitionCollection expanders) {
        this.expanderDefinitions = expanders;
        return this;
    }
}