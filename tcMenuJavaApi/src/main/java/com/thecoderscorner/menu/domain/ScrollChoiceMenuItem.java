package com.thecoderscorner.menu.domain;

import com.thecoderscorner.menu.domain.util.MenuItemVisitor;

import java.util.Objects;

/**
 * Represents a more configurable and more extensible version of enum that should be used when the number of choices is
 * larger, the choices are in eeprom, or you need more control at runtime of the choices.
 */
public class ScrollChoiceMenuItem extends MenuItem {
    public enum ScrollChoiceMode {ARRAY_IN_EEPROM, ARRAY_IN_RAM, CUSTOM_RENDERFN}
    private final int itemWidth;
    private final int eepromOffset;
    private final int numEntries;
    private final ScrollChoiceMode choiceMode;
    private final String variable;

    public ScrollChoiceMenuItem() {
        super("", null,-1, -1, null, false, false, true);
        variable = null;
        choiceMode = ScrollChoiceMode.ARRAY_IN_EEPROM;
        itemWidth = numEntries = eepromOffset = 0;
    }

    public ScrollChoiceMenuItem(String name, String varName, int id, int eepromAddress, String functionName, int itemWidth, int eepromOffset,
                                int numEntries, ScrollChoiceMode mode, String variable, boolean readOnly, boolean localOnly,
                                boolean visible) {
        super(name, varName, id, eepromAddress, functionName, readOnly, localOnly, visible);
        this.numEntries = numEntries;
        this.itemWidth = itemWidth;
        this.eepromOffset = eepromOffset;
        this.choiceMode = mode;
        this.variable = variable;
    }

    public int getItemWidth() {
        return itemWidth;
    }

    public int getEepromOffset() {
        return eepromOffset;
    }

    public int getNumEntries() {
        return numEntries;
    }

    public ScrollChoiceMode getChoiceMode() {
        return choiceMode;
    }

    public String getVariable() {
        return variable;
    }

    @Override
    public void accept(MenuItemVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScrollChoiceMenuItem that = (ScrollChoiceMenuItem) o;
        return getId() == that.getId() &&
                getEepromAddress() == that.getEepromAddress() &&
                isReadOnly() == that.isReadOnly() &&
                isVisible() == that.isVisible() &&
                isLocalOnly() == that.isLocalOnly() &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getFunctionName(), that.getFunctionName()) &&
                Objects.equals(getVariableName(), that.getVariableName()) &&
                choiceMode == that.getChoiceMode() && itemWidth == that.itemWidth && numEntries == that.numEntries &&
                eepromOffset == that.eepromOffset && Objects.equals(variable, that.variable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getId(), getEepromAddress(), getFunctionName(), getVariableName(), isReadOnly(),
                isLocalOnly(), choiceMode, itemWidth, numEntries, eepromOffset, variable);
    }
}
