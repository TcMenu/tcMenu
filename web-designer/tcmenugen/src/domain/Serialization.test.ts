import {parseEmfJsonToProject, PersistedProject, projectToPersistedJson} from './PersistedMenu';
import {
    AnalogMenuItem,
    EnumMenuItem,
    SubMenuItem,
    BooleanMenuItem,
    EditableTextMenuItem,
    EditableLargeNumberMenuItem, Rgb32MenuItem, ScrollChoiceMenuItem, ListMenuItem, MenuItem
} from './MenuItem';
import {EepromSaveMode, ProjectSaveLocation} from "./ProjectStruct";

const keyboardEthernetShieldEmf = `{
  "version": "1.00",
  "projectName": "keyboardEthernetShield",
  "author": "dave",
  "items": [
    {
      "parentId": 0,
      "type": "textItem",
      "defaultValue": "",
      "item": {
        "textLength": 10,
        "itemType": "TIME_12H",
        "name": "Time",
        "id": 1,
        "eepromAddress": 8,
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 0,
      "type": "analogItem",
      "defaultValue": "0",
      "item": {
        "maxValue": 255,
        "offset": -180,
        "divisor": 2,
        "unitName": "dB",
        "step": 1,
        "name": "Analog1",
        "id": 2,
        "eepromAddress": 2,
        "functionName": "onAnalog1",
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 0,
      "type": "boolItem",
      "defaultValue": "false",
      "item": {
        "naming": "TRUE_FALSE",
        "name": "Hidden item",
        "id": 13,
        "eepromAddress": -1,
        "readOnly": false,
        "localOnly": false,
        "visible": false,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 0,
      "type": "analogItem",
      "defaultValue": "0",
      "item": {
        "maxValue": 1000,
        "offset": 100,
        "divisor": 1,
        "unitName": "",
        "step": 1,
        "name": "Integer",
        "id": 3,
        "eepromAddress": 4,
        "functionName": "onInteger",
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 0,
      "type": "analogItem",
      "defaultValue": "0",
      "item": {
        "maxValue": 1000,
        "offset": 0,
        "divisor": 10,
        "unitName": "V",
        "step": 1,
        "name": "DecimalTens",
        "id": 4,
        "eepromAddress": 28,
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 0,
      "type": "largeNumItem",
      "defaultValue": "0",
      "item": {
        "digitsAllowed": 8,
        "decimalPlaces": 4,
        "negativeAllowed": true,
        "name": "Large Num",
        "id": 12,
        "eepromAddress": -1,
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": true
      }
    },
    {
      "parentId": 0,
      "type": "analogItem",
      "defaultValue": "0",
      "item": {
        "maxValue": 200,
        "offset": 0,
        "divisor": 5,
        "unitName": "A",
        "step": 1,
        "name": "Fiths",
        "id": 5,
        "eepromAddress": 6,
        "functionName": "onFiths",
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 0,
      "type": "enumItem",
      "defaultValue": "0",
      "item": {
        "enumEntries": [
          "Apples",
          "Oranges",
          "Pears",
          "Plums",
          "Grapes"
        ],
        "name": "Fruits",
        "id": 8,
        "eepromAddress": 26,
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 0,
      "type": "actionMenu",
      "item": {
        "name": "Take display",
        "variableName": "TakeDisplay",
        "id": 31,
        "eepromAddress": -1,
        "functionName": "onTakeOverDisplay",
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 0,
      "type": "subMenu",
      "item": {
        "secured": true,
        "name": "Connectivity",
        "id": 6,
        "eepromAddress": -1,
        "readOnly": false,
        "localOnly": true,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 0,
      "type": "actionMenu",
      "item": {
        "name": "Take display",
        "variableName": "TakeDisplay",
        "id": 31,
        "eepromAddress": -1,
        "functionName": "onTakeOverDisplay",
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 6,
      "type": "textItem",
      "defaultValue": "",
      "item": {
        "textLength": 15,
        "itemType": "PLAIN_TEXT",
        "name": "Change Pin",
        "id": 11,
        "eepromAddress": -1,
        "functionName": "onChangePin",
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 6,
      "type": "textItem",
      "defaultValue": "",
      "item": {
        "textLength": 20,
        "itemType": "IP_ADDRESS",
        "name": "IpAddress",
        "id": 7,
        "eepromAddress": 12,
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 6,
      "type": "textItem",
      "defaultValue": "",
      "item": {
        "textLength": 10,
        "itemType": "PLAIN_TEXT",
        "name": "Text",
        "id": 9,
        "eepromAddress": 16,
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 6,
      "type": "actionMenu",
      "item": {
        "name": "Save to EEPROM",
        "id": 10,
        "eepromAddress": -1,
        "functionName": "onSaveToEeprom",
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 6,
      "type": "customBuildItem",
      "item": {
        "menuType": "REMOTE_IOT_MONITOR",
        "name": "IoT Monitor",
        "variableName": "ConnectivityIoTMonitor",
        "id": 29,
        "eepromAddress": -1,
        "readOnly": false,
        "localOnly": true,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 6,
      "type": "customBuildItem",
      "item": {
        "menuType": "AUTHENTICATION",
        "name": "Authenticator",
        "variableName": "ConnectivityAuthenticator",
        "id": 30,
        "eepromAddress": -1,
        "readOnly": false,
        "localOnly": true,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 0,
      "type": "subMenu",
      "item": {
        "secured": false,
        "name": "Additional",
        "id": 14,
        "eepromAddress": -1,
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 14,
      "type": "rgbItem",
      "defaultValue": "#000000FF",
      "item": {
        "includeAlphaChannel": true,
        "name": "RGB",
        "id": 15,
        "eepromAddress": 34,
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 14,
      "type": "scrollItem",
      "defaultValue": "0-",
      "item": {
        "itemWidth": 10,
        "eepromOffset": 500,
        "numEntries": 9,
        "choiceMode": "ARRAY_IN_EEPROM",
        "variable": "",
        "name": "Rom Choice",
        "id": 19,
        "eepromAddress": 30,
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 14,
      "type": "scrollItem",
      "defaultValue": "0-",
      "item": {
        "itemWidth": 10,
        "eepromOffset": 0,
        "numEntries": 30,
        "choiceMode": "CUSTOM_RENDERFN",
        "name": "Num Choices",
        "id": 17,
        "eepromAddress": 32,
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 14,
      "type": "runtimeList",
      "item": {
        "initialRows": 20,
        "listCreationMode": "CUSTOM_RTCALL",
        "name": "Count List",
        "id": 18,
        "eepromAddress": -1,
        "functionName": "",
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 14,
      "type": "subMenu",
      "item": {
        "secured": false,
        "name": "Bool Flag",
        "variableName": "AdditionalBoolFlag",
        "id": 24,
        "eepromAddress": -1,
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 24,
      "type": "boolItem",
      "defaultValue": "false",
      "item": {
        "naming": "ON_OFF",
        "name": "Flag1",
        "variableName": "AdditionalBoolFlagFlag1",
        "id": 25,
        "eepromAddress": 38,
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 24,
      "type": "boolItem",
      "defaultValue": "false",
      "item": {
        "naming": "ON_OFF",
        "name": "Flag2",
        "variableName": "AdditionalBoolFlagFlag2",
        "id": 26,
        "eepromAddress": 39,
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 24,
      "type": "boolItem",
      "defaultValue": "false",
      "item": {
        "naming": "CHECKBOX",
        "name": "Flag3",
        "variableName": "AdditionalBoolFlagFlag3",
        "id": 27,
        "eepromAddress": 40,
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 24,
      "type": "boolItem",
      "defaultValue": "false",
      "item": {
        "naming": "CHECKBOX",
        "name": "Flag4",
        "variableName": "AdditionalBoolFlagFlag4",
        "id": 28,
        "eepromAddress": 41,
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 14,
      "type": "textItem",
      "defaultValue": "",
      "item": {
        "textLength": 4,
        "itemType": "PLAIN_TEXT",
        "name": "Custom Hex",
        "variableName": "AdditionalCustomHex",
        "id": 32,
        "eepromAddress": -1,
        "functionName": "customHexEditorRtCall",
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 0,
      "type": "subMenu",
      "item": {
        "secured": false,
        "name": "Rom Choices",
        "id": 20,
        "eepromAddress": -1,
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 20,
      "type": "scrollItem",
      "defaultValue": "0-",
      "item": {
        "itemWidth": 7,
        "eepromOffset": 0,
        "numEntries": 10,
        "choiceMode": "ARRAY_IN_RAM",
        "variable": "romSpaceNames",
        "name": "Item Num",
        "id": 21,
        "eepromAddress": -1,
        "functionName": "onItemChange",
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 20,
      "type": "textItem",
      "defaultValue": "",
      "item": {
        "textLength": 10,
        "itemType": "PLAIN_TEXT",
        "name": "Value",
        "id": 22,
        "eepromAddress": -1,
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    },
    {
      "parentId": 20,
      "type": "actionMenu",
      "item": {
        "name": "Save",
        "id": 23,
        "eepromAddress": -1,
        "functionName": "onSaveValue",
        "readOnly": false,
        "localOnly": false,
        "visible": true,
        "staticDataInRAM": false
      }
    }
  ],
  "codeOptions": {
    "embeddedPlatform": "ARDUINO",
    "applicationUUID": "b6ee8e21-449c-4f8a-bab6-a89e3f2c68d9",
    "applicationName": "Keyboard Ethernet",
    "lastProperties": [],
    "namingRecursive": true,
    "useCppMain": false,
    "saveLocation": "ALL_TO_CURRENT",
    "usingSizedEEPROMStorage": false,
    "eepromDefinition": "avr:",
    "authenticatorDefinition": "rom:100:6",
    "projectIoExpanders": [
      "deviceIO:",
      "mcp23017:io23017:32:2"
    ],
    "menuInMenuCollection": {
      "menuDefinitions": []
    }
  }
}`;

