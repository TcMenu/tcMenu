/*
 * Copyright (c)  2016-2021 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator;

import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;

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
            return this;
        }

        public CodeGeneratorOptions codeOptions() {
            return new CodeGeneratorOptions(embeddedPlatform, lastDisplayUuid, lastInputUuid, lastRemoteUuids, lastThemeUuid,
                    lastProperties, applicationUUID, applicationName, namingRecursive, saveToSrc, useCppMain);
        }

    public CodeGeneratorOptionsBuilder withRecursiveNaming(Boolean recursvie) {
        namingRecursive = recursvie;
        return this;
    }
}