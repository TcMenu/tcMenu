package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.remote.protocol.MessageField;

import java.util.Objects;

import static com.thecoderscorner.menu.remote.commands.MenuCommandType.FORM_DATA_RESPONSE;

/**
 * This command represents the form data response that is sent from the device to the remote. It is normally sent in
 * bin format and contains the XML definition of a mobile form.
 */
public class FormDataResponseCommand implements MenuCommand {
    private final String formData;

    public FormDataResponseCommand(String formData) {
        this.formData = formData;
    }

    public String getFormData() {
        return formData;
    }

    @Override
    public MessageField getCommandType() {
        return FORM_DATA_RESPONSE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormDataResponseCommand that = (FormDataResponseCommand) o;
        return Objects.equals(formData, that.formData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(formData);
    }

    @Override
    public String toString() {
        return "FormDataResponseCommand{" +
                "formData='" + formData + '\'' +
                '}';
    }
}
