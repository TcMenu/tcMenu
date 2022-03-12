package com.thecoderscorner.embedcontrol.jfxapp.panel;

import com.thecoderscorner.embedcontrol.jfxapp.dialog.BaseDialogSupport;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class AboutPanelPresentable implements PanelPresentable {
    @Override
    public void presentPanelIntoArea(BorderPane pane) throws Exception {
        var loader = new FXMLLoader(BaseDialogSupport.class.getResource("/aboutPage.fxml"));
        Pane loadedPane = loader.load();
        pane.setCenter(loadedPane);
    }

    @Override
    public String getPanelName() {
        return "About";
    }

    @Override
    public boolean canBeRemoved() {
        return false;
    }

    @Override
    public boolean closePanelIfPossible() {
        return true;
    }
}
