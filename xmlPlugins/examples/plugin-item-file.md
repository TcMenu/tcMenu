## Format of a plugin item definition

Each `Plugin` entry in the `tcmenu-plugin.xml` library xml definition is pointing to a file in this format. It must be a vaild XML file that can be parsed using a standard XML parser.  

For an example of this see the `example` directory where a simple plugin is demonstrated, the schema is also available see `tcmenu-plugin-item.xsd`

## The plugin item file format

The root item of the plugin contains the name of the plugin and also the subsystem and ID, the ID must be generated using the Java UUID generator, the best way is from a JDK 11+ jshell, type: `UUID.randomUUID()`.

    <TcMenuPlugin name="Name of the plugin"
                  id="valid-uuid" subsystem="DISPLAY"
                  requiresDesigner="2.0" needsTheme="true"
                  xmlns="https://www.thecoderscorner.com/libraries/tcmenuPluginItem"                      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:schemaLocation="https://www.thecoderscorner.com/libraries/tcmenuPluginItem https://tcmenu.github.io/documentation/arduino-libraries/libraries/tcmenu-plugin-item.xsd">

Next we provide the platforms on which the plugin can work, below lists all the possibilities for that field: 

        <SupportedPlatforms>
            <Platform>ARDUINO_UNO</Platform>
            <Platform>ARDUINO_AVR</Platform>
            <Platform>ARDUINO_32</Platform>
            <Platform>ARDUINO_ESP8266</Platform>
            <Platform>ARDUINO_ESP32</Platform>
            <Platform>MBED_RTOS</Platform>
        </SupportedPlatforms>

The description is used in the code generator dialog, when selecting a plugin to provide a detailed description of what this plugin does.

        <Description>The long description of what this plugin does.</Description>

A link to the documentation for the plugin

        <Documentation link="https://www.thecoderscorner.com"/>

A list of libraries that are required for this plugin

        <RequiredLibraries>
            <RequiredLibrary>Lib1</RequiredLibrary>
        </RequiredLibraries>

Plugin directories should always have an `/Images` directory. The image is relative to that directory.

        <ImageFile>adagfx-color.jpg</ImageFile>

## Properties

Properties are available during code generation, they can be used like variables, and can hold various types of information. We'll look at how they can be used later, but now consider these like variables or properties.

There are several types of property: 

* header, variable - uses variable name validation
* int - validate as integer betwen `min` and `max`
* boolean - only allow true and false
* font - allow any font definition, see theme documentation on fonts.
* choice - allow one of a number of choices
* pin - allow a variable or pin to be provided.
* io-device - a reference to an IoAbstractionRef (default should be devicePins)

Here are some example properties:

        <Properties>
            <Property id="VAR_ID" name="Display Name" initial="initialValue" 
                      desc="Longer plugin description" type="variable" />
            <Property id="BOOL_ID" name="Boolean property" initial="false" 
                      desc="Boolean item description" type="boolean" />
            <Property id="INT_PROP" name="Integer property" initial="5" 
                      desc="A numeric field" type="int" min="1" max="10"/>
            <Property id="TEST_CHOICE" name="Choices" initial="Choice1" 
                      desc="Test choices" type="choice">
                <Choices>
                    <Choice desc="description of choice">Choice1</Choice>
                    <Choice>Choice2</Choice>
                </Choices>
            </Property>
        </Properties>

There are a few additional property variables that you can use within your xml, these are always added so safe to use in any item:

* ROOT - this property always references the first menu item
* TARGET - always set to the board type you selected
* NAMESPACE - always set to the namespace entered during setup
* APP_NAME - the name of the application
* APP_UUID - the UUID of the application

## Complex applicability

Complex applicability conditions can be added by nesting ApplicabilityDef entries. See the separate [page on applicability definitions](applicability-definitions.md). You can apply applicability on nearly all definitions within this file, either as a reference to one declared here, or locally. When defined in the applicability defs section the applicabilities can be nested with `and` / `or` operations. 

    <ApplicabilityDefs>
        <ApplicabilityDef key="applicabilityDef" mode="and">
            <Applicability whenProperty="INTERRUPT_SWITCHES" isValue="true"/>
            <ApplicabilityDef mode="or">
                <Applicability whenProperty="INT_PROP" isValue="10"/>
                <ApplicabilityDef>
                    <Applicability whenProperty="ROOT" isValue="test"/>
                    <Applicability whenProperty="INT_PROP" isValue="20"/>
                </ApplicabilityDef>
            </ApplicabilityDef>
        </ApplicabilityDef>
    </ApplicabilityDefs>

