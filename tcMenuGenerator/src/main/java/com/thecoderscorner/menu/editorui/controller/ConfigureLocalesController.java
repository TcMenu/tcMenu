package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.editorui.MenuEditorApp;
import com.thecoderscorner.menu.editorui.uimodel.UrlsForDocumentation;
import com.thecoderscorner.menu.editorui.util.SafeNavigator;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import com.thecoderscorner.menu.persist.PropertiesLocaleEnabledHandler;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.*;

public class ConfigureLocalesController {
    public enum LocaleStatus { AVAILABLE, TO_CREATE }

    private PropertiesLocaleEnabledHandler localeHandler;
    public ListView<NamedLocale> availableLocaleList;
    public ListView<NamedLocaleWithStatus> activeLocalList;
    public Button moveToActiveButton;
    public Button removeFromActiveButton;
    public TextField fileResourceLocationField;
    public ComboBox<String> countryCodeCombo;
    private final ResourceBundle bundle = MenuEditorApp.getBundle();

    public void initialise(PropertiesLocaleEnabledHandler localeHandler) {
        this.localeHandler = localeHandler;
        List<NamedLocale> namedLocales = Arrays.stream(Locale.getISOLanguages())
                .map(Locale::of).map(NamedLocale::new)
                .sorted(Comparator.comparing(NamedLocale::toString)).toList();

        var countryList = new ArrayList<String>();
        countryList.add("--");
        for(var countryCode : Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2).stream().sorted().toList()) {
            countryList.add(countryCode);
        }
        countryCodeCombo.setItems(FXCollections.observableList(countryList));
        countryCodeCombo.getSelectionModel().select(0);

        this.availableLocaleList.setItems(FXCollections.observableList(namedLocales));
        this.availableLocaleList.getSelectionModel().selectFirst();

        var locales = localeHandler.getEnabledLocales().stream()
                .map(locale -> new NamedLocaleWithStatus(locale, LocaleStatus.AVAILABLE))
                .toList();
        this.activeLocalList.setItems(FXCollections.observableArrayList(locales));
        this.activeLocalList.getSelectionModel().selectFirst();
    }

    public void onMoveToActive(ActionEvent actionEvent) {
        var sel = availableLocaleList.getSelectionModel().getSelectedItem();
        if(sel == null) return;

        if(activeLocalList.getItems().stream().anyMatch(nl -> nl.locale().equals(sel))) return;
        var country = countryCodeCombo.getSelectionModel().getSelectedItem();

        // If there is no country, then it is a language level entry, otherwise it has language_country
        var locale = country.equals("--") ? sel.locale() : Locale.of(sel.locale().getLanguage(), country);
        activeLocalList.getItems().add(new NamedLocaleWithStatus(locale, LocaleStatus.TO_CREATE));
    }

    public void onRemoveFromActive(ActionEvent actionEvent) {
        var sel = activeLocalList.getSelectionModel().getSelectedItem();
        if(sel == null) return;
        if(sel.status() == LocaleStatus.TO_CREATE) {
            activeLocalList.getItems().remove(sel);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(bundle.getString("locale.dialog.remove.manual.header"));
            alert.setContentText(bundle.getString("locale.dialog.remove.manual.message"));
            alert.showAndWait();
        }
    }

    public void onOnlineHelp(ActionEvent actionEvent) {
        SafeNavigator.safeNavigateTo(UrlsForDocumentation.LOCALE_DOCUMENTATION_URL);
    }

    public void onCancelSettings(ActionEvent actionEvent) {
        ((Stage)fileResourceLocationField.getScene().getWindow()).close();
    }

    public void onApplySettings(ActionEvent actionEvent) {
        var items = activeLocalList.getItems().stream().filter(ns -> ns.status() == LocaleStatus.TO_CREATE).toList();

        for(var item : items) {
            var bundleLoader = localeHandler.getSafeLoader();
            bundleLoader.saveChangesKeepingFormatting(item.locale(), Map.of());
        }

        ((Stage)fileResourceLocationField.getScene().getWindow()).close();
    }

    record NamedLocale(Locale locale) {
        @Override
        public String toString() {
            return locale.getDisplayLanguage();
        }
    }

    record NamedLocaleWithStatus(Locale locale, LocaleStatus status) {
        @Override
        public String toString() {
            if(StringHelper.isStringEmptyOrNull(locale.getLanguage())) {
                return MenuEditorApp.getBundle().getString("locale.dialog.default.bundle");
            } else {
                return locale.getDisplayLanguage() + " " +  locale.getDisplayCountry() + ((status == LocaleStatus.TO_CREATE) ? " *" : "");
            }
        }
    }

}
