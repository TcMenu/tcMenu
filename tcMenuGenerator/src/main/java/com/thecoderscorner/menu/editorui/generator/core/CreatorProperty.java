/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.applicability.CodeApplicability;
import com.thecoderscorner.menu.editorui.generator.validation.PropertyValidationRules;
import com.thecoderscorner.menu.editorui.generator.validation.StringPropertyValidationRules;

import java.util.Objects;

/**
 * All creator instances can define properties. These are shown in the UI during code creation and can be edited by the
 * user. It is possible to validate the values entered into these properties and set different types and subsystems.
 *
 * Generally speaking the subsystem is the broad-brush level - IE is it a display or input. The type provides the
 * intended usage such as if the property defines a variable or is purely textual.
 *
 * Each object holds a string property and the current value is stored in here.
 */
public class CreatorProperty {
    private static final PropertyValidationRules BASE_RULE = new StringPropertyValidationRules(false, 32);

    /** Definitions of how a specific property is intended to be used */
    public enum PropType { USE_IN_DEFINE, VARIABLE, TEXTUAL }

    private String name;
    private String latestValue;
    private SubSystem subsystem;
    transient private final String initialValue;
    transient private final String description;
    transient private final String extendedDescription;
    transient private final CodeApplicability applicability;
    transient private PropType propType = PropType.TEXTUAL;
    transient private PropertyValidationRules validationRules = BASE_RULE;

    public CreatorProperty() {
        // for serialisation purposes.
        this.initialValue = null;
        this.description = null;
        this.extendedDescription = null;
        this.applicability = new AlwaysApplicable();
    }

    /**
     * Create a property setting all the fields upfront.
     * @param name the name of the property usually prefer uppercase with underscores in-between words
     * @param description a one line description of the properties intended use.
     * @param latestValue the value to be assigned to the property initially
     * @param subsystem the subsystem it belongs to
     * @param propType the type of the property
     * @param rules the validation rules to be applied
     */
    public CreatorProperty(String name, String description, String extendedDescription, String latestValue,
                           SubSystem subsystem, PropType propType, PropertyValidationRules rules,
                           CodeApplicability applicability) {
        this.name = name;
        this.description = description;
        this.extendedDescription = extendedDescription;
        this.initialValue = latestValue;
        this.latestValue = latestValue;
        this.subsystem = subsystem;
        this.propType = propType;
        this.validationRules = rules;
        this.applicability = applicability;
    }

    public void resetToInitial() {
        this.latestValue = this.initialValue;
    }

    /**
     * @return the latest value associated with this property
     */
    public String getLatestValue() {
        return latestValue;
    }

    public void setLatestValue(String latestValue) {
        this.latestValue = latestValue;
    }

    public CodeApplicability getApplicability() {
        return applicability;
    }

    /**
     * @return the name of the property
     */
    public String getName() {
        return name;
    }

    /**
     * @return the description for this property
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return which subsystem the property belongs to
     */
    public SubSystem getSubsystem() {
        return subsystem;
    }

    /** @return the initial value for this property */
    public String getInitialValue() {
        return initialValue;
    }

    /**
     * @return the type of property
     */
    public PropType getPropType() {
        return propType;
    }

    /**
     * @return the validation rules in use by this property
     */
    public PropertyValidationRules getValidationRules() {
        return validationRules;
    }

    /**
     * @return the extended description, intended to provide a more detailed description of the property
     */
    public String getExtendedDescription() {
        return extendedDescription;
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
                ", propType=" + propType +
                ", latestValue='" + latestValue + '\'' +
                ", subsystem=" + subsystem +
                '}';
    }
}
