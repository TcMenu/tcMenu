## Developing TcMenu locally in an IDE

In addition to the [packager-local-platforms.md] documentation, which is more aimed at building the UI locally this guide covers setting up the project for development in an IDE. Both guides are very useful and overlap to some extent.

## Required Components

* Java 20 OpenJDK runtime, you can use any that you wish. IMPORTANT you must use at least V20.
* OpenJFX, normally included from maven, if you don't use Liberica Full JDK, then you will need to adjust the designer pom.xml to bring in OpenJFX. See comment in pom.xml.
* IntelliJ either community or ultimate should work. Use the latest one available.
* Local clone of the main repo from https://github.com/davetcc/tcMenu

## Instructions

Once you have the local clone of the main repo, in the simplest case, just import the various projects into IntelliJ one at a time, starting with the JavaAPI and then both embedCONTROL projects and lastly, tcMenuGenerator. Once these are all installed into the IDE you should be able to run the designer.

## Running the designer locally

You can add the following parameters:

    -Ddevlog=Y -DalwaysShowSplash=Y

* Enabling `devlog` writes logs to the console along with to the log file.
* Enabling `alwaysShowSplash` shows the new version splash every time.

Note that the starting directory is relatively unimportant for most operations.

## Running the tests locally

At the moment the designer tests include all the UI automation tests written in TestFX. These actually exercise the UI and take quite a lot of time to run. I usually only run these a couple of times per release cycle to check for issues, if you want to run these add the argument below, to avoid running these, don't run the tests in the `uitest` package.

During or after the 3.2 release these will be moved into integration tests.

Add the following "VM options" as this avoids JavaFX throwing an error:

    -ea --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED



