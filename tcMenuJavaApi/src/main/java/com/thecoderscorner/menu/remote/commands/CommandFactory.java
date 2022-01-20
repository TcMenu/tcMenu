/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.remote.protocol.ApiPlatform;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import com.thecoderscorner.menu.remote.protocol.ProtocolUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.thecoderscorner.menu.remote.commands.MenuChangeCommand.ChangeType;
import static com.thecoderscorner.menu.remote.commands.MenuHeartbeatCommand.HeartbeatMode;

/**
 * These static helper methods are the preferred way to create command message that can be sent and received from
 * a remote connection. Each protocol can convert sent and received messages into this form.
 */
public class CommandFactory {
    /**
     * Create a new join command that has a random UUID
     * @param name the name that the remote will show for the connection
     * @return join command.
     */
    public static MenuJoinCommand newJoinCommand(String name) {
        return new MenuJoinCommand(name, ApiPlatform.JAVA_API, ProtocolUtil.getVersionFromProperties());
    }

    /**
     * Create a new join command that has a fixed UUID that you provide.
     * @param name the name that the remote will show for the connection
     * @param uuid the UUID that will the remote will see for this.
     * @return join command
     */
    public static MenuJoinCommand newJoinCommand(String name, UUID uuid) {
        return new MenuJoinCommand(uuid, name, ApiPlatform.JAVA_API, ProtocolUtil.getVersionFromProperties());
    }

    /**
     * Create a new heartbeat message with the frequency specified
     * @param frequency the frequency
     * @return heartbeat command
     */
    public static MenuHeartbeatCommand newHeartbeatCommand(int frequency, HeartbeatMode mode) {
        return new MenuHeartbeatCommand(frequency, mode);
    }

    /**
     * create an acknowledgement message for a given correlation and status
     * @param correlationId the correlation
     * @param status the status
     * @return the message
     */
    public static MenuAcknowledgementCommand newAcknowledgementCommand(CorrelationId correlationId, AckStatus status) {
        return new MenuAcknowledgementCommand(correlationId, status);
    }

    public static MenuPairingCommand newPairingCommand(String name, UUID uuid) {
        return new MenuPairingCommand(name, uuid);
    }

    public static MenuDialogCommand newDialogCommand(DialogMode mode, String header, String msg,
                                                     MenuButtonType b1, MenuButtonType b2,
                                                     CorrelationId correlationId) {
        return new MenuDialogCommand(mode, header, msg, b1, b2, correlationId);
    }

    /**
     * Create a new bootstrap message either to indicate the bootstrap start or end
     * @param type one of the enum values allowed
     * @return bootstrap message
     */
    public static MenuBootstrapCommand newBootstrapCommand(MenuBootstrapCommand.BootType type) {
        return new MenuBootstrapCommand(type);
    }

    /**
     * create a new analog bootstrap command.
     * @param parentId the parent onto which the item will be placed.
     * @param item the item itself.
     * @param currentVal the current value
     * @return a new analog boot command
     */
    public static MenuAnalogBootCommand newAnalogBootCommand(int parentId, AnalogMenuItem item, int currentVal) {
        return new MenuAnalogBootCommand(parentId, item, currentVal);
    }

    /**
     * Create a new runtime list boot command
     * @param parentId the parent onto which this will be placed
     * @param item the item itself.
     * @param val the current value
     * @return a new runtime list boot command
     */
    public static MenuRuntimeListBootCommand newRuntimeListBootCommand(int parentId, RuntimeListMenuItem item, List<String> val) {
        return new MenuRuntimeListBootCommand(parentId, item, val);
    }

    /**
     * create a new submenu bootstrap command.
     * @param parentId the parent onto which the item will be placed.
     * @param item the item itself.
     * @return a new submenu boot command
     */
    public static MenuSubBootCommand newMenuSubBootCommand(int parentId, SubMenuItem item) {
        return new MenuSubBootCommand(parentId, item, false);
    }

    /**
     * create a new enum bootstrap command.
     * @param parentId the parent onto which the item will be placed.
     * @param item the item itself.
     * @param currentVal the current value
     * @return a new enum boot command
     */
    public static MenuEnumBootCommand newMenuEnumBootCommand(int parentId, EnumMenuItem item, int currentVal) {
        return new MenuEnumBootCommand(parentId, item, currentVal);
    }

    /**
     * create a new boolean bootstrap command.
     * @param parentId the parent onto which the item will be placed.
     * @param item the item itself.
     * @param currentVal the current value
     * @return a new boolean boot command
     */
    public static MenuBooleanBootCommand newMenuBooleanBootCommand(int parentId, BooleanMenuItem item, boolean currentVal) {
        return new MenuBooleanBootCommand(parentId, item, currentVal);
    }

    /**
     * create a new float bootstrap command.
     * @param parentId the parent onto which the item will be placed.
     * @param item the item itself.
     * @param currentVal the current value
     * @return a new float boot command
     */
    public static MenuFloatBootCommand newMenuFloatBootCommand(int parentId, FloatMenuItem item, Float currentVal) {
        return new MenuFloatBootCommand(parentId, item, currentVal);
    }

    /**
     * create a new action bootstrap command.
     * @param parentId the parent onto which the item will be placed.
     * @param item the item itself.
     * @return a new action boot command
     */
    public static MenuActionBootCommand newMenuActionBootCommand(int parentId, ActionMenuItem item) {
        return new MenuActionBootCommand(parentId, item, false);
    }

    /**
     * create a new text bootstrap command.
     * @param parentId the parent onto which the item will be placed.
     * @param item the item itself.
     * @param currentVal the current value
     * @return a new text boot command
     */
    public static MenuTextBootCommand newMenuTextBootCommand(int parentId, EditableTextMenuItem item, String currentVal) {
        return new MenuTextBootCommand(parentId, item, currentVal);
    }

    /**
     * create a new large number bootstrap command.
     * @param parentId the parent onto which the item will be placed.
     * @param item the item itself.
     * @param currentVal the current value
     * @return a new number boot command
     */
    public static MenuLargeNumBootCommand newLargeNumberBootItem(int parentId, EditableLargeNumberMenuItem item, BigDecimal currentVal) {
        return new MenuLargeNumBootCommand(parentId, item, currentVal);
    }
    /**
     * Creates a new delta change command given the menu item and the delta change in value.
     * @param correlation a correlation ID that will be returned in the subsequent acknowledgement.
     * @param item the item for which to send
     * @param value the change in value as a delta of the current value
     * @return a new change message
     */
    public static MenuChangeCommand newDeltaChangeCommand(CorrelationId correlation, MenuItem item, int value) {
        return new MenuChangeCommand(correlation, item.getId(), ChangeType.DELTA, Integer.toString(value));
    }

    /**
     * Creates a new absolute change command given the menu item and the absolute change in value.
     * @param correlation a correlation ID that will be returned in the subsequent acknowledgement.
     * @param item the item for which to send
     * @param value the new value
     * @return a new change message
     */
    public static MenuChangeCommand newAbsoluteMenuChangeCommand(CorrelationId correlation, MenuItem item, Object value) {
        return new MenuChangeCommand(correlation, item.getId(), ChangeType.ABSOLUTE, value.toString());
    }

    /**
     * Creates a new delta change command given the menu item ID and the delta change in value.
     * @param correlation a correlation ID that will be returned in the subsequent acknowledgement.
     * @param itemId the item ID for which to send
     * @param value the change in value as a delta of the current value
     * @return a new change message
     */
    public static MenuChangeCommand newDeltaChangeCommand(CorrelationId correlation, int itemId, int value) {
        return new MenuChangeCommand(correlation, itemId, ChangeType.DELTA, Integer.toString(value));
    }

    /**
     * Creates a new absolute change command given the menu item ID and the absolute change in value.
     * @param correlation a correlation ID that will be returned in the subsequent acknowledgement.
     * @param itemId the item ID for which to send
     * @param value the new value
     * @return a new change message
     */
    public static MenuChangeCommand newAbsoluteMenuChangeCommand(CorrelationId correlation, int itemId, Object value) {
        return new MenuChangeCommand(correlation, itemId, ChangeType.ABSOLUTE, value.toString());
    }

    /**
     * Creates a new absolute change command given the menu item ID and the absolute change in value.
     * @param correlation a correlation ID that will be returned in the subsequent acknowledgement.
     * @param itemId the item ID for which to send
     * @param values the new value
     * @return a new change message
     */
    public static MenuChangeCommand newAbsoluteListChangeCommand(CorrelationId correlation, int itemId, List<String> values) {
        return new MenuChangeCommand(correlation, itemId, values);
    }

}
