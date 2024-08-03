# TcMenuNative wrapper library

This directory contains a small wrapper around freetype that has been tested on Windows, MacOS and also Ubuntu. It should be fairly easy to get this to compile on any platform that the underlying library supports.

Once you've built a release build, it is your responsibility to test it, and that includes running TcMenu to ensure the font creator is working properly. Try importing fonts, with different unicode ranges etc. Ensure they look bit perfect.

Once it is tested, replace the output DLL/DynLib/so file in the packaged directory.

## Windows

Use the Visual Studio toolchain as the dependencies it creates are on nearly all Windows boxes.

First you need a statically linked version of the freetype library, this is generally achieved by building the free type source with the static linking options enabled.

## MacOS

Ensure XCode is installed. Beyond this the library should build in release mode with no further options

## Linux Ubuntu

Ensure you have the following package installed.

    sudo apt-get install libfreetype6-dev

Beyond this a release build should work.