package com.thecoderscorner.menu.web.domain;

import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import lombok.Value;


@Value
public class PublishableCreationProperty {
    String name;
    SubSystem subsystem;
    String initialValue;

    public PublishableCreationProperty(CreatorProperty prop) {
        this.name = prop.getName();
        this.subsystem = prop.getSubsystem();
        this.initialValue = prop.getInitialValue();
    }
}
