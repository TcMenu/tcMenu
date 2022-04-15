package com.thecoderscorner.menu.domain;

/**
 * Builds an extensible version of enum that should be used when the number of choices is larger, the choices are in
 * eeprom, or you need more control at runtime of the choices using the builder pattern.
 */
public class ScrollChoiceMenuItemBuilder extends MenuItemBuilder<ScrollChoiceMenuItemBuilder, ScrollChoiceMenuItem> {
    private int itemWidth;
    private int eepromOffset;
    private int numEntries;
    private ScrollChoiceMenuItem.ScrollChoiceMode choiceMode = ScrollChoiceMenuItem.ScrollChoiceMode.ARRAY_IN_EEPROM;
    private String variable;

    @Override
    ScrollChoiceMenuItemBuilder getThis() {
        return this;
    }

    public ScrollChoiceMenuItemBuilder withItemWidth(int itemWidth) {
        this.itemWidth = itemWidth;
        return getThis();
    }

    public ScrollChoiceMenuItemBuilder withEepromOffset(int eepromOffset) {
        this.eepromOffset = eepromOffset;
        return getThis();
    }

    public ScrollChoiceMenuItemBuilder withNumEntries(int numEntries) {
        this.numEntries = numEntries;
        return getThis();
    }

    public ScrollChoiceMenuItemBuilder withChoiceMode(ScrollChoiceMenuItem.ScrollChoiceMode choiceMode) {
        this.choiceMode = choiceMode;
        return getThis();
    }

    public ScrollChoiceMenuItemBuilder withVariable(String variable) {
        this.variable = variable;
        return getThis();
    }

    public ScrollChoiceMenuItemBuilder withExisting(ScrollChoiceMenuItem item) {
        baseFromExisting(item);
        itemWidth = item.getItemWidth();
        numEntries = item.getNumEntries();
        eepromOffset = item.getEepromOffset();
        choiceMode = item.getChoiceMode();
        variable = item.getVariable();
        return getThis();
    }

    public ScrollChoiceMenuItem menuItem() {
        return new ScrollChoiceMenuItem(name, variableName, id, eepromAddr, functionName, itemWidth, eepromOffset, numEntries, choiceMode,
                variable, readOnly, localOnly, visible);
    }
}