## Source files

Source files refer to files that are packaged with the plugin. These need to be included within the project in order to make the code compile. You can provide replacements using regular expression find matches, but you must replace the whole string. You can use any property as a variable by defining as `${PROPERTY_ID}`. Variable expansion can also be used here. 

The optional overwrite flag on source files all you to generate a one off file that will be edited by the user and never replaced.

        <SourceFiles>
            <Replacement find="#define DISPLAY_HAS_MEMBUFFER (true|false)" replace="#define DISPLAY_HAS_MEMBUFFER ${DISPLAY_BUFFERED}"/>
            <Replacement find="Adafruit_ILI9341" replace="${DISPLAY_TYPE}"/>
            <SourceFile name="adaGfxDriver/someFile.cpp" overwrite="false"/>
            <SourceFile name="adaGfxDriver/someFile.h"/>
            <SourceFile name="adaGfxDriver/someFile.zip" unzip="clean" dest="relativePath" />
        </SourceFiles>

## Including or importing files
   
Any files that need to be included are added here as include files, again you can use variable expansion and applicability here, there are several options for inSource: 

* true - a standard header included in the menu project header with quoted include
* false - a standard header included in the menu project header with global include
* cppsrc - a header file only to be included once, such as a font, with quotes 
* cpp -  a header file only to be included once, such as a font, global include

The priority can also be defined on include files, high moves the include file to the top, low puts the include file at the bottom.

        <IncludeFiles>
            <Header name="tcMenuAdaFruitGfx.h" inSource="true" />
            <Header name="tcMenuAdaFruitGfx.h" inSource="true" priority="high"/>
            <Header name="tcMenuAdaFruitGfx.h" inSource="true" priority="low"/>
        </IncludeFiles>

### Special cases for Java code generator

When the inSource flag is true, then an import statement will be added with exactly the same spec as the name parameter. However, when inSource is false, the definition either represents a maven dependency or java module system definition. For example:

* `name="mvn:org.openjfx/javafx-fxml@18" inSource="false"` indicates that group `org.openjfx` package `javafx-fxml` version `18` should be added to the pom.xml if not already present.
* `name="mod:requires:javafx.base" inSource="false"` indicates that `requires javafx.base;` should be added to the module-info.java file if not already there.

## Global variables

You can define global variables that will appear at the top level of the project menu file, again you can use both applicability and property expansion here.
    
        <GlobalVariables>

Variables can be defined for export only, that means we expect them to exist, but we just need to declare the existence of the variable.

            <Variable name="${DISPLAY_VARIABLE}" type="${DISPLAY_TYPE}" export="only" />

Or we can fully create the variable, with any parameters needed. You can provide parameters as a reference, value, font.

            <Variable name="myInstance" type="MyClassName" export="true">
                <Param value="30"/>
                <Param value="applicationInfo.name"/>
                <Param ref="${DISPLAY_VARIABLE}Drawable"/>
                <Param font="${FONT_VARIABLE}"/>
            </Variable>
        </GlobalVariables>

## Function definitions

Now we define any functions that need to be called in setup, in most cases it's best to keep these as simple as possible, but you can even provide lambda syntax if needed. Again the applicability definitions and property expansion work here too:    

        <SetupFunctions>
            <Function object="renderer" name="setUpdatesPerSecond">
                <Param value="${UPDATES_PER_SEC}"/>
                <Param ref="${DISPLAY_VARIABLE}"/>
                <Param ref="${DISPLAY_CONFIG}"/>
            </Function>

You can also define lambdas here that will be added to a function, this is useful if you are trying to interface an event driven library with tcMenu. Keep them as simple as possible to avoid making the plugin too complex.

            <Lambda name="onReleaseFn">
                <Param type="pinid_t" name="/*key*/"/>
                <Param type="bool" name="held"/>
                <Function name="onMenuSelect" object="menuMgr">
                    <Param value="held"/>
                </Function>
            </Lambda>

Then in a function we refer to the lambda.
    
            <Function name="myFn">
                <Param lambda="myLambda"/>
            </Function>

        </SetupFunctions>
    </TcMenuPlugin>

### Special notes for Java variables and functions

In the Java domain, variables by default are created in the application context using the new operator. To make these variables available in the App file so you can access during setup set `export="true"` on the variable, it will then be available by its name during setup.

If you access a parameter using ref, then you are treating this parameter as a call to get something out of the context, IE by calling `context.getBean`.
