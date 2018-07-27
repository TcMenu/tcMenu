/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CreatorProperty {
    private String name;
    transient private String description;
    transient private StringProperty property;
    transient private PropType propType;
    private String latestValue;
    private SubSystem subsystem;

    public enum SubSystem { DISPLAY, INPUT, REMOTE }
    public enum PropType { USE_IN_DEFINE, VARIABLE, TEXTUAL }

    public CreatorProperty() {
        // for serialisation purposes.
        this.property = null;
    }

    public CreatorProperty(String name, String description, String latestValue, SubSystem subsystem) {
        this(name, description, latestValue, subsystem, PropType.USE_IN_DEFINE);
    }

    public CreatorProperty(String name, String description, String latestValue, SubSystem subsystem, PropType propType) {
        this.name = name;
        this.description = description;
        this.latestValue = latestValue;
        this.property = new SimpleStringProperty(latestValue);
        this.subsystem = subsystem;
        this.propType = propType;
    }

    public String getLatestValue() {
        if(property != null) {
            latestValue = property.getValue();
        }
        return latestValue;
    }

    public StringProperty getProperty() {
        return property;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public SubSystem getSubsystem() {
        return subsystem;
    }

    public int getLatestValueAsInt() {
        return Integer.parseInt(getLatestValue());
    }

    public PropType getPropType() {
        return propType;
    }
}
