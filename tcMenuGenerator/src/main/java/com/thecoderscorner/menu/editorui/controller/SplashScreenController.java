package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.editorui.dialog.BaseDialogSupport;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.util.function.Consumer;

import static com.thecoderscorner.menu.editorui.dialog.AppInformationPanel.*;

public class SplashScreenController {
    private CurrentProjectEditorUI editorUI;
    private Consumer<String> themeListener;
    public Button closeButton;

    public void initialise(CurrentProjectEditorUI editorUI, Consumer<String> themeListener) {
        this.editorUI = editorUI;
        this.themeListener = themeListener;
    }

    public void onGettingStartedVideo(ActionEvent actionEvent) {
        editorUI.browseToURL(GETTING_STARTED_PAGE_URL);
    }

    public void onTcMenuDocs(ActionEvent actionEvent) {
        editorUI.browseToURL(LIBRARY_DOCS_URL);
    }

    public void onGetStarted(ActionEvent actionEvent) {
        Stage s = (Stage) closeButton.getScene().getWindow();
        s.close();
    }

    public void onSponsorOptions(ActionEvent actionEvent) {
        editorUI.browseToURL(SPONSOR_TCMENU_PAGE);
    }

    public void onLightMode(ActionEvent actionEvent) {
        themeChange("lightMode");
    }

    public void onDarkMode(ActionEvent actionEvent) {
        themeChange("darkMode");
    }

    private void themeChange(String darkMode) {
        BaseDialogSupport.setTheme(darkMode);
        BaseDialogSupport.getJMetro().setScene(closeButton.getScene());
        themeListener.accept(darkMode);
    }
}
