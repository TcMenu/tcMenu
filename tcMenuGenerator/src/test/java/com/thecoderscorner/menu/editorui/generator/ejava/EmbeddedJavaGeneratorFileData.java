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
            import org.springframework.context.ApplicationContext;
            import org.springframework.context.annotation.AnnotationConfigApplicationContext;
            import com.thecoderscorner.menu.remote.protocol.*;
            import com.thecoderscorner.menu.remote.mgrclient.*;
            import java.util.concurrent.*;
                        
            /**
             * This class is the application class and should not be edited, it will be recreated on each code generation
             */
            public class UnitTestApp {
                private final MenuManagerServer manager;
                private final ApplicationContext context;
                private final TagValMenuCommandProtocol tagVal;
               \s
                public UnitTestApp() {
                    context = new AnnotationConfigApplicationContext(MenuConfig.class);
                    manager = context.getBean(MenuManagerServer.class);
                    tagVal = context.getBean(TagValMenuCommandProtocol.class);
                }
                        
                public void start() {
                    manager.addMenuManagerListener(context.getBean(Testsupp123Controller.class));
                    manager.addConnectionManager(socketClient);
                    tagVal.unitTestMe();
                    manager.start();
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
            import com.thecoderscorner.menu.persist.*;
            import org.springframework.beans.factory.annotation.Value;
            import org.springframework.context.annotation.*;
            import java.time.Clock;
            import java.util.UUID;
            import java.util.concurrent.*;
            import java.nio.file.Path;
            import com.thecoderscorner.menu.remote.protocol.*;
            import com.thecoderscorner.menu.remote.mgrclient.*;
                        
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
                public MenuStateSerialiser menuStateSerialiser(UnitTestMenu menuDef, @Value("${file.menu.storage}") String filePath) {
                    return new PropertiesMenuStateSerialiser(menuDef.getMenuTree(), Path.of(filePath));
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
                public ScheduledExecutorService executor(@Value("${threading.pool.size}") int poolSize) {
                    return Executors.newScheduledThreadPool(poolSize);
                }
                        
                @Bean
                public MenuManagerServer menuManagerServer(ScheduledExecutorService executor, UnitTestMenu menuDef, @Value("${server.name}") String serverName, @Value("${server.uuid}") String serverUUID, MenuAuthenticator authenticator, Clock clock) {
                    return new MenuManagerServer(executor, menuDef.getMenuTree(), serverName, UUID.fromString(serverUUID), authenticator, clock);
                }
                        
                @Bean
                public MenuAuthenticator menuAuthenticator() {
                    return new PreDefinedAuthenticator(true);
                }
                       
                @Bean
                public TagValMenuCommandProtocol tagVal() {
                    return new TagValMenuCommandProtocol();
                }
            
                @Bean
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
                    <jserialcomm.version>2.4.0</jserialcomm.version>
                    <jfx.version>17.0.0.1</jfx.version>
                    <tcmenu.api.version>1.2.3</tcmenu.api.version>
                    <springframework.version>5.3.16</springframework.version>
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
                        <groupId>org.springframework</groupId>
                        <artifactId>spring-context</artifactId>
                        <version>${springframework.version}</version>
                    </dependency>
                <dependency><groupId>com.thecoderscorner.tcmenu</groupId><artifactId>TestDep</artifactId><version>1.2.3</version></dependency></dependencies>
                        
                <build>
                    <finalName>UnitTest</finalName>
                    <resources>
                        <resource>
                            <directory>src/main/resources</directory>
                            <includes>
                                <include>version.properties</include>
                            </includes>
                            <filtering>true</filtering>
                        </resource>
                        <resource>
                            <directory>src/main/resources</directory>
                            <excludes>
                                <exclude>version.properties</exclude>
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
                                                <directory>${project.basedir}/src/main/deploy</directory>
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
}
