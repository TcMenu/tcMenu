/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.util;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.function.BiConsumer;

public class UiHelper {
    public static void createDialogStateAndShowSceneAdj(Stage parent, Pane root, String title, boolean modal,
                                                        BiConsumer<Scene, Stage> sceneAdjuster) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle(title);
        dialogStage.initOwner(parent);

        Scene scene = new Scene(root);
        sceneAdjuster.accept(scene, dialogStage);

        dialogStage.setScene(scene);
        if (modal) {
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.showAndWait();
        }
        else {
            dialogStage.show();
        }
    }


    public static void  createDialogStateAndShow(Stage parent, Pane root, String title, boolean modal) {
        createDialogStateAndShowSceneAdj(parent, root, title, modal, (scene, stage) -> {});
    }
}
