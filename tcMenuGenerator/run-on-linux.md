## How to run the java version of tcMenu Designer on Linux.

Thanks to @ptapping for the linux instructions and build script. *Please note that running on Linux is supported on a best efforts community basis, as we don't have a desktop Linux build to test with.*

### Things that are needed in order to run and work with TcMenuDesigner.

At the moment on Linux, in order to run designer on Linux, you'll need to build it first. Then you can run TcMenu designer, you'll need:

* An installation of Java OpenJDK **14** that can be readily installed from your package manager.
* An installation of maven, that again can be readily installed from your package manager.

We'll look into building a JAR file as part of 1.4.1 so that you won't need to install maven to build it, you'll just need OpenJDK 14 so that you can run it. 

### Using the build script

Run the `install_linux.sh` script in the `tcMenu/tcMenuGenerator` directory. Using the `--uninstall` option will remove the installed package. The script will tell you where the app has been installed. Take a look through the scripts before running to ensure you're happy with where it will place the files. 

### Raw instructions, example for building 1.4.1 and above.

    # Ensure java and maven build system installed
    # Arch Linux
    sudo pacman -Sy jdk14-openjdk maven
    # Java version can be selected with
    # sudo archlinux-java set java-14-openjdk

    # Ubuntu, Debian (untested, should work)
    sudo apt update && sudo apt install openjdk-11-jdk maven
    # Java version can be managed with
    # sudo update-alternatives --config java

    # Get source code
    wget https://github.com/davetcc/tcMenu/archive/1.4.1.tar.gz
    tar xvf 1.4.1.tar.gz
    cd tcMenu-1.4.1

    # Build app
    mvn -DskipTests clean install -f tcMenuGenerator/pom.xml

    # Run
    cd tcMenuGenerator/target/jfx/app
    java --module-path ../deps --add-modules com.thecoderscorner.tcmenu.menuEditorUI com.thecoderscorner.menu.editorui.MenuEditorApp

### Let's say you wanted master instead of a release

    # Get source code for master
    # Could use --recursive to get submodules, but 1.4.0 doesn't allow installation of them anymore
    git clone https://github.com/davetcc/tcMenu.git
    cd tcMenu
    git checkout master

    # Build app
    mvn -DskipTests clean install -f tcMenuGenerator

    # Run
    cd tcMenuGenerator/target/jfx/app/
    java --module-path ../deps --add-modules com.thecoderscorner.tcmenu.menuEditorUI com.thecoderscorner.menu.editorui.MenuEditorApp

### If you don't want to use Library Manager or Plugin automatic updates

By default the plugin manager will keep the plugins up to date, using a cache of the latest released plugins at thecoderscorner.com. If you are not happy with this, you can either copy the two directories `core-display` and `core-remote` from https://github.com/davetcc/tcMenu/tree/master/CoreXmlPlugins into .tcmenu/plugins (or even create a new plugin directory and add the following VM argument -DadditionalPluginsDir=myPath). Even if you do this manually, the plugin manager will warn you when they are no longer current.

If you do not want to use your Arduino IDE library manager, the embedded folder contains links to the embedded libraries and is  generally quite up to date. You can copy those into your library folder. We really recommend using Library Manager.
