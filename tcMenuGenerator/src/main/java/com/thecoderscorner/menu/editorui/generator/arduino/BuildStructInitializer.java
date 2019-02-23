/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.pluginapi.model.HeaderDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoItemGenerator.LINE_BREAK;

class BuildStructInitializer {
    private String structName;
    private String structType;
    private List<HeaderDefinition> headerRequirement = new ArrayList<>();
    private List<String> structElements = new ArrayList<>();
    private boolean requiresExtern = false;
    private boolean progMemInfo = false;
    private boolean stringChoices = false;

    public BuildStructInitializer(String structName, String structType) {
        this.structName = structName;
        this.structType = structType;
    }

    public BuildStructInitializer addQuoted(String value) {
        structElements.add('\"' + value + '\"');
        return this;
    }

    public BuildStructInitializer addElement(Object value) {
        structElements.add(value.toString());
        return this;
    }

    public BuildStructInitializer addHeaderFileRequirement(String include, boolean quotes) {
        headerRequirement.add(new HeaderDefinition(include, quotes, HeaderDefinition.PRIORITY_NORMAL));
        return this;
    }

    public BuildStructInitializer addPossibleFunction(String functionName) {
        structElements.add((functionName != null) ? functionName : "NO_CALLBACK");
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

    public String toSource() {
        if(stringChoices) {
            return doStringSource();
        }
        StringBuilder sb = new StringBuilder(256);
        if(progMemInfo) {
            sb.append("const PROGMEM ").append(structType).append(" minfo").append(structName).append(" = { ");
            sb.append(String.join(", ", structElements));
            sb.append(" };");
        }
        else {
            sb.append(structType).append(" menu").append(structName).append("(");
            sb.append(String.join(", ", structElements));
            sb.append(");");
        }
        return sb.toString();
    }

    private String doStringSource() {
        StringBuilder sb = new StringBuilder(256);
        IntStream.range(0, structElements.size()).forEach(i -> {
            String textRep = structElements.get(i);
            sb.append(String.format("const char enumStr%s_%d[] PROGMEM = %s;%s", structName, i, textRep, LINE_BREAK));
        });
        sb.append(String.format("const char* const enumStr%s[] PROGMEM  = { ", structName));
        sb.append(IntStream.range(0, structElements.size())
                .mapToObj(i -> "enumStr" + structName + "_" + i)
                .collect(Collectors.joining(", ")));
        sb.append(" };");

        return sb.toString();
    }

    public String toHeader() {
        String header = "";

        if(requiresExtern) {
            header = header + "extern " + structType + " menu" + structName + ";";
        }

        return header;
    }

    public List<HeaderDefinition> getHeaderRequirements() {
        return headerRequirement;
    }

    public BuildStructInitializer requiresExtern() {
        this.requiresExtern = true;
        return this;
    }

    public BuildStructInitializer progMemInfo() {
        this.progMemInfo = true;
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

    public boolean isProgMemInfo() {
        return progMemInfo;
    }

    public boolean isStringChoices() {
        return stringChoices;
    }
}
