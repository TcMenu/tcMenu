/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.util.AbstractMenuItemVisitor;
import com.thecoderscorner.menu.pluginapi.model.BuildStructInitializer;

import java.util.Arrays;
import java.util.List;

/**
 * This class follows the visitor pattern to generate code for each item
 */
public class MenuItemToEmbeddedGenerator extends AbstractMenuItemVisitor<List<BuildStructInitializer>> {
    private final String nextMenuName;
    private final String nextChild;
    private final String itemVar;

    public MenuItemToEmbeddedGenerator(String itemVar, String nextVar) {
        this.nextMenuName = generateMenuVariable(nextVar);
        this.itemVar = itemVar;
        this.nextChild = null;
    }

    public MenuItemToEmbeddedGenerator(String itemVar, String nextVar, String nextCh) {
        this.nextMenuName = generateMenuVariable(nextVar);
        this.nextChild = generateMenuVariable(nextCh);
        this.itemVar = itemVar;
    }

    private String generateMenuVariable(String var) {
        return (var == null || var.equals("NULL")) ? "NULL" : ("&menu" + var);
    }

    @Override
    public void visit(AnalogMenuItem item) {
        BuildStructInitializer info = new BuildStructInitializer(item, itemVar, "AnalogMenuInfo")
                .addQuoted(item.getName())
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(item.getMaxValue())
                .addPossibleFunction(item.getFunctionName())
                .addElement(item.getOffset())
                .addElement(item.getDivisor())
                .addQuoted(item.getUnitName())
                .progMemInfo();

        BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "AnalogMenuItem")
                .addElement("&minfo" + itemVar)
                .addElement(0)
                .addElement(nextMenuName)
                .requiresExtern();

        setResult(Arrays.asList(info, menu));
    }

    @Override
    public void visit(EditableTextMenuItem item) {
        if(item.getItemType() == EditItemType.IP_ADDRESS) {
            BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "IpAddressMenuItem")
                    .addElement(makeRtFunctionName())
                    .addElement(item.getId())
                    .addElement(nextMenuName)
                    .requiresExtern();
            setResult(List.of(menu));
        }
        else if(item.getItemType() == EditItemType.PLAIN_TEXT){
            BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "TextMenuItem")
                    .addElement(makeRtFunctionName())
                    .addElement(item.getId())
                    .addElement(item.getTextLength())
                    .addElement(nextMenuName)
                    .requiresExtern()
                    .addHeaderFileRequirement("RuntimeMenuItem.h", false);
            setResult(List.of(menu));
        }
        else {
            // time based
            BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "TimeFormattedMenuItem")
                    .addElement(makeRtFunctionName())
                    .addElement(item.getId())
                    .addElement(item.getItemType().getMsgId())
                    .addElement(nextMenuName)
                    .requiresExtern()
                    .addHeaderFileRequirement("RuntimeMenuItem.h", false);
            setResult(List.of(menu));
        }
    }

    private String makeRtFunctionName() {
        return "fn" + itemVar + "RtCall";
    }

    @Override
    public void visit(ActionMenuItem item) {

        BuildStructInitializer info = new BuildStructInitializer(item, itemVar, "AnyMenuInfo")
                .addQuoted(item.getName())
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(0)
                .addPossibleFunction(item.getFunctionName())
                .progMemInfo();

        BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "ActionMenuItem")
                .addElement("&minfo" + itemVar)
                .addElement(nextMenuName)
                .requiresExtern();

        setResult(Arrays.asList(info, menu));
    }

    @Override
    public void visit(FloatMenuItem item) {
        BuildStructInitializer info = new BuildStructInitializer(item, itemVar, "FloatMenuInfo")
                .addQuoted(item.getName())
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(item.getNumDecimalPlaces())
                .addPossibleFunction(item.getFunctionName())
                .progMemInfo();

        BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "FloatMenuItem")
                .addElement("&minfo" + itemVar)
                .addElement(nextMenuName)
                .requiresExtern();

        setResult(Arrays.asList(info, menu));
    }

    @Override
    public void visit(BooleanMenuItem item) {
        String itemNaming;
        switch (item.getNaming()) {
            case ON_OFF:
                itemNaming = "NAMING_ON_OFF";
                break;
            case YES_NO:
                itemNaming = "NAMING_YES_NO";
                break;
            case TRUE_FALSE:
            default:
                itemNaming = "NAMING_TRUE_FALSE";
                break;
        }

        BuildStructInitializer info = new BuildStructInitializer(item, itemVar, "BooleanMenuInfo")
                .addQuoted(item.getName())
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(1)
                .addPossibleFunction(item.getFunctionName())
                .addElement(itemNaming)
                .progMemInfo();

        BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "BooleanMenuItem")
                .addElement("&minfo" + itemVar)
                .addElement(false)
                .addElement(nextMenuName)
                .requiresExtern();

        setResult(Arrays.asList(info, menu));
    }

    @Override
    public void visit(EnumMenuItem item) {
        BuildStructInitializer choices = new BuildStructInitializer(item, itemVar, "")
                .stringChoices()
                .collectionOfElements(item.getEnumEntries(), true);

        BuildStructInitializer info = new BuildStructInitializer(item, itemVar, "EnumMenuInfo")
                .addQuoted(item.getName())
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(item.getEnumEntries().size() - 1)
                .addPossibleFunction(item.getFunctionName())
                .addElement("enumStr" + itemVar)
                .progMemInfo();

        BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "EnumMenuItem")
                .addElement("&minfo" + itemVar)
                .addElement(0)
                .addElement(nextMenuName)
                .requiresExtern();

        setResult(Arrays.asList(choices, info, menu));
    }

   @Override
    public void visit(RuntimeListMenuItem listItem) {
        BuildStructInitializer listStruct = new BuildStructInitializer(listItem, itemVar, "ListRuntimeMenuItem")
                .addElement(listItem.getId())
                .addElement(listItem.getInitialRows())
                .addElement(makeRtFunctionName())
                .addHeaderFileRequirement("RuntimeMenuItem.h", false)
                .addElement(nextMenuName)
                .requiresExtern();
        setResult(List.of(listStruct));
    }

    @Override
    public void visit(SubMenuItem item) {
        BuildStructInitializer info = new BuildStructInitializer(item, itemVar, "SubMenuInfo")
                .addQuoted(item.getName())
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(0)
                .addPossibleFunction(item.getFunctionName())
                .progMemInfo();

        BuildStructInitializer menuBack = new BuildStructInitializer(item, "Back" + itemVar, "BackMenuItem")
                .addElement(makeRtFunctionName())
                .addElement(nextChild)
                .requiresExtern();

        BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "SubMenuItem")
                .addElement("&minfo" + itemVar)
                .addElement("&menuBack" + itemVar)
                .addElement(nextMenuName)
                .requiresExtern();

        setResult(Arrays.asList(info, menuBack, menu));
    }
}
