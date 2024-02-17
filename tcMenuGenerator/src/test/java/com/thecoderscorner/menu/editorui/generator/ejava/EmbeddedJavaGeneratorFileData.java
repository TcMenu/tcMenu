package com.thecoderscorner.menu.editorui.generator.ejava;

public class EmbeddedJavaGeneratorFileData {
    public static final String EJAVA_CONTROLLER_CODE = """
            package com.tester.tcmenu;
                        
            import com.thecoderscorner.menu.mgr.*;
            import com.thecoderscorner.menu.domain.*;
            import com.thecoderscorner.menu.domain.state.*;
                        
            public class UnitTestController implements MenuManagerListener {
                private final UnitTestMenu  menuDef;
               \s
                public UnitTestController(UnitTestMenu menuDef) {
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
                
               \s
                // Auto generated menu callbacks end here. Please do not remove this line or change code after it.
               \s
                public void menuItemHasChanged(Object sender, MenuItem item) {
                    // Called every time any menu item changes
                }
                
                @Override
                public void managerWillStart() {
                    // This is called just before the menu manager starts up, you can initialise your system here.
                }
                        
                @Override
                public void managerWillStop() {
                    // This is called just before the menu manager stops, you can do any shutdown tasks here.
                }
                        
            }
            """;

    public static final String EJAVA_APP_CODE = """
            package com.tester.tcmenu;
                        
            import com.thecoderscorner.menu.mgr.*;
            import com.thecoderscorner.menu.persist.MenuStateSerialiser;
            import com.thecoderscorner.menu.remote.protocol.*;
            import com.thecoderscorner.menu.remote.mgrclient.*;
            import java.util.concurrent.*;
                        
            /**
             * This class is the application class and should not be edited, it will be recreated on each code generation
             */
            public class UnitTestApp {
                private final MenuManagerServer manager;
                private final MenuConfig context;
                private final TagValMenuCommandProtocol tagVal;
               \s
                public UnitTestApp() {
                    context = new MenuConfig();
                    manager = context.getBean(MenuManagerServer.class);
                    tagVal = context.getBean(TagValMenuCommandProtocol.class);
                }
                        
                public void start() {
                    var serializer = context.getBean(MenuStateSerialiser.class);
                    serializer.loadMenuStatesAndApply();
                    Runtime.getRuntime().addShutdownHook(new Thread(serializer::saveMenuStates));
                    manager.addMenuManagerListener(context.getBean(UnitTestController.class));
                    manager.addConnectionManager(socketClient);
                    tagVal.unitTestMe();
                }
                        
                public static void main(String[] args) {
                    new UnitTestApp().start();
                }
                        
            }
            """;

