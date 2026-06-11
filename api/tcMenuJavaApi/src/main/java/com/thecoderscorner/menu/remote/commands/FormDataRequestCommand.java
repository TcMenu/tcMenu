package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.remote.protocol.MessageField;

import java.util.Objects;

import static com.thecoderscorner.menu.remote.commands.MenuCommandType.FORM_DATA_REQUEST;

/**
 * This command represents the form data request that is sent from the remote to the device. When this message is
 * received, the device will immediately respond with a list of UI forms that it has available.
 */
public class FormDataRequestCommand implements MenuCommand {
    private final String formName;

    public FormDataRequestCommand(String formName) {
        this.formName = formName;
    }

    public String getFormName() {
        return formName;
    }

    @Override
    public MessageField getCommandType() {
        return FORM_DATA_REQUEST;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormDataRequestCommand that = (FormDataRequestCommand) o;
        return Objects.equals(formName, that.formName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(formName);
    }

    @Override
    public String toString() {
        return "FormDataRequestCommand{" + "formName='" + formName + "'}";
    }
}
