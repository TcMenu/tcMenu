/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import com.thecoderscorner.menu.remote.protocol.MessageField;

import java.util.Objects;

public class MenuDialogCommand implements MenuCommand {

    private final DialogMode dialogMode;
    private final String header;
    private final String buffer;
    private final MenuButtonType b1;
    private final MenuButtonType b2;
    private final CorrelationId correlationId;

    public MenuDialogCommand(DialogMode dialogMode, String header, String buffer, MenuButtonType b1, MenuButtonType b2,
                             CorrelationId correlationId) {
        this.dialogMode = dialogMode;
        this.header = header;
        this.buffer = buffer;
        this.b1 = b1;
        this.b2 = b2;
        this.correlationId = correlationId;
    }

    public DialogMode getDialogMode() {
        return dialogMode;
    }

    public String getHeader() {
        return header;
    }

    public String getBuffer() {
        return buffer;
    }

    public MenuButtonType getButton1() {
        return b1;
    }

    public MenuButtonType getButton2() {
        return b2;
    }

    public MessageField getCommandType() {
        return MenuCommandType.DIALOG_UPDATE;
    }

    public CorrelationId getCorrelationId() {
        return correlationId;
    }

    @Override
    public String toString() {
        return "MenuDialogCommand{" +
                "dialogMode=" + dialogMode +
                ", header='" + header + '\'' +
                ", buffer='" + buffer + '\'' +
                ", b1=" + b1 +
                ", b2=" + b2 +
                ", correlation=" + correlationId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuDialogCommand that = (MenuDialogCommand) o;
        return getDialogMode() == that.getDialogMode() &&
                Objects.equals(getHeader(), that.getHeader()) &&
                Objects.equals(getBuffer(), that.getBuffer()) &&
                Objects.equals(getCorrelationId(), that.getCorrelationId()) &&
                getButton1() == that.getButton1() &&
                getButton2() == that.getButton2();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDialogMode(), getHeader(), getBuffer(), getButton1(), getButton2(), getCorrelationId());
    }
}
