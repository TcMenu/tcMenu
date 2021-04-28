package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import static com.thecoderscorner.menu.editorui.dialog.AppInformationPanel.*;

public class SplashScreenController {
    private CurrentProjectEditorUI editorUI;
    public Button closeButton;

    public void initialise(CurrentProjectEditorUI editorUI) {
        this.editorUI = editorUI;
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
}
