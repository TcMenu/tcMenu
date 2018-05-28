/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.protocol;

import com.google.common.collect.ImmutableMap;
import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.commands.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.thecoderscorner.menu.domain.AnalogMenuItemBuilder.anAnalogMenuItemBuilder;
import static com.thecoderscorner.menu.domain.SubMenuItemBuilder.aSubMenuItemBuilder;
import static com.thecoderscorner.menu.remote.commands.CommandFactory.*;
import static com.thecoderscorner.menu.remote.commands.MenuBootstrapCommand.BootType;
import static com.thecoderscorner.menu.remote.commands.MenuChangeCommand.ChangeType;
import static com.thecoderscorner.menu.remote.protocol.TagValMenuFields.*;

/**
 * A protocol implementation that uses tag value pair notation with a few special text items
 * in order to create messages that can be transmitted. It is currently the default protocol.
 * Example of this format: "0=123|1=234~" where key 0 is 123 and key 1 is 234, tilde indicates
 * the end of the message.
 */
public class TagValMenuCommandProtocol implements MenuCommandProtocol {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, MenuCommandType> codeToCmdType;

    public TagValMenuCommandProtocol() {
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
        logger.info("Protocol convert in: {}", parser);
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
            case SUBMENU_BOOT_ITEM:
                return processSubMenuBootItem(parser);
            case ENUM_BOOT_ITEM:
                return processEnumBootItem(parser);
            case BOOLEAN_BOOT_ITEM:
                return processBoolBootItem(parser);
            case CHANGE_INT_FIELD:
                return processItemChange(parser);
            case TEXT_BOOT_ITEM:
                return processTextItem(parser);
            default:
                throw new IOException("Unknown message type " + cmdType);
        }
    }

    private MenuCommand processItemChange(TagValTextParser parser) throws IOException {
        ChangeType type = MenuChangeCommand.changeTypeFromInt(parser.getValueAsInt(KEY_CHANGE_TYPE));

        if(type == ChangeType.DELTA) {
            return newDeltaChangeCommand(
                    parser.getValueAsInt(KEY_PARENT_ID_FIELD),
                    parser.getValueAsInt(KEY_ID_FIELD),
                    parser.getValueAsInt(KEY_CURRENT_VAL)
            );
        }
        else {
            return newAbsoluteMenuChangeCommand(
                    parser.getValueAsInt(KEY_PARENT_ID_FIELD),
                    parser.getValueAsInt(KEY_ID_FIELD),
                    parser.getValueAsInt(KEY_CURRENT_VAL)
            );
        }
    }

    private MenuCommand processBoolBootItem(TagValTextParser parser) throws IOException {
        BooleanMenuItem item = BooleanMenuItemBuilder.aBooleanMenuItemBuilder()
                .withId(parser.getValueAsInt(KEY_ID_FIELD))
                .withName(parser.getValue(KEY_NAME_FIELD))
                .withReadOnly(parser.getValueAsInt(KEY_READONLY_FIELD) != 0)
                .withNaming(toNaming(parser.getValueAsInt(KEY_BOOLEAN_NAMING)))
                .menuItem();

        int parentId = parser.getValueAsInt(KEY_PARENT_ID_FIELD);
        int currentVal = parser.getValueAsInt(KEY_CURRENT_VAL);
        return newMenuBooleanBootCommand(parentId, item, currentVal != 0);
    }

    private MenuCommand processTextItem(TagValTextParser parser) throws IOException {
        TextMenuItem item = TextMenuItemBuilder.aTextMenuItemBuilder()
                .withId(parser.getValueAsInt(KEY_ID_FIELD))
                .withName(parser.getValue(KEY_NAME_FIELD))
                .withReadOnly(parser.getValueAsInt(KEY_READONLY_FIELD) != 0)
                .withLength(parser.getValueAsInt(KEY_MAX_LENGTH))
                .menuItem();

        int parentId = parser.getValueAsInt(KEY_PARENT_ID_FIELD);
        String currentVal = parser.getValue(KEY_CURRENT_VAL);
        return newMenuTextBootCommand(parentId, item, currentVal);
    }

    private BooleanMenuItem.BooleanNaming toNaming(int i) {
        if(i==0) {
            return BooleanMenuItem.BooleanNaming.TRUE_FALSE;
        }
        else if(i==1) {
            return BooleanMenuItem.BooleanNaming.ON_OFF;
        }
        else {
            return BooleanMenuItem.BooleanNaming.YES_NO;
        }
    }

    private MenuCommand processEnumBootItem(TagValTextParser parser) throws IOException {

        List<String> choices = new ArrayList<>();
        int noOfItems = parser.getValueAsInt(KEY_NO_OF_CHOICES);
        for(int i=0;i<noOfItems;i++) {
            String key = KEY_PREPEND_CHOICE + (char)(i + 'A');
            choices.add(parser.getValue(key));
        }

        EnumMenuItem item = EnumMenuItemBuilder.anEnumMenuItemBuilder()
                .withId(parser.getValueAsInt(KEY_ID_FIELD))
                .withName(parser.getValue(KEY_NAME_FIELD))
                .withReadOnly(parser.getValueAsInt(KEY_READONLY_FIELD) != 0)
                .withEnumList(choices)
                .menuItem();

        int parentId = parser.getValueAsInt(KEY_PARENT_ID_FIELD);
        int currentVal = parser.getValueAsInt(KEY_CURRENT_VAL);
        return newMenuEnumBootCommand(parentId, item, currentVal);
    }

    private MenuCommand processSubMenuBootItem(TagValTextParser parser) throws IOException {
        SubMenuItem item = aSubMenuItemBuilder()
                .withId(parser.getValueAsInt(KEY_ID_FIELD))
                .withName(parser.getValue(KEY_NAME_FIELD))
                .menuItem();
        int parentId = parser.getValueAsInt(KEY_PARENT_ID_FIELD);
        return newMenuSubBootCommand(parentId, item);
    }

    private MenuCommand processAnalogBootItem(TagValTextParser parser) throws IOException {
        AnalogMenuItem item = anAnalogMenuItemBuilder()
                .withId(parser.getValueAsInt(KEY_ID_FIELD))
                .withDivisor(parser.getValueAsInt(KEY_ANALOG_DIVISOR_FIELD))
                .withMaxValue(parser.getValueAsInt(KEY_ANALOG_MAX_FIELD))
                .withOffset(parser.getValueAsInt(KEY_ANALOG_OFFSET_FIELD))
                .withUnit(parser.getValue(KEY_ANALOG_UNIT_FIELD))
                .withName(parser.getValue(KEY_NAME_FIELD))
                .withReadOnly(parser.getValueAsInt(KEY_READONLY_FIELD) != 0)
                .menuItem();
        int parentId = parser.getValueAsInt(KEY_PARENT_ID_FIELD);
        int currentVal = parser.getValueAsInt(KEY_CURRENT_VAL);
        return newAnalogBootCommand(parentId, item, currentVal);
    }

    private MenuCommand processBootstrap(TagValTextParser parser) throws IOException {
        BootType bt= BootType.valueOf(parser.getValue(KEY_BOOT_TYPE_FIELD));
        return new MenuBootstrapCommand(bt);
    }

    private MenuCommand processJoin(TagValTextParser parser) throws IOException {
        return new MenuJoinCommand(parser.getValue(KEY_NAME_FIELD),
                ProtocolUtil.fromKeyToApiPlatform(parser.getValueAsInt(KEY_PLATFORM_ID)),
                parser.getValueAsInt(KEY_VER_FIELD));
    }

    private MenuCommand processHeartbeat(TagValTextParser parser) {
        return newHeartbeatCommand();
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
            case SUBMENU_BOOT_ITEM:
                writeSubMenuItem(sb, (MenuSubBootCommand)cmd);
                break;
            case ENUM_BOOT_ITEM:
                writeEnumMenuItem(sb, (MenuEnumBootCommand) cmd);
                break;
            case BOOLEAN_BOOT_ITEM:
                writeBoolMenuItem(sb, (MenuBooleanBootCommand) cmd);
                break;
            case CHANGE_INT_FIELD:
                writeChangeInt(sb, (MenuChangeCommand)cmd);
                break;
            case TEXT_BOOT_ITEM:
                writeTextMenuItem(sb, (MenuTextBootCommand) cmd);
                break;

        }
        sb.append('~');

        String msgStr = sb.toString();
        logger.debug("Protocol convert out: {}", msgStr);
        buffer.put(msgStr.getBytes());
    }

    private void writeChangeInt(StringBuilder sb, MenuChangeCommand cmd) {
        appendField(sb, KEY_PARENT_ID_FIELD, cmd.getParentItemId());
        appendField(sb, KEY_ID_FIELD, cmd.getMenuItemId());
        appendField(sb, KEY_CHANGE_TYPE, MenuChangeCommand.changeTypeToInt(cmd.getChangeType()));
        appendField(sb, KEY_CURRENT_VAL, cmd.getValue());
    }

    private void writeAnalogItem(StringBuilder sb, MenuAnalogBootCommand cmd) {
        writeCommonBootFields(sb, cmd);
        appendField(sb, KEY_ANALOG_OFFSET_FIELD, cmd.getMenuItem().getOffset());
        appendField(sb, KEY_ANALOG_DIVISOR_FIELD, cmd.getMenuItem().getDivisor());
        appendField(sb, KEY_ANALOG_MAX_FIELD, cmd.getMenuItem().getMaxValue());
        appendField(sb, KEY_ANALOG_UNIT_FIELD, cmd.getMenuItem().getUnitName());
        appendField(sb, KEY_CURRENT_VAL, cmd.getCurrentValue());
    }

    private void writeSubMenuItem(StringBuilder sb, MenuSubBootCommand cmd) {
        writeCommonBootFields(sb, cmd);
        appendField(sb, KEY_CURRENT_VAL, "0");
    }

    private void writeBoolMenuItem(StringBuilder sb, MenuBooleanBootCommand cmd) {
        writeCommonBootFields(sb, cmd);
        appendField(sb, KEY_BOOLEAN_NAMING, fromNaming(cmd.getMenuItem().getNaming()));
        appendField(sb, KEY_CURRENT_VAL, cmd.getCurrentValue() ? 1  : 0);
    }

    private void writeCommonBootFields(StringBuilder sb, BootItemMenuCommand cmd) {
        appendField(sb, KEY_PARENT_ID_FIELD, cmd.getSubMenuId());
        appendField(sb, KEY_ID_FIELD, cmd.getMenuItem().getId());
        appendField(sb, KEY_NAME_FIELD, cmd.getMenuItem().getName());
    }

    private void writeTextMenuItem(StringBuilder sb, MenuTextBootCommand cmd) {
        writeCommonBootFields(sb, cmd);
        appendField(sb, KEY_MAX_LENGTH, cmd.getMenuItem().getTextLength());
        appendField(sb, KEY_CURRENT_VAL, cmd.getCurrentValue());
    }

    private int fromNaming(BooleanMenuItem.BooleanNaming naming) {
        switch (naming) {

            case ON_OFF:
                return 1;
            case YES_NO:
                return 2;
            case TRUE_FALSE:
            default:
                return 0;
        }
    }

    private void writeEnumMenuItem(StringBuilder sb, MenuEnumBootCommand cmd) {
        writeCommonBootFields(sb, cmd);
        appendField(sb, KEY_CURRENT_VAL, cmd.getCurrentValue());
        List<String> entries = cmd.getMenuItem().getEnumEntries();
        appendField(sb, KEY_NO_OF_CHOICES, entries.size());
        for(int i=0;i<entries.size();++i) {
            appendField(sb, KEY_PREPEND_CHOICE + (char)('A' + i), entries.get(i));
        }
    }

    private void writeBootstrap(StringBuilder sb, MenuBootstrapCommand cmd) {
        appendField(sb, KEY_BOOT_TYPE_FIELD, cmd.getBootType());
    }

    private void writeJoin(StringBuilder sb, MenuJoinCommand cmd) {
        appendField(sb, KEY_NAME_FIELD, cmd.getMyName());
        appendField(sb, KEY_VER_FIELD, cmd.getApiVersion());
        appendField(sb, KEY_PLATFORM_ID, cmd.getPlatform().getKey());
    }

    private void appendField(StringBuilder sb, String key, Object value) {
        sb.append(key);
        sb.append('=');
        sb.append(value);
        sb.append('|');
    }
}
