package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.remote.protocol.MessageField;

import java.util.List;
import java.util.Objects;

import static com.thecoderscorner.menu.remote.commands.MenuCommandType.FORM_GET_NAMES_RESPONSE;

/**
 * Sent from the device to the remote when the FormGetNamesRequestCommand is received by the device. It sends back a
 * list of names of forms within the devices flash.
 */
public class FormGetNamesResponseCommand implements MenuCommand {
    private final List<String> formNames;

    public FormGetNamesResponseCommand(List<String> formNames) {
        this.formNames = formNames;
    }

    public List<String> getFormNames() {
        return formNames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormGetNamesResponseCommand that = (FormGetNamesResponseCommand) o;
        return Objects.equals(formNames, that.formNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(formNames);
    }

    @Override
    public String toString() {
        return "FormGetNamesResponse{formNames=" + formNames + '}';
    }

    @Override
    public MessageField getCommandType() {
        return FORM_GET_NAMES_RESPONSE;
    }
}
