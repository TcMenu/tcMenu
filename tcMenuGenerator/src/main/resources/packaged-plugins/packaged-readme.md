# %APPNAME% menu application

This application was installed by the tcMenu Designer embedded Java support. Bear in mind that this support is presently in BETA testing and should be used only for evaluation at the moment, it's just not ready for production use yet.

The application is loaded using a Spring application context, you can read more about spring configuration in the [Spring Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-basics), but in summary, you wire together components in the `MenuConfig` class and these components become the "application context".

## How the app is organised.

The application is split up into several files:

* MenuConfig - this is the spring application context, it is used to configure all the parts of your application.
* %APPCLASSNAME%App - this is the class that starts up your application and initialises any plugins you selected. You should not touch this class as it is overwritten every time around.
* %APPCLASSNAME%Menu - this is the class that holds all the menu definitions and the menu tree. It is available in the spring context, and you can inject it into any of your components that need it. It also has short-cut methods to get every menu item.
* %APPCLASSNAME%Controller - this is where any callbacks that you register go, at the moment we support only one controller, in future we may provide support for more than one. Each function callback that you declare in TcMenu Designer will turn into a method in here. This also allows you to listen for any menu item, and for start and stop events. Further, you can change the controller's constructor to include other components if needed.

## Building the app

By default, the app uses maven to build, ensure you have both OpenJDK 17 and a recent version of Maven installed. Maven is a very complete build tool and [you can read more about it here](https://maven.apache.org/guides/getting-started/), but here's a very quick summary:

* If you are using IntelliJ as your editor, it has complete maven support, just "open" the maven pom and it will create a project from it. Eclipse similarly has full support.
* To build from the command line ensure you are in the project root directory and type `mvn clean install`, which will build the application and bring down any dependencies.
* Running the application from the CLI - if the application is OpenJFX based, then the image will be in `target/jfx/app`.

## Plugins

Depending on which plugins you chose to install, there will be other files that are associated with those. We've provided a few documentation links below to get you started:

