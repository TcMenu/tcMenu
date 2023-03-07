/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation;
import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.validation.CannedPropertyValidators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation.*;

/**
 * When code is being converted we need to know the context of the conversion, this context should contain all the
 * properties of the conversion and the name of the root item in the tree. It also contains the platform.
 */
public class CodeConversionContext {
    private final String rootObject;
    private final Collection<CreatorProperty> properties;
    private final EmbeddedPlatform platform;
    private final CodeGeneratorOptions options;

    public CodeConversionContext(EmbeddedPlatform platform, String rootObject, CodeGeneratorOptions options, List<CreatorProperty> properties) {
        this.rootObject = rootObject;
        this.options = options;

        properties = new ArrayList<>(properties);
        properties.add(new CreatorProperty("ROOT", "Root", "Root", rootObject, SubSystem.INPUT, CreatorProperty.PropType.TEXTUAL, CannedPropertyValidators.textValidator(), new AlwaysApplicable()));
        properties.add(new CreatorProperty("TARGET", "Target", "Target", platform.getBoardId(), SubSystem.INPUT, CreatorProperty.PropType.TEXTUAL, CannedPropertyValidators.textValidator(), new AlwaysApplicable()));
        properties.add(new CreatorProperty("NAMESPACE", "Namespace", "Namespace", options.getPackageNamespace(), SubSystem.INPUT, CreatorProperty.PropType.TEXTUAL, CannedPropertyValidators.textValidator(), new AlwaysApplicable()));
        properties.add(new CreatorProperty("APP_NAME", "App name", "App name", options.getApplicationName(), SubSystem.INPUT, CreatorProperty.PropType.TEXTUAL, CannedPropertyValidators.textValidator(), new AlwaysApplicable()));
        properties.add(new CreatorProperty("APP_UUID", "App ID", "App ID", options.getApplicationUUID().toString(), SubSystem.INPUT, CreatorProperty.PropType.TEXTUAL, CannedPropertyValidators.textValidator(), new AlwaysApplicable()));

        // when using the generated folder option to separate generated code, sometimes an offset is needed to access non-generated files (always back one level)
        ProjectSaveLocation sl = options.getSaveLocation();
        var srcOffset = (sl == PROJECT_TO_SRC_WITH_GENERATED || sl == PROJECT_TO_CURRENT_WITH_GENERATED) ? "../" : "";
        properties.add(new CreatorProperty("SRC_DIR_OFFSET", "Src Dir Offset", "Src Dir Offset", srcOffset, SubSystem.INPUT, CreatorProperty.PropType.TEXTUAL, CannedPropertyValidators.textValidator(), new AlwaysApplicable()));
        this.properties = properties;
        this.platform = platform;
    }

    public String getRootObject() {
        return rootObject;
    }

    public Collection<CreatorProperty> getProperties() {
        return properties;
    }

    public EmbeddedPlatform getPlatform() {
        return platform;
    }

    public CodeGeneratorOptions getOptions() { return options; }
}
