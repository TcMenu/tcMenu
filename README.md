# tcMenu - A menu system for Arduino with IoT capabilities

A menu system for Arduino that is modular enough to support different input methods, display modules and remote control methods. TcMenu is more than just an Arduino menu library, think of it as a framework for building IoT applications that includes the ability to render menus locally onto a display.

Initially, you can use the menu designer UI that is packaged with every release, and available for both Windows and MacOS. It should also be easy to run on Linux - contact me if interested. The designer UI takes care of building the core menu code and putting any callback functions into your sketch file. Think of the designer like a form designer in the desktop domain. Furthermore, It's non destructive on the sketch file, so can be round tripped during development.

## Questions, community forum and support

You can get help from the community forum, there are also support and consultancy options available from TheCodersCorner.

* [TCC Libraries community discussion forum](https://www.thecoderscorner.com/jforum/)
* [Consultancy pages on the coders corner](https://www.thecoderscorner.com/support-services/consultancy/)

## Installation and documentation

Nearly all users should probably choose the designer UI package; it's available for Windows and MacOS and includes all the embedded libraries. The designer UI will copy all the required libraries into place for you. However, should you wish to go it alone, the embedded libraries are in the embedded directory in the above repository, and can be copied directly into the Arduino/libraries folder.

[Get the latest TcMenu Designer release](https://github.com/davetcc/tcMenu/releases), it's available as an executable for Windows, a disk image for MacOS and can probably be simply built for Linux desktop. Contact us if you desire a Linux build.

[UI user guide, getting started and other documentation](https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/)

[Full API embedded documentation](https://www.thecoderscorner.com/ref-docs/tcmenu/html/index.html)

## Generating a menu from the UI for the impatient

If you don't want to read the above documentation this gives a very quick start. Open the tcMenu Designer UI to start with and ensure the embedded libraries are up to date.

Load the EMF file from an example closest to the hardware you have. You'll see the menu tree structure on the left, and the details for each menu when selected on the right. Below the menu tree are buttons that manage items in the menu tree. 

Once you've arranged your menu using the UI how you'd like it, choose `Code -> ID & Eeprom analyser` from the menu
to check that you've not got any overlapping ranges, then choose `Code -> Generate` from the menu, choose appropriate
hardware arrangements and hit generate.

The Generator is capable of round trip development too - most of the code is offloaded into associated CPP and Header files.

## Priorities for the next versions

* Some more videos on youtube showing how to generate menus.
* More complete documentation on the coders corner showing how to use it - in progress

## TcMenu saves memory in many ways

Memory usage is so low that it's even viable for Arduino Uno and other smaller boards, by holding all static data possible in static RAM, and only including the display drivers and remotes that you're using. 

This means:

* No virtuals in the whole menuitem strucutre; while the API hides away the PROGMEM near completely.
* On smaller boards such as Uno, a reasonably sized menu with Serial and Ethernet fits into the small 2K RAM.
* Remote code is now plugin based, stored in a separate set of files, included into your sketch directory as needed, this will reduce the size for people not wanting remote capabilities, or wanting only Serial remote.
* If you previously used the RemoteListener, which was clunky to use, it's now replaced with a callback function that reports connections, disconnections and errors using a status type.

## Types of input supported

* Button based rotary encoder emulation (Up, Down and OK buttons) either local, i2c expander, shift register, or DfRobot analog.  
* Rotary encoder based input with no need any additional components in many cases. Either local or i2c expander.
* No local input facilities if your application is completely controlled remotely.

## Display types that are supported

* LiquidCrystal 20x4 or 16x2 displays - can be either directly connected, i2c or on a shift register.
* Adafruit_GFX - can render onto a display compatible with this library, tested with Color ILI9341 and Nokia 5110 display.

## Remote endpoints that are supported

This menu library provides complete remote control, presently over serial and ethernet. The full menu structure is sent over the wire and the Java API provides it as a tree that can be manipulated. There is also a defined protocol for other languages. In addition to this the menu can be programatically manipulated very easily on the device.

* RS232 endpoint that supports full control of the menu items using a Java API - example app included.
* Ethernet endpoint that supports either Ethernet2 library or UipEthernet.

## Ready built remote control for tcMenu

Is now included from 1.3 onwards and provides complete control of a menu without needing to build anything.

[https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/tcmenu-remote-connection-arduino-desktop/]

## Accessing TcMenu remotely with the Java API

There is a java API for accessing the menu remotely, source includes JavaDoc to help getting started. There is an example JavaFX UI built with it within the above Repo. Include the following into your maven build file:

        <dependency>
            <groupId>com.thecoderscorner.tcmenu</groupId>
            <artifactId>tcMenuJavaAPI</artifactId>
            <version>1.3.3</version>
        </dependency>

## Loading and saving menu items
tcMenu can also save menu item state to EEPROM storage. On AVR that will generally be internal EEPROM, on 32 bit boards generally an AT24 i2c EEPROM. 
