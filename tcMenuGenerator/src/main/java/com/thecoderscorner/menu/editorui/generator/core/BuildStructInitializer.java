/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType.*;
import static com.thecoderscorner.menu.editorui.util.StringHelper.isStringEmptyOrNull;

/**
 * This represents the menu info structure as used on a lot of boards. It is a code independent way of expressing
 * the structures needed for menu items and the tree on at least all Arduino boards.
 */
public class BuildStructInitializer {
    private final MenuItem menuItem;
    private final String structName;
    private final String structType;
    private List<HeaderDefinition> headerRequirement = new ArrayList<>();
    private List<String> structElements = new ArrayList<>();
    private boolean requiresExtern = false;
    private boolean progMem = false;
    private boolean stringChoices = false;
    private String prefix = " menu";

    public BuildStructInitializer(MenuItem item, String structName, String structType) {
        this.menuItem = item;
        this.structName = structName;
        this.structType = structType;
    }

    public BuildStructInitializer addQuoted(String value) {
        structElements.add('\"' + value + '\"');
        return this;
    }

    private String safeValue(Object val) {
        return (val != null) ? val.toString() : "";
    }

    public BuildStructInitializer addElement(Object value) {
        structElements.add(safeValue(value));
        return this;
    }

    public BuildStructInitializer addHeaderFileRequirement(String include, boolean quotes) {
        headerRequirement.add(new HeaderDefinition(include, quotes ? SOURCE : GLOBAL, HeaderDefinition.PRIORITY_NORMAL, new AlwaysApplicable()));
        return this;
    }

    public BuildStructInitializer addPossibleFunction(String functionName) {
        structElements.add((!isStringEmptyOrNull(functionName)) ? functionName : "NO_CALLBACK");
        return this;
    }

    public BuildStructInitializer addEeprom(int eepromAddress) {
        if(eepromAddress == -1) {
            structElements.add("0xffff");
        }
        else {
            structElements.add(Integer.toString(eepromAddress));
        }
        return this;
    }

    public List<HeaderDefinition> getHeaderRequirements() {
        return headerRequirement;
    }

    public BuildStructInitializer requiresExtern() {
        this.requiresExtern = true;
        return this;
    }

    public BuildStructInitializer progMemInfo() {
        this.progMem = true;
        this.prefix = " minfo";
        return this;
    }

    public BuildStructInitializer progMemStruct() {
        this.prefix = " ";
        this.progMem = true;
        return this;
    }

    public BuildStructInitializer stringChoices() {
        this.stringChoices = true;
        return this;
    }

    public BuildStructInitializer collectionOfElements(List<String> enumEntries, boolean requiresQuotes) {
        if(requiresQuotes) {
            structElements.addAll(enumEntries.stream()
                    .map(entry -> "\"" + entry + "\"")
                    .collect(Collectors.toList())
            );
        }
        else {
            structElements.addAll(enumEntries);
        }

        return this;
    }

    public String getStructName() {
        return structName;
    }

    public String getStructType() {
        return structType;
    }

    public List<String> getStructElements() {
        return structElements;
    }

    public boolean isRequiresExtern() {
        return requiresExtern;
    }

    public boolean isProgMem() {
        return progMem;
    }

    public boolean isStringChoices() {
        return stringChoices;
    }

    public String getPrefix() {
        return prefix;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }
}
