# EmbeddedJavaDemo menu application

This application was installed by the tcMenu Designer embedded Java support. Bear in mind that this support is presently in BETA testing and should be used only for evaluation at the moment, it's just not ready for production use yet.

The application is loaded using a Spring application context, you can read more about spring configuration in the [Spring Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-basics), but in summary, you wire together components in the `MenuConfig` class and these components become the "application context".

## How the app is organised.

The application is split up into several files:

* MenuConfig - this is the spring application context, it is used to configure all the parts of your application.
* EmbeddedJavaDemoApp - this is the class that starts up your application and initialises any plugins you selected. You should not touch this class as it is overwritten every time around.
* EmbeddedJavaDemoMenu - this is the class that holds all the menu definitions and the menu tree. It is available in the spring context, and you can inject it into any of your components that need it. It also has short-cut methods to get every menu item.
* EmbeddedJavaDemoController - this is where any callbacks that you register go, at the moment we support only one controller, in future we may provide support for more than one. Each function callback that you declare in TcMenu Designer will turn into a method in here. This also allows you to listen for any menu item, and for start and stop events. Further, you can change the controller's constructor to include other components if needed.

## Building the app

By default, the app uses maven to build, you'll need a couple of things installed to continue:

* Java - At least OpenJDK 17 (we recommend you use [Liberica Open JDK](https://bell-sw.com/pages/downloads/) as it has a build for Raspberry PI with JavaFX built in) 
* A recent maven 3 installation. Maven is a very complete build tool and [you can read more about it here](https://maven.apache.org/guides/getting-started/).
* A Java IDE - we mainly use IntelliJ, but have tried the project in Visual-Studio-Code too. Eclipse similarly should work very well with this project.
* To build from the command line ensure you are in the same directory as this README file and type `mvn clean install`, which will build the application and bring down any dependencies.

## Running the application from the CLI 
 
If you use the standard maven setup, after running the above build steps, you should see the following directory has been created: `target/jfx/` containing an `app` directory and a `deps` directory.

If you used a modular build (IE you have a `module-info.java` file in the `src/main/java` directory) then to run the application ensure that the right version of Java using `java -version` is on your path and then the run command should be `java --module-path ../deps "-Dprism.lcdtext=false" --add-modules com.thecoderscorner.menuexample.embeddedjavademo com.thecoderscorner.menuexample.tcmenu.EmbeddedJavaDemoApp`

## Plugins

Depending on which plugins you chose to install, there will be other files that are associated with those in the source tree, these are separated out into a `plugins` directory. We've provided a few documentation links below to get you started:

