## Developing TcMenu locally in an IDE

This guide is aimed at developers wanting to build the UI locally within an IDE.

## Required Components

* Java 20 OpenJDK runtime, you can use any that you wish. IMPORTANT you must use at least V20.
* OpenJFX, normally included from maven, if you don't use Liberica Full JDK, then you will need to adjust the designer pom.xml to bring in OpenJFX. See comment in pom.xml.
* IntelliJ either community or ultimate should work. Use the latest one available.
* Local clone of the main repo from https://github.com/davetcc/tcMenu
* If you want to build the JavaAPI via maven you will need the GPG tool installed

## Instructions for IntelliJ

1. Take a local clone of the main repo
2. Open the  tcMenu directory into IntelliJ it will show as a project in the open dialog.
3. Ensure the V20 JDK is setup from Settings/Project Structure. With this the project should be good to go.
4. You can use the run configurations existing within the project.

_IMPORTANT for Liberica JDK, if you are using Liberica Full JDK, ensure that the Settings/Compiler option to enforce modules with --release option is OFF._

## Instructions for simpler changes (text editor and maven)

Simply load the project directory in an editor such as VSCode that allows you to see the directory structure. Then use the packager-local-platforms.md instructions in tcMenuGenerator/scripts to build and run afterwards.

## Running the designer locally

You can add the following VM parameters:

    -Ddevlog=Y -DalwaysShowSplash=Y

* Enabling `devlog` writes logs to the console along with to the log file.
* Enabling `alwaysShowSplash` shows the new version splash every time.

Note that the starting directory is relatively unimportant for most operations.

## Running the designer automation/integration tests locally

At the moment the designer tests include all the UI automation tests written in TestFX. These actually exercise the UI and take quite a lot of time to run. I usually only run these a couple of times per release cycle to check for issues, if you want to run these add the argument below, to avoid running these, don't run the tests in the `uitest` package.

During or after the 3.2 release these will be moved into integration tests.

Add the following "VM options" as this avoids JavaFX throwing an error:

    -ea --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED

## Building the API or EmbedCONTROL Core with Maven

If for any reason you want to build the API with maven (IE outside of IntelliJ), you'll need to set up a GPG key. You don't need to follow the steps for broadcasting the key as you're not going to deploy the release to maven central.

https://maven.apache.org/developers/release/pmc-gpg-keys.html

See the "Configure passphrase in settings.xml" in the following: https://maven.apache.org/plugins/maven-gpg-plugin/usage.html

Once these steps are done, you should be able to build the API and embedCONTROL Core.

