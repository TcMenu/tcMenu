package com.thecoderscorner.embedcontrol.jfxapp.panel;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public interface PanelPresentable {
    void presentPanelIntoArea(BorderPane pane) throws Exception;
    String getPanelName();
    boolean canBeRemoved();
    boolean closePanelIfPossible();
}
