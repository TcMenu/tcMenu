/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.util.AbstractMenuItemVisitor;
import com.thecoderscorner.menu.editorui.generator.core.BuildStructInitializer;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;

import java.util.Arrays;
import java.util.List;

import static com.thecoderscorner.menu.domain.ScrollChoiceMenuItem.ScrollChoiceMode;

/**
 * This class follows the visitor pattern to generate code for each item
 */
public class MenuItemToEmbeddedGenerator extends AbstractMenuItemVisitor<List<BuildStructInitializer>> {
    private final String nextMenuName;
    private final String nextChild;
    private final String itemVar;
    private final Object defaultValue;
    private final LocaleMappingHandler handler;

    public MenuItemToEmbeddedGenerator(String itemVar, String nextVar, String nextCh, Object defaultValue,
                                       LocaleMappingHandler handler) {
        this.handler = handler;
        this.nextMenuName = generateMenuVariable(nextVar);
        this.nextChild = generateMenuVariable(nextCh);
        this.itemVar = itemVar;
        this.defaultValue = defaultValue;
    }

    private String generateMenuVariable(String var) {
        return (var == null || var.equals("NULL")) ? "NULL" : ("&menu" + var);
    }

    @Override
    public void visit(AnalogMenuItem item) {
        BuildStructInitializer info = new BuildStructInitializer(item, itemVar, "AnalogMenuInfo")
                .addQuoted(getItemName(item))
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(item.getMaxValue())
                .addPossibleFunction(item.getFunctionName())
                .addElement(item.getOffset())
                .addElement(Math.max(1, item.getDivisor()))
                .addQuoted(getUnitName(item))
                .memInfoBlock(!item.isStaticDataInRAM());

        BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "AnalogMenuItem")
                .addElement("&minfo" + itemVar)
                .addElement(defaultValue)
                .addElement(nextMenuName)
                .addElement(programMemArgument(item))
                .requiresExtern();

        setResult(Arrays.asList(info, menu));
    }

    private String getUnitName(AnalogMenuItem item) {
        return item.getUnitName() != null ? handler.getFromLocaleWithDefault(item.getUnitName(), item.getUnitName()) : "";
    }

    private String getItemName(MenuItem item) {
        return handler.getFromLocaleWithDefault(item.getName(), item.getName());
    }

    @Override
    public void visit(EditableLargeNumberMenuItem item) {
        BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "EditableLargeNumberMenuItem")
                .addElement(makeRtFunctionName())
                .addElement(defaultValue)
                .addElement(item.getId())
                .addElement(item.isNegativeAllowed())
                .addElement(nextMenuName)
                .requiresExtern()
                .addHeaderFileRequirement("RuntimeMenuItem.h", false)
                .addHeaderFileRequirement("EditableLargeNumberMenuItem.h", false);
        setResult(List.of(menu));

    }

    @Override
    public void visit(EditableTextMenuItem item) {
        if (item.getItemType() == EditItemType.IP_ADDRESS) {
            BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "IpAddressMenuItem")
                    .addElement(makeRtFunctionName())
                    .addElement(defaultValue)
                    .addElement(item.getId())
                    .addElement(nextMenuName)
                    .requiresExtern();
            setResult(List.of(menu));
        } else if (item.getItemType() == EditItemType.PLAIN_TEXT) {
            BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "TextMenuItem")
                    .addElement(makeRtFunctionName())
                    .addElement(defaultValue)
                    .addElement(item.getId())
                    .addElement(item.getTextLength())
                    .addElement(nextMenuName)
                    .requiresExtern()
                    .addHeaderFileRequirement("RuntimeMenuItem.h", false);
            setResult(List.of(menu));
        } else if (item.getItemType() == EditItemType.GREGORIAN_DATE) {
            BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "DateFormattedMenuItem")
                    .addElement(makeRtFunctionName())
                    .addElement(defaultValue)
                    .addElement(item.getId())
                    .addElement(nextMenuName)
                    .requiresExtern()
                    .addHeaderFileRequirement("RuntimeMenuItem.h", false);
            setResult(List.of(menu));

        } else {
            // time based
            BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "TimeFormattedMenuItem")
                    .addElement(makeRtFunctionName())
                    .addElement(defaultValue)
                    .addElement(item.getId())
                    .addElement("(MultiEditWireType)" + item.getItemType().getMsgId())
                    .addElement(nextMenuName)
                    .requiresExtern()
                    .addHeaderFileRequirement("RuntimeMenuItem.h", false);
            setResult(List.of(menu));
        }
    }

    public void visit(Rgb32MenuItem item) {
        BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "Rgb32MenuItem")
                .addElement(makeRtFunctionName())
                .addElement(defaultValue)
                .addElement(item.getId())
                .addElement(item.isIncludeAlphaChannel())
                .addElement(nextMenuName)
                .requiresExtern()
                .addHeaderFileRequirement("ScrollChoiceMenuItem.h", false);

        setResult(List.of(menu));
    }

    public void visit(ScrollChoiceMenuItem item) {
        BuildStructInitializer menu;
        if (item.getChoiceMode() == ScrollChoiceMode.ARRAY_IN_EEPROM) {
            menu = new BuildStructInitializer(item, itemVar, "ScrollChoiceMenuItem")
                    .addElement(item.getId())
                    .addElement(makeRtFunctionName())
                    .addElement(defaultValue)
                    .addElement(item.getEepromOffset())
                    .addElement(item.getItemWidth())
                    .addElement(item.getNumEntries())
                    .addElement(nextMenuName)
                    .requiresExtern();
        } else if (item.getChoiceMode() == ScrollChoiceMode.ARRAY_IN_RAM) {
            menu = new BuildStructInitializer(item, itemVar, "ScrollChoiceMenuItem")
                    .addElement(item.getId())
                    .addElement(makeRtFunctionName())
                    .addElement(defaultValue)
                    .addElement(item.getVariable())
                    .addElement(item.getItemWidth())
                    .addElement(item.getNumEntries())
                    .addElement(nextMenuName)
                    .requiresExtern();
        } else { // custom callback mode
            menu = new BuildStructInitializer(item, itemVar, "ScrollChoiceMenuItem")
                    .addElement(item.getId())
                    .addElement(makeRtFunctionName())
                    .addElement(defaultValue)
                    .addElement(item.getNumEntries())
                    .addElement(nextMenuName)
                    .requiresExtern();
        }
        menu.addHeaderFileRequirement("ScrollChoiceMenuItem.h", false);
        setResult(List.of(menu));
    }

    private String makeRtFunctionName() {
        return "fn" + itemVar + "RtCall";
    }

    @Override
    public void visit(ActionMenuItem item) {

        BuildStructInitializer info = new BuildStructInitializer(item, itemVar, "AnyMenuInfo")
                .addQuoted(getItemName(item))
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(0)
                .addPossibleFunction(item.getFunctionName())
                .memInfoBlock(!item.isStaticDataInRAM());

        BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "ActionMenuItem")
                .addElement("&minfo" + itemVar)
                .addElement(nextMenuName)
                .addElement(programMemArgument(item))
                .requiresExtern();

        setResult(Arrays.asList(info, menu));
    }

    @Override
    public void visit(FloatMenuItem item) {
        BuildStructInitializer info = new BuildStructInitializer(item, itemVar, "FloatMenuInfo")
                .addQuoted(getItemName(item))
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(item.getNumDecimalPlaces())
                .addPossibleFunction(item.getFunctionName())
                .memInfoBlock(!item.isStaticDataInRAM());

        BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "FloatMenuItem")
                .addElement("&minfo" + itemVar)
                .addElement(defaultValue)
                .addElement(nextMenuName)
                .addElement(programMemArgument(item))
                .requiresExtern();

        setResult(Arrays.asList(info, menu));
    }

    @Override
    public void visit(BooleanMenuItem item) {
        String itemNaming = switch (item.getNaming()) {
            case ON_OFF -> "NAMING_ON_OFF";
            case YES_NO -> "NAMING_YES_NO";
            case CHECKBOX -> "NAMING_CHECKBOX";
            default -> "NAMING_TRUE_FALSE";
        };

        BuildStructInitializer info = new BuildStructInitializer(item, itemVar, "BooleanMenuInfo")
                .addQuoted(getItemName(item))
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(1)
                .addPossibleFunction(item.getFunctionName())
                .addElement(itemNaming)
                .memInfoBlock(!item.isStaticDataInRAM());

        BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "BooleanMenuItem")
                .addElement("&minfo" + itemVar)
                .addElement(defaultValue)
                .addElement(nextMenuName)
                .addElement(programMemArgument(item))
                .requiresExtern();

        setResult(Arrays.asList(info, menu));
    }

    @Override
    public void visit(EnumMenuItem item) {
        List<String> enumEntries = item.getEnumEntries().stream()
                .map(v -> handler.getFromLocaleWithDefault(v, v))
                .toList();

        BuildStructInitializer choices = new BuildStructInitializer(item, itemVar, "")
                .stringChoices()
                .collectionOfElements(enumEntries, true);

        BuildStructInitializer info = new BuildStructInitializer(item, itemVar, "EnumMenuInfo")
                .addQuoted(getItemName(item))
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(item.getEnumEntries().size() - 1)
                .addPossibleFunction(item.getFunctionName())
                .addElement("enumStr" + itemVar)
                .memInfoBlock(!item.isStaticDataInRAM());

        BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "EnumMenuItem")
                .addElement("&minfo" + itemVar)
                .addElement(defaultValue)
                .addElement(nextMenuName)
                .addElement(programMemArgument(item))
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
    public void visit(CustomBuilderMenuItem customItem) {
        String textName = "pgmStr" + itemVar + "Text";
        var nameField = new BuildStructInitializer(customItem, textName + "[]", "char")
                .addQuoted(getItemName(customItem))
                .progMemStruct();

        switch (customItem.getMenuType()) {
            case AUTHENTICATION -> setResult(List.of(nameField, new BuildStructInitializer(customItem, itemVar, "EepromAuthenticationInfoMenuItem")
                    .addElement(textName)
                    .addPossibleFunction(customItem.getFunctionName())
                    .addElement(customItem.getId())
                    .addElement(nextMenuName)
                    .addHeaderFileRequirement("RemoteMenuItem.h", false)
                    .requiresExtern()
            ));
            case REMOTE_IOT_MONITOR -> setResult(List.of(nameField, new BuildStructInitializer(customItem, itemVar, "RemoteMenuItem")
                    .addElement(textName)
                    .addElement(customItem.getId())
                    .addElement(nextMenuName)
                    .addHeaderFileRequirement("RemoteMenuItem.h", false)
                    .requiresExtern()
            ));
        }
    }

    @Override
    public void visit(SubMenuItem item) {
        BuildStructInitializer info = new BuildStructInitializer(item, itemVar, "SubMenuInfo")
                .addQuoted(getItemName(item))
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(0)
                .addPossibleFunction(item.getFunctionName())
                .memInfoBlock(!item.isStaticDataInRAM());

        BuildStructInitializer menuBack = new BuildStructInitializer(item, "Back" + itemVar, "BackMenuItem")
                .addElement("&minfo" + itemVar)
                .addElement(nextChild)
                .addElement(programMemArgument(item))
                .requiresExtern();

        BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "SubMenuItem")
                .addElement("&minfo" + itemVar)
                .addElement("&menuBack" + itemVar)
                .addElement(nextMenuName)
                .addElement(programMemArgument(item))
                .requiresExtern();

        setResult(Arrays.asList(info, menuBack, menu));
    }

    private String programMemArgument(MenuItem item) {
        return item.isStaticDataInRAM() ? "INFO_LOCATION_RAM" : "INFO_LOCATION_PGM";
    }
}
