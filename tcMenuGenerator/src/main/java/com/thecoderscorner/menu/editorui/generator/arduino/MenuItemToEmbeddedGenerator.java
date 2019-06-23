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

import static com.thecoderscorner.menu.domain.RuntimeListMenuItemBuilder.makeRtCallName;
import static com.thecoderscorner.menu.domain.util.MenuItemHelper.makeNameToVar;

/**
 * This class follows the visitor pattern to generate code for each item
 */
public class MenuItemToEmbeddedGenerator extends AbstractMenuItemVisitor<List<BuildStructInitializer>> {
    private final String nextMenuName;
    private final String nextChild;

    public MenuItemToEmbeddedGenerator(String next) {
        nextMenuName = (next == null) ? "NULL" : "&menu" + makeNameToVar(next);
        nextChild = null;
    }

    public MenuItemToEmbeddedGenerator(String nextName, String nextCh) {
        nextMenuName = (nextName == null) ? "NULL" : "&menu" + makeNameToVar(nextName);
        nextChild = (nextCh == null) ? "NULL" : "&menu" + makeNameToVar(nextCh);
    }

    @Override
    public void visit(AnalogMenuItem item) {
        String nameNoSpaces = makeNameToVar(item.getName());

        BuildStructInitializer info = new BuildStructInitializer(item, nameNoSpaces, "AnalogMenuInfo")
                .addQuoted(item.getName())
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(item.getMaxValue())
                .addPossibleFunction(item.getFunctionName())
                .addElement(item.getOffset())
                .addElement(item.getDivisor())
                .addQuoted(item.getUnitName())
                .progMemInfo();

        BuildStructInitializer menu = new BuildStructInitializer(item, nameNoSpaces, "AnalogMenuItem")
                .addElement("&minfo" + nameNoSpaces)
                .addElement(0)
                .addElement(nextMenuName)
                .requiresExtern();

        setResult(Arrays.asList(info, menu));
    }

    @Override
    public void visit(EditableTextMenuItem item) {
        String nameNoSpaces = makeNameToVar(item.getName());

        if(item.getItemType() == EditItemType.IP_ADDRESS) {
            BuildStructInitializer menu = new BuildStructInitializer(item, nameNoSpaces, "IpAddressMenuItem")
                    .addElement(makeRtCallName(nameNoSpaces))
                    .addElement(item.getId())
                    .addElement(nextMenuName)
                    .requiresExtern();
            setResult(List.of(menu));
        }
        else {
            BuildStructInitializer menu = new BuildStructInitializer(item, nameNoSpaces, "TextMenuItem")
                    .addElement(makeRtCallName(nameNoSpaces))
                    .addElement(item.getId())
                    .addElement(item.getTextLength())
                    .addElement(nextMenuName)
                    .requiresExtern()
                    .addHeaderFileRequirement("RuntimeMenuItem.h", false);
            setResult(List.of(menu));
        }
    }

    @Override
    public void visit(RemoteMenuItem item) {
        String nameNoSpaces = makeNameToVar(item.getName());

        BuildStructInitializer info = new BuildStructInitializer(item, nameNoSpaces, "RemoteMenuInfo")
                .addQuoted(item.getName())
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(item.getRemoteNum())
                .addPossibleFunction(item.getFunctionName())
                .progMemInfo();

        BuildStructInitializer menu = new BuildStructInitializer(item, nameNoSpaces, "RemoteMenuItem")
                .addHeaderFileRequirement("RemoteMenuItem.h", false)
                .addElement("&minfo" + nameNoSpaces)
                .addElement("remoteServer.getRemoteConnector(" + item.getRemoteNum() + ")")
                .addElement(nextMenuName)
                .requiresExtern();

        setResult(Arrays.asList(info, menu));
    }

    @Override
    public void visit(ActionMenuItem item) {
        String nameNoSpaces = makeNameToVar(item.getName());

        BuildStructInitializer info = new BuildStructInitializer(item, nameNoSpaces, "AnyMenuInfo")
                .addQuoted(item.getName())
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(0)
                .addPossibleFunction(item.getFunctionName())
                .progMemInfo();

        BuildStructInitializer menu = new BuildStructInitializer(item, nameNoSpaces, "ActionMenuItem")
                .addElement("&minfo" + nameNoSpaces)
                .addElement(nextMenuName)
                .requiresExtern();

        setResult(Arrays.asList(info, menu));
    }

