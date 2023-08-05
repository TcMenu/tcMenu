package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.MenuEditorApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.util.ResourceBundle;

import static java.lang.System.Logger.Level.ERROR;

public abstract class BaseDialogSupport<T> {
    private static final Object metroLock = new Object();
    private static JMetro jMetro = null;

    protected final System.Logger logger = System.getLogger(getClass().getSimpleName());
    protected Stage dialogStage;
    protected T controller;
    protected ResourceBundle bundle = MenuEditorApp.getBundle();

    /**
     * Usually called to initialise the dialog and present on screen.
     * @param stage the stage to present on
     * @param resource the FXML resource file name as a string.
     * @param title the title for the dialog
     * @param modal if it should be modal.
     */
    public void tryAndCreateDialog(Stage stage, String resource, String title, boolean modal) {
        this.dialogStage = stage;
        try {
            var loader = new FXMLLoader(NewItemDialog.class.getResource(resource));
            if(bundle != null) {
                loader.setResources(bundle);
            }
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

    /**
     * Each dialog overrides this to initialise the controller object during
     * the new dialog being shown
     * @param controller the controller to initialise
     * @throws Exception if any problems occur
     */
    protected abstract void initialiseController(T controller) throws Exception;

    /**
     * Get the JMetro object that we use as the theme.
     * @return the jmetro object.
     */
    public static JMetro getJMetro() {
        synchronized (metroLock) {
            if (jMetro == null) {
                Style style = (getTheme().equals("lightMode") ? Style.LIGHT : Style.DARK);
                jMetro = new JMetro(style);
            }
        }
        return jMetro;
    }

    /**
     * gets the actual theme that is current being used to draw panels.
     * @return the current theme name
     */
    public static String getTheme() {
        return MenuEditorApp.getInstance().getCurrentTheme();
    }

    /**
     * @return true if the current theme is dark, and that suitable colours should be used
     */
    public static boolean isCurrentThemeDark() {
        return getTheme().equals("darkMode");
    }

    /**
     * Set the theme to a new value. Either lightMode or darkMode
     * @param theme the new theme.
     */
    public static void setTheme(String theme) {
        var mode = theme.equals("lightMode") ? "lightMode" : "darkMode";
        synchronized (metroLock) {
            jMetro = null;
            MenuEditorApp.getInstance().setCurrentTheme(mode);
        }
    }
}
