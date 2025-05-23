## tcMenu - A menu library and designer for Arduino and mbed with IoT capabilities
[![Java Test](https://github.com/TcMenu/tcMenu/actions/workflows/test.yml/badge.svg)](https://github.com/TcMenu/tcMenu/actions/workflows/test.yml)
[![Linux nightly](https://github.com/TcMenu/tcMenu/actions/workflows/build_linux.yml/badge.svg)](https://github.com/TcMenu/tcMenu/actions/workflows/build_linux.yml)
[![macOS nightly](https://github.com/TcMenu/tcMenu/actions/workflows/build_mac.yml/badge.svg)](https://github.com/TcMenu/tcMenu/actions/workflows/build_mac.yml)
[![Windows nightly](https://github.com/TcMenu/tcMenu/actions/workflows/build_windows.yml/badge.svg)](https://github.com/TcMenu/tcMenu/actions/workflows/build_windows.yml)
[![License: Apache 2.0](https://img.shields.io/badge/license-Apache--2.0-green.svg)](https://github.com/TcMenu/tcMenu/blob/main/LICENSE)
[![GitHub release](https://img.shields.io/github/release/TcMenu/tcMenu.svg?maxAge=3600)](https://github.com/TcMenu/tcMenu/releases)
[![davetcc](https://img.shields.io/badge/davetcc-dev-blue.svg)](https://github.com/davetcc)
[![JSC TechMinds](https://img.shields.io/badge/JSC-TechMinds-green.svg)](https://www.jsctm.cz)

A menu library and designer UI for Arduino and mbed that is modular enough to support different input methods, display modules and IoT / remote control methods. TcMenu is more than just an Arduino menu library, think of it as a framework for building IoT applications that includes the ability to render menus locally onto a display.

Initially, you can use the menu designer UI that is packaged with every release, and available for Windows, macOS, and Linux. The designer UI takes care of building the core menu code and putting any callback functions into your sketch file. Think of the designer like a form designer in the desktop domain. Furthermore, It's non destructive on the sketch file, so can be round tripped during development. 

TcMenu organisation invest a lot of time and resources into making this open source product which is used by literally thousands of users. Releasing a UI, renting server space cost more than you'd think. Please consider at least making this project cost neutral to me by using either option to sponsor the project.

Sponsor me on [GitHub](https://github.com/TcMenu/tcMenu) (this repository). 

<a href="https://www.buymeacoffee.com/davetcc" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-blue.png" alt="Buy Me A Coffee" style="height: 60px !important;width: 217px !important;" ></a>

In any fork, please ensure all text up to here is left unaltered.

## Documentation

* [UI user guide, getting started and other documentation](https://tcmenu.github.io/documentation/arduino-libraries/tc-menu/)
* [TcMenu API Examples and Project Starters repository](https://github.com/TcMenu/tcmenu-examples-starters)
* [Full API embedded documentation](https://tcmenu.github.io/documentation/ref-docs/tcmenu/html/index.html)
* [YouTube getting started Part 1](https://youtu.be/ucFqpzMss-4) 

## Questions, community forum and support

Community questions can be asked in the discussions section of this repo, or using the Arduino forum. We generally answer most community questions but the responses will not be timely. Before posting into the community make sure you've recreated the problem in a simple sketch, and please consider making at least a one time donation (see links further up):

* Discussions section of this git repo (available from top menu of github page).
* [Arduino discussion forum](https://forum.arduino.cc/) where questions can be asked, please tag me using `@davetcc`.
* [Legacy discussion forum probably to be made read only soon](https://www.thecoderscorner.com/jforum/).


## Packaged installation for Windows, Linux, and MacOS.

Releases are directly available from the releases page, there is a signed Windows version, notarized macOS version, and a package for Linux:

[Get the latest TcMenu Designer release](https://github.com/TcMenu/tcMenu/releases)

Although most will use the above packages, it's also possible to build from source, full instructions are in the tcMenuGenerator folder. We ask that you only build from source for your own use.

Here's a couple of screen-shots of the designer UI - runs on Windows, macOS and Linux:

![Menu designer for Arduino](zMedia/design-menu-for-arduino.jpg)

## Generating a menu from the UI for the impatient

If you don't want to read the above documentation this gives a very quick start. Open the tcMenu Designer UI and set up your Arduino directory in "Edit -> General Settings", then check the "Library Versions" tab to ensure the embedded libraries are installed / up to date.

Once the tcMenu library directory is located, the "File -> Examples" menu will load with all the examples. Load the example closest to the hardware you have. Once it's open, you'll see the menu tree structure on the left, and the details for each menu when selected on the right. Below the menu tree are buttons that manage items in the menu tree. 

Once you've arranged your menu using the UI how you'd like it, choose `Code -> ID & Eeprom analyser` from the menu to check that you've not got any overlapping ranges, then choose `Code -> Generate` from the menu, choose appropriate hardware arrangements and hit generate.

The Generator is capable of round trip development too - most of the code is offloaded into associated CPP and Header files.

## TcMenu still supports Uno with LiquidCrystal dfRobot shield or Ssd1306Ascii

We try to keep Uno viable for tcMenu. However, there are limitations to what we can do. You can run a full menu on an Uno, but it's unlikely that the remote Ethernet support will fit. For anything that includes remote control support, we recommend at least 64K of flash memory. We store the menu items in static RAM where it's supported by the hardware, to further reduce memory on the board.

## Libraries required for tcMenu

TcMenu supports different boards and build tools. It is possible to use it directly with both platformIO and Arduino IDE. It should also work with most mbed tooling as the libraries compile on mbed.

**The easiest way to get started is to install tcMenu library using library manager**, all the dependencies are automatically installed. However, if you like to manually manage libraries, below are the links to their repos:

### Embedded Libraries for Arduino and mbed 

* tcMenuLib - https://github.com/TcMenu/tcMenuLib
* IoAbstraction - https://github.com/TcMenu/IoAbstraction
* TaskManagerIO - https://github.com/TcMenu/TaskManagerIO
* SimpleCollections - https://github.com/TcMenu/SimpleCollections
* tcUnicodeHelper - https://github.com/TcMenu/tcUnicodeHelper

### Additional libraries you may need depending on display

* LiquidCrystalIO forked from Arduino version - https://github.com/TcMenu/LiquidCrystalIO 
* Adafruit-GFX-mbed-fork - https://github.com/TcMenu/Adafruit-GFX-mbed-fork
* TFT-eSPI by Bodmer - https://github.com/Bodmer/TFT_eSPI
* Adafruit_GFX by AdaFruit- https://github.com/adafruit/Adafruit-GFX-Library 
* U8G2 by olikraus - https://github.com/olikraus/u8g2

### Main Java source code locations

The designer UI code base and plugins for 2.0 onwards are located [in this repository](https://github.com/TcMenu/tcMenu/tree/main/xmlPlugins). The designer, library and shipped plugins are all Apache licensed.

## Input and display technologies

Here are a few examples of how the menu can look with version 2.0 of our menu library on Arduino, ESP, and mbed:

![Arduino menu running on ESP32 with ILI9341 and touch screen](zMedia/theme-cool-blue-modern.jpg)

![Arduino menu in dark mode running on ESP32 with ILI9341 and touch screen](zMedia/theme-dark-mode-modern.jpg)

![Arduino menu on OLED running on ESP8266 with SH1106 and touch screen](zMedia/theme-oled-bordered.jpg)

![Arduino menu on LCD running on AVR](zMedia/menu-on-lcd-avr.jpg)

### Support for rotary encoders, digital/analog joysticks and touch buttons

We fully support rotary encoder based input with no need for any additional components in many cases. You can even connect your rotary encoder on a PCF8574, AW9523 or MCP23017. Further, we even support more than one encoder.

You can configure 3 or more buttons to work like a digital joystick using button based rotary encoder emulation (Up, Down and OK buttons with optional left and right) on either board pins, i2c expander, shift register. DfRobot analog input style buttons. Either DfRobot, or other analog ladder (configurable in code).

We also support the ESP32 touch pad interface, allowing up to 9 touch buttons to be used for menu input, they currently configure as per digital joystick.

### Support for matrix keyboards

Matrix Keyboards of configurable size and key combination. Pre-canned options for 4x3 and 4x4 layouts. Most of the core functions work with a matrix keyboard.

### Support for touch screens

From 2.0 onwards we'll support touch screen interfaces. We have built the support so that we can add many devices later, but to start with we will support resistive touch screens using 4 inputs, and the STM32 BSP provided touch screen interface.

### Drawing to LiquidCrystal (i2c or direct)

We have a fork LiquidCrystal for 20x4 or 16x2 displays - can be either directly connected, over an i2c sheild (PCF8574, MCP23017, AW9523)  or on a shift register. Our version of the library integrates better with task manager, yielding frequently.

### Adafruit_GFX integration for many displays

Most libraries that are compatible with Adafruit_GFX will work with tcMenu, we've tested with the following TFT's ILI9341, ST7735 and also Nokia 5110 display. We even have a quick start option that helps you get started with this option.

For mbed RTOS 5/6 we have a custom Adafruit_GFX OLED driver https://github.com/TcMenu/Adafruit-GFX-mbed-fork that supports SSD1306, SH1106. 

### U8G2 integration for mono display

We can render onto most buffered displays using this library. Tested with OLED devices such as SSD1306 and SH1106. We can even provide a custom I2C byte function that yields to task manager frequently, making it work better with task manager, and correctly yield on ESP boards too.

### TFT_eSPI and STM32 LTDC framebuffer integration

From 2.0 onwards we'll support TFT_eSPI and STM32 LTDC framebuffer based BSP functions to provide very high performance display rendering, we've tested with these two options on both ESP32 and STM32F429, the results were highly impressive.

### No local input or display techonologies

Should your app not need any local display or input technologies, you can set up tcMenu so that it does not have local input or display, or you could have a single switch or LED on the device and manage it manually. In this case you'd use the below IoT support to manage the device remotely.

## Remote IoT support on Ethernet, WiFi, Serial and Bluetooth/BLE 

This menu library provides complete IoT remote control, presently over serial and ethernet. We've tested the serial support with both USB serial and Bluetooth, both work acceptably well. The full menu structure is sent over the wire and the Java API provides it as a tree that can be manipulated. There is also a defined protocol for other languages. In addition to this the menu can be programatically manipulated very easily on the device.

* RS232 endpoint that supports full control of the menu items using a Java API - example app included.
* Ethernet endpoint that supports either Ethernet2 library or UipEthernet.
* Ethernet endpoint for mbed that supports the mbed socket implementation.
* ESP8266 and ESP32 based WiFi both supported.

## Ready built APIs and remote control UIs for tcMenu - embedCONTROL

TcMenu was built from day one for remote control and there are a few different offerings, we'll go through each one in turn, along with their intended use case:

### Java embedCONTROL UI

This desktop UI can be easily built from the source here using the instructions provided, it is also packaged for desktop those who want to use it as it exists out of the box. This offering builds on top of JavaFX, it has an automated UI that builds automatically for any menu app with form support for more advanced cases.

### Java / JVM API

There is a java API for accessing the menu remotely, source includes JavaDoc to help getting started. There is an example JavaFX UI built with it within the above Repo. Include the following into your maven build file:

        <dependency>
            <groupId>com.thecoderscorner.tcmenu</groupId>
            <artifactId>tcMenuJavaAPI</artifactId>
        </dependency>

### C# API

The C# API is relatively complete and can do most of what the Java API can do, it is well tested and stable.

Repo: https://github.com/TcMenu/tcmenu-dotnet-sdk

### Python API

There is now a Python API thanks to @vzahradnik - https://github.com/TcMenu/tcmenu-python-sdk

### JavaScript and TypeScript

There is also an early version of a TypeScript API that can be used from any JavaScript environment. https://github.com/TcMenu/embedcontrolJS. In the future, we may better package this API to a package manager.

There is also a Webserver/HTML based app that you can deploy, but it only really works from a Raspberry PI with the embedded Java support. It is somewhat limited in what it offers. After careful evaluation we decided NOT to support ESP32 and STM32 for this app at the moment as it is too large for them.

## Working with menus using the CLI

The most recent builds of TcMenu Designer include a CLI that has support for creating projects, adding and removing items, verifying and generating menus. [Building and Generating menus from the CLI](https://tcmenu.github.io/documentation/arduino-libraries/tc-menu/tcmenu-cli-workflow/)

## Loading and saving menu items

tcMenu can also save menu item state to EEPROM storage. On AVR that will generally be internal EEPROM, on 32 bit boards generally an AT24 i2c EEPROM. 

## Developer setup

[See the developer guide](zMedia/tcmenu-java-local-dev.md)
