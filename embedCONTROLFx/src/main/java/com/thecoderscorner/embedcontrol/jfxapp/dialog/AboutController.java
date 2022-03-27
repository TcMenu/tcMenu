package com.thecoderscorner.embedcontrol.jfxapp.dialog;

import com.thecoderscorner.embedcontrol.jfx.controlmgr.SafeNavigator;
import javafx.event.ActionEvent;

public class AboutController {
    public void onVisitDocs(ActionEvent actionEvent) {
        SafeNavigator.safeNavigateTo("https://www.thecoderscorner.com/products/apps/embed-control/");
    }

    public void onVisitForum(ActionEvent actionEvent) {
        SafeNavigator.safeNavigateTo("https://www.thecoderscorner.com/jforum/forums/show/11.page");
    }
}
