package com.thecoderscorner.embedcontrol.jfxapp.dialog;

import com.thecoderscorner.embedcontrol.jfx.controlmgr.SafeNavigator;
import com.thecoderscorner.embedcontrol.jfxapp.VersionHelper;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;

public class AboutController {
    public Label versionLabel;

    public void initialise(VersionHelper versionHelper) {
        versionLabel.setText("Version " + versionHelper.getVersion() + " built on " + versionHelper.getBuildTimestamp());
    }

    public void onVisitDocs(ActionEvent actionEvent) {
        SafeNavigator.safeNavigateTo("https://www.thecoderscorner.com/products/apps/embed-control/");
    }

    public void onVisitForum(ActionEvent actionEvent) {
        SafeNavigator.safeNavigateTo("https://www.thecoderscorner.com/jforum/forums/show/11.page");
    }
}
