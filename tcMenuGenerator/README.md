# Designer UI repository - all platforms.

This the repository for the designer UI, it is a cross-platform UI built using JavaFX that runs on OpenJDK. We test and then package this for Windows, macOS and Linux with every release, so for most people there is no need to build it from source.

The designer itself needs plugins that tell it how to generate code for all the supported displays, input, and remote technologies. There are three plugins in the core set, and for 2.0 onwards they are packaged in the xmlPlugins folder of the tcMenu repository:

* [core-display](https://github.com/davetcc/tcMenu/tree/master/xmlPlugins/core-display)
* [core-themes](https://github.com/davetcc/tcMenu/tree/master/xmlPlugins/core-themes)
* [core-remote](https://github.com/davetcc/tcMenu/tree/master/xmlPlugins/core-remote)

For 1.7, you can find them here [https://github.com/davetcc/tcMenuXmlPlugins]

These plugins are versioned and served up from thecoderscorner.com, but essentially, you could take a copy of them from the repository and put it into `~/.tcmenu/plugins` if you really wanted to. Further, if you prefer to build from source [using the packaging instructions](packager-all-platforms.md)

