package com.thecoderscorner.menu.editorui.controller;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.thecoderscorner.menu.editorui.util.BuildVersionUtil;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class RegistrationController {
    private static final String REGISTRATION_URL_LOCAL = "http://localhost:8080/tcc/registerTcMenu";
    private static final String REGISTRATION_URL = "https://www.thecoderscorner.com/tcc/registerTcMenu";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Pattern emailPattern = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");

    public TextField yourName;
    public TextField emailAddress;
    public TextField companyName;
    public TextField country;
    public CheckBox newsLetter;
    public Button generateButton;
    public Button cancelButton;
    private AtomicReference<String> errorStore = new AtomicReference<>(null);


    public void init() {
        yourName.textProperty().addListener(this::onTextChanged);
        emailAddress.textProperty().addListener(this::onTextChanged);
        onTextChanged(null, null, null);
    }

    private void onTextChanged(Observable obs, String oldVal, String newVal) {
        boolean valid = !Strings.isNullOrEmpty(yourName.getText());
        valid = valid && isValidEmail(emailAddress.getText());

        generateButton.setDisable(!valid);
        if(valid) {
            generateButton.setTooltip(new Tooltip("All valid, good to go"));
            yourName.setStyle("-fx-background-color: white;");
            emailAddress.setStyle("-fx-background-color: white;");
        }
        else {
            generateButton.setTooltip(new Tooltip("Please fill in name and email first"));
            yourName.setStyle("-fx-background-color: #ae8f8b;");
            emailAddress.setStyle("-fx-background-color: #ae8f8b;");
        }
    }

    private boolean isValidEmail(String text) {
        return !Strings.isNullOrEmpty(text) && emailPattern.matcher(text).matches();
    }

    public void onRegister(ActionEvent event) {
        Gson gson = new Gson();
        Registration registration = new Registration(
                yourName.getText(),
                emailAddress.getText(),
                companyName.getText(),
                country.getText(),
                newsLetter.isSelected()
        );
        String json = gson.toJson(registration);
        logger.info(json);
        generateButton.setDisable(true);
        yourName.setDisable(true);
        emailAddress.setDisable(true);
        companyName.setDisable(true);
        country.setDisable(true);
        newsLetter.setDisable(true);
        generateButton.setText("Sending");
        generateButton.setTooltip(new Tooltip("Sending to server"));
        Thread th = new Thread(new RegisterTask(json));
        th.start();
    }

    private void registrationFailed() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Failed to register");
        alert.setHeaderText("Registration could not be sent");

        StringBuilder sb = new StringBuilder();
        if(errorStore.get() != null) {
            sb.append(errorStore.get());
        }
        sb.append("\nSorry for the inconvenience.");
        alert.setContentText(sb.toString());
        alert.showAndWait();

        generateButton.setText("Retry");
        generateButton.setTooltip(new Tooltip("Retry sending"));
        generateButton.setDisable(false);
    }

    public void onCancel(ActionEvent event) {
        closeIt();
    }

    private void closeIt() {
        Stage s = (Stage) yourName.getScene().getWindow();
        s.close();
    }

    class RegisterTask implements Runnable {

        private final String json;
        public RegisterTask(String json) {
            this.json = json;
        }

        @Override
        public void run() {
            try {
                URL tccUrl = new URL(REGISTRATION_URL_LOCAL);
                HttpURLConnection con = (HttpURLConnection) tccUrl.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type","application/json");
                con.setDoOutput(true);
                con.setDoInput(true);
                con.getOutputStream().write(json.getBytes());

                String data = new String(con.getInputStream().readAllBytes());
                JsonElement element = new JsonParser().parse(data);
                JsonElement regElement = element.getAsJsonObject().get("registered");
                JsonElement regWhoElement = element.getAsJsonObject().get("regUser");
                JsonElement errorElement = element.getAsJsonObject().get("error");
                if(regElement.getAsBoolean()) {
                    BuildVersionUtil.storeRegistration(regWhoElement.getAsString());
                }
                else {
                    errorStore.set(errorElement.getAsString());
                    Platform.runLater(RegistrationController.this::registrationFailed);
                }
                Platform.runLater(RegistrationController.this::closeIt);
            } catch (Exception e) {
                logger.error("Did not connect to the coders corner.",e );
                errorStore.set("Unexpected error during registration");
                Platform.runLater(RegistrationController.this::registrationFailed);
            }
        }
    }

    class Registration {
        private String name;
        private String email;
        private String companyName;
        private String country;
        private boolean newsletter;

        public Registration(String name, String email, String companyName, String country, boolean newsletter) {
            this.name = name;
            this.email = email;
            this.companyName = companyName;
            this.country = country;
            this.newsletter = newsletter;
        }
    }

}