    public static final String EJAVA_APP_CODE_MENU_IN_MENU = """
            package com.tester.tcmenu;
                        
            import com.thecoderscorner.menu.mgr.*;
            import com.thecoderscorner.menu.persist.MenuStateSerialiser;
            import com.thecoderscorner.menu.remote.protocol.*;
            import com.thecoderscorner.menu.remote.mgrclient.*;
            import java.util.concurrent.*;
            import com.thecoderscorner.menu.remote.*;
            import com.thecoderscorner.menu.remote.socket.*;
            import java.time.*;
            import com.thecoderscorner.embedcontrol.core.rs232;
                        
            /**
             * This class is the application class and should not be edited, it will be recreated on each code generation
             */
            public class UnitTestApp {
                private final MenuManagerServer manager;
                private final MenuConfig context;
                private final TagValMenuCommandProtocol tagVal;
               \s
                public UnitTestApp() {
                    context = new MenuConfig();
                    manager = context.getBean(MenuManagerServer.class);
                    tagVal = context.getBean(TagValMenuCommandProtocol.class);
                }
                        
                public void start() {
                    var serializer = context.getBean(MenuStateSerialiser.class);
                    serializer.loadMenuStatesAndApply();
                    Runtime.getRuntime().addShutdownHook(new Thread(serializer::saveMenuStates));
                    manager.addMenuManagerListener(context.getBean(UnitTestController.class));
                    buildMenuInMenuComponents();
                    manager.addConnectionManager(socketClient);
                    tagVal.unitTestMe();
                }
                        
                public static void main(String[] args) {
                    new UnitTestApp().start();
                }
                        
                public void buildMenuInMenuComponents() {
                    MenuManagerServer menuManager = context.getBean(MenuManagerServer.class);
                    MenuCommandProtocol protocol = context.getBean(MenuCommandProtocol.class);
                    ScheduledExecutorService executor = context.getBean(ScheduledExecutorService.class);
                    LocalIdentifier localId = new LocalIdentifier(menuManager.getServerUuid(), menuManager.getServerName());
                    var remMenuMenuipConnector = new SocketBasedConnector(localId, executor, Clock.systemUTC(), protocol, "localhost", 3333, ConnectMode.FULLY_AUTHENTICATED);
                    var remMenuMenuip = new MenuInMenu(remMenuMenuipConnector, menuManager, menuManager.getManagedMenu().getMenuById(404).orElseThrow(), MenuInMenu.ReplicationMode.REPLICATE_ADD_STATUS_ITEM, 100000, 65000);
                    remMenuMenuip.start();
                    var remMenuMenuserConnector = new Rs232RemoteConnector(localId, COM1, 9600, protocol, executor, Clock.systemUTC(), ConnectMode.FULLY_AUTHENTICATED);
                    var remMenuMenuser = new MenuInMenu(remMenuMenuserConnector, menuManager, menuManager.getManagedMenu().getMenuById(1001).orElseThrow(), MenuInMenu.ReplicationMode.REPLICATE_NOTIFY, 200000, 75000);
                    remMenuMenuser.start();
                }
                        
            }
            """;
    public static final String EJAVA_APP_CONTEXT = """
            package com.tester.tcmenu;
                        
            import com.thecoderscorner.menu.auth.*;
            import com.thecoderscorner.menu.mgr.MenuManagerServer;
            import com.thecoderscorner.menu.persist.*;
            import com.thecoderscorner.embedcontrol.core.util.*;
            import com.thecoderscorner.embedcontrol.core.service.*;
            import com.thecoderscorner.embedcontrol.customization.*;
            import com.thecoderscorner.embedcontrol.jfx.controlmgr.*;
            import com.thecoderscorner.menu.remote.protocol.ConfigurableProtocolConverter;
            import java.time.Clock;
            import java.util.*;
            import java.util.concurrent.*;
            import java.nio.file.Path;
            import com.thecoderscorner.menu.remote.protocol.*;
            import com.thecoderscorner.menu.remote.mgrclient.*;
                        
            /**
             * This class creates an application context out of all these components, and you can request any components that are
             * put into the context using getBean(ClassName.class). See the base class BaseMenuConfig for more details. Generally
             * don't change the constructor, as it is rebuild each time around. Prefer putting your own code in appCustomConfiguration
             */
            public class MenuConfig extends BaseMenuConfig {
                public MenuConfig() {
                    // Do not change this constructor, it is replaced with each build, put your objects in appCustomConfiguration
                    super(null, null);
                    Clock clock = asBean(Clock.systemUTC());
                    var executorService = asBean(Executors.newScheduledThreadPool(propAsIntWithDefault("threading.pool.size", 4)));
                    var menuDef = asBean(new UnitTestMenu());
                    asBean(new PropertiesMenuStateSerialiser(menuDef.getMenuTree(), Path.of(resolvedProperties.getProperty("file.menu.storage")).resolve("menuStorage.properties")));
                    scanForComponents();
                }
                        
                @TcComponent
                public JfxNavigationHeader navMgr(ScheduledExecutorService executorService, GlobalSettings settings) {
                    return new JfxNavigationHeader(executorService, settings);
                }
                        
                @TcComponent
                public ConfigurableProtocolConverter protocol() {
                    return new ConfigurableProtocolConverter(true);
                }
                        
                @TcComponent
                public MenuManagerServer menuManagerServer(Clock clock, UnitTestMenu menuDef, ScheduledExecutorService executorService, MenuAuthenticator authenticator) {
                    return new MenuManagerServer(executorService, menuDef.getMenuTree(), mandatoryStringProp("server.name"), UUID.fromString(mandatoryStringProp("server.uuid")), authenticator, clock);
                }
                        
                @TcComponent
                public UnitTestController menuController() {
                    return new EmbeddedJavaDemoController(
                            getBean(EmbeddedJavaDemoMenu.class),
                            getBean(JfxNavigationManager.class),
                            getBean(ScheduledExecutorService.class),
                            getBean(GlobalSettings.class),
                            getBean(MenuItemStore.class)
                    );
                }
                        
                @TcComponent
                public MenuItemStore itemStore(GlobalSettings settings, UnitTestMenu menuDef) {
                    return new MenuItemStore(settings, menuDef.getMenuTree(), "", 7, 2, settings.isDefaultRecursiveRendering());
                }
                        
                @TcComponent
                public GlobalSettings globalSettings() {
                    var settings = new GlobalSettings(new ApplicationThemeManager());
                    // load or adjust the settings as needed here. You could use the JDBC components to load and store
                    // these values just like embed control does. See TcPreferencesPersistence and TccDatabaseUtilities.
                    settings.setDefaultFontSize(16);
                    settings.setDefaultRecursiveRendering(false);
                    return settings;
                }
                        
                @TcComponent
                public MenuAppVersion versionInfo() {
                    var version = mandatoryStringProp("build.version");
                    var timestamp = mandatoryStringProp("build.timestamp");
                    var groupId = mandatoryStringProp("build.groupId");
                    var artifact = mandatoryStringProp("build.artifactId");
                    return new MenuAppVersion(new VersionInfo(version), timestamp, groupId, artifact);
                }
                        
                @TcComponent
                public LocaleMappingHandler localeHandler() {
                    return LocaleMappingHandler.NOOP_IMPLEMENTATION;
                }
                        
                @TcComponent
                public MenuAuthenticator menuAuthenticator() {
                    return new PreDefinedAuthenticator(true);
                }
                        
                @TcComponent
                public TagValMenuCommandProtocol tagVal() {
                    return new TagValMenuCommandProtocol();
                }
                        
                @TcComponent
                public SocketServerConnectionManager socketClient(TagValMenuCommandProtocol protocol, ScheduledExecutorService executor, Clock clock) {
                    return new SocketServerConnectionManager(protocol, executor, 3333, clock);
                }
                        
                // Auto generated menu callbacks end here. Please do not remove this line or change code after it.
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
                public UnitTestMenu() {
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
                        
            }
            """;

