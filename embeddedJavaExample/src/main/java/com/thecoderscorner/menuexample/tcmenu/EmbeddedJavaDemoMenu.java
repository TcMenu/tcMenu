package com.thecoderscorner.menuexample.tcmenu;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.*;
import com.thecoderscorner.menu.persist.JsonMenuItemSerializer;

public class EmbeddedJavaDemoMenu {
    private final static String APP_MENU_ITEMS = """
tcMenuCopy:[
  {
    "parentId": 0,
    "type": "analogItem",
    "item": {
      "maxValue": 100,
      "offset": 0,
      "divisor": 1,
      "unitName": "%",
      "name": "LED1  brightness",
      "variableName": "LED1Brightness",
      "id": 1,
      "eepromAddress": 4,
      "functionName": "led1BrightnessHasChanged",
      "readOnly": false,
      "localOnly": false,
      "visible": true
    }
  },
  {
    "parentId": 0,
    "type": "analogItem",
    "item": {
      "maxValue": 100,
      "offset": 0,
      "divisor": 1,
      "unitName": "%",
      "name": "LED2 brightness",
      "variableName": "LED2Brightness",
      "id": 2,
      "eepromAddress": 2,
      "functionName": "led2BrightnessHasChanged",
      "readOnly": false,
      "localOnly": false,
      "visible": true
    }
  },
  {
    "parentId": 0,
    "type": "enumItem",
    "item": {
      "enumEntries": [
        "AC Input",
        "Battery",
        "Unregulated"
      ],
      "name": "Input Control",
      "variableName": "InputControl",
      "id": 3,
      "eepromAddress": 11,
      "functionName": "inputControlHasChanged",
      "readOnly": false,
      "localOnly": false,
      "visible": true
    }
  },
  {
    "parentId": 0,
    "type": "boolItem",
    "item": {
      "naming": "ON_OFF",
      "name": "Logging Monitor",
      "variableName": "LoggingMonitor",
      "id": 4,
      "eepromAddress": 10,
      "readOnly": false,
      "localOnly": false,
      "visible": true
    }
  },
  {
    "parentId": 0,
    "type": "subMenu",
    "item": {
      "secured": false,
      "name": "Status",
      "variableName": "Status",
      "id": 5,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true
    }
  },
  {
    "parentId": 5,
    "type": "floatItem",
    "item": {
      "numDecimalPlaces": 1,
      "name": "Case Temp oC",
      "variableName": "StatusCaseTempOC",
      "id": 6,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true
    }
  },
  {
    "parentId": 5,
    "type": "rgbItem",
    "item": {
      "includeAlphaChannel": false,
      "name": "Light Color",
      "variableName": "StatusLightColor",
      "id": 7,
      "eepromAddress": 6,
      "readOnly": false,
      "localOnly": false,
      "visible": true
    }
  },
  {
    "parentId": 5,
    "type": "textItem",
    "item": {
      "textLength": 5,
      "itemType": "IP_ADDRESS",
      "name": "IP Address",
      "variableName": "SettingsNewTextItem",
      "id": 10,
      "eepromAddress": -1,
      "readOnly": true,
      "localOnly": false,
      "visible": true
    }
  },
  {
    "parentId": 5,
    "type": "customBuildItem",
    "item": {
      "menuType": "AUTHENTICATION",
      "name": "Authenticator",
      "id": 12,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true
    }
  },
  {
    "parentId": 5,
    "type": "customBuildItem",
    "item": {
      "menuType": "REMOTE_IOT_MONITOR",
      "name": "IoT Monitor",
      "id": 13,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true
    }
  },
  {
    "parentId": 5,
    "type": "runtimeList",
    "item": {
      "initialRows": 0,
      "name": "My List Item",
      "variableName": "StatusMyListItem",
      "id": 15,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true
    }
  },
  {
    "parentId": 0,
    "type": "subMenu",
    "item": {
      "secured": false,
      "name": "Settings",
      "variableName": "Settings",
      "id": 8,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true
    }
  },
  {
    "parentId": 8,
    "type": "textItem",
    "item": {
      "textLength": 15,
      "itemType": "PLAIN_TEXT",
      "name": "Some Text",
      "variableName": "SettingsSomeText",
      "id": 11,
      "eepromAddress": 21,
      "readOnly": false,
      "localOnly": false,
      "visible": true
    }
  },
  {
    "parentId": 8,
    "type": "largeNumItem",
    "item": {
      "digitsAllowed": 8,
      "decimalPlaces": 3,
      "negativeAllowed": true,
      "name": "Large Num",
      "variableName": "SettingsLargeNum",
      "id": 14,
      "eepromAddress": 13,
      "readOnly": false,
      "localOnly": false,
      "visible": true
    }
  }
]""";
    private final MenuTree menuTree;
    private final JsonMenuItemSerializer jsonSerializer;
    
    public EmbeddedJavaDemoMenu() {
        jsonSerializer = new JsonMenuItemSerializer();
        menuTree = jsonSerializer.newMenuTreeWithItems(APP_MENU_ITEMS);
        menuTree.initialiseStateForEachItem();
    }

    public MenuTree getMenuTree() {
        return menuTree;
    }

    public JsonMenuItemSerializer getJsonSerializer() {
        return jsonSerializer;
    }

    // Accessors for each menu item now follow
    
    public AnalogMenuItem getLED1Brightness() {
        return (AnalogMenuItem) menuTree.getMenuById(1).orElseThrow();
    }

    public AnalogMenuItem getLED2Brightness() {
        return (AnalogMenuItem) menuTree.getMenuById(2).orElseThrow();
    }

    public EnumMenuItem getInputControl() {
        return (EnumMenuItem) menuTree.getMenuById(3).orElseThrow();
    }

    public BooleanMenuItem getLoggingMonitor() {
        return (BooleanMenuItem) menuTree.getMenuById(4).orElseThrow();
    }

    public SubMenuItem getStatus() {
        return (SubMenuItem) menuTree.getMenuById(5).orElseThrow();
    }

    public FloatMenuItem getStatusCaseTempOC() {
        return (FloatMenuItem) menuTree.getMenuById(6).orElseThrow();
    }

    public Rgb32MenuItem getStatusLightColor() {
        return (Rgb32MenuItem) menuTree.getMenuById(7).orElseThrow();
    }

    public SubMenuItem getSettings() {
        return (SubMenuItem) menuTree.getMenuById(8).orElseThrow();
    }

    public EditableTextMenuItem getSettingsNewTextItem() {
        return (EditableTextMenuItem) menuTree.getMenuById(10).orElseThrow();
    }

    public EditableTextMenuItem getSettingsSomeText() {
        return (EditableTextMenuItem) menuTree.getMenuById(11).orElseThrow();
    }

    public CustomBuilderMenuItem getStatusAuthenticator() {
        return (CustomBuilderMenuItem) menuTree.getMenuById(12).orElseThrow();
    }

    public CustomBuilderMenuItem getStatusIoTMonitor() {
        return (CustomBuilderMenuItem) menuTree.getMenuById(13).orElseThrow();
    }

    public EditableLargeNumberMenuItem getSettingsLargeNum() {
        return (EditableLargeNumberMenuItem) menuTree.getMenuById(14).orElseThrow();
    }

    public RuntimeListMenuItem getStatusMyListItem() {
        return (RuntimeListMenuItem) menuTree.getMenuById(15).orElseThrow();
    }

}
