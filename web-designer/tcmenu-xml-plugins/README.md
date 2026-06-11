## Core plugins for tcMenu designer.

These are the core plugins available under an apache license for tcMenu Designer. They contain all the core input, display, and remote capabilities provided by designer. These are the various options you see in the code generator window when selecting a plugin. Each plugin item can set the platform applicability so that it's possible to control where something is visible. For example there's no purpose showing ESP8266 WiFi options unless the platform is appropriate.

Normally, for most users, the plugins will be managed by tcMenu Designer, and easily kept up to date by checking versions and using the in-app upgrader.

## Writing a plugin for TcMenu Designer

Each plugin library [contains a definition file](examples/plugin-definition-file.md) that references the plugins to be loaded, along with details about the library. [Each plugin item is an xml file](examples/plugin-item-file.md) that's loaded at start up by the designer. They contain the definitions of global variables, function calls to make during startup and other definitions and include files needed for the driver to work. These are then [used during code generation to put the needed code in place](examples/code-generator-and-plugins.md).

## Advanced use: Using the plugins manually

**Advanced users only:** If you wish to write the display, input and IoT/remote glue code yourself, then either take a look at the nearest XML plugin to the one you need, or just start with the nearest tcMenu example. You'll need to make sure that at least all the global variables are declared and any setup functions called. We strongly recommend building menu structures using the designer UI.
