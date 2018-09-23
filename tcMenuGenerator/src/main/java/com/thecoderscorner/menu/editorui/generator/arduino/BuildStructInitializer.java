package com.thecoderscorner.menu.editorui.generator.arduino;

import java.util.ArrayList;
import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoItemGenerator.LINE_BREAK;

class BuildStructInitializer {
        private String structName;
        private String structType;
        private List<String> structElements = new ArrayList<>();

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

        public String toMenuInfo() {
            StringBuilder sb = new StringBuilder(256);
            sb.append("const PROGMEM ").append(structType).append(" minfo").append(structName).append(" = { ");
            sb.append(String.join(", ", structElements));
            sb.append(" };");
            sb.append(LINE_BREAK);
            return sb.toString();
        }

        public String toMenuItem() {
            StringBuilder sb = new StringBuilder(256);
            sb.append(structType).append(" menu").append(structName).append("(");
            sb.append(String.join(", ", structElements));
            sb.append(");");
            sb.append(LINE_BREAK);
            return sb.toString();
        }

        public String toMenuHeader() {
            return "extern " + structType + " menu" + structName + ";" + LINE_BREAK;
        }
    }
