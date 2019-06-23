/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.protocol;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.commands.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import static com.thecoderscorner.menu.domain.AnalogMenuItemBuilder.anAnalogMenuItemBuilder;
import static com.thecoderscorner.menu.domain.SubMenuItemBuilder.aSubMenuItemBuilder;
import static com.thecoderscorner.menu.remote.commands.CommandFactory.*;
import static com.thecoderscorner.menu.remote.commands.MenuBootstrapCommand.BootType;
import static com.thecoderscorner.menu.remote.commands.MenuChangeCommand.ChangeType;
import static com.thecoderscorner.menu.remote.protocol.TagValMenuFields.*;
import static java.lang.System.Logger.Level.DEBUG;

/**
 * A protocol implementation that uses tag value pair notation with a few special text items
 * in order to create messages that can be transmitted. It is currently the default protocol.
 * Example of this format: "0=123|1=234~" where key 0 is 123 and key 1 is 234, tilde indicates
 * the end of the message.
 */
public class TagValMenuCommandProtocol implements MenuCommandProtocol {
    private static final byte PROTOCOL_TAG_VAL = 1;
    private static final boolean DEBUG_ALL_MESSAGES = false;

    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final Map<String, MenuCommandType> codeToCmdType;

    public TagValMenuCommandProtocol() {
        codeToCmdType = new HashMap<>();
        for (MenuCommandType ty : MenuCommandType.values()) {
            codeToCmdType.put(ty.getCode(), ty);
        }
    }

