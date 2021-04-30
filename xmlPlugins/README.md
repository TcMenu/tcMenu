## Core plugins for tcMenu designer.

These are the core plugins available under an apache license for tcMenu Designer. Each plugin library contains a definition file that references the plugins to be loaded, along with details about the library. Each plugin is an xml file that's loaded at start up by the designer. They contain the definitions of global variables, function calls to make during startup and other definitions and include files needed for the driver to work. 

Full guide: https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/tcmenu-plugins/building-plugins-for-use-in-menu-designer/

See also: [https://github.com/davetcc/tcMenu]

## The plugin library definition - tcmenu-plugin.xml

For an example of this see [examples/example-plugin.xml], the schema is available in this folder called [tcmenu-plugin.xsd]

## Each plugin XML file, referenced by the library.

For an example of this see [examples/example-plugin-item.xml], the schema is available in this folder called [tcmenu-plugin-item.xsd]

## Advanced use: Using the plugins manually

**Advanced users only:** If you wish to write the display, input and IoT/remote glue code yourself, then either take a look at the nearest XML plugin to the one you need, or just start with the nearest tcMenu example. You'll need to make sure that at least all the global variables are declared and any setup functions called. We strongly recommend building menu structures using the designer UI.