    public static final String EJAVA_POM_WITH_DEP = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?><!-- it is safe to edit this file, it will not be replaced by TcMenu designer unless you delete it --><project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.tester</groupId>
                <artifactId>UnitTest</artifactId>
                <name>UnitTest</name>
                <description>An application built with TcMenu Designer</description>
                <version>0.0.1-SNAPSHOT</version>
                        
                <properties>
                    <jdk.version>17</jdk.version>
                    <jserialcomm.version>2.9.2</jserialcomm.version>
                    <jfx.version>17.0.0.1</jfx.version>
                    <tcmenu.api.version>1.2.3</tcmenu.api.version>
                    <timestamp>${maven.build.timestamp}</timestamp>
                </properties>
                        
                <dependencies>
                    <dependency>
                        <groupId>com.fazecast</groupId>
                        <artifactId>jSerialComm</artifactId>
                        <version>${jserialcomm.version}</version>
                    </dependency>
                    <dependency>
                        <groupId>com.thecoderscorner.tcmenu</groupId>
                        <artifactId>tcMenuJavaAPI</artifactId>
                        <version>${tcmenu.api.version}</version>
                    </dependency>
                    <dependency>
                        <groupId>com.thecoderscorner.tcmenu</groupId>
                        <artifactId>embedCONTROLCore</artifactId>
                        <version>${tcmenu.api.version}</version>
                    </dependency>
                <dependency><groupId>com.thecoderscorner.tcmenu</groupId><artifactId>TestDep</artifactId><version>1.2.3</version></dependency></dependencies>
                        
