package com.thecoderscorner.menu.editorui.generator.ejava;

public class EmbeddedJavaGeneratorFileData {
    public static final String EJAVA_CONTROLLER_CODE = """
            import com.thecoderscorner.menu.mgr.*;
            import com.thecoderscorner.menu.domain.*;
            import com.thecoderscorner.menu.domain.state.*;
            import xyz;
            class Xyz implements MenuManagerListener{
                private final EmbeddedJavaDemoMenu  menuDef;
                private final JfxNavigationManager navigationManager;
                private final ScheduledExecutorService executorService;
                private final GlobalSettings globalSettings;
                private final MenuDef  menuDef;
                // Auto generated menu fields end here. Add your own fields after here. Please do not remove this line.
            
                // Start of menu callbacks
            
                public Controller(MenuDef menuDef) {
                    this.menuDef = menuDef;
                }
            
                @MenuCallback(id=2)
                public void callback1(Object sender, AnalogMenuItem item) {
                    // TODO - implement your menu behaviour here for test2
                }
            
                @MenuCallback(id=20)
                public void callback1(Object sender, EnumMenuItem item) {
                    // TODO - implement your menu behaviour here for Extra
                }
            
                @MenuCallback(id=79)
                public void _headerOnly(Object sender, EditableTextMenuItem item) {
                    // TODO - implement your menu behaviour here for Ip Item
                }
            
                @MenuCallback(id=99)
                public void callback2(Object sender, EditableTextMenuItem item) {
                    // TODO - implement your menu behaviour here for Text Item
                }
            
                @MenuCallback(id=2039, listResult=true)
                public void listHasChanged(Object sender, RuntimeListMenuItem item, ListResponse selInfo) {
                    // TODO - implement your menu behaviour here for My List
                }
            
                // Auto generated menu callbacks end here. Please do not remove this line or change code after it.
            
                public void menuItemHasChanged(Object sender, MenuItem item) {
                }
            
                @Override
                public void managerWillStart() {
                }
            
                @Override
                public void managerWillStop() {
                }
            }
            """;


    //                public void buildMenuInMenuComponents() {
    //                    MenuManagerServer menuManager = context.getBean(MenuManagerServer.class);
    //                    MenuCommandProtocol protocol = context.getBean(MenuCommandProtocol.class);
    //                    ScheduledExecutorService executor = context.getBean(ScheduledExecutorService.class);
    //                    LocalIdentifier localId = new LocalIdentifier(menuManager.getServerUuid(), menuManager.getServerName());
    //                    var remMenuMenuipConnector = new SocketBasedConnector(localId, executor, Clock.systemUTC(), protocol, "localhost", 3333, ConnectMode.FULLY_AUTHENTICATED);
    //                    var remMenuMenuip = new MenuInMenu(remMenuMenuipConnector, menuManager, menuManager.getManagedMenu().getMenuById(404).orElseThrow(), MenuInMenu.ReplicationMode.REPLICATE_ADD_STATUS_ITEM, 100000, 65000);
    //                    remMenuMenuip.start();
    //                    var remMenuMenuserConnector = new Rs232RemoteConnector(localId, COM1, 9600, protocol, executor, Clock.systemUTC(), ConnectMode.FULLY_AUTHENTICATED);
    //                    var remMenuMenuser = new MenuInMenu(remMenuMenuserConnector, menuManager, menuManager.getManagedMenu().getMenuById(1001).orElseThrow(), MenuInMenu.ReplicationMode.REPLICATE_NOTIFY, 200000, 75000);
    //                    remMenuMenuser.start();
    //                }

