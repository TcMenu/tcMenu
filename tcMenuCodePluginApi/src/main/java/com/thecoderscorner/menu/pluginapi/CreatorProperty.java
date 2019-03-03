/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi;

import com.thecoderscorner.menu.pluginapi.validation.PropertyValidationRules;
import com.thecoderscorner.menu.pluginapi.validation.StringPropertyValidationRules;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;

public class CreatorProperty {
    private static final PropertyValidationRules BASE_RULE = new StringPropertyValidationRules(false, 32);

    public enum PropType { USE_IN_DEFINE, VARIABLE, TEXTUAL }

    private String name;
    private String latestValue;
    private SubSystem subsystem;
    transient private String description;
    transient private StringProperty property;
    transient private PropType propType = PropType.TEXTUAL;
    transient private PropertyValidationRules validationRules = BASE_RULE;

    public CreatorProperty() {
        // for serialisation purposes.
        this.property = null;
    }

    public CreatorProperty(String name, String description, String latestValue, SubSystem subsystem) {
        this(name, description, latestValue, subsystem, PropType.USE_IN_DEFINE, BASE_RULE);
    }

    public CreatorProperty(String name, String description, String latestValue, SubSystem subsystem, PropertyValidationRules rules) {
        this(name, description, latestValue, subsystem, PropType.USE_IN_DEFINE, rules);
    }

    public CreatorProperty(String name, String description, String latestValue, SubSystem subsystem,
                           PropType propType, PropertyValidationRules rules) {
        this.name = name;
        this.description = description;
        this.latestValue = latestValue;
        this.property = new SimpleStringProperty(latestValue);
        this.subsystem = subsystem;
        this.propType = propType;
        this.validationRules = rules;
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

    public PropertyValidationRules getValidationRules() {
        return validationRules;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreatorProperty that = (CreatorProperty) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                getPropType() == that.getPropType() &&
                Objects.equals(getLatestValue(), that.getLatestValue()) &&
                getSubsystem() == that.getSubsystem();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDescription(), getPropType(), getLatestValue(), getSubsystem());
    }

    @Override
    public String toString() {
        return "CreatorProperty{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", property=" + property +
                ", propType=" + propType +
                ", latestValue='" + latestValue + '\'' +
                ", subsystem=" + subsystem +
                '}';
    }
}
