# tcMenu beta - An embedded menu system for Arduino

TcMenu is a full feature menu system for Arduino, that is modular enough to support different input types and displays;
it is currently in BETA testing and not quite ready for full production.

As it stands tcMenu supports both button based and rotary encoder input, with output to either 20x4 or 16x2 displays.
MenuItem's are mainly stored in program memory with a small amount of state in RAM; put it another way, a reasonably
complete menu can fit in less than 700 bytes including i2c and Serial libraries.

## Getting started with tcMenu

You start a menu project either by using the provided user interface (recommended), or by coding a menu manually into a sketch. 

[Get a copy of the project from the releases page](https://github.com/davetcc/tcMenu/releases), where there's a JAR build that will require a modern version of Java installed on your machine. I'll build native packages before the next major release, but during the testing, please download Java from Oracle or use an open JDK, if you use OpenJDK you'll also need the open-javafx package.

## Generating a menu from the UI

When working with menus in the UI, I normally save the menu file into a new sketch directory. The reason for this is
that the generator will generate the sketch for you once you've defined the menus.

NOTE: This is not a round trip solution, it's currently a one shot design a menu and then generate code. It will 
backup and then overwrite your sketch.

Once you've arranged your menu using the UI how you'd like it, choose Code -> ID & Eeprom analyser from the menu
to check that you've not got any overlapping ranges, then choose Code -> Generate from the menu, choose appropriate
hardware arrangements and hit generate.

## The Java API

There is a java API for accessing the menu remotely, include the following into your maven build file:

        <dependency>
            <groupId>com.thecoderscorner.tcmenu</groupId>
            <artifactId>tcMenuJavaAPI</artifactId>
            <version>0.3</version>
        </dependency>

## More documentation

More complete documentation is available on the coders corner website:
[https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/] 
