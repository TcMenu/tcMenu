package com.thecoderscorner.bmped.controller;

import com.thecoderscorner.bmped.gfxui.CreateBitmapWidgetController;
import com.thecoderscorner.bmped.gfxui.CreateFontUtilityController;
import com.thecoderscorner.bmped.util.LocalBmpEditorUI;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.SafeNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainWindowController {
    String TWITTER_X_FOLLOW_URL = "https://twitter.com/thecoderscorner";
    String FACEBOOK_PAGE_URL = "https://www.facebook.com/thecoderscorner";

    @FXML
    private TabPane mainTabPane;
    @FXML
    private Tab homeTab;
    @FXML
    private Tab fontEditorTab;
    @FXML
    private Tab bitmapEditorTab;

    @FXML
    public void initialize(Stage stage) {
        try {
            buildPages(stage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void buildPages(Stage stage) throws IOException {
        var editorUI = new LocalBmpEditorUI(stage);
        var loader = new FXMLLoader(getClass().getResource("/fxui/aboutDialog.fxml"));
        Parent aboutPane = loader.load();
        AboutController controller = loader.getController();
        controller.initialise();
        homeTab.setContent(aboutPane);

        var bmpLoader = new FXMLLoader(getClass().getResource("/fxui/ImageToNativeBitmapConverter.fxml"));
        GridPane bitmapPane = bmpLoader.load();
        CreateBitmapWidgetController bmpController = bmpLoader.getController();
        bmpController.initialise(editorUI);
        bitmapEditorTab.setContent(bitmapPane);

        var fontLoader = new FXMLLoader(getClass().getResource("/fxui/createFontPanel.fxml"));
        GridPane fontPane = fontLoader.load();
        CreateFontUtilityController fontController = fontLoader.getController();
        fontController.initialise(editorUI);
        fontEditorTab.setContent(fontPane);
    }

    public void onFollowTwitter(ActionEvent actionEvent) {
        SafeNavigator.safeNavigateTo(TWITTER_X_FOLLOW_URL);
    }

    public void onFollowFacebook(ActionEvent actionEvent) {
        SafeNavigator.safeNavigateTo(FACEBOOK_PAGE_URL);
    }

    public void onTheCodersCorner(ActionEvent actionEvent) {
        SafeNavigator.safeNavigateTo("https://www.thecoderscorner.com");
    }
}
