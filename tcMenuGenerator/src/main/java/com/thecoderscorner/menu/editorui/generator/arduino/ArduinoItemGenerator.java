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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ArduinoItemGenerator extends AbstractMenuItemVisitor<List<BuildStructInitializer>> {
    private final String nextMenuName;
    private final String nextChild;

    public ArduinoItemGenerator(String next) {
        nextMenuName = (next == null) ? "NULL" : "&menu" + makeNameToVar(next);
        nextChild = null;
    }

    public ArduinoItemGenerator(String nextName, String nextCh) {
        nextMenuName = (nextName == null) ? "NULL" : "&menu" + makeNameToVar(nextName);
        nextChild = (nextCh == null) ? "NULL" : "&menu" + makeNameToVar(nextCh);
    }

    @Override
    public void visit(AnalogMenuItem item) {
        String nameNoSpaces = makeNameToVar(item.getName());

        BuildStructInitializer info = new BuildStructInitializer(nameNoSpaces, "AnalogMenuInfo")
                .addQuoted(item.getName())
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(item.getMaxValue())
                .addPossibleFunction(item.getFunctionName())
                .addElement(item.getOffset())
                .addElement(item.getDivisor())
                .addQuoted(item.getUnitName())
                .progMemInfo();

        BuildStructInitializer menu = new BuildStructInitializer(nameNoSpaces, "AnalogMenuItem")
                .addElement("&minfo" + nameNoSpaces)
                .addElement(0)
                .addElement(nextMenuName)
                .requiresExtern();

        setResult(Arrays.asList(info, menu));
    }

    @Override
    public void visit(TextMenuItem item) {
        String nameNoSpaces = makeNameToVar(item.getName());

        BuildStructInitializer info = new BuildStructInitializer(nameNoSpaces, "TextMenuInfo")
                .addQuoted(item.getName())
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(item.getTextLength())
                .addPossibleFunction(item.getFunctionName())
                .progMemInfo();

        BuildStructInitializer menu = new BuildStructInitializer(nameNoSpaces, "TextMenuItem")
                .addElement("&minfo" + nameNoSpaces)
                .addElement(nextMenuName)
                .requiresExtern();

        setResult(Arrays.asList(info, menu));

    }

    @Override
    public void visit(RemoteMenuItem item) {
        String nameNoSpaces = makeNameToVar(item.getName());

        BuildStructInitializer info = new BuildStructInitializer(nameNoSpaces, "RemoteMenuInfo")
                .addQuoted(item.getName())
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(item.getRemoteNum())
                .addPossibleFunction(item.getFunctionName())
                .progMemInfo();

        BuildStructInitializer menu = new BuildStructInitializer(nameNoSpaces, "RemoteMenuItem")
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

        BuildStructInitializer info = new BuildStructInitializer(nameNoSpaces, "AnyMenuInfo")
                .addQuoted(item.getName())
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(0)
                .addPossibleFunction(item.getFunctionName())
                .progMemInfo();

        BuildStructInitializer menu = new BuildStructInitializer(nameNoSpaces, "ActionMenuItem")
                .addElement("&minfo" + nameNoSpaces)
                .addElement(nextMenuName)
                .requiresExtern();

        setResult(Arrays.asList(info, menu));
    }

    @Override
    public void visit(FloatMenuItem item) {
        String nameNoSpaces = makeNameToVar(item.getName());

        BuildStructInitializer info = new BuildStructInitializer(nameNoSpaces, "FloatMenuInfo")
                .addQuoted(item.getName())
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(item.getNumDecimalPlaces())
                .addPossibleFunction(item.getFunctionName())
                .progMemInfo();

        BuildStructInitializer menu = new BuildStructInitializer(nameNoSpaces, "FloatMenuItem")
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

        BuildStructInitializer info = new BuildStructInitializer(nameNoSpaces, "BooleanMenuInfo")
                .addQuoted(item.getName())
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(1)
                .addPossibleFunction(item.getFunctionName())
                .addElement(itemNaming)
                .progMemInfo();

        BuildStructInitializer menu = new BuildStructInitializer(nameNoSpaces, "BooleanMenuItem")
                .addElement("&minfo" + nameNoSpaces)
                .addElement(false)
                .addElement(nextMenuName)
                .requiresExtern();

        setResult(Arrays.asList(info, menu));
    }

    @Override
    public void visit(EnumMenuItem item) {
        String nameNoSpaces = makeNameToVar(item.getName());

        BuildStructInitializer choices = new BuildStructInitializer(nameNoSpaces, "")
                .stringChoices()
                .collectionOfElements(item.getEnumEntries(), true);

        BuildStructInitializer info = new BuildStructInitializer(nameNoSpaces, "EnumMenuInfo")
                .addQuoted(item.getName())
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(item.getEnumEntries().size() - 1)
                .addPossibleFunction(item.getFunctionName())
                .addElement("enumStr" + nameNoSpaces)
                .progMemInfo();

        BuildStructInitializer menu = new BuildStructInitializer(nameNoSpaces, "EnumMenuItem")
                .addElement("&minfo" + nameNoSpaces)
                .addElement(0)
                .addElement(nextMenuName)
                .requiresExtern();

        setResult(Arrays.asList(choices, info, menu));
    }

    @Override
    public void visit(SubMenuItem item) {
        String nameNoSpaces = makeNameToVar(item.getName());

        BuildStructInitializer info = new BuildStructInitializer(nameNoSpaces, "SubMenuInfo")
                .addQuoted(item.getName())
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(0)
                .addPossibleFunction(item.getFunctionName())
                .progMemInfo();

        BuildStructInitializer menuBack = new BuildStructInitializer("Back" + nameNoSpaces, "BackMenuItem")
                .addElement(nextChild)
                .addElement("(const AnyMenuInfo*)&minfo" + nameNoSpaces)
                .requiresExtern();

        BuildStructInitializer menu = new BuildStructInitializer(nameNoSpaces, "SubMenuItem")
                .addElement("&minfo" + nameNoSpaces)
                .addElement("&menuBack" + nameNoSpaces)
                .addElement(nextMenuName)
                .requiresExtern();

        setResult(Arrays.asList(info, menuBack, menu));
    }

    public static String makeNameToVar(String name) {
        Collection<String> parts = Arrays.asList(name.split("[\\p{P}\\p{Z}\\t\\r\\n\\v\\f^]+"));
        return parts.stream().map(ArduinoItemGenerator::capitaliseFirst).collect(Collectors.joining());
    }

    private static String capitaliseFirst(String s) {
        if(s.isEmpty()) return s;

        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
