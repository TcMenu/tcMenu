## The overall plugin definition file

When tcMenu Designer starts up, it traverses the `~/.tcmenu/plugins` directory to find all the plugin libraries that can be loaded. A valid plugin library must have the library index file `tcmenu-plugin.xml`. This index file provides the core details about the plugin library, including the list of actual plugins to be loaded.

## The plugin library definition - tcmenu-plugin.xml

For an example of this see [example-plugin.xml], the schema is available too [../tcmenu-plugin.xsd].

## The plugin library format explained

All plugins must be valid XML documents, so should always include the xml tag.

    <?xml version="1.0" ?>

We have a schema for the plugin, always include it in the top level document as it makes it much easier to determine if the file is valid. 

    <TcMenuPluginDefinition shortName="core-themes" xmlns="https://www.thecoderscorner.com/libraries/tcmenuPlugin" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="https://www.thecoderscorner.com/libraries/tcmenuPlugin https://www.thecoderscorner.com/libraries/tcmenu-plugin.xsd">

Within the top level element we provide the general details, the author of the plugin, the url, version, name and description. You also provide a license, and a URL to the licence file.

        <GeneralDetails>
            <Author name="The Coders Corner" url="http://www.thecoderscorner.com/" />
            <Version>2.1.2</Version>
            <Name>Core themes plugin</Name>
            <Description>This plugin provides themes that can be used with all graphical displays.</Description>
            <License name="Apache 2.0" url="http://www.apache.org/licenses/LICENSE-2.0"/>
        </GeneralDetails>
    
Now we list all plugins that are included in this plugin library, there may be one or more. Each referenced XML file should be in the root directory of the plugin library. For example if the directory contained two plugin xml files `Plugin1.xml` and `Plugin2.xml`:

        <Plugins>
            <Plugin>Plugin1.xml</Plugin>
            <Plugin>Plugin2.xml</Plugin>
        </Plugins>

Lastly we terminate the top level element

    </TcMenuPluginDefinition>

