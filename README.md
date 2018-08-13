# tcMenu beta - An embedded menu system for Arduino

TcMenu is a full feature menu system for Arduino, that is modular enough to support different input types and displays;
it is currently under development so be careful before using in full production.

TcMenu is more than just an Arduino menu library, think of it more like a framework for building IoT applications that provides
many useful abstractions and remote control capabilities, including the ability to render menus locally onto a display.

## Types of input supported

* Button based rotary encoder emulation (Up, Down and OK buttons)
* Rotary encoder based input with no need any additional components in many cases.
* No local input facilities if your application is completely controlled remotely.

## Display types that are supported

* LiquidCrystal 20x4 or 16x2 displays - can be either directly connected, i2c or on a shift register.
* Adafruit_GFX - can render onto a display compatible with this library 

## Remote endpoints that are supported

* RS232 endpoint that supports full control of the menu items using a Java API - example app included.
* Ethernet endpoint that supports the Ethernet library specification.

## More information 

MenuItem's are mainly stored in program memory with a small amount of state in RAM; put it another way, a reasonably
complete menu can fit in less than 700 bytes including i2c and Serial libraries. The system also supports loading and saving
menu items to/from EEPROM storage.

## Getting started with tcMenu

You start a menu project either by using the provided user interface (recommended), or by coding a menu manually into a sketch. 

[Get a copy of the project from the releases page](https://github.com/davetcc/tcMenu/releases), it's available as an executable for Windows, a disk image for MacOS and also as a zip for any platform that can run Java 9 or above such as Linux, Windows or MacOS.

[Full documentation of the library on my site](https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/) 


## Generating a menu from the UI

When working with menus in the UI, I normally save the menu file into a new sketch directory. The reason for this is
that the generator will generate the sketch for you once you've defined the menus. The Generator as of V0.4 is capable of round trip development too - most of the code is offloaded into a CPP and Header.

Once you've arranged your menu using the UI how you'd like it, choose Code -> ID & Eeprom analyser from the menu
to check that you've not got any overlapping ranges, then choose Code -> Generate from the menu, choose appropriate
hardware arrangements and hit generate.

## The Java API

There is a java API for accessing the menu remotely, include the following into your maven build file:

        <dependency>
            <groupId>com.thecoderscorner.tcmenu</groupId>
            <artifactId>tcMenuJavaAPI</artifactId>
            <version>0.8</version>
        </dependency>

## More documentation

More complete documentation is available on the coders corner website:
[https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/] 
