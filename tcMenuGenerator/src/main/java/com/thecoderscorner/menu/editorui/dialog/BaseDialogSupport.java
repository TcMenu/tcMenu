package com.thecoderscorner.menu.editorui.dialog;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.util.prefs.Preferences;

import static java.lang.System.Logger.Level.ERROR;

public abstract class BaseDialogSupport<T> {
    private static final Object metroLock = new Object();
    private static JMetro jMetro = null;

    protected final System.Logger logger = System.getLogger(getClass().getSimpleName());
    protected Stage dialogStage;
    protected T controller;

    void tryAndCreateDialog(Stage stage, String resource, String title, boolean modal) {
        this.dialogStage = stage;
        try {
            var loader = new FXMLLoader(NewItemDialog.class.getResource(resource));
            Pane pane = loader.load();
            controller = loader.getController();
            initialiseController(controller);
            createDialogStateAndShow(stage, pane, title, modal);
        }
        catch(Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error creating form " + title, ButtonType.CLOSE);
            getJMetro().setScene(alert.getDialogPane().getScene());

            alert.setHeaderText("Error creating the form, more detail is in the log");
            alert.showAndWait();

            logger.log(ERROR, "Unable to create the form", e);
        }

    }

    public static void createDialogStateAndShow(Stage parent, Pane root, String title, boolean modal) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle(title);
        dialogStage.initOwner(parent);

        Scene scene = new Scene(root);
        getJMetro().setScene(scene);

        dialogStage.setScene(scene);
        if (modal) {
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.showAndWait();
        }
        else {
            dialogStage.show();
        }
    }

    protected abstract void initialiseController(T controller) throws Exception;

    public static JMetro getJMetro() {
        synchronized (metroLock) {
            if (jMetro == null) {
                Style style = (getTheme().equals("lightMode") ? Style.LIGHT : Style.DARK);
                jMetro = new JMetro(style);
            }
        }
        return jMetro;
    }

    private static String getTheme() {
        var prefs = Preferences.userNodeForPackage(BaseDialogSupport.class);
        return prefs.get("uiTheme", "lightMode");
    }

    public static void setTheme(String theme) {
        var prefs = Preferences.userNodeForPackage(BaseDialogSupport.class);
        prefs.put("uiTheme", theme.equals("lightMode") ? "lightMode" : "darkMode");
        synchronized (metroLock) {
            jMetro = null;
        }
    }
}
