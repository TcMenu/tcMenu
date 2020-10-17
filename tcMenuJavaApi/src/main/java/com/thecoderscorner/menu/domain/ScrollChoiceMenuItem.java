package com.thecoderscorner.menu.domain;

import com.thecoderscorner.menu.domain.state.CurrentScrollPosition;
import com.thecoderscorner.menu.domain.state.CurrentScrollPositionMenuState;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.util.MenuItemVisitor;

import java.util.Enumeration;

/**
 * Represents a more configurable and more extensible version of enum that should be used when the number of choices is
 * larger, the choices are in eeprom, or you need more control at runtime of the choices.
 */
public class ScrollChoiceMenuItem extends MenuItem<CurrentScrollPosition> {
    public enum ScrollChoiceMode {ARRAY_IN_EEPROM, ARRAY_IN_RAM, CUSTOM_RENDERFN}
    private final int itemWidth;
    private final int eepromOffset;
    private final int numEntries;
    private final ScrollChoiceMode choiceMode;
    private final String variable;

    public ScrollChoiceMenuItem() {
        super("", -1, -1, null, false, false, true);
        variable = null;
        choiceMode = ScrollChoiceMode.ARRAY_IN_EEPROM;
        itemWidth = numEntries = eepromOffset = 0;
    }

    public ScrollChoiceMenuItem(String name, int id, int eepromAddress, String functionName, int itemWidth, int eepromOffset,
                                int numEntries, ScrollChoiceMode mode, String variable, boolean readOnly, boolean localOnly,
                                boolean visible) {
        super(name, id, eepromAddress, functionName, readOnly, localOnly, visible);
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
    public MenuState<CurrentScrollPosition> newMenuState(CurrentScrollPosition value, boolean changed, boolean active) {
        return new CurrentScrollPositionMenuState(this, changed, active, value);
    }

    @Override
    public void accept(MenuItemVisitor visitor) {
        visitor.visit(this);
    }
}