                <build>
                    <finalName>UnitTest</finalName>
                    <resources>
                        <resource>
                            <directory>src/main/resources</directory>
                            <includes>
                                <include>application.properties</include>
                            </includes>
                            <filtering>true</filtering>
                        </resource>
                        <resource>
                            <directory>src/main/resources</directory>
                            <excludes>
                                <exclude>application.properties</exclude>
                            </excludes>
                            <filtering>false</filtering>
                        </resource>
                    </resources>
                    <plugins>
                        <plugin>
                            <groupId>org.apache.maven.plugins</groupId>
                            <artifactId>maven-compiler-plugin</artifactId>
                            <version>3.8.0</version>
                            <configuration>
                                <source>${jdk.version}</source>
                                <target>${jdk.version}</target>
                            </configuration>
                        </plugin>
                        <plugin>
                            <!-- copy all the JARs for the dependencies into the jfx/deps folder -->
                            <groupId>org.apache.maven.plugins</groupId>
                            <artifactId>maven-dependency-plugin</artifactId>
                            <version>3.0.2</version>
                            <executions>
                                <execution>
                                    <id>copy-dependencies</id>
                                    <phase>prepare-package</phase>
                                    <goals>
                                        <goal>copy-dependencies</goal>
                                    </goals>
                                    <configuration>
                                        <outputDirectory>${project.build.directory}/lib</outputDirectory>
                                        <overWriteReleases>false</overWriteReleases>
                                        <overWriteSnapshots>false</overWriteSnapshots>
                                        <overWriteIfNewer>true</overWriteIfNewer>
                                    </configuration>
                                </execution>
                                <execution>
                                    <id>copy-deps-to-package</id>
                                    <phase>prepare-package</phase>
                                    <goals>
                                        <goal>copy-dependencies</goal>
                                    </goals>
                                    <configuration>
                                        <outputDirectory>${project.build.directory}/jfx/deps</outputDirectory>
                                        <includeScope>runtime</includeScope>
                                        <overWriteReleases>false</overWriteReleases>
                                        <overWriteSnapshots>false</overWriteSnapshots>
                                        <overWriteIfNewer>true</overWriteIfNewer>
                                    </configuration>
                                </execution>
                            </executions>
                        </plugin>
                        <plugin>
                            <!-- copy the application JAR file from target dir into jfx/deps -->
                            <groupId>org.apache.maven.plugins</groupId>
                            <artifactId>maven-resources-plugin</artifactId>
                            <version>3.0.2</version>
                            <executions>
                                <execution>
                                    <id>copy-resources-jar</id>
                                    <phase>install</phase>
                                    <goals>
                                        <goal>copy-resources</goal>
                                    </goals>
                                    <configuration>
                                        <outputDirectory>${basedir}/target/jfx/deps</outputDirectory>
                                        <resources>
                                            <resource>
                                                <directory>${project.basedir}/target</directory>
                                                <filtering>false</filtering>
                                                <includes>
                                                    <include>${project.build.finalName}.jar</include>
                                                </includes>
                                            </resource>
                                        </resources>
                                    </configuration>
                                </execution>
                            </executions>
                        </plugin>
                        <plugin>
                            <!-- copy the data folder into the jfx/app folder -->
                            <artifactId>maven-resources-plugin</artifactId>
                            <version>3.0.2</version>
                            <executions>
                                <execution>
                                    <id>copy-resources-logging</id>
                                    <phase>validate</phase>
                                    <goals>
                                        <goal>copy-resources</goal>
                                    </goals>
                                    <configuration>
                                        <outputDirectory>${project.basedir}/target/jfx/app/</outputDirectory>
                                        <resources>
                                            <resource>
                                                <directory>${project.basedir}/data</directory>
                                                <filtering>false</filtering>
                                            </resource>
                                        </resources>
                                    </configuration>
                                </execution>
                            </executions>
                        </plugin>
                    </plugins>
                </build>
            </project>""";

    public static final String MODULE_FILE_CONTENTS = """
            module com.tester.unittest {
                requires java.logging;
                requires java.prefs;
                requires java.desktop;
                requires com.google.gson;
                requires com.fazecast.jSerialComm;
                requires com.thecoderscorner.tcmenu.javaapi;
                requires com.thecoderscorner.embedcontrol.core;
                exports com.tester.tcmenu.plugins;
                opens com.tester.tcmenu;
            }
            """;
}
