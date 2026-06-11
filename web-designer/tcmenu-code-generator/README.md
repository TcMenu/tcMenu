# Designer UI repository - all platforms.

This the repository for the designer UI, it is a cross-platform UI built using JavaFX that runs on OpenJDK. We test and then package this for Windows, macOS and Linux with every release, so for most people there is no need to build it from source.

The designer itself needs plugins that tell it how to generate code for all the supported displays, input, and remote technologies. There are three plugins in the core set, and for 2.0 onwards they are packaged in the xmlPlugins folder of the tcMenu repository:

* [core-display](https://github.com/TcMenu/tcMenu/tree/main/xmlPlugins/core-display)
* [core-themes](https://github.com/TcMenu/tcMenu/tree/main/xmlPlugins/core-themes)
* [core-remote](https://github.com/TcMenu/tcMenu/tree/main/xmlPlugins/core-remote)

These plugins are always packaged with the designer and the designer will no longer update them as that causes more problems that it solves. If you prefer to build from source [using the packaging instructions](scripts/packager-all-platforms.md), in the case put the above plugins into "~/.tcmenu/plugins

