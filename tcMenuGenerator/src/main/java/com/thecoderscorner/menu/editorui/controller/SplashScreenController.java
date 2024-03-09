package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.editorui.dialog.BaseDialogSupport;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.ResourceBundle;
import java.util.function.Consumer;

import static com.thecoderscorner.menu.editorui.dialog.AppInformationPanel.*;
import static com.thecoderscorner.menu.editorui.uimodel.UrlsForDocumentation.*;

public class SplashScreenController {
    private CurrentProjectEditorUI editorUI;
    private Consumer<String> themeListener;
    public Button closeButton;
    public Label tcMenuVersionField;

    public void initialise(CurrentProjectEditorUI editorUI, Consumer<String> themeListener, ConfigurationStorage storage, ResourceBundle bundle) {
        this.editorUI = editorUI;
        this.themeListener = themeListener;
        this.tcMenuVersionField.setText(bundle.getString("splash.dialog.you.installed.version") + storage.getVersion());
    }

    public void onGettingStartedVideo(ActionEvent ignored) {
        editorUI.browseToURL(GETTING_STARTED_PAGE_URL);
    }

    public void onTcMenuDocs(ActionEvent ignored) {
        editorUI.browseToURL(LIBRARY_DOCS_URL);
    }

    public void onGetStarted(ActionEvent ignored) {
        Stage s = (Stage) closeButton.getScene().getWindow();
        s.close();
    }

    public void onSponsorOptions(ActionEvent ignored) {
        editorUI.browseToURL(SPONSOR_TCMENU_PAGE);
    }

    public void onLightMode(ActionEvent ignored) {
        themeChange("lightMode");
    }

    public void onDarkMode(ActionEvent ignored) {
        themeChange("darkMode");
    }

    private void themeChange(String darkMode) {
        BaseDialogSupport.setTheme(darkMode);
        BaseDialogSupport.getJMetro().setScene(closeButton.getScene());
        themeListener.accept(darkMode);
    }

    public void onSupportOptions(ActionEvent ignored) {
        editorUI.browseToURL(TCMENU_COMMERCIAL_SUPPORT_URL);
    }

    public void onBuyMeACoffee(ActionEvent ignored) {
        editorUI.browseToURL(BUY_ME_A_COFFEE_URL);
    }

    public void onFollowFacebook(ActionEvent ignored) {
        editorUI.browseToURL(FACEBOOK_PAGE_URL);
    }

    public void onFollowTwitter(ActionEvent ignored) {
        editorUI.browseToURL(TWITTER_X_FOLLOW_URL);
    }
}
