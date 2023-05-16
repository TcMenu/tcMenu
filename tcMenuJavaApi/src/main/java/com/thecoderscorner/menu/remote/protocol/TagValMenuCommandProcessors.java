/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.protocol;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.CurrentScrollPosition;
import com.thecoderscorner.menu.domain.state.ListResponse;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.remote.commands.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.thecoderscorner.menu.domain.AnalogMenuItemBuilder.anAnalogMenuItemBuilder;
import static com.thecoderscorner.menu.domain.SubMenuItemBuilder.aSubMenuItemBuilder;
import static com.thecoderscorner.menu.remote.commands.CommandFactory.*;
import static com.thecoderscorner.menu.remote.commands.MenuBootstrapCommand.BootType;
import static com.thecoderscorner.menu.remote.commands.MenuChangeCommand.ChangeType;
import static com.thecoderscorner.menu.remote.commands.MenuHeartbeatCommand.HeartbeatMode.*;
import static com.thecoderscorner.menu.remote.protocol.TagValMenuFields.*;

/**
 * A series of protocol handlers for most of the common tag value messages. This is currently the default protocol.
 * Example of this format: "0=123|1=234~" where key 0 is 123 and key 1 is 234, tilde indicates
 * the end of the message.
 * @see ConfigurableProtocolConverter
 */
public class TagValMenuCommandProcessors {
    public void addHandlersToProtocol(ConfigurableProtocolConverter proto) {

        proto.addTagValInProcessor(MenuCommandType.JOIN, this::processJoin);
        proto.addTagValInProcessor(MenuCommandType.HEARTBEAT, this::processHeartbeat);
        proto.addTagValInProcessor(MenuCommandType.BOOTSTRAP, this::processBootstrap);
        proto.addTagValInProcessor(MenuCommandType.ANALOG_BOOT_ITEM, this::processAnalogBootItem);
        proto.addTagValInProcessor(MenuCommandType.SUBMENU_BOOT_ITEM, this::processSubMenuBootItem);
        proto.addTagValInProcessor(MenuCommandType.ENUM_BOOT_ITEM, this::processEnumBootItem);
        proto.addTagValInProcessor(MenuCommandType.BOOLEAN_BOOT_ITEM, this::processBoolBootItem);
        proto.addTagValInProcessor(MenuCommandType.LARGE_NUM_BOOT_ITEM, this::processLargeNumBootItem);
        proto.addTagValInProcessor(MenuCommandType.CHANGE_INT_FIELD, this::processItemChange);
        proto.addTagValInProcessor(MenuCommandType.TEXT_BOOT_ITEM, this::processTextItem);
        proto.addTagValInProcessor(MenuCommandType.FLOAT_BOOT_ITEM, this::processFloatItem);
        proto.addTagValInProcessor(MenuCommandType.ACTION_BOOT_ITEM, this::processActionItem);
        proto.addTagValInProcessor(MenuCommandType.RUNTIME_LIST_BOOT, this::processRuntimeListBoot);
        proto.addTagValInProcessor(MenuCommandType.BOOT_RGB_COLOR, this::processRuntimeRgbColor);
        proto.addTagValInProcessor(MenuCommandType.BOOT_SCROLL_CHOICE, this::processRuntimeScrollChoice);
        proto.addTagValInProcessor(MenuCommandType.ACKNOWLEDGEMENT, this::processAcknowledgement);
        proto.addTagValInProcessor(MenuCommandType.PAIRING_REQUEST, this::processPairingRequest);
        proto.addTagValInProcessor(MenuCommandType.DIALOG_UPDATE, this::processDialogUpdate);

        proto.addTagValOutProcessor(MenuCommandType.HEARTBEAT, this::writeHeartbeat, MenuHeartbeatCommand.class);
        proto.addTagValOutProcessor(MenuCommandType.JOIN, this::writeJoin, MenuJoinCommand.class);
        proto.addTagValOutProcessor(MenuCommandType.ACKNOWLEDGEMENT, this::writeAcknowledgement, MenuAcknowledgementCommand.class);
        proto.addTagValOutProcessor(MenuCommandType.BOOTSTRAP, this::writeBootstrap, MenuBootstrapCommand.class);
        proto.addTagValOutProcessor(MenuCommandType.ANALOG_BOOT_ITEM, this::writeAnalogItem, MenuAnalogBootCommand.class);
        proto.addTagValOutProcessor(MenuCommandType.SUBMENU_BOOT_ITEM, this::writeSubMenuItem, MenuSubBootCommand.class);
        proto.addTagValOutProcessor(MenuCommandType.ENUM_BOOT_ITEM, this::writeEnumMenuItem, MenuEnumBootCommand.class);
        proto.addTagValOutProcessor(MenuCommandType.ACTION_BOOT_ITEM, this::writeActionBootItem, MenuActionBootCommand.class);
        proto.addTagValOutProcessor(MenuCommandType.FLOAT_BOOT_ITEM, this::writeFloatBootItem, MenuFloatBootCommand.class);
        proto.addTagValOutProcessor(MenuCommandType.BOOLEAN_BOOT_ITEM, this::writeBoolMenuItem, MenuBooleanBootCommand.class);
        proto.addTagValOutProcessor(MenuCommandType.RUNTIME_LIST_BOOT, this::writeRuntimeListBootItem, MenuRuntimeListBootCommand.class);
        proto.addTagValOutProcessor(MenuCommandType.LARGE_NUM_BOOT_ITEM, this::writeLargeNumberBootItem, MenuLargeNumBootCommand.class);
        proto.addTagValOutProcessor(MenuCommandType.CHANGE_INT_FIELD, this::writeChangeInt, MenuChangeCommand.class);
        proto.addTagValOutProcessor(MenuCommandType.TEXT_BOOT_ITEM, this::writeTextMenuItem, MenuTextBootCommand.class);
        proto.addTagValOutProcessor(MenuCommandType.PAIRING_REQUEST, this::writePairingRequest, MenuPairingCommand.class);
        proto.addTagValOutProcessor(MenuCommandType.DIALOG_UPDATE, this::writeDialogUpdate, MenuDialogCommand.class);
        proto.addTagValOutProcessor(MenuCommandType.BOOT_RGB_COLOR, this::writeRgbBoot, MenuRgb32BootCommand.class);
        proto.addTagValOutProcessor(MenuCommandType.BOOT_SCROLL_CHOICE, this::writeScrollBoot, MenuScrollChoiceBootCommand.class);
    }

