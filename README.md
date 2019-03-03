# tcMenu beta - An embedded menu system for Arduino

TcMenu is a full feature menu system for Arduino, that is modular enough to support different input types and displays;
it is currently under development so be careful before using in full production. TcMenu is more than just an Arduino menu library, think of it as a framework for building IoT applications that provides many useful abstractions and remote control capabilities, including the ability to render menus locally onto a display.

To start one normally uses the menu designer UI, which is packaged with every release, and available for both Windows and MacOS. It should also be easy to run on Linux - contact me if interested. The designer UI takes care of building the core menu code and putting any callback functions into your sketch file. Think of the designer like a form designer in the desktop domain. Furthermore, It's non destructive on the sketch file, so can be round tripped during development.

## Questions, community forum and support

You can get help from the community forum, there are also support options available from TheCodersCorner.

* [TCC Libraries community discussion forum](https://www.thecoderscorner.com/jforum/)
* [Consultancy pages on the coders corner](https://www.thecoderscorner.com/products/consultancy/)

## Installation and documentation

There are pre-built packages for Windows and MacOS that include the Generator UI and the UI can copy over the libraries automatically. However, the embedded libraries are in the embedded directory in the above repository, and can be copied directly into the Arduino/libraries folder.

[Get a copy of the project from the releases page](https://github.com/davetcc/tcMenu/releases), it's available as an executable for Windows, a disk image for MacOS and also as a zip for any platform that can run Java 11 or above; such as most Linux distros, Windows 7, 8, 10 or any recent MacOSX.

[UI user guide, getting started and other documentation](https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/)

[Full API embedded documentation](https://www.thecoderscorner.com/ref-docs/tcmenu/html/index.html)

## Current Priorities to get the menu out of BETA

* Some more videos on youtube showing how to generate menus.
* More complete documentation on the coders corner showing how to use it.
* Better and more configurable options for the AdaGfx display range.
* Complete support for most types of OLED display.
* Complete support for the Nokia 5110 display.
* Improve the 16x2 LCD support to have an additional renderer with one item per page.

## Coming in the next version V1.2

* Acceleration on rotary encoders and repeat-key switches.
* Support for DfRobot LCD shield input switches on analog input.
* Plugin support to make it easy to support new devices, programming languages and boards.

## The Java API

There is a java API for accessing the menu remotely, source includes JavaDoc to help getting started. There is an example JavaFX UI built with it within the above Repo. Include the following into your maven build file:

        <dependency>
            <groupId>com.thecoderscorner.tcmenu</groupId>
            <artifactId>tcMenuJavaAPI</artifactId>
            <version>1.1</version>
        </dependency>

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
* Adafruit_GFX - can render onto a display compatible with this library.

## Remote endpoints that are supported

This menu library provides complete remote control, presently over serial and ethernet. The full menu structure is sent over the wire and the Java API provides it as a tree that can be manipulated. There is also a defined protocol for other languages. In addition to this the menu can be programatically manipulated very easily on the device.

* RS232 endpoint that supports full control of the menu items using a Java API - example app included.
* Ethernet endpoint that supports the Ethernet library specification.

## More information 

MenuItem's are mainly stored in program memory with a small amount of state in RAM. The system also supports loading and saving menu items to/from EEPROM storage. On AVR that will generally be internal EEPROM, on 32 bit boards generally an AT24 i2c EEPROM. 

## Getting started with tcMenu

You start a menu project either by using the provided user interface (recommended), or by coding a menu manually into a sketch. 

## Generating a menu from the UI

When working with menus in the UI, I normally save the menu file into a new sketch directory. The reason for this is
that the generator will generate the sketch for you once you've defined the menus. The Generator as of V0.4 is capable of round trip development too - most of the code is offloaded into a CPP and Header.

Once you've arranged your menu using the UI how you'd like it, choose Code -> ID & Eeprom analyser from the menu
to check that you've not got any overlapping ranges, then choose Code -> Generate from the menu, choose appropriate
hardware arrangements and hit generate.


