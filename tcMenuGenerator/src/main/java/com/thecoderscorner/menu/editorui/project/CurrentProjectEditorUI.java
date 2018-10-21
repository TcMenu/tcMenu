package com.thecoderscorner.menu.editorui.project;

import javafx.stage.Stage;

import java.util.Optional;

public interface CurrentProjectEditorUI {
    Optional<String> findFileNameFromUser(boolean open);
    void alertOnError(String heading, String description);
    boolean questionYesNo(String title, String header);
    void setTitle(String s);
}
