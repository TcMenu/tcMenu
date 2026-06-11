package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.remote.protocol.MessageField;

import java.util.Objects;

import static com.thecoderscorner.menu.remote.commands.MenuCommandType.FORM_GET_NAMES_REQUEST;

/**
 * Gets the names of all the forms that were compiled into the embedded application ahead of time. When this request
 * is made the server immediately responds with a FormGetNamesResponseCommand.
 */
public class FormGetNamesRequestCommand implements MenuCommand {
    private final String criteria = "*";

    public String getCriteria() {
        return criteria;
    }

    @Override
    public MessageField getCommandType() {
        return FORM_GET_NAMES_REQUEST;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormGetNamesRequestCommand that = (FormGetNamesRequestCommand) o;
        return Objects.equals(criteria, that.criteria);
    }

    @Override
    public int hashCode() {
        return Objects.hash(criteria);
    }

    @Override
    public String toString() {
        return "FormGetNamesRequest{criteria='" + criteria + "'}";
    }
}
