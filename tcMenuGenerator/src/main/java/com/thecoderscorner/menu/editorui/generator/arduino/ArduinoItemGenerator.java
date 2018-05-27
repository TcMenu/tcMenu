/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.util.AbstractMenuItemVisitor;
import com.thecoderscorner.menu.editorui.generator.CppAndHeader;

import java.util.Arrays;
import java.util.Collection;
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
        StringBuilder sbCpp = new StringBuilder(256);
        sbCpp.append(String.format("const PROGMEM AnalogMenuInfo minfo%s = { \"%s\", %d, %s, %d, %d, %d, \"%s\"%s };%s",
                nameNoSpaces, item.getName(), item.getId(), asEeprom(item.getEepromAddress()), item.getMaxValue(),
                item.getOffset(), item.getDivisor(), item.getUnitName(), possibleFunction(item), LINE_BREAK)
        );
        sbCpp.append(String.format("AnalogMenuItem menu%s(&minfo%s, 0, %s);%s",
                nameNoSpaces, nameNoSpaces, nextMenuName, LINE_BREAK
        ));
        String header = String.format("extern AnalogMenuItem menu%s;%s", nameNoSpaces, LINE_BREAK);
        setResult(new CppAndHeader(sbCpp.toString(), header));
    }

    private String asEeprom(int eepromAddress) {
        if(eepromAddress == -1) {
            return "0xffff";
        }
        else {
            return Integer.toString(eepromAddress);
        }
    }

    @Override
    public void visit(TextMenuItem item) {
        String nameNoSpaces = makeNameToVar(item.getName());
        StringBuilder sbCpp = new StringBuilder(256);
        sbCpp.append(String.format("const PROGMEM TextMenuInfo minfo%s = { \"%s\", %d, %s, %d%s };%s",
                nameNoSpaces, item.getName(), item.getId(), asEeprom(item.getEepromAddress()), item.getTextLength(),
                possibleFunction(item), LINE_BREAK)
        );
        sbCpp.append(String.format("TextMenuItem menu%s(&minfo%s, %s);%s",
                nameNoSpaces, nameNoSpaces, nextMenuName, LINE_BREAK
        ));
        String header = String.format("extern TextMenuItem menu%s;%s", nameNoSpaces, LINE_BREAK);
        setResult(new CppAndHeader(sbCpp.toString(), header));
    }

    @Override
    public void visit(BooleanMenuItem item) {
        String nameNoSpaces = makeNameToVar(item.getName());
        StringBuilder sb = new StringBuilder(256);
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
        sb.append(String.format("const PROGMEM BooleanMenuInfo minfo%s = { \"%s\", %d, %s, %s%s };%s",
                nameNoSpaces, item.getName(), item.getId(), asEeprom(item.getEepromAddress()), itemNaming,
                possibleFunction(item), LINE_BREAK
        ));
        sb.append(String.format("BooleanMenuItem menu%s(&minfo%s, false, %s);%s",
                nameNoSpaces, nameNoSpaces, nextMenuName, LINE_BREAK
        ));
        String header = String.format("extern BooleanMenuItem menu%s;%s", nameNoSpaces, LINE_BREAK);
        setResult(new CppAndHeader(sb.toString(), header));
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
        sb.append(String.format("const PROGMEM EnumMenuInfo minfo%s = { \"%s\", %d, %s, enumStr%s, %d%s };%s",
                nameNoSpaces, item.getName(), item.getId(), asEeprom(item.getEepromAddress()), nameNoSpaces,
                item.getEnumEntries().size(), possibleFunction(item), LINE_BREAK)
        );
        sb.append(String.format("EnumMenuItem menu%s(&minfo%s, 0, %s);%s",
                nameNoSpaces, nameNoSpaces, nextMenuName, LINE_BREAK
        ));
        String header = String.format("extern EnumMenuItem menu%s;%s", nameNoSpaces, LINE_BREAK);
        setResult(new CppAndHeader(sb.toString(), header));
    }

    private String possibleFunction(MenuItem item) {
        if(item.getFunctionName() != null) {
            return ", " + item.getFunctionName();
        }
        else {
            return ", NO_CALLBACK";
        }
    }

    @Override
    public void visit(SubMenuItem item) {
        String nameNoSpaces = makeNameToVar(item.getName());
        StringBuilder sb = new StringBuilder(256);
        sb.append(String.format("const PROGMEM SubMenuInfo minfo%s = { \"%s\", %d };\n",
                nameNoSpaces, item.getName(), item.getId()));
        sb.append(String.format("BackMenuItem backMnu%s(%s, minfo%s.name);\n", nameNoSpaces, nextChild, nameNoSpaces));
        sb.append(String.format("SubMenuItem menu%s(&minfo%s, &backMnu%s, %s);\n", nameNoSpaces, nameNoSpaces,
                nameNoSpaces, nextMenuName));

        setResult(new CppAndHeader(sb.toString(), ""));
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
