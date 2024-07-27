## Developing TcMenu locally in an IDE

This guide is aimed at developers wanting to build the UI locally within an IDE.

## Required Components

* Java 20 OpenJDK runtime, you can use any that you wish. IMPORTANT you must use at least V20.
* OpenJFX, normally included from maven, if you don't use Liberica Full JDK, then you will need to adjust the designer pom.xml to bring in OpenJFX. See comments in pom.xml of each module.
* IntelliJ either community or ultimate should work. Use the latest one available.
* Local clone of the main repo from https://github.com/TcMenu/tcMenu
* If you want to build the JavaAPI via maven you will need the GPG tool installed

## Instructions for IntelliJ

1. Take a local clone of the main repo
2. Open the  tcMenu directory into IntelliJ it will show as a project in the open dialog.
3. Ensure the V20 JDK is setup from Settings/Project Structure. With this the project should be good to go.
4. You can use the run configurations existing within the project.

_IMPORTANT for Liberica JDK, if you are using Liberica Full JDK, ensure that the Settings/Compiler option to enforce modules with --release option is OFF._

## Instructions for simpler changes (text editor and maven)

Simply load the project directory in an editor such as VSCode that allows you to see the directory structure. Then use the [packager-local-platforms.md](/tcMenuGenerator/scripts/packager-all-platforms.md) instructions in tcMenuGenerator/scripts to build and run afterwards.

## Running the designer locally

There are a couple of common VM parameters, they are added by default to the designer run configuration:

    -Ddevlog=Y -DalwaysShowSplash=Y

* Enabling `devlog` writes logs to the console along with to the log file.
* Enabling `alwaysShowSplash` shows the new version splash every time.

Note that the starting directory is relatively unimportant for most operations.

## Standard IntelliJ run configurations:

Most of the common things you'd want to run are already set up as run configurations:

* tcMenuJavaAPI Tests - run the tests in the Java API project
* TcMenuDesigner Tests - run the unit tests in the designer project
* TcMenuDesigner IntTests - run the integration / UI automation tests in the designer
* TcMenu Designer UI - run the designer UI with the console logging option turned on.
* EmbeddedJavaDemoApp - runs the RaspberryPI demo application build on top of the API.
* EmbedControlApp - runs the embedCONTROL application

## Designer test cases

The designer test cases are split into two, unit and UI integration tests. The unit tests are within the `editorui` package and run within a second or so. The UI integration tests actually start the UI components and these are located in the `editorint` package.  UI automation tests written in TestFX and exercise the UI, they take quite a lot of time to run. I usually only run these a couple of times per release cycle to check for issues, if you want to run these add the argument below, to avoid running these, don't run the tests in the `editorint` package.

If you're not using the standard run configurations in IntelliJ, then add the following "VM options" as this avoids JavaFX throwing an error:

    -ea --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED

## Some notes about the API

The API is broken into several features.

* domain - these classes are the representation of menu items in the Java domain
* domain/state - these classes hold the current value of a menu item
* mgr - contain classes for embedded Java systems (IE Java being server side of connection)
* remote - contains both classes for acting as a client, and general remote server code.

Classes of note

* MenuItem - the core menu item, items are immutable, they tend to be created using a builder ending in Builder 
* MenuTree - holds all the menu items and where they fit into the structure.
* MenuManagerServer - this is mainly for use in embedded Java applications it implements the server side of the protocol.
* RemoteMenuController - this provides the client side of a Java API connection, handling MenuTree updates
* RemoteConnector - this is the base class of all remote connections for the client side.
* ConfigurableProtocolConverter - this is the starting point for TagVal protocol.

## Some core classes with designer

In IntelliJ classes can be found quickly by either using Ctrl-N or Ctrl-O depending on your setup. It is convenient to make your way around using find usages (Ctrl-Alt-F7) and go to implementation/definition (Ctrl-B)/(Ctrl-Alt-B).

Important packages

* cli - contains all the CLI commands that can be used with the tcmenu command.
* controller - contains each of the JavaFX controller objects (MVC pattern)
* dialog - contains a class that can create each type of form / dialog
* generator - the code generator is under here, split into a few sub packages, for various functions
* project - the code to handle loading, saving and processing tcmenu projects
* simui - this simply allows for embedCONTROL to be integrated so as to show a proper prototype
* storage - contains mainly configuration classes
* uimodel - contains a UI editor for each menu item type

Important classes

* MenuEditorApp - this is the main class
* UIMenuItem - there is one of these for each type of menu item, generally looking at this package, you'll see there is a near 1-1 mapping with MenuItem.
* LocalMappingHandler - this is the component that is responsible for i18n of menus in the UI. It is technically in the API but shown here because of its importance.
* AppInfoPanel - the app information that appears when root is selected
* CoreCodeGenerator - also ArduinoCodeGenerator and MbedCodeGenerator are used to do conversions
* ArduinoSketchFileAdjuster - is responsible for saving changes to sketches.
* MenuItemToEmbeddedGenerator - converts the menu items into C++ code for Arduino and mbed.

## How to develop plugins quicker

Plugins are installed into the `~/.tcmenu/plugins` directory, each directory under this folder is treated as a plugin, and will be expected to follow the plugins format, see the `xmlPlugins` [README.md](/xmlPlugins/README.md) file that describes the format in detail. However, from a development perspective, if you are changing plugins it is more convenient not to need to run maven to package them each time. How to locally edit them:

* In the `.tcmenu/plugins` folder add an empty file `.development` which will prevent the designer trying to overwite anything.
* Remove the three plugin directories so that the only file left is the one we created
* Then for each plugin directory in the tcMenuRepo/xmlPlugins directory create a symlink to each of the three plugins under the xmlPlugins directory. 

On macOS/Linux 

    ln -s path-to-tcMenu/xmlPlugins/core-display
    ln -s path-to-tcMenu/xmlPlugins/core-themes
    ln -s path-to-tcMenu/xmlPlugins/core-remote

On Windows

    mklink /D core-display path-to-tcMenu\xmlPlugins\core-display
    mklink /D core-remote path-to-tcMenu\xmlPlugins\core-remote
    mklink /D core-themes path-to-tcMenu\xmlPlugins\core-themes


## Building the API or EmbedCONTROL Core with Maven

If for any reason you want to build the API with maven (IE outside of IntelliJ), you'll need to set up a GPG key. You don't need to follow the steps for broadcasting the key as you're not going to deploy the release to maven central.

https://maven.apache.org/developers/release/pmc-gpg-keys.html

See the "Configure passphrase in settings.xml" in the following: https://maven.apache.org/plugins/maven-gpg-plugin/usage.html

Once these steps are done, you should be able to build the API and embedCONTROL Core.

