/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.protocol;

import com.thecoderscorner.menu.domain.DomainFixtures;
import com.thecoderscorner.menu.domain.EditItemType;
import com.thecoderscorner.menu.domain.FloatMenuItem;
import com.thecoderscorner.menu.domain.FloatMenuItemBuilder;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.remote.commands.*;
import com.thecoderscorner.menu.remote.commands.MenuChangeCommand.ChangeType;
import com.thecoderscorner.menu.remote.commands.MenuHeartbeatCommand.HeartbeatMode;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.thecoderscorner.menu.domain.BooleanMenuItem.BooleanNaming;
import static com.thecoderscorner.menu.remote.commands.CommandFactory.*;
import static com.thecoderscorner.menu.remote.commands.MenuCommandType.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class TagValMenuCommandProtocolTest {
    private TagValMenuCommandProtocol protocol = new TagValMenuCommandProtocol();
    private byte[] msgData;
    private ByteBuffer bb;

    @Before
    public void setUp() {
        msgData = new byte[2048];
        bb = ByteBuffer.wrap(msgData);
    }

    @Test
    public void testReceiveJoinCommand() throws IOException {
        var cmd = protocol.fromChannel(toBuffer(JOIN, "NM=IoTdevice|UU=07cd8bc6-734d-43da-84e7-6084990becfc|VE=1223|PF=1|\u0002"));
        assertTrue(cmd instanceof MenuJoinCommand);
        MenuJoinCommand join = (MenuJoinCommand) cmd;
        assertEquals("07cd8bc6-734d-43da-84e7-6084990becfc", join.getAppUuid().toString());
        assertEquals("IoTdevice", join.getMyName());
        assertEquals(1223, join.getApiVersion());
        assertEquals(ApiPlatform.JAVA_API, join.getPlatform());
        assertEquals(JOIN, join.getCommandType());
    }

    @Test
    public void testReceiveDialogCommand() throws IOException {
        var cmd = protocol.fromChannel(toBuffer(DIALOG_UPDATE, "MO=S|HF=Hello\\||BU=Buffer\\=|B1=0|B2=4|\u0002"));
        assertTrue(cmd instanceof MenuDialogCommand);
        MenuDialogCommand dlg = (MenuDialogCommand) cmd;
        assertEquals(DialogMode.SHOW, dlg.getDialogMode());
        assertEquals("Hello|", dlg.getHeader());
        assertEquals("Buffer=", dlg.getBuffer());
        assertEquals(MenuButtonType.OK, dlg.getButton1());
        assertEquals(MenuButtonType.NONE, dlg.getButton2());
    }

    @Test
    public void testReceiveHeartbeatCommand() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer(HEARTBEAT, "\u0002"));
        assertTrue(cmd instanceof MenuHeartbeatCommand);
        MenuHeartbeatCommand hb = (MenuHeartbeatCommand) cmd;
        assertEquals(HEARTBEAT, hb.getCommandType());
    }

    @Test
    public void testReceiveBootstrap() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer(BOOTSTRAP, "BT=START|\u0002"));
        checkBootstrapFields(cmd,MenuBootstrapCommand.BootType.START);

        cmd = protocol.fromChannel(toBuffer(BOOTSTRAP,"BT=END|\u0002"));
        checkBootstrapFields(cmd,MenuBootstrapCommand.BootType.END);
    }

    private void checkBootstrapFields(MenuCommand cmd, MenuBootstrapCommand.BootType bootTy) {
        assertTrue(cmd instanceof MenuBootstrapCommand);
        MenuBootstrapCommand bs = (MenuBootstrapCommand) cmd;
        assertEquals(bootTy, bs.getBootType());
        assertEquals(BOOTSTRAP, bs.getCommandType());
    }

    @Test
    public void testReceiveAnalogItem() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer(ANALOG_BOOT_ITEM,"PI=321|ID=1|RO=1|VI=1|AM=255|AO=-180|AD=2|AU=dB|NM=Volume|VC=22|\u0002"));
        assertTrue(cmd instanceof MenuAnalogBootCommand);
        MenuAnalogBootCommand analog = (MenuAnalogBootCommand) cmd;
        assertEquals(-180, analog.getMenuItem().getOffset());
        assertEquals(255, analog.getMenuItem().getMaxValue());
        assertEquals(2, analog.getMenuItem().getDivisor());
        assertEquals("dB", analog.getMenuItem().getUnitName());
        assertEquals(1, analog.getMenuItem().getId());
        assertEquals("Volume", analog.getMenuItem().getName());
        assertEquals(321, analog.getSubMenuId());
        assertTrue(analog.getMenuItem().isReadOnly());
        assertTrue(analog.getMenuItem().isVisible());
    }

    @Test
    public void testReceiveFloatBootCommand() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer(FLOAT_BOOT_ITEM, "PI=2|RO=1|VI=0|NM=menuName|ID=1|FD=5|VC=12.3456|\u0002"));
        assertEquals(FLOAT_BOOT_ITEM,  cmd.getCommandType());
        MenuFloatBootCommand floatCmd = (MenuFloatBootCommand) cmd;
        assertEquals((float)12.3456, floatCmd.getCurrentValue(), 0.00001);
        assertEquals(5, floatCmd.getMenuItem().getNumDecimalPlaces());
        assertEquals("menuName", floatCmd.getMenuItem().getName());
        assertEquals(1, floatCmd.getMenuItem().getId());
        assertEquals(2, floatCmd.getSubMenuId());
        assertTrue(floatCmd.getMenuItem().isReadOnly());
        assertFalse(floatCmd.getMenuItem().isVisible());
    }

    @Test
    public void testReceiveRuntimeListBootCommand() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer(RUNTIME_LIST_BOOT, "PI=2|RO=1|VI=1|NM=runList|ID=1|NC=3|CA=abc|CB=def|CC=ghi|\u0002"));
        assertEquals(RUNTIME_LIST_BOOT,  cmd.getCommandType());
        MenuRuntimeListBootCommand ipCmd = (MenuRuntimeListBootCommand) cmd;
        assertEquals("runList", ipCmd.getMenuItem().getName());
        assertEquals(1, ipCmd.getMenuItem().getId());
        assertEquals(2, ipCmd.getSubMenuId());
        assertEquals(3, ipCmd.getCurrentValue().size());
        assertEquals("abc", ipCmd.getCurrentValue().get(0));
        assertEquals("def", ipCmd.getCurrentValue().get(1));
        assertEquals("ghi", ipCmd.getCurrentValue().get(2));
        assertTrue(ipCmd.getMenuItem().isReadOnly());
        assertTrue(ipCmd.getMenuItem().isVisible());
    }

    @Test
    public void testReceiveTextBootCommand() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer(TEXT_BOOT_ITEM, "PI=2|RO=0|VI=0|NM=menuName|ID=1|ML=10|EM=1|VC=12345678|\u0002"));
        assertEquals(TEXT_BOOT_ITEM,  cmd.getCommandType());
        MenuTextBootCommand textCmd = (MenuTextBootCommand) cmd;
        assertEquals("12345678", textCmd.getCurrentValue());
        assertEquals(10, textCmd.getMenuItem().getTextLength());
        assertEquals("menuName", textCmd.getMenuItem().getName());
        assertEquals(EditItemType.IP_ADDRESS, textCmd.getMenuItem().getItemType());
        assertEquals(1, textCmd.getMenuItem().getId());
        assertEquals(2, textCmd.getSubMenuId());
        assertFalse(textCmd.getMenuItem().isReadOnly());
        assertFalse(textCmd.getMenuItem().isReadOnly());
    }

    @Test
    public void testReceiveTextBootCommandInvalidEditMode() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer(TEXT_BOOT_ITEM, "PI=2|RO=0|NM=menuName|ID=1|ML=10|EM=99999|VC=12345678|\u0002"));
        assertEquals(TEXT_BOOT_ITEM,  cmd.getCommandType());
        MenuTextBootCommand textCmd = (MenuTextBootCommand) cmd;
        // the edit mode in the message was corrupt, should be set to plain text by default.
        assertEquals(EditItemType.PLAIN_TEXT, textCmd.getMenuItem().getItemType());
    }

    @Test
    public void testReceiveEnumItem() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer(ENUM_BOOT_ITEM,
                "PI=42|RO=1|ID=21|NM=Choices|NC=3|CA=Choice1|CB=Choice2|CC=Choice3|VC=2|\u0002"));
        assertTrue(cmd instanceof MenuEnumBootCommand);
        MenuEnumBootCommand enumItem = (MenuEnumBootCommand) cmd;
        assertEquals(21, enumItem.getMenuItem().getId());
        assertEquals("Choices", enumItem.getMenuItem().getName());
        assertEquals(42, enumItem.getSubMenuId());
        assertThat(enumItem.getMenuItem().getEnumEntries(), is(Arrays.asList("Choice1", "Choice2", "Choice3")));
        assertTrue(enumItem.getMenuItem().isReadOnly());
    }

    @Test
    public void testReceiveLargeNumberNegativeDefault() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer(LARGE_NUM_BOOT_ITEM, "PI=10|ID=111|IE=64|NM=largeNum|RO=0|FD=4|ML=12|VC=11.1[2]34|\u0002"));
        assertTrue(cmd instanceof MenuLargeNumBootCommand);
        MenuLargeNumBootCommand numMenu = (MenuLargeNumBootCommand) cmd;
        assertEquals(111, numMenu.getMenuItem().getId());
        assertEquals("largeNum", numMenu.getMenuItem().getName());
        assertEquals(10, numMenu.getSubMenuId());
        assertEquals(4, numMenu.getMenuItem().getDecimalPlaces());
        assertEquals(12, numMenu.getMenuItem().getDigitsAllowed());
        assertEquals(11.1234, numMenu.getCurrentValue().doubleValue(), 0.00001);
        assertTrue(numMenu.getMenuItem().isNegativeAllowed());
    }

    @Test
    public void testReceiveLargeNumber() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer(LARGE_NUM_BOOT_ITEM, "PI=10|ID=111|IE=64|NM=largeNum|RO=0|FD=4|NA=0|ML=12|VC=11.1[2]34|\u0002"));
        assertTrue(cmd instanceof MenuLargeNumBootCommand);
        MenuLargeNumBootCommand numMenu = (MenuLargeNumBootCommand) cmd;
        assertEquals(111, numMenu.getMenuItem().getId());
        assertEquals("largeNum", numMenu.getMenuItem().getName());
        assertEquals(10, numMenu.getSubMenuId());
        assertEquals(4, numMenu.getMenuItem().getDecimalPlaces());
        assertEquals(12, numMenu.getMenuItem().getDigitsAllowed());
        assertEquals(11.1234, numMenu.getCurrentValue().doubleValue(), 0.00001);
        assertFalse(numMenu.getMenuItem().isNegativeAllowed());
    }

    @Test
    public void testReceiveSubMenuItem() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer(SUBMENU_BOOT_ITEM, "RO=0|PI=0|ID=1|NM=SubMenu|\u0002"));
        assertTrue(cmd instanceof MenuSubBootCommand);
        MenuSubBootCommand subMenu = (MenuSubBootCommand) cmd;
        assertEquals(1, subMenu.getMenuItem().getId());
        assertEquals("SubMenu", subMenu.getMenuItem().getName());
        assertEquals(0, subMenu.getSubMenuId());
    }

    @Test
    public void testReceiveRgb32Item() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer(BOOT_RGB_COLOR, "RO=0|PI=0|ID=1|NM=rgb|RA=1|VC=#22334455|\u0002"));
        assertTrue(cmd instanceof MenuRgb32BootCommand);
        MenuRgb32BootCommand rgb = (MenuRgb32BootCommand) cmd;
        assertEquals(1, rgb.getMenuItem().getId());
        assertEquals("rgb", rgb.getMenuItem().getName());
        assertEquals(0, rgb.getSubMenuId());
        assertTrue(rgb.getMenuItem().isIncludeAlphaChannel());

        PortableColor pc = new PortableColor("#22334455");
        assertEquals(pc, rgb.getCurrentValue());
    }

    @Test
    public void testReceiveScrollChoice() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer(BOOT_SCROLL_CHOICE, "RO=0|PI=0|ID=1|NM=scroll|WI=10|NC=20|VC=1-hello|\u0002"));
        assertTrue(cmd instanceof MenuScrollChoiceBootCommand);
        MenuScrollChoiceBootCommand sc = (MenuScrollChoiceBootCommand) cmd;
        assertEquals(1, sc.getMenuItem().getId());
        assertEquals("scroll", sc.getMenuItem().getName());
        assertEquals(0, sc.getSubMenuId());
        assertEquals(10, sc.getMenuItem().getItemWidth());
        assertEquals(20, sc.getMenuItem().getNumEntries());
        assertEquals(1, sc.getCurrentValue().getPosition());
        assertEquals("hello", sc.getCurrentValue().getValue());
    }

    @Test
    public void testReceiveActionMenuItem() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer(ACTION_BOOT_ITEM, "RO=0|PI=0|ID=1|NM=Action|\u0002"));
        assertTrue(cmd instanceof MenuActionBootCommand);
        MenuActionBootCommand actMenu = (MenuActionBootCommand) cmd;
        assertEquals(1, actMenu.getMenuItem().getId());
        assertEquals("Action", actMenu.getMenuItem().getName());
        assertEquals(0, actMenu.getSubMenuId());
    }

    @Test
    public void testReceiveBooleanMenuItem() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer(BOOLEAN_BOOT_ITEM, "PI=0|RO=1|VI=1|ID=1|BN=1|NM=BoolItem|VC=1|\u0002"));
        checkBooleanCmdFields(cmd, true, BooleanNaming.ON_OFF);
        cmd = protocol.fromChannel(toBuffer(BOOLEAN_BOOT_ITEM, "PI=0|RO=1|VI=1|ID=1|BN=0|NM=BoolItem|VC=0|\u0002"));
        checkBooleanCmdFields(cmd, false, BooleanNaming.TRUE_FALSE);
        cmd = protocol.fromChannel(toBuffer(BOOLEAN_BOOT_ITEM, "PI=0|ID=1|RO=1|VI=1|BN=2|NM=BoolItem|VC=0|\u0002"));
        checkBooleanCmdFields(cmd, false, BooleanNaming.YES_NO);
    }

    private void checkBooleanCmdFields(MenuCommand cmd, boolean current, BooleanNaming naming) {
        assertTrue(cmd instanceof MenuBooleanBootCommand);
        MenuBooleanBootCommand boolCmd = (MenuBooleanBootCommand) cmd;
        assertEquals(1, boolCmd.getMenuItem().getId());
        assertEquals("BoolItem", boolCmd.getMenuItem().getName());
        assertEquals(0, boolCmd.getSubMenuId());
        assertEquals(current, boolCmd.getCurrentValue());
        assertEquals(naming, boolCmd.getMenuItem().getNaming());
        assertTrue(boolCmd.getMenuItem().isReadOnly());
    }

    @Test
    public void testReceivePairing() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer(PAIRING_REQUEST, "NM=someUI|UU=575d327e-fe76-4e68-b0b8-45eea154a126|\u0002"));
        assertTrue(cmd instanceof MenuPairingCommand);
        MenuPairingCommand pairing = (MenuPairingCommand) cmd;
        assertEquals("someUI", pairing.getName());
        assertEquals("575d327e-fe76-4e68-b0b8-45eea154a126", pairing.getUuid().toString());
    }

    @Test
    public void testReceiveAcknowledgementCases() throws IOException {
        // success
        MenuCommand cmd = protocol.fromChannel(toBuffer(ACKNOWLEDGEMENT, "IC=FDE05CAD|ST=0"));
        checkAckFields(cmd, AckStatus.SUCCESS);
        assertFalse(AckStatus.SUCCESS.isError());

        // warning.
        cmd = protocol.fromChannel(toBuffer(ACKNOWLEDGEMENT, "IC=FDE05CAD|ST=-1"));
        checkAckFields(cmd, AckStatus.VALUE_RANGE_WARNING);
        assertFalse(AckStatus.VALUE_RANGE_WARNING.isError());

        // error.
        cmd = protocol.fromChannel(toBuffer(ACKNOWLEDGEMENT, "IC=FDE05CAD|ST=1"));
        checkAckFields(cmd, AckStatus.ID_NOT_FOUND);
        assertTrue(AckStatus.ID_NOT_FOUND.isError());

        // bad login id.
        cmd = protocol.fromChannel(toBuffer(ACKNOWLEDGEMENT, "IC=FDE05CAD|ST=2"));
        checkAckFields(cmd, AckStatus.INVALID_CREDENTIALS);
        assertTrue(AckStatus.INVALID_CREDENTIALS.isError());

        // unknown error.
        cmd = protocol.fromChannel(toBuffer(ACKNOWLEDGEMENT, "IC=FDE05CAD|ST=222"));
        checkAckFields(cmd, AckStatus.UNKNOWN_ERROR);
        assertTrue(AckStatus.UNKNOWN_ERROR.isError());
    }

    private void checkAckFields(MenuCommand cmd, AckStatus st) {
        assertTrue(cmd instanceof MenuAcknowledgementCommand);
        MenuAcknowledgementCommand ack = (MenuAcknowledgementCommand) cmd;
        assertEquals("fde05cad", ack.getCorrelationId().toString());
        assertEquals(st, ack.getAckStatus());
    }


    @Test
    public void testReceiveDeltaChange() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer(CHANGE_INT_FIELD, "IC=CA039424|ID=22|TC=0|VC=1|\u0002"));
        verifyChangeFields(cmd, ChangeType.DELTA, 1);
    }

    @Test
    public void testReceiveAbsoluteChange() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer(CHANGE_INT_FIELD,"IC=ca039424|ID=22|TC=1|VC=-10000|\u0002"));
        verifyChangeFields(cmd, ChangeType.ABSOLUTE, -10000);
    }

    @Test
    public void testReceiveListChange() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer(CHANGE_INT_FIELD,"IC=ca039424|ID=22|TC=2|NC=2|cA=R1|CA=123|cB=R2|CB=456|\u0002"));
        MenuChangeCommand chg = (MenuChangeCommand) cmd;

        assertEquals(CHANGE_INT_FIELD, chg.getCommandType());
        assertEquals("ca039424", chg.getCorrelationId().toString());
        assertEquals(22, chg.getMenuItemId());
        assertEquals(ChangeType.ABSOLUTE_LIST, chg.getChangeType());
        assertThat(chg.getValues(), containsInAnyOrder("R1\t123", "R2\t456"));
    }

    private void verifyChangeFields(MenuCommand cmd, ChangeType chType, int value) {
        assertTrue(cmd instanceof MenuChangeCommand);
        MenuChangeCommand chg = (MenuChangeCommand) cmd;

        assertEquals(chType, chg.getChangeType());
        assertEquals(value, Integer.parseInt(chg.getValue()));
        assertEquals(22, chg.getMenuItemId());
        assertEquals(CHANGE_INT_FIELD, chg.getCommandType());
        assertEquals("ca039424", chg.getCorrelationId().toString());
    }


    @Test(expected = IOException.class)
    public void testReceivingUnknownMessageThrowsException() throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(10);
        bb.put((byte) '?').put((byte) '?').put((byte) '~').flip();

        protocol.fromChannel(bb);
        // should throw exception.
    }

    @Test
    public void testWritingHeartbeat() {
        protocol.toChannel(bb, newHeartbeatCommand(10000, HeartbeatMode.NORMAL));
        testBufferAgainstExpected(HEARTBEAT, "HI=10000|HR=0|\u0002");
    }

    @Test
    public void testWritingJoin() {
        var uuid = UUID.fromString("07cd8bc6-734d-43da-84e7-6084990becfc");
        protocol.toChannel(bb, new MenuJoinCommand(uuid,"dave", ApiPlatform.ARDUINO, 101));
        testBufferAgainstExpected(JOIN, "NM=dave|UU=07cd8bc6-734d-43da-84e7-6084990becfc|VE=101|PF=0|\u0002");
    }

    @Test
    public void testWritingLargeIntegerBoot() {
        protocol.toChannel(bb, new MenuLargeNumBootCommand(10,DomainFixtures.aLargeNumber("largeNum", 111, 4, true), BigDecimal.ONE));
        testBufferAgainstExpected(LARGE_NUM_BOOT_ITEM, "PI=10|ID=111|IE=64|NM=largeNum|RO=0|VI=1|FD=4|NA=1|ML=12|VC=1.0000|\u0002");
    }

    @Test
    public void testWritingBootstrap() {
        protocol.toChannel(bb, new MenuBootstrapCommand(MenuBootstrapCommand.BootType.START));
        testBufferAgainstExpected(BOOTSTRAP, "BT=START|\u0002");
    }

    @Test
    public void testWritingAnalogItem() {
        protocol.toChannel(bb, new MenuAnalogBootCommand(321,
                DomainFixtures.anAnalogItem("Test", 123),
                25));
        testBufferAgainstExpected(ANALOG_BOOT_ITEM, "PI=321|ID=123|IE=104|NM=Test|RO=0|VI=1|AO=102|AD=2|AM=255|AU=dB|VC=25|\u0002");
    }

    @Test
    public void testWritingEnumItem() {
        protocol.toChannel(bb, new MenuEnumBootCommand(22,
                DomainFixtures.anEnumItem("Test", 2),
                1));
        testBufferAgainstExpected(ENUM_BOOT_ITEM, "PI=22|ID=2|IE=101|NM=Test|RO=0|VI=1|VC=1|NC=2|CA=Item1|CB=Item2|\u0002");
    }

    @Test
    public void testWritingSubMenu() {
        protocol.toChannel(bb, new MenuSubBootCommand(22,
                DomainFixtures.aSubMenu("Sub", 1),
                false));
        testBufferAgainstExpected(SUBMENU_BOOT_ITEM, "PI=22|ID=1|IE=102|NM=Sub|RO=0|VI=1|VC=0|\u0002");
    }

    @Test
    public void testWritingBooleanItem() {
        protocol.toChannel(bb, new MenuBooleanBootCommand(22,
                DomainFixtures.aBooleanMenu("Bool", 1, BooleanNaming.TRUE_FALSE),
                false));
        testBufferAgainstExpected(BOOLEAN_BOOT_ITEM, "PI=22|ID=1|IE=102|NM=Bool|RO=0|VI=1|BN=0|VC=0|\u0002");
    }

    @Test
    public void testWritingFloatItem() {
        FloatMenuItem floatItem = DomainFixtures.aFloatMenu("FloatMenu", 1);
        floatItem = new FloatMenuItemBuilder().withExisting(floatItem).withVisible(false).menuItem();
        protocol.toChannel(bb, new MenuFloatBootCommand(22,
                floatItem, (float)12.0));
        testBufferAgainstExpected(FLOAT_BOOT_ITEM,"PI=22|ID=1|IE=105|NM=FloatMenu|RO=0|VI=0|FD=3|VC=12.0|\u0002");
    }

    @Test
    public void testWritingRuntimeListItem() {
        protocol.toChannel(bb, new MenuRuntimeListBootCommand(22,
                DomainFixtures.aRuntimeListMenu("List", 1, 2),
                List.of("ABC", "DEF")));
        testBufferAgainstExpected(RUNTIME_LIST_BOOT, "PI=22|ID=1|IE=88|NM=List|RO=0|VI=1|NC=2|CA=ABC|CB=DEF|\u0002");
    }

    @Test
    public void testWritingActionItem() {
        protocol.toChannel(bb, new MenuActionBootCommand(22,
                DomainFixtures.anActionMenu("Action", 1), false));
        testBufferAgainstExpected(ACTION_BOOT_ITEM, "PI=22|ID=1|IE=20|NM=Action|RO=0|VI=1|VC=|\u0002");
    }

    @Test
    public void testWritingTextItem() {
        protocol.toChannel(bb, new MenuTextBootCommand(22,
                DomainFixtures.aTextMenu("TextItem", 1), "ABC"));
        testBufferAgainstExpected(TEXT_BOOT_ITEM, "PI=22|ID=1|IE=101|NM=TextItem|RO=0|VI=1|ML=10|EM=0|VC=ABC|\u0002");
    }

    @Test
    public void testWritingBooleanItemOnOff() {
        protocol.toChannel(bb, new MenuBooleanBootCommand(22,
                DomainFixtures.aBooleanMenu("Bool", 1, BooleanNaming.ON_OFF),
                true));
        testBufferAgainstExpected(BOOLEAN_BOOT_ITEM, "PI=22|ID=1|IE=102|NM=Bool|RO=0|VI=1|BN=1|VC=1|\u0002");
    }

    @Test
    public void testWritingBooleanItemYesNo() {
        protocol.toChannel(bb, new MenuBooleanBootCommand(22,
                DomainFixtures.aBooleanMenu("Bool", 1, BooleanNaming.YES_NO),
                true));
        testBufferAgainstExpected(BOOLEAN_BOOT_ITEM, "PI=22|ID=1|IE=102|NM=Bool|RO=0|VI=1|BN=2|VC=1|\u0002");
    }

    @Test
    public void testWritingAnAbsoluteChange() {
        protocol.toChannel(bb, newAbsoluteMenuChangeCommand(new CorrelationId("00134654"), 2, 1));
        testBufferAgainstExpected(CHANGE_INT_FIELD, "IC=00134654|ID=2|TC=1|VC=1|\u0002");
    }

    @Test
    public void testWritingADeltaChange() {
        protocol.toChannel(bb, newDeltaChangeCommand(new CorrelationId("C04239"), 2, 1));
        testBufferAgainstExpected(CHANGE_INT_FIELD,"IC=00c04239|ID=2|TC=0|VC=1|\u0002");
    }

    @Test
    public void testWritingListChange() {
        protocol.toChannel(bb, newAbsoluteListChangeCommand(new CorrelationId("C04239"), 2,
                List.of("123", "456")));
        testBufferAgainstExpected(CHANGE_INT_FIELD, "IC=00c04239|ID=2|TC=2|NC=2|CA=123|CB=456|\u0002");
    }

    @Test
    public void testWritingAck() {
        protocol.toChannel(bb, newAcknowledgementCommand(new CorrelationId("1234567a"), AckStatus.ID_NOT_FOUND));
        testBufferAgainstExpected(ACKNOWLEDGEMENT, "IC=1234567a|ST=1|\u0002");
    }

    @Test
    public void testWritingPairing() {
        protocol.toChannel(bb, newPairingCommand("pairingtest", UUID.fromString("575d327e-fe76-4e68-b0b8-45eea154a126")));
        testBufferAgainstExpected(PAIRING_REQUEST,"NM=pairingtest|UU=575d327e-fe76-4e68-b0b8-45eea154a126|\u0002");
    }

    @Test
    public void testWritingDialogUpdate() throws IOException {

        protocol.toChannel(bb, newDialogCommand(DialogMode.SHOW, "Hello", "Buffer", MenuButtonType.NONE,
                MenuButtonType.CLOSE, CorrelationId.EMPTY_CORRELATION));
        testBufferAgainstExpected(DIALOG_UPDATE, "MO=S|HF=Hello|BU=Buffer|B1=4|B2=3|IC=00000000|\u0002");
    }


    private void testBufferAgainstExpected(MenuCommandType expectedMsg, String expectedData) {
        bb.flip();

        // check the actual data is right
        String s = new String(msgData, 0, bb.limit());
        assertEquals(expectedData, s);
    }


    private ByteBuffer toBuffer(MenuCommandType type, String s) {
        ByteBuffer bb = ByteBuffer.allocate(s.length() + 10);
        return bb.put((byte) type.getHigh())
                .put((byte) type.getLow())
                .put(s.getBytes())
                .flip();
    }
}