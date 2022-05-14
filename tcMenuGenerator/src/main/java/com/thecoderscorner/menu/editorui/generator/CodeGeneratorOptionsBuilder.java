/*
 * Copyright (c)  2016-2021 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator;

import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.parameters.AuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.EepromDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinitionCollection;
import com.thecoderscorner.menu.editorui.generator.parameters.MenuInMenuCollection;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.NoAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.eeprom.NoEepromDefinition;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
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
    private String packageNamespace = "";
    private List<CreatorProperty> lastProperties = List.of();
    private IoExpanderDefinitionCollection expanderDefinitions = new IoExpanderDefinitionCollection();
    private boolean namingRecursive = false;
    private boolean appIsModular = false;
    private boolean saveToSrc = false;
    private boolean useCppMain = false;
    private EepromDefinition eepromDef = new NoEepromDefinition();
    private AuthenticatorDefinition authDef = new NoAuthenticatorDefinition();
    private MenuInMenuCollection menuInMenuDefinitions;

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
        packageNamespace = other.getPackageNamespace();
        appIsModular = other.isModularApp();
        menuInMenuDefinitions = other.getMenuInMenuCollection();
        return this;
    }

    public CodeGeneratorOptions codeOptions() {
        return new CodeGeneratorOptions(embeddedPlatform, lastDisplayUuid, lastInputUuid, lastRemoteUuids, lastThemeUuid,
                lastProperties, applicationUUID, applicationName, packageNamespace, eepromDef, authDef, expanderDefinitions,
                menuInMenuDefinitions, appIsModular, namingRecursive, saveToSrc, useCppMain);
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

    public CodeGeneratorOptionsBuilder withModularApp(boolean modularApp) {
        this.appIsModular = modularApp;
        return this;
    }

    public CodeGeneratorOptionsBuilder withPackageNamespace(String namespace) {
        packageNamespace = namespace;
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

    public CodeGeneratorOptionsBuilder withProperties(List<CreatorProperty> allProps) {
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

    public CodeGeneratorOptionsBuilder withMenuInMenu(MenuInMenuCollection collection) {
        this.menuInMenuDefinitions = collection;
        return this;
    }
}