    @Override
    public MenuCommand fromChannel(ByteBuffer buffer) throws IOException {
        TagValTextParser parser = new TagValTextParser(buffer);
        if(DEBUG_ALL_MESSAGES) logger.log(DEBUG, "Protocol convert in: {0}", parser);
        String ty = parser.getValue(KEY_MSG_TYPE);
        MenuCommandType cmdType = codeToCmdType.get(ty);
        if(cmdType == null) throw new TcProtocolException("Protocol received unexpected message: " + ty);

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
            case REMOTE_BOOT_ITEM:
                return processRemoteItem(parser);
            case FLOAT_BOOT_ITEM:
                return processFloatItem(parser);
            case ACTION_BOOT_ITEM:
                return processActionItem(parser);
            case RUNTIME_LIST_BOOT:
                return processRuntimeListBoot(parser);
            case ACKNOWLEDGEMENT:
                return processAcknowledgement(parser);
            case PAIRING_REQUEST:
                return processPairingRequest(parser);
            case DIALOG_UPDATE:
                return processDialogUpdate(parser);
            default:
                throw new TcProtocolException("Unknown message type " + cmdType);
        }
    }

    private MenuCommand processDialogUpdate(TagValTextParser parser) throws IOException {
        var cor = parser.getValueWithDefault(KEY_CORRELATION_FIELD, "");
        var correlationId = (cor.isEmpty()) ? CorrelationId.EMPTY_CORRELATION : new CorrelationId(cor);

        return newDialogCommand(
                asDialogMode(parser.getValue(KEY_MODE_FIELD)),
                parser.getValueWithDefault(KEY_HEADER_FIELD, ""),
                parser.getValueWithDefault(KEY_BUFFER_FIELD, ""),
                asButton(parser.getValueAsIntWithDefault(KEY_BUTTON1_FIELD, 0)),
                asButton(parser.getValueAsIntWithDefault(KEY_BUTTON2_FIELD, 0)),
                correlationId
        );
    }

    private DialogMode asDialogMode(String mode) {
        if(mode.equals("S")) {
            return DialogMode.SHOW;
        }
        else if(mode.equals("H")) {
            return DialogMode.HIDE;
        }
        else return DialogMode.ACTION;
    }

    private MenuButtonType asButton(int req) {
        return Arrays.stream(MenuButtonType.values())
                .filter(b -> b.getTypeVal() == req)
                .findFirst().orElse(MenuButtonType.NONE);
    }

    private MenuCommand processPairingRequest(TagValTextParser parser) throws IOException {
        return newPairingCommand(
                parser.getValue(KEY_NAME_FIELD),
                UUID.fromString(parser.getValue(KEY_UUID_FIELD))
        );
    }

    private MenuCommand processAcknowledgement(TagValTextParser parser) throws IOException {
        CorrelationId id = new CorrelationId(parser.getValueWithDefault(KEY_CORRELATION_FIELD, "0"));
        return newAcknowledgementCommand(id, fromCode(parser.getValueAsInt(KEY_ACK_STATUS)));
    }

    private AckStatus fromCode(int codeIn) {
        return Arrays.stream(AckStatus.values())
                .filter(s-> s.getStatusCode() == codeIn)
                .findFirst().orElse(AckStatus.UNKNOWN_ERROR);
    }

    private MenuCommand processItemChange(TagValTextParser parser) throws IOException {
        ChangeType type = MenuChangeCommand.changeTypeFromInt(parser.getValueAsInt(KEY_CHANGE_TYPE));

        var corStr = parser.getValueWithDefault( KEY_CORRELATION_FIELD, "");
        var correlation = corStr.isEmpty() ? CorrelationId.EMPTY_CORRELATION : new CorrelationId(corStr);
        if(type == ChangeType.DELTA) {
            return newDeltaChangeCommand(
                    correlation,
                    parser.getValueAsInt(KEY_ID_FIELD),
                    parser.getValueAsInt(KEY_CURRENT_VAL)
            );
        }
        else if(type == ChangeType.ABSOLUTE) {
            return newAbsoluteMenuChangeCommand(
                    correlation,
                    parser.getValueAsInt(KEY_ID_FIELD),
                    parser.getValue(KEY_CURRENT_VAL)
            );
        }
        else {
            List<String> choices = choicesFromMsg(parser);
            return newAbsoluteListChangeCommand(
                    correlation,
                    parser.getValueAsInt(KEY_ID_FIELD),
                    choices
            );
        }
    }

    private MenuCommand processRuntimeListBoot(TagValTextParser parser) throws IOException {
        RuntimeListMenuItem item = RuntimeListMenuItemBuilder.aRuntimeListMenuItemBuilder()
                .withId(parser.getValueAsInt(KEY_ID_FIELD))
                .withEepromAddr(parser.getValueAsIntWithDefault(KEY_EEPROM_FIELD, 0))
                .withName(parser.getValue(KEY_NAME_FIELD))
                .withReadOnly(parser.getValueAsInt(KEY_READONLY_FIELD) != 0)
                .withInitialRows(parser.getValueAsInt(KEY_NO_OF_CHOICES))
                .menuItem();

        int parentId = parser.getValueAsInt(KEY_PARENT_ID_FIELD);
        List<String> choices = choicesFromMsg(parser);
        return newRuntimeListBootCommand(
                parentId,
                item,
                choices
        );
    }

    private MenuCommand processBoolBootItem(TagValTextParser parser) throws IOException {
        BooleanMenuItem item = BooleanMenuItemBuilder.aBooleanMenuItemBuilder()
                .withId(parser.getValueAsInt(KEY_ID_FIELD))
                .withEepromAddr(parser.getValueAsIntWithDefault(KEY_EEPROM_FIELD, 0))
                .withName(parser.getValue(KEY_NAME_FIELD))
                .withReadOnly(parser.getValueAsInt(KEY_READONLY_FIELD) != 0)
                .withNaming(toNaming(parser.getValueAsInt(KEY_BOOLEAN_NAMING)))
                .menuItem();

        int parentId = parser.getValueAsInt(KEY_PARENT_ID_FIELD);
        int currentVal = parser.getValueAsInt(KEY_CURRENT_VAL);
        return newMenuBooleanBootCommand(parentId, item, currentVal != 0);
    }

    private MenuCommand processTextItem(TagValTextParser parser) throws IOException {
        EditableTextMenuItem item = EditableTextMenuItemBuilder.aTextMenuItemBuilder()
                .withId(parser.getValueAsInt(KEY_ID_FIELD))
                .withEepromAddr(parser.getValueAsIntWithDefault(KEY_EEPROM_FIELD, 0))
                .withName(parser.getValue(KEY_NAME_FIELD))
                .withReadOnly(parser.getValueAsInt(KEY_READONLY_FIELD) != 0)
                .withEditItemType(EditItemType.fromId(parser.getValueAsInt(KEY_EDIT_TYPE)))
                .withLength(parser.getValueAsInt(KEY_MAX_LENGTH))
                .menuItem();

        int parentId = parser.getValueAsInt(KEY_PARENT_ID_FIELD);
        String currentVal = parser.getValue(KEY_CURRENT_VAL);
        return newMenuTextBootCommand(parentId, item, currentVal);
    }

    private MenuCommand processFloatItem(TagValTextParser parser) throws IOException {
        FloatMenuItem item = FloatMenuItemBuilder.aFloatMenuItemBuilder()
                .withId(parser.getValueAsInt(KEY_ID_FIELD))
                .withEepromAddr(parser.getValueAsIntWithDefault(KEY_EEPROM_FIELD, 0))
                .withName(parser.getValue(KEY_NAME_FIELD))
                .withReadOnly(parser.getValueAsInt(KEY_READONLY_FIELD) != 0)
                .withDecimalPlaces(parser.getValueAsInt(KEY_FLOAT_DECIMAL_PLACES))
                .menuItem();

        int parentId = parser.getValueAsInt(KEY_PARENT_ID_FIELD);
        String currentVal = parser.getValue(KEY_CURRENT_VAL);
        return newMenuFloatBootCommand(parentId, item, Float.valueOf(currentVal));
    }

    private MenuCommand processRemoteItem(TagValTextParser parser) throws IOException {
        RemoteMenuItem item = RemoteMenuItemBuilder.aRemoteMenuItemBuilder()
                .withId(parser.getValueAsInt(KEY_ID_FIELD))
                .withEepromAddr(parser.getValueAsIntWithDefault(KEY_EEPROM_FIELD, 0))
                .withName(parser.getValue(KEY_NAME_FIELD))
                .withReadOnly(parser.getValueAsInt(KEY_READONLY_FIELD) != 0)
                .withRemoteNo(parser.getValueAsInt(KEY_REMOTE_NUM))
                .menuItem();

        int parentId = parser.getValueAsInt(KEY_PARENT_ID_FIELD);
        String currentVal = parser.getValue(KEY_CURRENT_VAL);
        return newMenuRemoteBootCommand(parentId, item, currentVal);
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

        List<String> choices = choicesFromMsg(parser);

        EnumMenuItem item = EnumMenuItemBuilder.anEnumMenuItemBuilder()
                .withId(parser.getValueAsInt(KEY_ID_FIELD))
                .withEepromAddr(parser.getValueAsIntWithDefault(KEY_EEPROM_FIELD, 0))
                .withName(parser.getValue(KEY_NAME_FIELD))
                .withReadOnly(parser.getValueAsInt(KEY_READONLY_FIELD) != 0)
                .withEnumList(choices)
                .menuItem();

        int parentId = parser.getValueAsInt(KEY_PARENT_ID_FIELD);
        int currentVal = parser.getValueAsInt(KEY_CURRENT_VAL);
        return newMenuEnumBootCommand(parentId, item, currentVal);
    }

    private List<String> choicesFromMsg(TagValTextParser parser) throws IOException {
        List<String> choices = new ArrayList<>();
        int noOfItems = parser.getValueAsInt(KEY_NO_OF_CHOICES);
        for(int i=0;i<noOfItems;i++) {
            String keyVal = KEY_PREPEND_CHOICE + (char)(i + 'A');
            String keyName = KEY_PREPEND_NAMECHOICE + (char)(i + 'A');
            String keyText = parser.getValueWithDefault(keyName, "");
            String valText = parser.getValueWithDefault(keyVal, "");
            if(keyText.isEmpty()) {
                choices.add(valText);
            }
            else {
                choices.add(keyText + "\t" + valText);
            }
        }
        return choices;
    }

    private MenuCommand processSubMenuBootItem(TagValTextParser parser) throws IOException {
        SubMenuItem item = aSubMenuItemBuilder()
                .withId(parser.getValueAsInt(KEY_ID_FIELD))
                .withEepromAddr(parser.getValueAsIntWithDefault(KEY_EEPROM_FIELD, 0))
                .withName(parser.getValue(KEY_NAME_FIELD))
                .menuItem();
        int parentId = parser.getValueAsInt(KEY_PARENT_ID_FIELD);
        return newMenuSubBootCommand(parentId, item);
    }

    private MenuCommand processActionItem(TagValTextParser parser) throws IOException {
        ActionMenuItem item = ActionMenuItemBuilder.anActionMenuItemBuilder()
                .withId(parser.getValueAsInt(KEY_ID_FIELD))
                .withEepromAddr(parser.getValueAsIntWithDefault(KEY_EEPROM_FIELD, 0))
                .withName(parser.getValue(KEY_NAME_FIELD))
                .menuItem();
        int parentId = parser.getValueAsInt(KEY_PARENT_ID_FIELD);
        return new MenuActionBootCommand(parentId, item, Boolean.FALSE);
    }

    private MenuCommand processAnalogBootItem(TagValTextParser parser) throws IOException {
        AnalogMenuItem item = anAnalogMenuItemBuilder()
                .withId(parser.getValueAsInt(KEY_ID_FIELD))
                .withDivisor(parser.getValueAsInt(KEY_ANALOG_DIVISOR_FIELD))
                .withMaxValue(parser.getValueAsInt(KEY_ANALOG_MAX_FIELD))
                .withEepromAddr(parser.getValueAsIntWithDefault(KEY_EEPROM_FIELD, 0))
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
        var uuidStr = parser.getValueWithDefault(KEY_UUID_FIELD, "");
        var uuid = uuidStr.isEmpty() ? UUID.randomUUID() : UUID.fromString(uuidStr);
        return new MenuJoinCommand(
                uuid,
                parser.getValue(KEY_NAME_FIELD),
                ProtocolUtil.fromKeyToApiPlatform(parser.getValueAsInt(KEY_PLATFORM_ID)),
                parser.getValueAsInt(KEY_VER_FIELD));
    }

    private MenuCommand processHeartbeat(TagValTextParser parser) throws IOException {
        return newHeartbeatCommand(parser.getValueAsIntWithDefault(HB_FREQUENCY_FIELD, 10000));
    }

    @Override
    public void toChannel(ByteBuffer buffer, MenuCommand cmd) {
        StringBuilder sb = new StringBuilder(128);
        appendField(sb, KEY_MSG_TYPE, cmd.getCommandType().getCode());

        switch(cmd.getCommandType()) {
            case HEARTBEAT:
                writeHeartbeat(sb, (MenuHeartbeatCommand)cmd);
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
            case REMOTE_BOOT_ITEM:
                writeRemoteBootItem(sb, (MenuRemoteBootCommand) cmd);
                break;
            case ACTION_BOOT_ITEM:
                writeActionBootItem(sb, (MenuActionBootCommand) cmd);
                break;
            case FLOAT_BOOT_ITEM:
                writeFloatBootItem(sb, (MenuFloatBootCommand) cmd);
                break;
            case BOOLEAN_BOOT_ITEM:
                writeBoolMenuItem(sb, (MenuBooleanBootCommand) cmd);
                break;
            case RUNTIME_LIST_BOOT:
                writeRuntimeListBootItem(sb, (MenuRuntimeListBootCommand) cmd);
                break;
            case CHANGE_INT_FIELD:
                writeChangeInt(sb, (MenuChangeCommand)cmd);
                break;
            case TEXT_BOOT_ITEM:
                writeTextMenuItem(sb, (MenuTextBootCommand) cmd);
                break;
            case ACKNOWLEDGEMENT:
                writeAcknowledgement(sb, (MenuAcknowledgementCommand)cmd);
                break;
            case PAIRING_REQUEST:
                writePairingRequest(sb, (MenuPairingCommand)cmd);
                break;
            case DIALOG_UPDATE:
                writeDialogUpdate(sb, (MenuDialogCommand)cmd);
                break;

        }
        sb.append('~');

        String msgStr = sb.toString();
        if(DEBUG_ALL_MESSAGES) logger.log(DEBUG, "Protocol convert out: {0}", msgStr);
        buffer.put(msgStr.getBytes());
    }

    private void writeDialogUpdate(StringBuilder sb, MenuDialogCommand cmd) {
        appendField(sb, KEY_MODE_FIELD, asWireMode(cmd.getDialogMode()));
        if(cmd.getHeader() != null) appendField(sb, KEY_HEADER_FIELD, cmd.getHeader());
        if(cmd.getBuffer() != null) appendField(sb, KEY_BUFFER_FIELD, cmd.getBuffer());
        appendField(sb, KEY_BUTTON1_FIELD, cmd.getButton1().getTypeVal());
        appendField(sb, KEY_BUTTON2_FIELD, cmd.getButton2().getTypeVal());
        appendField(sb, KEY_CORRELATION_FIELD, cmd.getCorrelationId());
    }

    private String asWireMode(DialogMode dialogMode) {
        if(dialogMode == DialogMode.SHOW) return "S";
        else if(dialogMode == DialogMode.HIDE) return "H";
        else return "A";
    }

    private void writePairingRequest(StringBuilder sb, MenuPairingCommand cmd) {
        appendField(sb, KEY_NAME_FIELD, cmd.getName());
        appendField(sb, KEY_UUID_FIELD, cmd.getUuid());
    }

    private void writeAcknowledgement(StringBuilder sb, MenuAcknowledgementCommand cmd) {
        appendField(sb, KEY_CORRELATION_FIELD, cmd.getCorrelationId().toString());
        appendField(sb, KEY_ACK_STATUS, cmd.getAckStatus().getStatusCode());
    }

    private void writeHeartbeat(StringBuilder sb, MenuHeartbeatCommand cmd) {
        appendField(sb, HB_FREQUENCY_FIELD, cmd.getHearbeatInterval());
    }

    @Override
    public byte getKeyIdentifier() {
        return PROTOCOL_TAG_VAL;
    }

    private void writeChangeInt(StringBuilder sb, MenuChangeCommand cmd) {
        appendField(sb, KEY_CORRELATION_FIELD, cmd.getCorrelationId());
        appendField(sb, KEY_ID_FIELD, cmd.getMenuItemId());
        appendField(sb, KEY_CHANGE_TYPE, MenuChangeCommand.changeTypeToInt(cmd.getChangeType()));
        if(cmd.getValues() != null) {
            appendChoices(sb, cmd.getValues());
        }
        else appendField(sb, KEY_CURRENT_VAL, cmd.getValue());
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

    private void writeActionBootItem(StringBuilder sb, MenuActionBootCommand cmd) {
        writeCommonBootFields(sb, cmd);
        appendField(sb, KEY_CURRENT_VAL, "");
    }

    private void writeBoolMenuItem(StringBuilder sb, MenuBooleanBootCommand cmd) {
        writeCommonBootFields(sb, cmd);
        appendField(sb, KEY_BOOLEAN_NAMING, fromNaming(cmd.getMenuItem().getNaming()));
        appendField(sb, KEY_CURRENT_VAL, cmd.getCurrentValue() ? 1  : 0);
    }

    private void writeCommonBootFields(StringBuilder sb, BootItemMenuCommand cmd) {
        appendField(sb, KEY_PARENT_ID_FIELD, cmd.getSubMenuId());
        appendField(sb, KEY_ID_FIELD, cmd.getMenuItem().getId());
        appendField(sb, KEY_EEPROM_FIELD, cmd.getMenuItem().getEepromAddress());
        appendField(sb, KEY_NAME_FIELD, cmd.getMenuItem().getName());
        appendField(sb, KEY_READONLY_FIELD, cmd.getMenuItem().isReadOnly() ? 1 : 0);
    }

    private void writeRuntimeListBootItem(StringBuilder sb, MenuRuntimeListBootCommand cmd) {
        writeCommonBootFields(sb, cmd);
        appendChoices(sb, cmd.getCurrentValue());

    }

    private void writeRemoteBootItem(StringBuilder sb, MenuRemoteBootCommand cmd) {
        writeCommonBootFields(sb, cmd);
        appendField(sb, KEY_REMOTE_NUM, cmd.getMenuItem().getRemoteNum());
        appendField(sb, KEY_CURRENT_VAL, cmd.getCurrentValue());
    }

    private void writeFloatBootItem(StringBuilder sb, MenuFloatBootCommand cmd) {
        writeCommonBootFields(sb, cmd);
        appendField(sb, KEY_FLOAT_DECIMAL_PLACES, cmd.getMenuItem().getNumDecimalPlaces());
        appendField(sb, KEY_CURRENT_VAL, cmd.getCurrentValue());
    }

    private void writeTextMenuItem(StringBuilder sb, MenuTextBootCommand cmd) {
        writeCommonBootFields(sb, cmd);
        appendField(sb, KEY_MAX_LENGTH, cmd.getMenuItem().getTextLength());
        appendField(sb, KEY_EDIT_TYPE, cmd.getMenuItem().getItemType().getMsgId());
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
        appendChoices(sb, entries);
    }

    private void appendChoices(StringBuilder sb, List<String> entries) {
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
        appendField(sb, KEY_UUID_FIELD, cmd.getAppUuid());
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
