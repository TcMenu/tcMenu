
export const counterStarter = `tcMenuCopy:[
  {
    "parentId": "0",
    "type": "analogItem",
    "item": {
      "name": "Count",
      "id": 4,
      "eepromAddress": -1,
      "readOnly": true,
      "localOnly": false,
      "visible": true,
      "staticDataInRAM": false,
      "functionName": "",
      "variableName": "",
      "maxValue": 255,
      "offset": 0,
      "divisor": 1,
      "unitName": "tms",
      "step": 0
    },
    "defaultValue": "0"
  },
  {
    "parentId": "0",
    "type": "actionMenu",
    "item": {
      "name": "Add",
      "id": 5,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true,
      "staticDataInRAM": false,
      "functionName": "tcAdded_onAddToCount",
      "variableName": ""
    },
    "defaultValue": "false"
  }
]`;

export const audioAmpStarter = `tcMenuCopy:[
  {
    "parentId": 0,
    "type": "analogItem",
    "defaultValue": "0",
    "item": {
      "maxValue": 100,
      "offset": 0,
      "divisor": 1,
      "unitName": "%",
      "step": 1,
      "name": "Volume",
      "variableName": "Volume",
      "id": 1,
      "eepromAddress": -1,
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
        "USB Audio",
        "Bluetooth",
        "CD Player",
        "Turntable"
      ],
      "name": "Channel",
      "variableName": "Channel",
      "id": 2,
      "eepromAddress": -1,
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
      "naming": "ON_OFF",
      "name": "Mute",
      "variableName": "Mute",
      "id": 3,
      "eepromAddress": -1,
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
      "naming": "ON_OFF",
      "name": "Direct",
      "variableName": "Direct",
      "id": 4,
      "eepromAddress": -1,
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
      "name": "Status",
      "variableName": "Status",
      "id": 5,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true,
      "staticDataInRAM": false
    }
  },
  {
    "parentId": 5,
    "type": "enumItem",
    "defaultValue": "0",
    "item": {
      "enumEntries": [
        "Standby",
        "Protection",
        "Overload",
        "Warm up",
        "Running"
      ],
      "name": "Amp State",
      "variableName": "AmpState",
      "id": 6,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true,
      "staticDataInRAM": false
    }
  },
  {
    "parentId": 5,
    "type": "analogItem",
    "defaultValue": "0",
    "item": {
      "maxValue": 3000,
      "offset": -2000,
      "divisor": 100,
      "unitName": "dB",
      "step": 1,
      "name": "Left Level",
      "variableName": "LeftLevel",
      "id": 7,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true,
      "staticDataInRAM": false
    }
  },
  {
    "parentId": 5,
    "type": "analogItem",
    "defaultValue": "0",
    "item": {
      "maxValue": 3000,
      "offset": -2000,
      "divisor": 100,
      "unitName": "dB",
      "step": 1,
      "name": "Right Level",
      "variableName": "RightLevel",
      "id": 8,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true,
      "staticDataInRAM": false
    }
  }
]`;

export const benchPsuStarter = `tcMenuCopy:[
  {
    "parentId": 0,
    "type": "analogItem",
    "defaultValue": "0",
    "item": {
      "maxValue": 500,
      "offset": 0,
      "divisor": 10,
      "unitName": "V",
      "step": 1,
      "name": "Tgt Volt1",
      "variableName": "TgtVolt1",
      "id": 2,
      "eepromAddress": -1,
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
      "maxValue": 500,
      "offset": 0,
      "divisor": 10,
      "unitName": "V",
      "step": 1,
      "name": "Tgt Volt2",
      "variableName": "TgtVolt2",
      "id": 1,
      "eepromAddress": -1,
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
      "maxValue": 300,
      "offset": 0,
      "divisor": 100,
      "unitName": "A",
      "step": 1,
      "name": "Tgt Amps1",
      "variableName": "TgtAmps1",
      "id": 3,
      "eepromAddress": -1,
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
      "maxValue": 300,
      "offset": 0,
      "divisor": 100,
      "unitName": "A",
      "step": 1,
      "name": "Tgt Amps2",
      "variableName": "TgtAmps2",
      "id": 4,
      "eepromAddress": -1,
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
      "naming": "YES_NO",
      "name": "Curr Limit1",
      "variableName": "CurrLimit1",
      "id": 5,
      "eepromAddress": -1,
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
      "naming": "YES_NO",
      "name": "Curr Limit2",
      "variableName": "CurrLimit2",
      "id": 6,
      "eepromAddress": -1,
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
        "Combined",
        "Separate"
      ],
      "name": "Operation",
      "variableName": "Operation",
      "id": 7,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true,
      "staticDataInRAM": false
    }
  }
]`;

export const iotMenuTemplate = `tcMenuCopy:[
  {
    "parentId": 0,
    "type": "subMenu",
    "item": {
      "secured": false,
      "name": "IoT Setup",
      "id": 1,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true,
      "staticDataInRAM": false
    }
  },
  {
    "parentId": 1,
    "type": "textItem",
    "defaultValue": "",
    "item": {
      "textLength": 20,
      "itemType": "PLAIN_TEXT",
      "name": "SSID",
      "variableName": "SSID",
      "id": 4,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true,
      "staticDataInRAM": false
    }
  },
  {
    "parentId": 1,
    "type": "textItem",
    "defaultValue": "",
    "item": {
      "textLength": 5,
      "itemType": "PLAIN_TEXT",
      "name": "Password",
      "variableName": "Password",
      "id": 5,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true,
      "staticDataInRAM": false
    }
  },
  {
    "parentId": 1,
    "type": "textItem",
    "defaultValue": "",
    "item": {
      "textLength": 5,
      "itemType": "IP_ADDRESS",
      "name": "IP Addr",
      "id": 2,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true,
      "staticDataInRAM": false
    }
  },
  {
    "parentId": 1,
    "type": "customBuildItem",
    "item": {
      "menuType": "REMOTE_IOT_MONITOR",
      "name": "IoT Monitor",
      "id": 3,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": true,
      "visible": true,
      "staticDataInRAM": false
    }
  },
  {
    "parentId": 1,
    "type": "customBuildItem",
    "item": {
      "menuType": "AUTHENTICATION",
      "name": "Authenticator",
      "id": 6,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": true,
      "visible": true,
      "staticDataInRAM": false
    }
  }
]`;

export const motorControlMenuTemplate = `tcMenuCopy:[
  {
    "parentId": 0,
    "type": "subMenu",
    "item": {
      "secured": false,
      "name": "Motor",
      "variableName": "Motor",
      "id": 1,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true,
      "staticDataInRAM": false
    }
  },
  {
    "parentId": 1,
    "type": "analogItem",
    "defaultValue": "0",
    "item": {
      "maxValue": 255,
      "offset": 0,
      "divisor": 1,
      "unitName": "mph",
      "step": 1,
      "name": "Speed",
      "variableName": "Speed",
      "id": 2,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true,
      "staticDataInRAM": false
    }
  },
  {
    "parentId": 1,
    "type": "enumItem",
    "defaultValue": "0",
    "item": {
      "enumEntries": [
        "North",
        "East",
        "South",
        "West"
      ],
      "name": "Direction",
      "variableName": "Direction",
      "id": 3,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true,
      "staticDataInRAM": false
    }
  },
  {
    "parentId": 1,
    "type": "enumItem",
    "defaultValue": "0",
    "item": {
      "enumEntries": [
        "Auto",
        "Manual"
      ],
      "name": "Mode",
      "variableName": "Mode",
      "id": 4,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true,
      "staticDataInRAM": false
    }
  },
  {
    "parentId": 1,
    "type": "floatItem",
    "defaultValue": "0.0",
    "item": {
      "numDecimalPlaces": 2,
      "name": "Acceleration",
      "variableName": "Acceleration",
      "id": 5,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true,
      "staticDataInRAM": false
    }
  },
  {
    "parentId": 1,
    "type": "boolItem",
    "defaultValue": "false",
    "item": {
      "naming": "TRUE_FALSE",
      "name": "Enable Motor",
      "variableName": "EnableMotor",
      "id": 7,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true,
      "staticDataInRAM": false
    }
  },
  {
    "parentId": 1,
    "type": "subMenu",
    "item": {
      "secured": false,
      "name": "Jog control",
      "variableName": "JogControl",
      "id": 6,
      "eepromAddress": -1,
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
      "name": "Go Faster..",
      "variableName": "GoFaster",
      "id": 8,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true,
      "staticDataInRAM": false
    }
  }
]`;

export const batteryMenuTemplate = `tcMenuCopy:[
  {
    "parentId": 0,
    "type": "subMenu",
    "item": {
      "secured": false,
      "name": "Power",
      "variableName": "Power",
      "id": 1,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true,
      "staticDataInRAM": false
    }
  },
  {
    "parentId": 1,
    "type": "analogItem",
    "defaultValue": "0",
    "item": {
      "maxValue": 100,
      "offset": 0,
      "divisor": 1,
      "unitName": "%",
      "step": 1,
      "name": "Battery Level",
      "variableName": "BatteryLevel",
      "id": 2,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true,
      "staticDataInRAM": false
    }
  },
  {
    "parentId": 1,
    "type": "enumItem",
    "defaultValue": "0",
    "item": {
      "enumEntries": [
        "Eco",
        "Normal",
        "Performance"
      ],
      "name": "Pwr Mode",
      "variableName": "PwrMode",
      "id": 3,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true,
      "staticDataInRAM": false
    }
  },
  {
    "parentId": 1,
    "type": "analogItem",
    "defaultValue": "0",
    "item": {
      "maxValue": 110,
      "offset": 10,
      "divisor": 1,
      "unitName": "s",
      "step": 1,
      "name": "Backlight Time",
      "variableName": "BacklightTime",
      "id": 4,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true,
      "staticDataInRAM": false
    }
  },
  {
    "parentId": 1,
    "type": "analogItem",
    "defaultValue": "0",
    "item": {
      "maxValue": 255,
      "offset": 0,
      "divisor": 1,
      "unitName": "s",
      "step": 1,
      "name": "Sleep Time",
      "variableName": "SleepTime",
      "id": 5,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true,
      "staticDataInRAM": false
    }
  },
  {
    "parentId": 1,
    "type": "enumItem",
    "defaultValue": "0",
    "item": {
      "enumEntries": [
        "Fast",
        "Trickle",
        "Failure",
        "Not Charging"
      ],
      "name": "Charging Status",
      "variableName": "ChargingStatus",
      "id": 6,
      "eepromAddress": -1,
      "readOnly": false,
      "localOnly": false,
      "visible": true,
      "staticDataInRAM": false
    }
  }
]`;

export interface  TcMenuStarter {
  name: string;
  copyText: string;
};

export const tcMenuStarers: TcMenuStarter[] = [
    { name: 'Audio Amplifier Starter', copyText: audioAmpStarter },
    { name: 'Bench PSU Starter', copyText: benchPsuStarter },
    { name: 'Count On Press Starter', copyText: counterStarter },
    { name: 'Motor Control Menu Template', copyText: motorControlMenuTemplate },
    { name: 'IoT Menu Template', copyText: iotMenuTemplate },
    { name: 'Battery Menu Template', copyText: batteryMenuTemplate }
];