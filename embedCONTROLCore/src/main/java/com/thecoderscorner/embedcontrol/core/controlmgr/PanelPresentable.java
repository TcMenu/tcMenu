package com.thecoderscorner.embedcontrol.core.controlmgr;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public interface PanelPresentable<T> {
    T getPanelToPresent(double width) throws Exception;
    String getPanelName();
    boolean canBeRemoved();
    boolean canClose();
    void closePanel();
}
