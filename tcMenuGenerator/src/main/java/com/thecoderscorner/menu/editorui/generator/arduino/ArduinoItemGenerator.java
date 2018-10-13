/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.util.AbstractMenuItemVisitor;
import com.thecoderscorner.menu.editorui.generator.CppAndHeader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ArduinoItemGenerator extends AbstractMenuItemVisitor<CppAndHeader> {
    public static final String LINE_BREAK = System.getProperty("line.separator");
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
                .addQuoted(nameNoSpaces)
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(item.getMaxValue())
                .addPossibleFunction(item.getFunctionName())
                .addElement(item.getOffset())
                .addElement(item.getDivisor())
                .addQuoted(item.getUnitName());

        BuildStructInitializer menu = new BuildStructInitializer(nameNoSpaces, "AnalogMenuItem")
                .addElement("&minfo" + nameNoSpaces)
                .addElement(0)
                .addElement(nextMenuName);

        setResult(new CppAndHeader(info.toMenuInfo() + menu.toMenuItem(), menu.toMenuHeader()));
    }

    @Override
    public void visit(TextMenuItem item) {
        String nameNoSpaces = makeNameToVar(item.getName());

        BuildStructInitializer info = new BuildStructInitializer(nameNoSpaces, "TextMenuInfo")
                .addQuoted(nameNoSpaces)
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(item.getTextLength())
                .addPossibleFunction(item.getFunctionName());

        BuildStructInitializer menu = new BuildStructInitializer(nameNoSpaces, "TextMenuItem")
                .addElement("&minfo" + nameNoSpaces)
                .addElement(nextMenuName);

        setResult(new CppAndHeader(info.toMenuInfo() + menu.toMenuItem(), menu.toMenuHeader()));
    }

    @Override
    public void visit(RemoteMenuItem item) {
        String nameNoSpaces = makeNameToVar(item.getName());

        BuildStructInitializer info = new BuildStructInitializer(nameNoSpaces, "RemoteMenuInfo")
                .addQuoted(nameNoSpaces)
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(item.getRemoteNum())
                .addPossibleFunction(item.getFunctionName());

        BuildStructInitializer menu = new BuildStructInitializer(nameNoSpaces, "RemoteMenuItem")
                .addHeaderFileRequirement("RemoteMenuItem.h")
                .addElement("&minfo" + nameNoSpaces)
                .addElement("remoteServer.getRemoteConnector(" + item.getRemoteNum() + ")")
                .addElement(nextMenuName);

        setResult(new CppAndHeader(info.toMenuInfo() + menu.toMenuItem(), menu.toMenuHeader()));
    }

    @Override
    public void visit(ActionMenuItem item) {
        String nameNoSpaces = makeNameToVar(item.getName());

        BuildStructInitializer info = new BuildStructInitializer(nameNoSpaces, "AnyMenuInfo")
                .addQuoted(nameNoSpaces)
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(0)
                .addPossibleFunction(item.getFunctionName());

        BuildStructInitializer menu = new BuildStructInitializer(nameNoSpaces, "ActionMenuItem")
                .addElement("&minfo" + nameNoSpaces)
                .addElement(nextMenuName);

        setResult(new CppAndHeader(info.toMenuInfo() + menu.toMenuItem(), menu.toMenuHeader()));
    }

    @Override
    public void visit(FloatMenuItem item) {
        String nameNoSpaces = makeNameToVar(item.getName());

        BuildStructInitializer info = new BuildStructInitializer(nameNoSpaces, "FloatMenuInfo")
                .addQuoted(nameNoSpaces)
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(item.getNumDecimalPlaces())
                .addPossibleFunction(item.getFunctionName());

        BuildStructInitializer menu = new BuildStructInitializer(nameNoSpaces, "FloatMenuItem")
                .addElement("&minfo" + nameNoSpaces)
                .addElement(nextMenuName);

        setResult(new CppAndHeader(info.toMenuInfo() + menu.toMenuItem(), menu.toMenuHeader()));
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
                .addQuoted(nameNoSpaces)
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(1)
                .addPossibleFunction(item.getFunctionName())
                .addElement(itemNaming);

        BuildStructInitializer menu = new BuildStructInitializer(nameNoSpaces, "BooleanMenuItem")
                .addElement("&minfo" + nameNoSpaces)
                .addElement(false)
                .addElement(nextMenuName);

        setResult(new CppAndHeader(info.toMenuInfo() + menu.toMenuItem(), menu.toMenuHeader()));
    }

    @Override
    public void visit(EnumMenuItem item) {
        String nameNoSpaces = makeNameToVar(item.getName());
        StringBuilder sb = new StringBuilder(256);
        IntStream.range(0, item.getEnumEntries().size()).forEach(i -> {
            String textRep = item.getEnumEntries().get(i);
            sb.append(String.format("const char enumStr%s_%d[] PROGMEM = \"%s\";%s", nameNoSpaces, i, textRep, LINE_BREAK));
        });
        sb.append(String.format("const char* const enumStr%s[] PROGMEM  = { ", nameNoSpaces));
        sb.append(IntStream.range(0, item.getEnumEntries().size())
                .mapToObj(i -> "enumStr" + nameNoSpaces + "_" + i)
                .collect(Collectors.joining(", ")));
        sb.append(" };").append(LINE_BREAK);

        BuildStructInitializer info = new BuildStructInitializer(nameNoSpaces, "EnumMenuInfo")
                .addQuoted(nameNoSpaces)
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(item.getEnumEntries().size() - 1)
                .addPossibleFunction(item.getFunctionName())
                .addElement("enumStr" + nameNoSpaces);

        BuildStructInitializer menu = new BuildStructInitializer(nameNoSpaces, "EnumMenuItem")
                .addElement("&minfo" + nameNoSpaces)
                .addElement(false)
                .addElement(nextMenuName);

        setResult(new CppAndHeader(sb.toString() + info.toMenuInfo() + menu.toMenuItem(), menu.toMenuHeader()));
    }

    @Override
    public void visit(SubMenuItem item) {
        String nameNoSpaces = makeNameToVar(item.getName());

        BuildStructInitializer info = new BuildStructInitializer(nameNoSpaces, "SubMenuInfo")
                .addQuoted(nameNoSpaces)
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(0)
                .addPossibleFunction(item.getFunctionName());

        BuildStructInitializer menuBack = new BuildStructInitializer("Back" + nameNoSpaces, "BackMenuItem")
                .addElement(nextChild)
                .addElement("(const AnyMenuInfo*)&minfo" + nameNoSpaces);

        BuildStructInitializer menu = new BuildStructInitializer(nameNoSpaces, "SubMenuItem")
                .addElement("&minfo" + nameNoSpaces)
                .addElement("&menuBack" + nameNoSpaces)
                .addElement(nextMenuName);

        setResult(new CppAndHeader(info.toMenuInfo() + menuBack.toMenuItem() + menu.toMenuItem(), menu.toMenuHeader()));
    }

    public static String makeNameToVar(String name) {
        Collection<String> parts = Arrays.asList(name.split("\\s"));
        return parts.stream().map(ArduinoItemGenerator::capitaliseFirst).collect(Collectors.joining());
    }

    private static String capitaliseFirst(String s) {
        if(s.isEmpty()) return s;

        StringBuilder sb = new StringBuilder();
        sb.append(Character.toUpperCase(s.charAt(0)));
        sb.append(s.substring(1));
        return sb.toString();
    }
}