    private MenuCommand processDialogUpdate(TagValTextParser parser) throws TcProtocolException {
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

    private MenuCommand processPairingRequest(TagValTextParser parser) throws TcProtocolException {
        return newPairingCommand(
                parser.getValue(KEY_NAME_FIELD),
                UUID.fromString(parser.getValue(KEY_UUID_FIELD))
        );
    }

    private MenuCommand processAcknowledgement(TagValTextParser parser) throws TcProtocolException {
        CorrelationId id = new CorrelationId(parser.getValueWithDefault(KEY_CORRELATION_FIELD, "0"));
        return newAcknowledgementCommand(id, fromCode(parser.getValueAsInt(KEY_ACK_STATUS)));
    }

    private AckStatus fromCode(int codeIn) {
        return Arrays.stream(AckStatus.values())
                .filter(s-> s.getStatusCode() == codeIn)
                .findFirst().orElse(AckStatus.UNKNOWN_ERROR);
    }

    private MenuCommand processItemChange(TagValTextParser parser) throws TcProtocolException {
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
        else if(type == ChangeType.LIST_STATE_CHANGE) {
            return newListResponseChangeCommand(
                    correlation,
                    parser.getValueAsInt(KEY_ID_FIELD),
                    ListResponse.fromString(parser.getValue(KEY_CURRENT_VAL)).orElse(ListResponse.EMPTY)
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

    private MenuCommand processRuntimeListBoot(TagValTextParser parser) throws TcProtocolException {
        RuntimeListMenuItem item = RuntimeListMenuItemBuilder.aRuntimeListMenuItemBuilder()
                .withId(parser.getValueAsInt(KEY_ID_FIELD))
                .withEepromAddr(parser.getValueAsIntWithDefault(KEY_EEPROM_FIELD, 0))
                .withName(parser.getValue(KEY_NAME_FIELD))
                .withReadOnly(parser.getValueAsInt(KEY_READONLY_FIELD) != 0)
                .withVisible(parser.getValueAsIntWithDefault(KEY_VISIBLE_FIELD, 1) != 0)
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

    private MenuCommand processBoolBootItem(TagValTextParser parser) throws TcProtocolException {
        BooleanMenuItem item = BooleanMenuItemBuilder.aBooleanMenuItemBuilder()
                .withId(parser.getValueAsInt(KEY_ID_FIELD))
                .withEepromAddr(parser.getValueAsIntWithDefault(KEY_EEPROM_FIELD, 0))
                .withName(parser.getValue(KEY_NAME_FIELD))
                .withReadOnly(parser.getValueAsInt(KEY_READONLY_FIELD) != 0)
                .withVisible(parser.getValueAsIntWithDefault(KEY_VISIBLE_FIELD, 1) != 0)
                .withNaming(toNaming(parser.getValueAsInt(KEY_BOOLEAN_NAMING)))
                .menuItem();

        int parentId = parser.getValueAsInt(KEY_PARENT_ID_FIELD);
        int currentVal = parser.getValueAsInt(KEY_CURRENT_VAL);
        return newMenuBooleanBootCommand(parentId, item, currentVal != 0);
    }

    private MenuCommand processRuntimeRgbColor(TagValTextParser parser) throws TcProtocolException {
        Rgb32MenuItem item = new Rgb32MenuItemBuilder()
                .withId(parser.getValueAsInt(KEY_ID_FIELD))
                .withEepromAddr(parser.getValueAsIntWithDefault(KEY_EEPROM_FIELD, 0))
                .withName(parser.getValue(KEY_NAME_FIELD))
                .withReadOnly(parser.getValueAsInt(KEY_READONLY_FIELD) != 0)
                .withVisible(parser.getValueAsIntWithDefault(KEY_VISIBLE_FIELD, 1) != 0)
                .withAlpha(parser.getValueAsIntWithDefault(KEY_ALPHA_FIELD, 0)!=0)
                .menuItem();

        int parentId = parser.getValueAsInt(KEY_PARENT_ID_FIELD);
        var currentVal = parser.getValue(KEY_CURRENT_VAL);
        return new MenuRgb32BootCommand(parentId, item, new PortableColor(currentVal));
    }

    private MenuCommand processRuntimeScrollChoice(TagValTextParser parser) throws TcProtocolException {
        ScrollChoiceMenuItem item = new ScrollChoiceMenuItemBuilder()
                .withId(parser.getValueAsInt(KEY_ID_FIELD))
                .withEepromAddr(parser.getValueAsIntWithDefault(KEY_EEPROM_FIELD, 0))
                .withName(parser.getValue(KEY_NAME_FIELD))
                .withReadOnly(parser.getValueAsInt(KEY_READONLY_FIELD) != 0)
                .withVisible(parser.getValueAsIntWithDefault(KEY_VISIBLE_FIELD, 1) != 0)
                .withItemWidth(parser.getValueAsInt(KEY_WIDTH_FIELD))
                .withNumEntries(parser.getValueAsInt(KEY_NO_OF_CHOICES))
                .menuItem();

        int parentId = parser.getValueAsInt(KEY_PARENT_ID_FIELD);
        var currentVal = parser.getValue(KEY_CURRENT_VAL);
        return new MenuScrollChoiceBootCommand(parentId, item, new CurrentScrollPosition(currentVal));
    }
    private MenuCommand processLargeNumBootItem(TagValTextParser parser) throws TcProtocolException {
        EditableLargeNumberMenuItem item = EditableLargeNumberMenuItemBuilder.aLargeNumberItemBuilder()
                .withId(parser.getValueAsInt(KEY_ID_FIELD))
                .withEepromAddr(parser.getValueAsIntWithDefault(KEY_EEPROM_FIELD, 0))
                .withName(parser.getValue(KEY_NAME_FIELD))
                .withReadOnly(parser.getValueAsInt(KEY_READONLY_FIELD) != 0)
                .withVisible(parser.getValueAsIntWithDefault(KEY_VISIBLE_FIELD, 1) != 0)
                .withDecimalPlaces(parser.getValueAsInt(KEY_FLOAT_DECIMAL_PLACES))
                .withNegativeAllowed(parser.getValueAsIntWithDefault(KEY_NEGATIVE_ALLOWED, 1) != 0)
                .withTotalDigits(parser.getValueAsInt(KEY_MAX_LENGTH))
                .menuItem();

        int parentId = parser.getValueAsInt(KEY_PARENT_ID_FIELD);
        var text = parser.getValue(KEY_CURRENT_VAL).replaceAll("[\\[\\]]", "");
        return newLargeNumberBootItem(parentId, item, safeBigDecimal(text));
    }

    private BigDecimal safeBigDecimal(String text) {
        try {
            return new BigDecimal(text);
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    private MenuCommand processTextItem(TagValTextParser parser) throws TcProtocolException {
        EditableTextMenuItem item = EditableTextMenuItemBuilder.aTextMenuItemBuilder()
                .withId(parser.getValueAsInt(KEY_ID_FIELD))
                .withEepromAddr(parser.getValueAsIntWithDefault(KEY_EEPROM_FIELD, 0))
                .withName(parser.getValue(KEY_NAME_FIELD))
                .withReadOnly(parser.getValueAsInt(KEY_READONLY_FIELD) != 0)
                .withVisible(parser.getValueAsIntWithDefault(KEY_VISIBLE_FIELD, 1) != 0)
                .withEditItemType(EditItemType.fromId(parser.getValueAsInt(KEY_EDIT_TYPE)))
                .withLength(parser.getValueAsInt(KEY_MAX_LENGTH))
                .menuItem();

        int parentId = parser.getValueAsInt(KEY_PARENT_ID_FIELD);
        String currentVal = parser.getValue(KEY_CURRENT_VAL);
        return newMenuTextBootCommand(parentId, item, currentVal);
    }

    private MenuCommand processFloatItem(TagValTextParser parser) throws TcProtocolException {
        FloatMenuItem item = FloatMenuItemBuilder.aFloatMenuItemBuilder()
                .withId(parser.getValueAsInt(KEY_ID_FIELD))
                .withEepromAddr(parser.getValueAsIntWithDefault(KEY_EEPROM_FIELD, 0))
                .withName(parser.getValue(KEY_NAME_FIELD))
                .withReadOnly(parser.getValueAsInt(KEY_READONLY_FIELD) != 0)
                .withVisible(parser.getValueAsIntWithDefault(KEY_VISIBLE_FIELD, 1) != 0)
                .withDecimalPlaces(parser.getValueAsInt(KEY_FLOAT_DECIMAL_PLACES))
                .menuItem();

        int parentId = parser.getValueAsInt(KEY_PARENT_ID_FIELD);
        String currentVal = parser.getValue(KEY_CURRENT_VAL);
        return newMenuFloatBootCommand(parentId, item, Float.valueOf(currentVal));
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

    private MenuCommand processEnumBootItem(TagValTextParser parser) throws TcProtocolException {

        List<String> choices = choicesFromMsg(parser);

        EnumMenuItem item = EnumMenuItemBuilder.anEnumMenuItemBuilder()
                .withId(parser.getValueAsInt(KEY_ID_FIELD))
                .withEepromAddr(parser.getValueAsIntWithDefault(KEY_EEPROM_FIELD, 0))
                .withName(parser.getValue(KEY_NAME_FIELD))
                .withReadOnly(parser.getValueAsInt(KEY_READONLY_FIELD) != 0)
                .withVisible(parser.getValueAsIntWithDefault(KEY_VISIBLE_FIELD, 1) != 0)
                .withEnumList(choices)
                .menuItem();

        int parentId = parser.getValueAsInt(KEY_PARENT_ID_FIELD);
        int currentVal = parser.getValueAsInt(KEY_CURRENT_VAL);
        return newMenuEnumBootCommand(parentId, item, currentVal);
    }

    private List<String> choicesFromMsg(TagValTextParser parser) throws TcProtocolException {
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

    private MenuCommand processSubMenuBootItem(TagValTextParser parser) throws TcProtocolException {
        SubMenuItem item = aSubMenuItemBuilder()
                .withId(parser.getValueAsInt(KEY_ID_FIELD))
                .withEepromAddr(parser.getValueAsIntWithDefault(KEY_EEPROM_FIELD, 0))
                .withName(parser.getValue(KEY_NAME_FIELD))
                .menuItem();
        int parentId = parser.getValueAsInt(KEY_PARENT_ID_FIELD);
        return newMenuSubBootCommand(parentId, item);
    }

    private MenuCommand processActionItem(TagValTextParser parser) throws TcProtocolException {
        ActionMenuItem item = ActionMenuItemBuilder.anActionMenuItemBuilder()
                .withId(parser.getValueAsInt(KEY_ID_FIELD))
                .withEepromAddr(parser.getValueAsIntWithDefault(KEY_EEPROM_FIELD, 0))
                .withName(parser.getValue(KEY_NAME_FIELD))
                .menuItem();
        int parentId = parser.getValueAsInt(KEY_PARENT_ID_FIELD);
        return new MenuActionBootCommand(parentId, item, Boolean.FALSE);
    }

    private MenuCommand processAnalogBootItem(TagValTextParser parser) throws TcProtocolException {
        AnalogMenuItem item = anAnalogMenuItemBuilder()
                .withId(parser.getValueAsInt(KEY_ID_FIELD))
                .withDivisor(parser.getValueAsInt(KEY_ANALOG_DIVISOR_FIELD))
                .withMaxValue(parser.getValueAsInt(KEY_ANALOG_MAX_FIELD))
                .withEepromAddr(parser.getValueAsIntWithDefault(KEY_EEPROM_FIELD, 0))
                .withOffset(parser.getValueAsInt(KEY_ANALOG_OFFSET_FIELD))
                .withStep(parser.getValueAsIntWithDefault(KEY_ANALOG_STEP_FIELD, 1))
                .withUnit(parser.getValue(KEY_ANALOG_UNIT_FIELD))
                .withName(parser.getValue(KEY_NAME_FIELD))
                .withReadOnly(parser.getValueAsInt(KEY_READONLY_FIELD) != 0)
                .withVisible(parser.getValueAsIntWithDefault(KEY_VISIBLE_FIELD, 1) != 0)
                .menuItem();
        int parentId = parser.getValueAsInt(KEY_PARENT_ID_FIELD);
        int currentVal = parser.getValueAsInt(KEY_CURRENT_VAL);
        return newAnalogBootCommand(parentId, item, currentVal);
    }

    private MenuCommand processBootstrap(TagValTextParser parser) throws TcProtocolException {
        BootType bt= BootType.valueOf(parser.getValue(KEY_BOOT_TYPE_FIELD));
        return new MenuBootstrapCommand(bt);
    }

    private MenuCommand processJoin(TagValTextParser parser) throws TcProtocolException {
        var uuidStr = parser.getValueWithDefault(KEY_UUID_FIELD, "");
        var uuid = uuidStr.isEmpty() ? UUID.randomUUID() : UUID.fromString(uuidStr);
        return new MenuJoinCommand(
                uuid,
                parser.getValue(KEY_NAME_FIELD),
                ProtocolUtil.fromKeyToApiPlatform(parser.getValueAsInt(KEY_PLATFORM_ID)),
                parser.getValueAsInt(KEY_VER_FIELD),
                parser.getValueAsIntWithDefault(KEY_SERIAL_NO, 0));
    }

    private MenuCommand processHeartbeat(TagValTextParser parser) throws TcProtocolException {
        return newHeartbeatCommand(
                parser.getValueAsIntWithDefault(HB_FREQUENCY_FIELD, 10000),
                toHbMode(parser.getValueAsIntWithDefault(HB_MODE_FIELD, 0))
        );
    }

    private MenuHeartbeatCommand.HeartbeatMode toHbMode(int hbModeInt) {
        switch (hbModeInt) {
            case 1:
                return START;
            case 2:
                return END;
            case 0:
            default:
                return NORMAL;
        }
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
        int hbMode;
        switch(cmd.getMode()) {
            case START:
                hbMode = 1;
                break;
            case END:
                hbMode = 2;
                break;
            default:
            case NORMAL:
                hbMode = 0;
                break;
        }
        appendField(sb, HB_FREQUENCY_FIELD, cmd.getHearbeatInterval());
        appendField(sb, HB_MODE_FIELD, hbMode);
    }

    private void writeChangeInt(StringBuilder sb, MenuChangeCommand cmd) {
        appendField(sb, KEY_CORRELATION_FIELD, cmd.getCorrelationId());
        appendField(sb, KEY_ID_FIELD, cmd.getMenuItemId());
        appendField(sb, KEY_CHANGE_TYPE, MenuChangeCommand.changeTypeToInt(cmd.getChangeType()));
        if(cmd.getChangeType() == ChangeType.ABSOLUTE_LIST) {
            appendChoices(sb, cmd.getValues());
        }
        else appendField(sb, KEY_CURRENT_VAL, cmd.getValue());
    }

    private void writeLargeNumberBootItem(StringBuilder sb, MenuLargeNumBootCommand cmd) {
        writeCommonBootFields(sb, cmd);
        int decimalPlaces = cmd.getMenuItem().getDecimalPlaces();
        boolean isNegativeAllowed = cmd.getMenuItem().isNegativeAllowed();
        appendField(sb, KEY_FLOAT_DECIMAL_PLACES, decimalPlaces);
        appendField(sb, KEY_NEGATIVE_ALLOWED, isNegativeAllowed ? 1 : 0);
        appendField(sb, KEY_MAX_LENGTH, cmd.getMenuItem().getDigitsAllowed());
        NumberFormat fmt = NumberFormat.getInstance();
        fmt.setGroupingUsed(false);
        fmt.setMinimumFractionDigits(decimalPlaces);
        fmt.setMaximumFractionDigits(decimalPlaces);
        appendField(sb, KEY_CURRENT_VAL, fmt.format(cmd.getCurrentValue()));
    }

    private void writeScrollBoot(StringBuilder sb, MenuScrollChoiceBootCommand cmd) {
        writeCommonBootFields(sb, cmd);
        appendField(sb, KEY_WIDTH_FIELD, cmd.getMenuItem().getItemWidth());
        appendField(sb, KEY_NO_OF_CHOICES, cmd.getMenuItem().getNumEntries());
        appendField(sb, KEY_CURRENT_VAL, cmd.getCurrentValue().toString());
    }

    private void writeRgbBoot(StringBuilder sb, MenuRgb32BootCommand cmd) {
        writeCommonBootFields(sb, cmd);
        appendField(sb, KEY_ALPHA_FIELD, cmd.getMenuItem().isIncludeAlphaChannel() ? 1 : 0);
        appendField(sb, KEY_CURRENT_VAL, cmd.getCurrentValue().toString());
    }

    private void writeAnalogItem(StringBuilder sb, MenuAnalogBootCommand cmd) {
        writeCommonBootFields(sb, cmd);
        appendField(sb, KEY_ANALOG_OFFSET_FIELD, cmd.getMenuItem().getOffset());
        appendField(sb, KEY_ANALOG_DIVISOR_FIELD, cmd.getMenuItem().getDivisor());
        appendField(sb, KEY_ANALOG_MAX_FIELD, cmd.getMenuItem().getMaxValue());
        appendField(sb, KEY_ANALOG_STEP_FIELD, cmd.getMenuItem().getStep());
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

    private void writeCommonBootFields(StringBuilder sb, BootItemMenuCommand<?, ?> cmd) {
        appendField(sb, KEY_PARENT_ID_FIELD, cmd.getSubMenuId());
        appendField(sb, KEY_ID_FIELD, cmd.getMenuItem().getId());
        appendField(sb, KEY_EEPROM_FIELD, cmd.getMenuItem().getEepromAddress());
        appendField(sb, KEY_NAME_FIELD, cmd.getMenuItem().getName());
        appendField(sb, KEY_READONLY_FIELD, cmd.getMenuItem().isReadOnly() ? 1 : 0);
        appendField(sb, KEY_VISIBLE_FIELD, cmd.getMenuItem().isVisible() ? 1 : 0);
    }

    private void writeRuntimeListBootItem(StringBuilder sb, MenuRuntimeListBootCommand cmd) {
        writeCommonBootFields(sb, cmd);
        appendChoices(sb, cmd.getCurrentValue());

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
        if(entries == null) {
            appendField(sb, KEY_NO_OF_CHOICES, 0);
        }
        else {
            appendField(sb, KEY_NO_OF_CHOICES, entries.size());
            for (int i = 0; i < entries.size(); ++i) {
                appendField(sb, KEY_PREPEND_CHOICE + (char) ('A' + i), entries.get(i));
            }
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

    public static void appendField(StringBuilder sb, String key, Object value) {
        if(value instanceof String) {
            String val = (String) value;
            if(val.indexOf('|') != -1) {
                val = val.replace("|", "\\|");
            }
            if(val.indexOf('=') != -1) {
                val = val.replace("=", "\\=");
            }
            value = val;
        }
        sb.append(key);
        sb.append('=');
        sb.append(value);
        sb.append('|');
    }

}
