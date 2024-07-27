## Internationalisation of the menu designer

In Java i18n is achieved using resource bundles. These are properties files that are provided at the default (English), language and language_country level. The most relevant is used first (language_country) down to the least relevant (default) based on the locale selected.

There are several sets of files that control internationalisation, each of these is listed below:

* The menu designer resource bundles have text for the dialogs: https://github.com/TcMenu/tcMenu/tree/main/tcMenuGenerator/src/main/resources/i18n
* The input and display plugin resource bundles - https://github.com/TcMenu/tcMenu/tree/main/xmlPlugins/core-display/i18n
* The theme plugin resource bundles - https://github.com/TcMenu/tcMenu/tree/main/xmlPlugins/core-themes/i18n
* The remote plugin has not yet been attempted, once it is internationalized, it will be added here.

The easiest way to test is to set your locale to the one you are working with, you can also switch locales in the designer, but it needs that locale to have been added to the general settings first as things stand. To do this add a new LocaleWithDescription to the list. Below is what the structure looks like now:

    private final List<LocaleWithDescription> availableLocales = List.of(
            new LocaleWithDescription("Default language", Locale.getDefault(), true),
            new LocaleWithDescription("English", Locale.ENGLISH, false),
            new LocaleWithDescription("Français (French)", Locale.FRENCH, false)
    );

Once this is added, you can change locale in the general settings dialog, this value is stored in the global settings and will apply with each start up. You can revert to the default language by just choosing default.

There are instructions for [building the application locally without an IDE](packager-all-platforms.md) in this folder.