/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.protocol;

import com.google.common.collect.ImmutableMap;
import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.AnalogMenuItemBuilder;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.commands.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import static com.thecoderscorner.menu.domain.AnalogMenuItemBuilder.*;
import static com.thecoderscorner.menu.remote.commands.MenuBootstrapCommand.*;
import static com.thecoderscorner.menu.remote.protocol.ProtocolUtil.getVersionFromProperties;
import static com.thecoderscorner.menu.remote.protocol.TagValMenuFields.*;

/**
 * A protocol implementation that uses tag value pair notation with a few special text items
 * in order to create messages that can be transmitted. It is currently the default protocol.
 * Example of this format: "0=123|1=234~" where key 0 is 123 and key 1 is 234, tilde indicates
 * the end of the message.
 */
public class TagValMenuCommandProtocol implements MenuCommandProtocol {

    private final String apiVer;
    private final Map<String, MenuCommandType> codeToCmdType;

    public TagValMenuCommandProtocol() {
        this.apiVer = getVersionFromProperties();
        ImmutableMap.Builder<String, MenuCommandType> builder = ImmutableMap.builder();
        for (MenuCommandType ty : MenuCommandType.values()) {
            builder.put(ty.getCode(), ty);
        }
        codeToCmdType = builder.build();
    }

    @Override
    public MenuCommand fromChannel(ByteBuffer buffer) throws IOException {

        TagValTextParser parser = new TagValTextParser(buffer);
        String ty = parser.getValue(KEY_MSG_TYPE);
        MenuCommandType cmdType = codeToCmdType.get(ty);
        if(cmdType == null) throw new IOException("Protocol received unexpected message: " + ty);

        switch (cmdType) {
            case JOIN:
                return processJoin(parser);
            case HEARTBEAT:
                return processHeartbeat(parser);
            case BOOTSTRAP:
                return processBootstrap(parser);
            case ANALOG_BOOT_ITEM:
                return processAnalogBootItem(parser);
            default:
                throw new IOException("Unknown message type " + cmdType);
        }
    }

    private MenuCommand processAnalogBootItem(TagValTextParser parser) throws IOException {
        AnalogMenuItem item = anAnalogMenuItemBuilder()
                .withId(parser.getValueAsInt(KEY_ID_FIELD))
                .withDivisor(parser.getValueAsInt(KEY_ANALOG_DIVISOR_FIELD))
                .withMaxValue(parser.getValueAsInt(KEY_ANALOG_MAX_FIELD))
                .withOffset(parser.getValueAsInt(KEY_ANALOG_OFFSET_FIELD))
                .withUnit(parser.getValue(KEY_ANALOG_UNIT_FIELD))
                .withName(parser.getValue(KEY_NAME_FIELD))
                .menuItem();
        return new MenuAnalogBootCommand(parser.getValueAsInt(KEY_PARENT_ID_FIELD), item);
    }

    private MenuCommand processBootstrap(TagValTextParser parser) throws IOException {
        BootType bt= BootType.valueOf(parser.getValue(KEY_BOOT_TYPE_FIELD));
        return new MenuBootstrapCommand(bt);
    }

    private MenuCommand processJoin(TagValTextParser parser) throws IOException {
        return new MenuJoinCommand(parser.getValue(KEY_NAME_FIELD), parser.getValue(KEY_VER_FIELD));
    }

    private MenuCommand processHeartbeat(TagValTextParser parser) {
        return new MenuHeartbeatCommand();
    }

    @Override
    public void toChannel(ByteBuffer buffer, MenuCommand cmd) {
        StringBuilder sb = new StringBuilder(128);
        appendField(sb, KEY_MSG_TYPE, cmd.getCommandType().getCode());

        switch(cmd.getCommandType()) {
            case HEARTBEAT:
                // empty message - nothing extra to be added
                break;
            case JOIN:
                writeJoin(sb, (MenuJoinCommand)cmd);
                break;
            case BOOTSTRAP:
                writeBootstrap(sb, (MenuBootstrapCommand)cmd);
                break;
            case ANALOG_BOOT_ITEM:
                writeAnalogItem(sb, (MenuAnalogBootCommand)cmd);
                break;
        }

        sb.append('~');
        buffer.put(sb.toString().getBytes());
    }

    private void writeAnalogItem(StringBuilder sb, MenuAnalogBootCommand cmd) {
        appendField(sb, KEY_PARENT_ID_FIELD, cmd.getSubMenuId());
        appendField(sb, KEY_ID_FIELD, cmd.getMenuItem().getId());
        appendField(sb, KEY_NAME_FIELD, cmd.getMenuItem().getName());
        appendField(sb, KEY_ANALOG_OFFSET_FIELD, cmd.getMenuItem().getOffset());
        appendField(sb, KEY_ANALOG_DIVISOR_FIELD, cmd.getMenuItem().getDivisor());
        appendField(sb, KEY_ANALOG_MAX_FIELD, cmd.getMenuItem().getMaxValue());
        appendField(sb, KEY_ANALOG_UNIT_FIELD, cmd.getMenuItem().getUnitName());
    }

    private void writeBootstrap(StringBuilder sb, MenuBootstrapCommand cmd) {
        appendField(sb, KEY_BOOT_TYPE_FIELD, cmd.getBootType());
    }

    private void writeJoin(StringBuilder sb, MenuJoinCommand cmd) {
        appendField(sb, KEY_NAME_FIELD, cmd.getMyName());
        appendField(sb, KEY_VER_FIELD, cmd.getApiVersion());
    }

    private void appendField(StringBuilder sb, String key, Object value) {
        sb.append(key);
        sb.append('=');
        sb.append(value);
        sb.append('|');
    }
}
