# How to build tcMenu Designer into a native package using OpenJDK 16 onwards.

**For most users, we recommend using our pre-packaged software.** However, should you wish to build it yourself, follow these instructions, you'll end up with a native installer for Windows, a disk image for macOS, or a debian install archive for Linux.

Firstly, ensure that you have OpenJDK 16 and a recent maven 3 on your system, without these it will not be possible to build. We recommend that you install AdoptOpenJDK 16 and the latest maven 3 from the Apache website. 

* For OpenJDK we recommend [https://adoptopenjdk.net/]
* For apache maven always use [https://maven.apache.org/]

Using git or zip download, get the contents of the tcMenu repository locally, for example: 

    git clone https://github.com/davetcc/tcMenu.git
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

    java --module-path ../deps --enable-preview -Dprism.lcdtext=false --add-modules com.thecoderscorner.tcmenu.menuEditorUI com.thecoderscorner.menu.editorui.cli.TcMenuDesignerCmd

## Packaging - Build the package - Windows all versions

Ensure you are in the tcMenuGenerator/target directory.

    cp classes/img/tcMenuDesigner.ico .

    jpackage --type app-image -n tcMenuDesigner -p jfx/deps --input jfx/app --resource-dir .\classes\img\ --icon tcMenuDesigner.ico --app-version 2.1.0-beta1 --verbose --java-options "--enable-preview -Dprism.lcdtext=false" -m com.thecoderscorner.tcmenu.menuEditorUI/com.thecoderscorner.menu.editorui.cli.TcMenuDesignerCmd

## Packaging - build for Debian / Ubuntu using package command

Ensure you are in the tcMenuGenerator/target directory.

    jpackage -n tcMenuDesigner -p jfx/deps --input jfx/app --icon ./classes/img/menu-icon.png --verbose --license-file ../../LICENSE --linux-app-category Development --linux-menu-group "Development;Utility;" --java-options "--enable-preview -Dprism.lcdtext=false" --app-version 2.1.0-beta1 -m com.thecoderscorner.tcmenu.menuEditorUI/com.thecoderscorner.menu.editorui.cli.TcMenuDesignerCmd

## Packaging - macOS build all versions

Ensure you are in the tcMenuGenerator/target directory.

    jpackage -n tcMenuDesigner -p jfx/deps --input jfx/app --icon ./classes/img/AppIcon.icns --verbose --license-file ../../LICENSE --vendor TheCodersCorner --app-version 2.1.0 --java-options "--enable-preview -Dprism.lcdtext=false" --verbose -m com.thecoderscorner.tcmenu.menuEditorUI/com.thecoderscorner.menu.editorui.cli.TcMenuDesignerCmd

To allow for CLI on macOS: alias tcmenu=/Applications/tcMenuDesigner.app/Contents/MacOS/tcMenuDesigner

## Notes about the packaged plugins

We keep a set of packaged plugins with the designer, they are assembled from the tcMenu/xmlPlugins directory into a zip file that's even in the repository, to make builds easier. The packaged plugins file is  tcMenuGenerator/src/main/resources/packaged-plugins/initialPlugins.zip and can be built by zipping up the .tcmenu/plugins directory once they are in a satisfactory state. Each plugin is at the top level and will be expanded into ~/.tcmenu/plugins directory by the app.

## Extra steps that we follow for production releases

We follow a couple of extra steps for production releases, this is to notarize the macOS app, and code sign the Windows app. We can not show those here, but to build a release for yourself, you shouldn't need them.