    public static final String EJAVA_MENU_CODE = """
            package pkg;
            
            import com.thecoderscorner.menu.domain.*;
            import com.thecoderscorner.menu.domain.state.*;
            import com.thecoderscorner.menu.persist.JsonMenuItemSerializer;
            import com.thecoderscorner.embedcontrol.core.util.BaseMenuConfig;
            import com.thecoderscorner.embedcontrol.core.util.TcApiDefinitions;;
            
            public class MenuDef implements TcApiDefinitions {
                private final static String APP_MENU_ITEMS = ""\"
            tcMenuCopy:[
              {
                "parentId": 0,
                "type": "enumItem",
                "item": {
                  "enumEntries": [
                    "test"
                  ],
                  "name": "Extra",
                  "id": 20,
                  "eepromAddress": 5,
                  "functionName": "callback1",
                  "readOnly": false,
                  "localOnly": false,
                  "visible": true,
                  "staticDataInRAM": false
                }
              },
              {
                "parentId": 0,
                "type": "analogItem",
                "item": {
                  "maxValue": 100,
                  "offset": 0,
                  "divisor": 1,
                  "unitName": "dB",
                  "step": 0,
                  "name": "test",
                  "id": 1,
                  "eepromAddress": 2,
                  "readOnly": true,
                  "localOnly": false,
                  "visible": true,
                  "staticDataInRAM": false
                }
              },
              {
                "parentId": 0,
                "type": "runtimeList",
                "item": {
                  "initialRows": 2,
                  "name": "Abc",
                  "id": 1043,
                  "eepromAddress": 0,
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
                  "name": "sub",
                  "variableName": "OverrideSubName",
                  "id": 100,
                  "eepromAddress": -1,
                  "readOnly": false,
                  "localOnly": true,
                  "visible": true,
                  "staticDataInRAM": false
                }
              },
              {
                "parentId": 100,
                "type": "analogItem",
                "item": {
                  "maxValue": 100,
                  "offset": 0,
                  "divisor": 1,
                  "unitName": "dB",
                  "step": 1,
                  "name": "test2",
                  "variableName": "OverrideAnalog2Name",
                  "id": 2,
                  "eepromAddress": 4,
                  "functionName": "callback1",
                  "readOnly": true,
                  "localOnly": true,
                  "visible": true,
                  "staticDataInRAM": false
                }
              },
              {
                "parentId": 100,
                "type": "textItem",
                "item": {
                  "textLength": 10,
                  "itemType": "PLAIN_TEXT",
                  "name": "Text Item",
                  "id": 99,
                  "eepromAddress": -1,
                  "functionName": "callback2",
                  "readOnly": false,
                  "localOnly": false,
                  "visible": true,
                  "staticDataInRAM": false
                }
              },
              {
                "parentId": 100,
                "type": "textItem",
                "item": {
                  "textLength": 20,
                  "itemType": "IP_ADDRESS",
                  "name": "Ip Item",
                  "id": 79,
                  "eepromAddress": -1,
                  "functionName": "@headerOnly",
                  "readOnly": false,
                  "localOnly": false,
                  "visible": true,
                  "staticDataInRAM": false
                }
              },
              {
                "parentId": 0,
                "type": "runtimeList",
                "item": {
                  "initialRows": 10,
                  "name": "My List",
                  "id": 2039,
                  "eepromAddress": 0,
                  "functionName": "listHasChanged",
                  "readOnly": false,
                  "localOnly": false,
                  "visible": true,
                  "staticDataInRAM": false
                }
              }
            ]""\";
                private final MenuTree menuTree;
                private final JsonMenuItemSerializer jsonSerializer;
               \s
                public MenuDef() {
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
               \s
                public AnalogMenuItem getTest() {
                    return (AnalogMenuItem) menuTree.getMenuById(1).orElseThrow();
                }
            
                public AnalogMenuItem getOverrideAnalog2Name() {
                    return (AnalogMenuItem) menuTree.getMenuById(2).orElseThrow();
                }
            
                public EnumMenuItem getExtra() {
                    return (EnumMenuItem) menuTree.getMenuById(20).orElseThrow();
                }
            
                public EditableTextMenuItem getIpItem() {
                    return (EditableTextMenuItem) menuTree.getMenuById(79).orElseThrow();
                }
            
                public EditableTextMenuItem getTextItem() {
                    return (EditableTextMenuItem) menuTree.getMenuById(99).orElseThrow();
                }
            
                public SubMenuItem getOverrideSubName() {
                    return (SubMenuItem) menuTree.getMenuById(100).orElseThrow();
                }
            
                public RuntimeListMenuItem getAbc() {
                    return (RuntimeListMenuItem) menuTree.getMenuById(1043).orElseThrow();
                }
            
                public RuntimeListMenuItem getMyList() {
                    return (RuntimeListMenuItem) menuTree.getMenuById(2039).orElseThrow();
                }
            
               \s
                public void configureMenuInMenuComponents(BaseMenuConfig config) {
                }
            
            }
            """;
}
