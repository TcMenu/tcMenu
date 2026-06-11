package com.thecoderscorner.menu.editorui.util;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.menu.editorui.dialog.BaseDialogSupport;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

import static java.lang.System.Logger.Level.INFO;

public class AlertUtil {

    public static Optional<ButtonType> showAlertAndWait(Alert.AlertType alertType, String message, ButtonType... close) {
        return showAlertAndWait(alertType, message, "", close);
    }

    public static Optional<ButtonType> showAlertAndWait(Alert.AlertType alertType, String header, String message, ButtonType... close) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        var ty = alertType.toString();
        alert.setTitle("TcMenu " + ty.charAt(0) + ty.substring(1) + " Notification");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.getButtonTypes().setAll(close);
        alert.getDialogPane().setStyle("-fx-font-size:" + GlobalSettings.defaultFontSize());
        BaseDialogSupport.getJMetro().setScene(alert.getDialogPane().getScene());
        System.getLogger("Alerts").log(INFO, "Show {0} with heading: {1}, description: {2}", alertType, header, message);
        var btn = alert.showAndWait();
        System.getLogger("Alerts").log(INFO, "Alert button pressed: " + btn);
        return btn;
    }
}
