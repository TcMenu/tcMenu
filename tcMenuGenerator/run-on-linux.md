## How to run the java version of tcMenu Designer on Linux.

Thanks to @ptapping for the linux instructions. Please note that running on Linux is supported on a best efforts community basis, as we don't have a desktop Linux build to test with.

    # Ensure java and maven build system installed
    # Arch Linux
    sudo pacman -Sy jdk11-openjdk maven
    # Java version can be selected with
    # sudo archlinux-java set java-11-openjdk

    # Ubuntu, Debian (untested, should work)
    sudo apt update && sudo apt install openjdk-11-jdk maven
    # Java version can be managed with
    # sudo update-alternatives --config java

    # Get source code
    wget https://github.com/davetcc/tcMenu/archive/1.3.5.tar.gz
    tar xvf 1.3.5.tar.gz
    cd tcMenu-1.3.5

    # Build plugins
    mvn clean install -f baseInputDisplayPlugin/pom.xml
    mvn clean install -f dfRobotCodePlugin/pom.xml

    # Build app
    mvn -DskipTests clean install -f tcMenuGenerator/pom.xml

    # Run
    cd tcMenuGenerator/target/jfx/app/
    java --module-path ../deps --add-modules com.thecoderscorner.tcmenu.menuEditorUI com.thecoderscorner.menu.editorui.MenuEditorApp
    
Another example for 1.4.0

    # Ensure java and maven build system installed
    # Arch Linux
    sudo pacman -Sy jdk11-openjdk maven
    # Java version can be selected with
    # sudo archlinux-java set java-11-openjdk

    # Ubuntu, Debian (untested, should work)
    sudo apt update && sudo apt install openjdk-11-jdk maven
    # Java version can be managed with
    # sudo update-alternatives --config java

    # Get source code for 1.4.0
    # Could use --recursive to get submodules, but 1.4.0 doesn't allow installation of them anymore
    git clone https://github.com/davetcc/tcMenu.git
    cd tcMenu
    git checkout 9c502b0
    git switch -c 1.4.0

    # Build plugins
    mvn clean install -f baseInputDisplayPlugin
    mvn clean install -f dfRobotCodePlugin

    # Build app
    mvn -DskipTests clean install -f tcMenuGenerator

    # Run
    cd tcMenuGenerator/target/jfx/app/
    java --module-path ../deps --add-modules com.thecoderscorner.tcmenu.menuEditorUI com.thecoderscorner.menu.editorui.MenuEditorApp
