# %APPNAME% menu application

This application was installed by the tcMenu Designer embedded Java support.

The application is loaded using an "application context" which you can consider like a very lightweight dependency injection container. In summary, you wire together components in the `MenuConfig` class and these components become the "application context" that you can take objects from wherever needed using its `getBean` method.

## How the app is organised.

The application is split up into several files:

* MenuConfig - this is the application context, it is used to configure all the parts of your application.
* %APPNAME%App - this is the class that starts up your application and initialises any plugins you selected. You should not touch this class as it is overwritten every time around.
* %APPNAME%Menu - this is the class that holds all the menu definitions and the menu tree. It is available in the spring context, and you can inject it into any of your components that need it. It also has short-cut methods to get every menu item.
* %APPNAME%Controller - this is where any callbacks that you register go, at the moment we support only one controller, in future we may provide support for more than one. Each function callback that you declare in TcMenu Designer will turn into a method in here. This also allows you to listen for any menu item, and for start and stop events. Further, you can change the controller's constructor to include other components if needed.

## Building the app

By default, the app uses maven to build, you'll need a couple of things installed to continue:

* Java - we recommend OpenJDK 21 [Liberica Open JDK](https://bell-sw.com/pages/downloads/) as it has a build for Raspberry PI with JavaFX built in) 
* A recent maven 3 installation. Maven is a very complete build tool and [you can read more about it here](https://maven.apache.org/guides/getting-started/).
* A Java IDE - we mainly use IntelliJ, but have tried the project in Visual-Studio-Code too. Eclipse similarly should work very well with this project.
* To build from the command line ensure you are in the same directory as this README file and type `mvn clean install`, which will build the application and bring down any dependencies.

## Running the application from the CLI 
 
If you use the standard maven setup, after running the above build steps, you should see the following directory has been created: `target/jfx/` containing an `app` directory and a `deps` directory. We recommend running the application from the `target/jfx/app` directory.

If you used a modular build (IE you have a `module-info.java` file in the `src/main/java` directory) then to run the application ensure that the right version of Java using `java -version` is on your path and then the run command should be `java --module-path ../deps "-Dprism.lcdtext=false" --add-modules %PACKAGE_NAME%.%MODULE_NAME% %PACKAGE_NAME%.tcmenu.%APPNAME%App`

## Plugins

Depending on which plugins you chose to install, there will be other files that are associated with those in the source tree, these are separated out into a `plugins` directory. We've provided a few documentation links below to get you started:

