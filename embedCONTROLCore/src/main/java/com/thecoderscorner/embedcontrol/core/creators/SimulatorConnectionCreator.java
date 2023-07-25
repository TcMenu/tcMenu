package com.thecoderscorner.embedcontrol.core.creators;

import com.thecoderscorner.embedcontrol.core.simulator.SimulatedRemoteConnection;
import com.thecoderscorner.embedcontrol.core.util.StringHelper;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.persist.JsonMenuItemSerializer;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteMenuController;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import static com.thecoderscorner.menu.domain.state.MenuTree.ROOT;

/**
 * This class provides the ability to create simulator connections. It is mainly
 * used by embedCONTROL remote to both present and deal with new connections.
 */
public class SimulatorConnectionCreator implements ConnectionCreator {
    public static final String SIMULATED_CREATOR_TYPE = "simulator";
    private final String DEFAULT_DATA = """
            tcMenuCopy:[
              {
                "parentId": 0,
                "type": "analogItem",
                "item": {
                  "maxValue": 255,
                  "offset": -128,
                  "divisor": 2,
                  "unitName": "V",
                  "name": "Voltage",
                  "variableName": "",
                  "id": 1,
                  "eepromAddress": 2,
                  "functionName": "onVoltageChange",
                  "readOnly": false,
                  "localOnly": false,
                  "visible": true
                }
              },
              {
                "parentId": 0,
                "type": "analogItem",
                "item": {
                  "maxValue": 255,
                  "offset": 0,
                  "divisor": 100,
                  "unitName": "A",
                  "name": "Current",
                  "variableName": "",
                  "id": 2,
                  "eepromAddress": 4,
                  "functionName": "onCurrentChange",
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
                    "Current",
                    "Voltage"
                  ],
                  "name": "Limit",
                  "variableName": "",
                  "id": 3,
                  "eepromAddress": 6,
                  "functionName": "onLimitMode",
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
                  "variableName": "",
                  "id": 4,
                  "eepromAddress": -1,
                  "readOnly": false,
                  "localOnly": false,
                  "visible": true
                }
              },
              {
                "parentId": 4,
                "type": "boolItem",
                "item": {
                  "naming": "YES_NO",
                  "name": "Pwr Delay",
                  "variableName": "",
                  "id": 5,
                  "eepromAddress": -1,
                  "readOnly": false,
                  "localOnly": false,
                  "visible": true
                }
              },
              {
                "parentId": 4,
                "type": "actionMenu",
                "item": {
                  "name": "Save all",
                  "variableName": "",
                  "id": 10,
                  "eepromAddress": -1,
                  "functionName": "onSaveRom",
                  "readOnly": false,
                  "localOnly": false,
                  "visible": true
                }
              },
              {
                "parentId": 4,
                "type": "subMenu",
                "item": {
                  "secured": false,
                  "name": "Advanced",
                  "variableName": "",
                  "id": 11,
                  "eepromAddress": -1,
                  "readOnly": false,
                  "localOnly": false,
                  "visible": true
                }
              },
              {
                "parentId": 11,
                "type": "boolItem",
                "item": {
                  "naming": "ON_OFF",
                  "name": "S-Circuit Protect",
                  "variableName": "",
                  "id": 12,
                  "eepromAddress": 8,
                  "readOnly": false,
                  "localOnly": false,
                  "visible": true
                }
              },
              {
                "parentId": 11,
                "type": "actionMenu",
                "item": {
                  "name": "Hidden item",
                  "variableName": "",
                  "id": 16,
                  "eepromAddress": -1,
                  "readOnly": false,
                  "localOnly": false,
                  "visible": false
                }
              },
              {
                "parentId": 11,
                "type": "boolItem",
                "item": {
                  "naming": "ON_OFF",
                  "name": "Temp Check",
                  "variableName": "",
                  "id": 13,
                  "eepromAddress": 9,
                  "readOnly": false,
                  "localOnly": false,
                  "visible": true
                }
              },
              {
                "parentId": 11,
                "type": "rgbItem",
                "item": {
                  "includeAlphaChannel": false,
                  "name": "RGB",
                  "variableName": "",
                  "id": 26,
                  "eepromAddress": 16,
                  "functionName": "onRgbChanged",
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
                  "variableName": "",
                  "id": 7,
                  "eepromAddress": -1,
                  "readOnly": false,
                  "localOnly": false,
                  "visible": true
                }
              },
              {
                "parentId": 7,
                "type": "floatItem",
                "item": {
                  "numDecimalPlaces": 2,
                  "name": "Volt A0",
                  "variableName": "",
                  "id": 8,
                  "eepromAddress": -1,
                  "readOnly": false,
                  "localOnly": false,
                  "visible": true
                }
              },
              {
                "parentId": 7,
                "type": "floatItem",
                "item": {
                  "numDecimalPlaces": 2,
                  "name": "Volt A1",
                  "variableName": "",
                  "id": 9,
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
                  "name": "Connectivity",
                  "variableName": "",
                  "id": 14,
                  "eepromAddress": -1,
                  "readOnly": false,
                  "localOnly": false,
                  "visible": true
                }
              },
              {
                "parentId": 14,
                "type": "textItem",
                "item": {
                  "textLength": 20,
                  "itemType": "IP_ADDRESS",
                  "name": "Ip Address",
                  "variableName": "",
                  "id": 15,
                  "eepromAddress": 10,
                  "readOnly": false,
                  "localOnly": false,
                  "visible": true
                }
              },
              {
                "parentId": 14,
                "type": "customBuildItem",
                "item": {
                  "name": "IoT Monitor",
                  "variableName": "IoTMonitor",
                  "id": 27,
                  "eepromAddress": -1,
                  "readOnly": false,
                  "localOnly": true,
                  "visible": true
                }
              },
              {
                "parentId": 14,
                "type": "customBuildItem",
                "item": {
                  "menuType": "AUTHENTICATION",
                  "name": "Authenticator",
                  "variableName": "Authenticator",
                  "id": 28,
                  "eepromAddress": -1,
                  "readOnly": false,
                  "localOnly": true,
                  "visible": true
                }
              },
              {
                "parentId": 0,
                "type": "subMenu",
                "item": {
                  "secured": false,
                  "name": "Rom Values",
                  "variableName": "",
                  "id": 20,
                  "eepromAddress": -1,
                  "functionName": "",
                  "readOnly": false,
                  "localOnly": false,
                  "visible": true
                }
              },
              {
                "parentId": 20,
                "type": "scrollItem",
                "item": {
                  "itemWidth": 10,
                  "eepromOffset": 1024,
                  "numEntries": 10,
                  "choiceMode": "ARRAY_IN_EEPROM",
                  "name": "Rom Choice",
                  "variableName": "",
                  "id": 25,
                  "eepromAddress": 14,
                  "readOnly": false,
                  "localOnly": false,
                  "visible": true
                }
              },
              {
                "parentId": 20,
                "type": "scrollItem",
                "item": {
                  "itemWidth": 10,
                  "eepromOffset": 0,
                  "numEntries": 10,
                  "choiceMode": "CUSTOM_RENDERFN",
                  "name": "Rom Location",
                  "variableName": "",
                  "id": 24,
                  "eepromAddress": -1,
                  "functionName": "",
                  "readOnly": false,
                  "localOnly": false,
                  "visible": true
                }
              },
              {
                "parentId": 20,
                "type": "textItem",
                "item": {
                  "textLength": 10,
                  "itemType": "PLAIN_TEXT",
                  "name": "Rom Text",
                  "variableName": "",
                  "id": 21,
                  "eepromAddress": -1,
                  "readOnly": false,
                  "localOnly": false,
                  "visible": true
                }
              },
              {
                "parentId": 20,
                "type": "actionMenu",
                "item": {
                  "name": "Save item",
                  "variableName": "",
                  "id": 23,
                  "eepromAddress": -1,
                  "functionName": "onSaveItem",
                  "readOnly": false,
                  "localOnly": false,
                  "visible": true
                }
              },
              {
                "parentId": 0,
                "type": "actionMenu",
                "item": {
                  "name": "Take display",
                  "variableName": "",
                  "id": 17,
                  "eepromAddress": -1,
                  "functionName": "onTakeDisplay",
                  "readOnly": false,
                  "localOnly": false,
                  "visible": true
                }
              }
            ]""";

