package com.thecoderscorner.embedcontrol.jfx.controlmgr.texted;

import javafx.scene.Node;

public interface FieldEditHandler {
    Node getEditorComponent();
    boolean isCurrentlyValid();
    String getValueAsString();
    void markInvalid();
}
