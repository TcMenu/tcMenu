# How to build tcMenu Designer into a native package using OpenJDK 17 onwards.

**For most users, we recommend using our pre-packaged software.** However, should you wish to build it yourself, follow these instructions, you'll end up with a native executable for Windows, a disk image for macOS, or an  archive for Linux. Please only use these steps to build a UI for you own purposes.

Firstly, ensure that you have the most recent OpenJDK and a recent version of maven on your system, without these it will not be possible to build.

* All OpenJDK's that we've tested work for this, we've tried: Liberica, Adoptium, Amazon Corretto and Microsoft JDK.   
* For Apache maven we recommend using [https://maven.apache.org/]

Using git or zip download, get the contents of the tcMenu repository locally, for example: 

    git clone https://github.com/TcMenu/tcMenu.git
    git checkout <release-branch-name>

## Optional step - building a non-released version (advanced users)

It is far easier and safer to build a released version. But if you build a non-released version, you'll need to build the API too, here's how.

Drop to a command-line, in the tcMenu/tcMenuJavaApi directory, you will need to create a gpg key first as the menu API is signed with a GPG key. [You can create a key following these instructions](https://www.gnupg.org/gph/en/manual/c14.html). Once this is done, run a local maven build as follows:

    mvn clean install

## Building tcMenu 

Drop to a command-line, in the tcMenu/tcMenuGenerator directory and run a maven build, the tests will not run from the command line as they include a full UI test suite, so we need to skip them

    mvn clean install -DskipTests

## Packaging - Check the archive worked and versioning is right

Run and smoke test, ensure you are in the tcMenuGenerator/target/jfx/app directory:

    java --module-path ../deps "-Dprism.lcdtext=false" "-Doverride.core.plugin.dir=." --add-modules com.thecoderscorner.tcmenu.menuEditorUI com.thecoderscorner.menu.editorui.cli.TcMenuDesignerCmd

## Packaging - Build the package - Windows all versions

Ensure you are in the tcMenuGenerator/target directory. On Windows all these steps can be performed by running `buildTcMenuWin.cmd` in the target directory:

    cp classes/img/tcMenuDesigner.ico .

    jpackage --type app-image -n tcmenu -p jfx/deps --input jfx/app --win-console --resource-dir .\classes\img\ --icon tcMenuDesigner.ico --app-version 4.3.0 --verbose --java-options "-Dprism.lcdtext=false --enable-preview -Djava.library.path=$APPDIR/win" --add-modules "jdk.crypto.cryptoki" -m com.thecoderscorner.tcmenu.menuEditorUI/com.thecoderscorner.menu.editorui.cli.TcMenuDesignerCmd

    jpackage --type app-image -n tcMenuDesigner -p jfx/deps --input jfx/app  --resource-dir .\classes\img\ --icon tcMenuDesigner.ico --app-version 4.3.0 --verbose --java-options "-Dprism.lcdtext=false --enable-preview -Djava.library.path=$APPDIR/win" --add-modules "jdk.crypto.cryptoki" -m com.thecoderscorner.tcmenu.menuEditorUI/com.thecoderscorner.menu.editorui.cli.TcMenuDesignerCmd

Copy the following files from tcMenuDesigner into tcmenu

* app/tcMenuDesigner.cfg
* tcMenuDesigner.exe

## Packaging - build for Debian / Ubuntu using package command

Ensure you are in the tcMenuGenerator/target directory.

    jpackage -n tcMenuDesigner -p jfx/deps --input jfx/app --icon ./classes/img/menu-icon.png --verbose --license-file ../../LICENSE --linux-app-category Development --linux-menu-group "Development;Utility;" --java-options "-Dprism.lcdtext=false --enable-preview -Djava.library.path=$APPDIR/lin" --app-version 4.3.0  --add-modules "jdk.crypto.cryptoki" -m com.thecoderscorner.tcmenu.menuEditorUI/com.thecoderscorner.menu.editorui.cli.TcMenuDesignerCmd

## Packaging - macOS build all versions (without notarization)

Ensure you are in the tcMenuGenerator/target directory.

    jpackage -n tcMenuDesigner -p jfx/deps --input jfx/app --icon ./classes/img/AppIcon.icns --verbose --license-file ../../LICENSE --vendor TheCodersCorner --app-version 4.3.0  --add-modules "jdk.crypto.cryptoki" --java-options "-Dprism.lcdtext=false --enable-preview -Djava.library.path=$APPDIR/mac" --verbose -m com.thecoderscorner.tcmenu.menuEditorUI/com.thecoderscorner.menu.editorui.cli.TcMenuDesignerCmd

To allow for CLI on macOS: alias tcmenu=/Applications/tcMenuDesigner.app/Contents/MacOS/tcMenuDesigner

## Notes about the packaged plugins

We keep a set of packaged plugins with the designer, they are assembled from the tcMenu/xmlPlugins directory into a zip file that's even in the repository, to make builds easier. The packaged plugins are now built automatically as part of the maven build (see the assembly plugin). Each plugin is at the top level and will be expanded into ~/.tcmenu/plugins directory by the app.

## Extra steps that we follow for production releases

We follow a couple of extra steps for production releases, this is to notarize the macOS app, and code sign the Windows app. We can not show those here, but to build a release for yourself, you shouldn't need them.

## embedCONTROL builds

This requires that you have built the Java API above.

### Building the embedCONTROL core project

Drop to a command-line, in the tcMenu/embedCONTROLCore directory, run a local maven build as follows:

    mvn clean install

Testing before deployment

    java --module-path ../deps "-Dprism.lcdtext=false" -m com.thecoderscorner.tcmenu.embedcontrolfx/com.thecoderscorner.embedcontrol.jfxapp.EmbedControlApp
