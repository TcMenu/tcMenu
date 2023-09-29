package com.thecoderscorner.menu.editorui.embed;

import com.thecoderscorner.embedcontrol.jfx.controlmgr.SafeNavigator;
import javafx.event.ActionEvent;

public class AboutController {

    public void initialise() {
    }

    public void onVisitDocs(ActionEvent actionEvent) {
        SafeNavigator.safeNavigateTo("https://www.thecoderscorner.com/products/apps/embed-control/");
    }

    public void onVisitForum(ActionEvent actionEvent) {
        SafeNavigator.safeNavigateTo("https://www.thecoderscorner.com/jforum/forums/show/11.page");
    }
}
