# How to build the bitmap and font editor into a native package using OpenJDK 21 onwards.

**For most users, we recommend using our pre-packaged software.** However, should you wish to build it yourself, follow these instructions, you'll end up with a native executable for Windows, a disk image for macOS, or an  archive for Linux. Please only use these steps to build a UI for you own purposes.

Firstly, ensure that you have the most recent OpenJDK and a recent version of maven on your system, without these it will not be possible to build.

* All OpenJDK's that we've tested work for this, we've tried: Liberica, Adoptium, Amazon Corretto and Microsoft JDK.   
* For Apache maven we recommend using [https://maven.apache.org/]

Using git or zip download, get the contents of the tcMenu repository locally, for example: 

    git clone https://github.com/TcMenu/tcMenu.git
    git checkout <release-branch-name>

## Optional step - building a non-released version (advanced users)

It is far easier and safer to build a released version. But if you build a non-released version, you'll need to build the API too, here's how.

Drop to a command-line, in the `tcMenu/api/tcMenuJavaApi` directory.

    mvn clean install -Dgpg.skip=true

Drop to a command-line, in the `tcMenu/api/embedCONTROLCore` directory.

    mvn clean install -Dgpg.skip=true

## Building the app itself 

Drop to a command-line, in the `tcMenu/font-bmp-editor` directory and run a maven build, the tests will not run from the command line as they include a full UI test suite, so we need to skip them

    mvn clean install -DskipTests

## Packaging - Check the archive worked and versioning is right

Run and smoke test, ensure you are in the `font-bmp-editor/target/jfx/app` directory:

    java --module-path ../deps "-Dprism.lcdtext=false" "-Doverride.core.plugin.dir=." \
    --enable-native-access=javafx.graphics,com.thecoderscorner.bmped "-Djava.library.path=win/" \
    --add-modules com.thecoderscorner.bmped com.thecoderscorner.bmped.BitMapEditApp

## Packaging - Build the package - Windows all versions

Ensure you are in the `font-bmp-editor/target` directory.

    cp classes/img/tcMenuDesigner.ico .

    jpackage --type app-image -n BitMapEdit -p jfx/deps --input jfx/app  \ 
    --resource-dir .\classes\img\ --icon tcMenuDesigner.ico --app-version 4.5.9 --verbose \
    --java-options '-Dprism.lcdtext=false  -Djava.library.path=$APPDIR/win' \
    --add-modules "jdk.crypto.cryptoki" \
    -m com.thecoderscorner.bmped/com.thecoderscorner.bmped.BitMapEditApp


## Packaging - macOS build all versions (without notarization)

Ensure you are in the `font-bmp-editor/target` directory.

    jpackage  -n BitMapEdit -p jfx/deps --input jfx/app  --icon ./classes/img/AppIcon.icns \
    --app-version 4.5.9 --verbose \
    --java-options '-Djava.library.path=$APPDIR/mac -Dprism.lcdtext=false' \
    --add-modules "jdk.crypto.cryptoki" \
    -m com.thecoderscorner.bmped/com.thecoderscorner.bmped.BitMapEditApp

