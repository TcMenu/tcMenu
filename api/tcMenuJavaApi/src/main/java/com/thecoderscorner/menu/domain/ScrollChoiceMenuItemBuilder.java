package com.thecoderscorner.menu.domain;

import com.thecoderscorner.menu.domain.build.MenuTreeBuilder;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;

import java.util.Objects;

/**
 * Builds an extensible version of enum that should be used when the number of choices is larger, the choices are in
 * eeprom, or you need more control at runtime of the choices using the builder pattern.
 */
public class ScrollChoiceMenuItemBuilder extends MenuItemBuilder<ScrollChoiceMenuItemBuilder, ScrollChoiceMenuItem> {
    private final MenuTreeBuilder menuTreeBuilder;
    private final int defVal;
    private int itemWidth;
    private int eepromOffset;
    private int numEntries;
    private ScrollChoiceMenuItem.ScrollChoiceMode choiceMode = ScrollChoiceMenuItem.ScrollChoiceMode.ARRAY_IN_EEPROM;
    private String variable;

    public ScrollChoiceMenuItemBuilder(MenuTreeBuilder menuTreeBuilder, int defVal) {
        this.menuTreeBuilder = menuTreeBuilder;
        this.defVal = defVal;
    }
 
    public ScrollChoiceMenuItemBuilder() {
        this.menuTreeBuilder = null;
        this.defVal = 0;
    }

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

    /**
     * Should only be called if this builder was created with a MenuTreeBuilder
     * @return the actual builder
     */
    public MenuTreeBuilder endItem() {
        var item = menuItem();
        Objects.requireNonNull(menuTreeBuilder).rawPushItem(item);
        MenuItemHelper.setMenuState(item, defVal, menuTreeBuilder.asTree());
        return menuTreeBuilder;
    }

    public ScrollChoiceMenuItem menuItem() {
        return new ScrollChoiceMenuItem(name, variableName, id, eepromAddr, functionName, itemWidth, eepromOffset, numEntries, choiceMode,
                variable, readOnly, localOnly, visible, staticDataInRAM);
    }
}
