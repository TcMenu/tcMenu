# tcMenu - A menu system for Arduino and mbed with IoT capabilities

A menu system for Arduino and mbed* that is modular enough to support different input methods, display modules and remote control methods. TcMenu is more than just an Arduino menu library, think of it as a framework for building IoT applications that includes the ability to render menus locally onto a display.

Initially, you can use the menu designer UI that is packaged with every release, and available for both Windows, MacOS and Linux. The designer UI takes care of building the core menu code and putting any callback functions into your sketch file. Think of the designer like a form designer in the desktop domain. Furthermore, It's non destructive on the sketch file, so can be round tripped during development.

*\*NOTE: mbed support is in early access at the moment, to use it you need to switch stream in the designer UI to BETA.*

## Questions, community forum and support

You can get help from the community forum, there are also support and consultancy options available from TheCodersCorner.

* [TCC Libraries community discussion forum](https://www.thecoderscorner.com/jforum/)
* [Consultancy pages on the coders corner](https://www.thecoderscorner.com/support-services/consultancy/)
* I also monitor the Arduino forum [https://forum.arduino.cc/], Arduino related questions can be asked there too.

## Installation and documentation

Nearly all users should probably choose the designer UI package; it's available for Windows and MacOS and includes all the embedded libraries. The designer UI will copy all the required libraries into place for you. However, should you wish to go it alone, the embedded libraries are in the embedded directory in the above repository, and can be copied directly into the Arduino/libraries folder.

### Windows 10:

For Windows 10 you can directly obtain from the Windows store using the following link: [https://www.microsoft.com/store/productId/9NHJNH9BCNJN]

### Any MacOS from High Sierra onwards

For any MacOS from High Sierra onwards, obtain directly from the Mac App Store: [https://apps.apple.com/gb/app/tcmenu-designer/id1527782002?mt=12] 

### Windows 7, 8 and Linux

[Get the latest TcMenu Designer release](https://github.com/davetcc/tcMenu/releases), it's available as an executable for Windows 7 and an archive of the package for Linux users. 

### Documentation

[UI user guide, getting started and other documentation](https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/)

[Full API embedded documentation](https://www.thecoderscorner.com/ref-docs/tcmenu/html/index.html)

## Generating a menu from the UI for the impatient

If you don't want to read the above documentation this gives a very quick start. Open the tcMenu Designer UI to start with and ensure the embedded libraries are up to date.

Load the EMF file from an example closest to the hardware you have. You'll see the menu tree structure on the left, and the details for each menu when selected on the right. Below the menu tree are buttons that manage items in the menu tree. 

Once you've arranged your menu using the UI how you'd like it, choose `Code -> ID & Eeprom analyser` from the menu
to check that you've not got any overlapping ranges, then choose `Code -> Generate` from the menu, choose appropriate
hardware arrangements and hit generate.

The Generator is capable of round trip development too - most of the code is offloaded into associated CPP and Header files.

## XML Plugins for tcMenu Designer

We needed to move these into their own repo, so that we could manage production versioning properly: [https://github.com/davetcc/tcMenuXmlPlugins]

## TcMenu saves memory in many ways

We try and keep even the smallest boards viable for tcMenu. However, there are limitations to what we can do. You can run a full menu on an Uno, but it's unlikely that the remote Ethernet support will fit. For anything that includes remote control support, we recommend at least 64K of flash memory. We store the menu items in static RAM where it's supported by the hardware, to further reduce memory on the board.

## Types of input supported

* Rotary encoder based input with no need for any additional components in many cases. Either local or connected to PCF8574 or MCP23017.
* Button based rotary encoder emulation (Up, Down and OK buttons) either local, i2c expander, shift register.
* DfRobot analog input style buttons. Either DfRobot, or other analog ladder (configurable in code).
* Matrix Keyboards of configurable size and key combination. Pre-canned options for 4x3 and 4x4 layouts.
* Analog Joystick rotary encoder. Allows for joystick input, where you move scroll through values with up / down.
* No local input facilities if your application is completely controlled remotely.

## Display types that are supported

* LiquidCrystal 20x4 or 16x2 displays - can be either directly connected, i2c or on a shift register.
* Adafruit_GFX - can render onto a display compatible with this library, tested with Color ILI9341, ST7735 and Nokia 5110 display.
* U8G2 - can render onto most buffered displays using this library. Tested with OLED devices such as SSD1306 and SH1106.

## Remote endpoints that are supported

This menu library provides complete remote control, presently over serial and ethernet. The full menu structure is sent over the wire and the Java API provides it as a tree that can be manipulated. There is also a defined protocol for other languages. In addition to this the menu can be programatically manipulated very easily on the device.

* RS232 endpoint that supports full control of the menu items using a Java API - example app included.
* Ethernet endpoint that supports either Ethernet2 library or UipEthernet.
* Ethernet endpoint for mbed that supports the mbed socket implementation.
* ESP8266 and ESP32 based WiFi both supported.

## Ready built remote control for tcMenu - embedCONTROL

[https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/tcmenu-remote-connection-arduino-desktop/]

## Accessing TcMenu remotely using an API

## Java / JVM API

There is a java API for accessing the menu remotely, source includes JavaDoc to help getting started. There is an example JavaFX UI built with it within the above Repo. Include the following into your maven build file:

        <dependency>
            <groupId>com.thecoderscorner.tcmenu</groupId>
            <artifactId>tcMenuJavaAPI</artifactId>
            <version>1.3.3</version>
        </dependency>

## Coming Soon C# / .NET API

We are currently quite far along on a C# port of the API. There's an issue in the issue track for the port and we'll let you know when it's further along.

## Loading and saving menu items

tcMenu can also save menu item state to EEPROM storage. On AVR that will generally be internal EEPROM, on 32 bit boards generally an AT24 i2c EEPROM. 
