/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.util.SimpleHttpClient;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.lang.System.Logger.Level;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static com.thecoderscorner.menu.editorui.util.AlertUtil.showAlertAndWait;
import static com.thecoderscorner.menu.editorui.util.IHttpClient.HttpDataType;

public class RegistrationController {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final Pattern emailPattern = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");

    public TextField yourName;
    public TextField emailAddress;
    public TextField companyName;
    public TextField country;
    public CheckBox newsLetter;
    public Button generateButton;
    public Button cancelButton;
    private AtomicReference<String> errorStore = new AtomicReference<>(null);
    private ConfigurationStorage configStorage;
    private String registrationUrl;


    public void init(ConfigurationStorage configStorage, String registrationUrl) {
        this.configStorage = configStorage;
        this.registrationUrl = registrationUrl;
        yourName.textProperty().addListener(this::onTextChanged);
        emailAddress.textProperty().addListener(this::onTextChanged);
        onTextChanged(null, null, null);
    }

    private void onTextChanged(Observable obs, String oldVal, String newVal) {
        boolean valid = !StringHelper.isStringEmptyOrNull(yourName.getText());
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
        return !StringHelper.isStringEmptyOrNull(text) && emailPattern.matcher(text).matches();
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
        logger.log(Level.INFO, "Sending json to reg service " + json);
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
        StringBuilder sb = new StringBuilder();
        if(errorStore.get() != null) {
            sb.append(errorStore.get());
        }
        sb.append("\nSorry for the inconvenience.");
        showAlertAndWait(Alert.AlertType.ERROR, "Registration failure", sb.toString(), ButtonType.CLOSE);
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
                SimpleHttpClient simpleHttpClient = new SimpleHttpClient();
                var data = simpleHttpClient.postRequestForString(registrationUrl, json, HttpDataType.JSON_DATA);

                JsonElement element = JsonParser.parseString(data);
                JsonElement regElement = element.getAsJsonObject().get("registered");
                JsonElement regWhoElement = element.getAsJsonObject().get("regUser");
                JsonElement errorElement = element.getAsJsonObject().get("error");
                if(regElement.getAsBoolean()) {
                    configStorage.setRegisteredKey(regWhoElement.getAsString());
                    Platform.runLater(RegistrationController.this::closeIt);
                }
                else {
                    errorStore.set(errorElement.getAsString());
                    Platform.runLater(RegistrationController.this::registrationFailed);
                }
            } catch (Exception e) {
                logger.log(Level.ERROR, "Did not connect to the coders corner.", e);
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
