## Core plugins for tcMenu designer.

These are the core plugins available in the free version of tcMenu Designer. Each plugin library contains a definition file that references each of the plugins to load, along with details about the library. Each plugin is an xml file that's loaded at start up by the designer. They contain the definitions of global variables, function calls to make during startup and other definitions and include files needed for the driver to work. 

## The plugin library definition - tcmenu-plugin.xml

For an example of this see [examples/example-plugin.xml], the schema is available in this folder called [tcmenu-plugin.xsd]

## Each plugin XML file, referenced by the library.

For an example of this see [examples/example-plugin-item.xml], the schema is available in this folder called [tcmenu-plugin-item.xsd]

## Using one of the plugin driver classes manually

We strongly recommend building menu structures using the designer UI. However, if for some reason you've decided not to use it. Nearly all plugins are used in the packaged examples, you'd take the appropriate code from here and probably start with one of the examples. 