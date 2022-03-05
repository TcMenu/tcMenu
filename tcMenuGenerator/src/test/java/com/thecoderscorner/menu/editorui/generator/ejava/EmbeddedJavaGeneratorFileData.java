package com.thecoderscorner.menu.editorui.generator.ejava;

public class EmbeddedJavaGeneratorFileData {
    public static final String EJAVA_CONTROLLER_CODE = """
            package com.tester.tcmenu;
                        
            import com.thecoderscorner.menu.mgr.*;
            import com.thecoderscorner.menu.domain.*;
                        
            public class UnitTestController implements MenuManagerListener {
                private final UnitTestMenu  menuDef;
               \s
                public UnitTestController(UnitTestMenu menuDef) {
                    this.menuDef = menuDef;
                }
                        
                @MenuCallback(id=2)
                public void callback1(AnalogMenuItem item, boolean remoteAction) {
                    // TODO - implement your menu behaviour here for test2
                }
                        
                @MenuCallback(id=20)
                public void callback1(EnumMenuItem item, boolean remoteAction) {
                    // TODO - implement your menu behaviour here for Extra
                }
                        
                @MenuCallback(id=79)
                public void _headerOnly(EditableTextMenuItem item, boolean remoteAction) {
                    // TODO - implement your menu behaviour here for Ip Item
                }
                        
                @MenuCallback(id=99)
                public void callback2(EditableTextMenuItem item, boolean remoteAction) {
                    // TODO - implement your menu behaviour here for Text Item
                }
                        
                public void menuItemHasChanged(MenuItem item, boolean remoteAction) {
                    // Called every time any menu item changes
                }
                        
            }
            """;

    public static final String EJAVA_APP_CODE = """
            package com.tester.tcmenu;

            import com.thecoderscorner.menu.mgr.*;
            import org.springframework.context.ApplicationContext;
            import org.springframework.context.annotation.AnnotationConfigApplicationContext;

            /**
             * This class is the application class and should not be edited, it will be recreated on each code generation
             */
            public class UnitTestApp {
                private final MenuManagerServer manager;
                private final ApplicationContext context;
               \s
                public UnitTestApp() {
                    context = new AnnotationConfigApplicationContext(MenuConfig.class);
                    manager = context.getBean(MenuManagerServer.class);
                }

                public void start() {
                    manager.start();
                    manager.addMenuManagerListener(context.getBean(Testsupp123Controller.class));
                }

                public static void main(String[] args) {
                    new Testsupp123App().start();
                }

            }
            """;

    public static final String EJAVA_APP_CONTEXT = """
            package com.tester.tcmenu;
                        
            import com.thecoderscorner.menu.auth.*;
            import com.thecoderscorner.menu.mgr.MenuManagerServer;
            import org.springframework.beans.factory.annotation.Value;
            import org.springframework.context.annotation.*;
            import java.time.Clock;
            import java.util.UUID;
            import java.util.concurrent.*;
                        
            /**
             * Spring creates an application context out of all these components, you can wire together your own objects in either
             * this same file, or you can import another file. See the spring configuration for more details. You're safe to edit
             * this file as the designer only appends new entries
             */
            @Configuration
            @PropertySource("classpath:application.properties")
            public class MenuConfig {
                @Bean
                public Clock clock() {
                    return Clock.systemUTC();
                }
                        
                @Bean
                public UnitTestMenu menuDef() {
                    return new UnitTestMenu();
                }
                        
                @Bean
                public UnitTestController menuController(UnitTestMenu menuDef) {
                    return new UnitTestController(menuDef);
                }
                        
                @Bean
                public MenuAuthenticator menuAuthenticator() {
                    return new PreDefinedAuthenticator(true);
                }
                        
                @Bean
                public ScheduledExecutorService executor(@Value("${threading.pool.size}") int poolSize) {
                    return Executors.newScheduledThreadPool(poolSize);
                }
                        
                @Bean
                public MenuManagerServer menuManagerServer(ScheduledExecutorService executor, UnitTestMenu menuDef, @Value("${server.name}") String serverName, @Value("${server.uuid}") String serverUUID, MenuAuthenticator authenticator, Clock clock) {
                    return new MenuManagerServer(executor, menuDef.getMenuTree(), serverName, UUID.fromString(serverUUID), authenticator, clock);
                }
                        
            }
            """;

    public static final String EJAVA_MENU_CODE = """
            package com.tester.tcmenu;
                        
            import com.thecoderscorner.menu.domain.*;
            import com.thecoderscorner.menu.domain.state.*;
            import com.thecoderscorner.menu.persist.JsonMenuItemSerializer;
                        
            public class UnitTestMenu {
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
                  "unitName": "dB",
                  "name": "test",
                  "id": 1,
                  "eepromAddress": 2,
                  "readOnly": true,
                  "localOnly": false,
                  "visible": true
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
                  "visible": true
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
                  "visible": true
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
                  "name": "test2",
                  "variableName": "OverrideAnalog2Name",
                  "id": 2,
                  "eepromAddress": 4,
                  "functionName": "callback1",
                  "readOnly": true,
                  "localOnly": true,
                  "visible": true
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
                  "visible": true
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
                  "visible": true
                }
              }
            ]""\";
                private final MenuTree menuTree;
                private final JsonMenuItemSerializer jsonSerializer;
               \s
                public UnitTestMenu() {
                            jsonSerializer = new JsonMenuItemSerializer();
                            menuTree = jsonSerializer.newMenuTreeWithItems(APP_MENU_ITEMS);
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
                        
            }
            """;
}
