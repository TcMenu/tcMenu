## Running tcMenu Designer on Linux

**NOTE that you can now use the pre-built debian packages that are available on our website, there is no need to build from source or install a JDK.**

See [https://www.thecoderscorner.com/products/apps/tcmenu-designer/]

### Prerequisites

* An installation of Java OpenJDK **11** or later that can be readily installed from your package manager.

### Installation

* Copy the linux tar.gz package from the release area and expand somewhere in your home area.
* From terminal change into the package directory that you've just expanded.
* Run `startDesigner.sh` which should present the designer window after a few moments.

Hopefully, that should be it. Have fun! 

## These are the older instructions

Thanks to @ptapping for the linux instructions and build script. *Please note that running on Linux is supported on a best efforts community basis, as we don't have a desktop Linux build to test with.*

### Things that are needed in order to run and work with TcMenuDesigner.

At the moment on Linux, in order to run designer on Linux, you'll need to build it first. Then you can run TcMenu designer, you'll need:

* An installation of Java OpenJDK **11** or later that can be readily installed from your package manager.
* An installation of maven, that again can be readily installed from your package manager.

We'll look into building a package in the future as time permits.

### Using the build script

Run the `install_linux.sh` script in the `tcMenu/tcMenuGenerator` directory. Using the `--uninstall` option will remove the installed package. The script will tell you where the app has been installed. Take a look through the scripts before running to ensure you're happy with where it will place the files. 

### Raw instructions, example for building 1.4.2 and above.

    # Ensure java and maven build system installed
    # Arch Linux
    sudo pacman -Sy jdk11-openjdk maven # or sudo pacman -Sy jdk14-openjdk maven
    # Java version can be selected with
    # sudo archlinux-java set <java-11-openjdk or java-14-openjdk>

    # Ubuntu, Debian (untested, should work)
    sudo apt update && sudo apt install openjdk-11-jdk maven # or use 14 if you wish
    # Java version can be managed with
    # sudo update-alternatives --config java

    # Get source code
    wget https://github.com/davetcc/tcMenu/archive/1.4.2.tar.gz
    tar xvf 1.4.2.tar.gz
    cd tcMenu-1.4.2

    # Build app
    mvn -DskipTests clean install -f tcMenuGenerator/pom.xml

    # Run
    cd tcMenuGenerator/target/jfx/app
    java --module-path ../deps --add-modules com.thecoderscorner.tcmenu.menuEditorUI com.thecoderscorner.menu.editorui.MenuEditorApp

### If you don't want to use Library Manager or Plugin automatic updates

By default the plugin manager will keep the plugins up to date, using a cache of the latest released plugins at thecoderscorner.com. If you are not happy with this, you can either copy the two directories `core-display` and `core-remote` from https://github.com/davetcc/tcMenu/tree/master/CoreXmlPlugins into .tcmenu/plugins (or even create a new plugin directory and add the following VM argument -DadditionalPluginsDir=myPath). Even if you do this manually, the plugin manager will warn you when they are no longer current.

If you do not want to use your Arduino IDE library manager, the embedded folder contains links to the embedded libraries and is  generally quite up to date. You can copy those into your library folder. We really recommend using Library Manager.