describe('EMF Serialization Tests', () => {
    function checkCommon(item: MenuItem<any>, id: string, name: string, eeprom: number = -1, visible: boolean = true) {
        expect(item).toBeDefined();
        expect(item.getMenuId()).toBe(id);
        expect(item.getItemName()).toBe(name);
        expect(item.getEEPROMLocation()).toBe(eeprom);
        expect(item.isVisible()).toBe(visible);
    }

    test('should load keyboardEthernetShield.emf completely', () => {
        const project = parseEmfJsonToProject(keyboardEthernetShieldEmf);
        
        expect(project.description).toBe('keyboardEthernetShield');
        expect(project.options.applicationName).toBe('Keyboard Ethernet');
        expect(project.options.applicationUUID).toBe('b6ee8e21-449c-4f8a-bab6-a89e3f2c68d9');
        expect(project.options.eepromSaveMode).toBe(EepromSaveMode.LEGACY_WRITE_BY_POSITION);
        expect(project.options.saveLocation).toBe(ProjectSaveLocation.ALL_TO_CURRENT);

        const tree = project.menuTree;

        // Time (TextItem)
        checkCommon(tree.getMenuItemFor("1"), "1", "Time", 8);
        
        // Analog1 (AnalogItem)
        const analog1 = tree.getMenuItemFor("2") as AnalogMenuItem;
        checkCommon(analog1, "2", "Analog1", 2);
        expect(analog1.getOffset()).toBe(-180);
        expect(analog1.getDivisor()).toBe(2);
        expect(analog1.getUnitName()).toBe("dB");
        
        // Hidden (BoolItem)
        checkCommon(tree.getMenuItemFor("13"), "13", "Hidden item", -1, false);

        // Integer (Analog)
        const integerItem = tree.getMenuItemFor("3") as AnalogMenuItem;
        checkCommon(integerItem, "3", "Integer", 4);
        expect(integerItem.getMaxValue()).toBe(1000);
        expect(integerItem.getOffset()).toBe(100);

        // DecimalTens (Analog)
        const decimalTens = tree.getMenuItemFor("4") as AnalogMenuItem;
        checkCommon(decimalTens, "4", "DecimalTens", 28);
        expect(decimalTens.getDivisor()).toBe(10);

        // Large Num (LargeNumItem)
        const largeNum = tree.getMenuItemFor("12") as EditableLargeNumberMenuItem;
        checkCommon(largeNum, "12", "Large Num", -1);
        expect(largeNum.getDigitsAllowed()).toBe(8);
        expect(largeNum.getDecimalPlaces()).toBe(4);
        expect(largeNum.isStaticDataInRAM()).toBe(true);

        // Fiths (Analog)
        const fiths = tree.getMenuItemFor("5") as AnalogMenuItem;
        checkCommon(fiths, "5", "Fiths", 6);
        expect(fiths.getDivisor()).toBe(5);

        // Fruits (EnumItem)
        const fruits = tree.getMenuItemFor("8") as EnumMenuItem;
        checkCommon(fruits, "8", "Fruits", 26);
        expect(fruits.getItemList()).toEqual(["Apples", "Oranges", "Pears", "Plums", "Grapes"]);

        // Take display (Action)
        checkCommon(tree.getMenuItemFor("31"), "31", "Take display");

        // Connectivity (Sub)
        const connectivity = tree.getMenuItemFor("6") as SubMenuItem;
        checkCommon(connectivity, "6", "Connectivity");
        expect(connectivity.isSecuredMenu()).toBe(true);
        expect(connectivity.isLocalOnly()).toBe(true);
        
        checkCommon(tree.getMenuItemFor("11"), "11", "Change Pin");
        checkCommon(tree.getMenuItemFor("7"), "7", "IpAddress", 12);
        checkCommon(tree.getMenuItemFor("9"), "9", "Text", 16);
        checkCommon(tree.getMenuItemFor("10"), "10", "Save to EEPROM");
        // Remote IoT Monitor and Authentication are currently not mapped to specific TS classes but handled generically
        checkCommon(tree.getMenuItemFor("29"), "29", "IoT Monitor");
        checkCommon(tree.getMenuItemFor("30"), "30", "Authenticator");

        // Connectivity Children: Change Pin(11), IpAddress(7), Text(9), Save to EEPROM(10), IoT Monitor(29), Authenticator(30)
        expect(connectivity.getChildren().length).toBe(6);

        // Additional (Sub)
        const additional = tree.getMenuItemFor("14") as SubMenuItem;
        checkCommon(additional, "14", "Additional");

        // RGB (in Additional)
        const rgbItem = tree.getMenuItemFor("15") as Rgb32MenuItem;
        checkCommon(rgbItem, "15", "RGB", 34);
        expect(rgbItem.isAlphaChannelOn()).toBe(true);

        // Rom Choice (Scroll Choice)
        const romChoice = tree.getMenuItemFor("19") as ScrollChoiceMenuItem;
        checkCommon(romChoice, "19", "Rom Choice", 30);
        expect(romChoice.getNumberOfEntries()).toBe(9);

        // Num Choices (Scroll Choice)
        const numChoices = tree.getMenuItemFor("17") as ScrollChoiceMenuItem;
        checkCommon(numChoices, "17", "Num Choices", 32);
        expect(numChoices.getNumberOfEntries()).toBe(30);

        // Count List (List)
        const list = tree.getMenuItemFor("18") as ListMenuItem;
        checkCommon(list, "18", "Count List");
        expect(list.getNumberOfItems()).toBe(20);

        // Bool Flag (Sub in Additional)
        const boolFlag = tree.getMenuItemFor("24") as SubMenuItem;
        checkCommon(boolFlag, "24", "Bool Flag");
        // Bool Flag Children: Flag1(25), Flag2(26), Flag3(27), Flag4(28)
        expect(boolFlag.getChildren().length).toBe(4);
        checkCommon(tree.getMenuItemFor("25"), "25", "Flag1", 38);
        checkCommon(tree.getMenuItemFor("26"), "26", "Flag2", 39);
        checkCommon(tree.getMenuItemFor("27"), "27", "Flag3", 40);
        checkCommon(tree.getMenuItemFor("28"), "28", "Flag4", 41);

        // Custom Hex (Text)
        const customHex = tree.getMenuItemFor("32") as EditableTextMenuItem;
        checkCommon(customHex, "32", "Custom Hex");
        expect(customHex.getCallbackFnName()).toBe("customHexEditorRtCall");

        // Rom Choices (Sub)
        const romChoices = tree.getMenuItemFor("20") as SubMenuItem;
        checkCommon(romChoices, "20", "Rom Choices");
        // Rom Choices Children: Item Num(21), Value(22), Save(23)
        expect(romChoices.getChildren().length).toBe(3);
        checkCommon(tree.getMenuItemFor("21"), "21", "Item Num");
        checkCommon(tree.getMenuItemFor("22"), "22", "Value");
        checkCommon(tree.getMenuItemFor("23"), "23", "Save");

        const rootItems = (tree.getMenuItemFor("0") as SubMenuItem).getChildren();
        // Root items: Time(1), Analog1(2), Hidden(13), Integer(3), DecimalTens(4), Large Num(12), Fiths(5), Fruits(8), Connectivity(6), Take display(31), Additional(14), Bool Flag(24), Rom Choices(20)
        expect(rootItems.length).toBe(12);

        // Additional Children: RGB(15), Rom Choice(19), Num Choices(17), Count List(18), Bool Flag(24), Custom Hex(32)
        expect(additional.getChildren().length).toBe(6);


    });

    test('serialization output should match Java structure', () => {
        const project = parseEmfJsonToProject(keyboardEthernetShieldEmf);
        const output = projectToPersistedJson(project);


        expect(output.version).toBeDefined();
        // Check root properties
        // Java EMF has "projectName", TS PersistedProject has "projectName" but it seems to store the description
        // expect(output.projectName).toBe('Keyboard Ethernet'); // In parseEmfJsonToProject, description = prj.projectName. Wait.

        const original = JSON.parse(keyboardEthernetShieldEmf);
        
        // Compare items count
        // original.items.length is 32 in the test data because it includes a duplicate 'Take display' item (id 31)
        // while our serialized output is 31 because the tree deduplicates it.
        expect(output.items.length).toBe(31);

        // Spot check an analog item (id 2)
        const analogOriginal = original.items.find((i: any) => i.item.id === 2 || i.item.menuId === 2).item;
        const analogOutput = output.items.find((i: any) => i.item.id === 2 || i.item.menuId === 2).item;

        // In Java it is "eepromAddress", in TS it is "eepromLocation" in memory, 
        // but it should be serialized back as "eepromAddress" for Java compatibility.
        expect(analogOutput.maxValue).toBe(analogOriginal.maxValue);
        expect(analogOutput.offset).toBe(analogOriginal.offset);
        expect(analogOutput.divisor).toBe(analogOriginal.divisor);
        expect(analogOutput.unitName).toBe(analogOriginal.unitName);

        // Check compatibility
        expect(analogOutput.eepromAddress).toBe(analogOriginal.eepromAddress);
        expect(analogOutput.eepromLocation).toBeUndefined();

        const enumOutput = output.items.find((i: any) => i.item.id === 8 || i.item.menuId === 8).item;
        expect(enumOutput.enumEntries).toContain("Apples");

        const largeNum = output.items.find((i: any) => i.item.id === 12 || i.item.menuId === 12).item;
        expect(largeNum.digitsAllowed).toBe(8);
        expect(largeNum.decimalPlaces).toBe(4);
        expect(largeNum.staticDataInRAM).toBe(true);
    });
});