    @Override
    public void visit(FloatMenuItem item) {
        String nameNoSpaces = makeNameToVar(item.getName());

        BuildStructInitializer info = new BuildStructInitializer(item, nameNoSpaces, "FloatMenuInfo")
                .addQuoted(item.getName())
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(item.getNumDecimalPlaces())
                .addPossibleFunction(item.getFunctionName())
                .progMemInfo();

        BuildStructInitializer menu = new BuildStructInitializer(item, nameNoSpaces, "FloatMenuItem")
                .addElement("&minfo" + nameNoSpaces)
                .addElement(nextMenuName)
                .requiresExtern();

        setResult(Arrays.asList(info, menu));
    }

    @Override
    public void visit(BooleanMenuItem item) {
        String nameNoSpaces = makeNameToVar(item.getName());
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

        BuildStructInitializer info = new BuildStructInitializer(item, nameNoSpaces, "BooleanMenuInfo")
                .addQuoted(item.getName())
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(1)
                .addPossibleFunction(item.getFunctionName())
                .addElement(itemNaming)
                .progMemInfo();

        BuildStructInitializer menu = new BuildStructInitializer(item, nameNoSpaces, "BooleanMenuItem")
                .addElement("&minfo" + nameNoSpaces)
                .addElement(false)
                .addElement(nextMenuName)
                .requiresExtern();

        setResult(Arrays.asList(info, menu));
    }

    @Override
    public void visit(EnumMenuItem item) {
        String nameNoSpaces = makeNameToVar(item.getName());

        BuildStructInitializer choices = new BuildStructInitializer(item, nameNoSpaces, "")
                .stringChoices()
                .collectionOfElements(item.getEnumEntries(), true);

        BuildStructInitializer info = new BuildStructInitializer(item, nameNoSpaces, "EnumMenuInfo")
                .addQuoted(item.getName())
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(item.getEnumEntries().size() - 1)
                .addPossibleFunction(item.getFunctionName())
                .addElement("enumStr" + nameNoSpaces)
                .progMemInfo();

        BuildStructInitializer menu = new BuildStructInitializer(item, nameNoSpaces, "EnumMenuItem")
                .addElement("&minfo" + nameNoSpaces)
                .addElement(0)
                .addElement(nextMenuName)
                .requiresExtern();

        setResult(Arrays.asList(choices, info, menu));
    }

   @Override
    public void visit(RuntimeListMenuItem listItem) {
        String nameNoSpaces = makeNameToVar(listItem.getName());
        BuildStructInitializer listStruct = new BuildStructInitializer(listItem, nameNoSpaces, "ListRuntimeMenuItem")
                .addElement(listItem.getId())
                .addElement(listItem.getInitialRows())
                .addElement(makeRtCallName(nameNoSpaces))
                .addHeaderFileRequirement("RuntimeMenuItem.h", false)
                .addElement(nextMenuName)
                .requiresExtern();
        setResult(List.of(listStruct));
    }

    @Override
    public void visit(SubMenuItem item) {
        String nameNoSpaces = makeNameToVar(item.getName());

        BuildStructInitializer info = new BuildStructInitializer(item, nameNoSpaces, "SubMenuInfo")
                .addQuoted(item.getName())
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(0)
                .addPossibleFunction(item.getFunctionName())
                .progMemInfo();

        BuildStructInitializer menuBack = new BuildStructInitializer(item, "Back" + nameNoSpaces, "BackMenuItem")
                .addElement(makeRtCallName(nameNoSpaces))
                .addElement(nextChild)
                .requiresExtern();

        BuildStructInitializer menu = new BuildStructInitializer(item, nameNoSpaces, "SubMenuItem")
                .addElement("&minfo" + nameNoSpaces)
                .addElement("&menuBack" + nameNoSpaces)
                .addElement(nextMenuName)
                .requiresExtern();

        setResult(Arrays.asList(info, menuBack, menu));
    }
}
