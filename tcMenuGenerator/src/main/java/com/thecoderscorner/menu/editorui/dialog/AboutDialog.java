package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.util.BuildVersionUtil;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/** Example of displaying a splash page for a standalone JavaFX application */
public class AboutDialog {

    private static final int SPLASH_WIDTH = 358;
    private static final int SPLASH_HEIGHT = 290;

    public void showSplash(Stage initStage) {
        ImageView splash = new ImageView(new Image("/img/splash.jpg"));
        Button button = new Button("Close");
        button.setPrefWidth(SPLASH_WIDTH);
        button.setStyle("-fx-font-size: 160%; -fx-border-color: black; -fx-border-radius: 4px; -fx-border-width: 2px; -fx-background-color: blue;-fx-text-fill: white;");
        button.setCancelButton(true);

        Label versionText = new Label(BuildVersionUtil.getVersionInfo());
        versionText.setStyle("-fx-padding: 5px;");

        Pane splashLayout = new VBox();
        splashLayout.getChildren().addAll(splash, versionText, button);
        splashLayout.setStyle("-fx-padding: 5px; -fx-border-width:5px; -fx-border-color: black;");
        splashLayout.setEffect(new DropShadow());

        Stage dialog = new Stage();
        dialog.initOwner(initStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        Scene splashScene = new Scene(splashLayout);
        dialog.setScene(splashScene);
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        dialog.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
        dialog.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);

        button.setOnAction((e)-> {
            dialog.close();
        });

        dialog.showAndWait();
    }
}