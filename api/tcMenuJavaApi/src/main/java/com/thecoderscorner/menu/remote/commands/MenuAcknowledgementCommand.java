/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import com.thecoderscorner.menu.remote.protocol.MessageField;

import java.util.Objects;

public class MenuAcknowledgementCommand implements MenuCommand {

    private final CorrelationId correlationId;
    private final AckStatus ackStatus;

    public MenuAcknowledgementCommand(CorrelationId correlationId, AckStatus ackStatus) {
        this.correlationId = correlationId;
        this.ackStatus = ackStatus;
    }

    public CorrelationId getCorrelationId() {
        return correlationId;
    }

    public AckStatus getAckStatus() {
        return ackStatus;
    }

    @Override
    public MessageField getCommandType() {
        return MenuCommandType.ACKNOWLEDGEMENT;
    }

    @Override
    public String toString() {
        return "MenuAcknowledgementCommand{" +
                "correlationId=" + correlationId +
                ", ackStatus=" + ackStatus +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuAcknowledgementCommand that = (MenuAcknowledgementCommand) o;
        return Objects.equals(correlationId, that.correlationId) &&
                ackStatus == that.ackStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(correlationId, ackStatus);
    }
}