    private String jsonForTree;
    private final String name;
    private final ScheduledExecutorService executorService;
    private final JsonMenuItemSerializer serializer;

    public SimulatorConnectionCreator(String jsonForTree, String name, ScheduledExecutorService executorService,
                                      JsonMenuItemSerializer serializer) {
        this.jsonForTree = jsonForTree;
        this.name = name;
        this.executorService = executorService;
        this.serializer = serializer;
    }

    public String getJsonForTree() {
        return jsonForTree;
    }

    @Override
    public AuthStatus currentState() {
        return null;
    }

    @Override
    public RemoteMenuController start() throws Exception {
        var menuTree = new MenuTree();
        try {
            var treeData = StringHelper.isStringEmptyOrNull(jsonForTree) ? DEFAULT_DATA : jsonForTree;
            var persistedMenus = serializer.copyTextToItems(treeData);
            for (var persistedItem : persistedMenus) {
                menuTree.addMenuItem((SubMenuItem) menuTree.getMenuById(persistedItem.getParentId()).orElse(ROOT), persistedItem.getItem());
            }
        }
        catch (Exception ex) {
            // ignore for now
        }

        SimulatedRemoteConnection remoteConnection = new SimulatedRemoteConnection(menuTree, name, UUID.randomUUID(), 100, new HashMap<>(), executorService);
        var controller = new RemoteMenuController(remoteConnection, menuTree);
        controller.start();
        return controller;
    }

    @Override
    public boolean attemptPairing(Consumer<AuthStatus> statusConsumer) {
        return true;
    }

    @Override
    public String toString() {
        return "SimulatorConnectionCreator{" +
                "name='" + name + '\'' +
                '}';
    }
}
