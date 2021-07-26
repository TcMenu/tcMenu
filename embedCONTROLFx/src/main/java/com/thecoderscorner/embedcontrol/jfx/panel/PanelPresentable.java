package com.thecoderscorner.embedcontrol.jfx.panel;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;

import java.io.IOException;

public interface PanelPresentable {
    void presentPanelIntoArea(ScrollPane pane) throws Exception;
    String getPanelName();
    boolean canBeRemoved();
    boolean closePanelIfPossible();
}
