/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.protocol;

import com.thecoderscorner.menu.domain.DomainFixtures;
import com.thecoderscorner.menu.domain.EditItemType;
import com.thecoderscorner.menu.remote.commands.*;
import com.thecoderscorner.menu.remote.commands.MenuChangeCommand.ChangeType;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.thecoderscorner.menu.domain.BooleanMenuItem.BooleanNaming;
import static com.thecoderscorner.menu.remote.commands.CommandFactory.*;
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
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=NJ|NM=IoTdevice|UU=07cd8bc6-734d-43da-84e7-6084990becfc|VE=1223|PF=1|~"));
        assertTrue(cmd instanceof MenuJoinCommand);
        MenuJoinCommand join = (MenuJoinCommand) cmd;
        assertEquals("07cd8bc6-734d-43da-84e7-6084990becfc", join.getAppUuid().toString());
        assertEquals("IoTdevice", join.getMyName());
        assertEquals(1223, join.getApiVersion());
        assertEquals(ApiPlatform.JAVA_API, join.getPlatform());
        assertEquals(MenuCommandType.JOIN, join.getCommandType());
    }

    @Test
    public void testReceiveDialogCommand() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=DM|MO=S|HF=Hello|BU=Buffer|B1=0|B2=4|~"));
        assertTrue(cmd instanceof MenuDialogCommand);
        MenuDialogCommand dlg = (MenuDialogCommand) cmd;
        assertEquals(DialogMode.SHOW, dlg.getDialogMode());
        assertEquals("Hello", dlg.getHeader());
        assertEquals("Buffer", dlg.getBuffer());
        assertEquals(MenuButtonType.NONE, dlg.getButton1());
        assertEquals(MenuButtonType.CLOSE, dlg.getButton2());
    }

    @Test
    public void testReceiveHeartbeatCommand() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=HB|~"));
        assertTrue(cmd instanceof MenuHeartbeatCommand);
        MenuHeartbeatCommand hb = (MenuHeartbeatCommand) cmd;
        assertEquals(MenuCommandType.HEARTBEAT, hb.getCommandType());
    }

    @Test
    public void testReceiveBootstrap() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=BS|BT=START|~"));
        checkBootstrapFields(cmd,MenuBootstrapCommand.BootType.START);

        cmd = protocol.fromChannel(toBuffer("MT=BS|BT=END|~"));
        checkBootstrapFields(cmd,MenuBootstrapCommand.BootType.END);
    }

    private void checkBootstrapFields(MenuCommand cmd, MenuBootstrapCommand.BootType bootTy) {
        assertTrue(cmd instanceof MenuBootstrapCommand);
        MenuBootstrapCommand bs = (MenuBootstrapCommand) cmd;
        assertEquals(bootTy, bs.getBootType());
        assertEquals(MenuCommandType.BOOTSTRAP, bs.getCommandType());
    }

    @Test
    public void testReceiveAnalogItem() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=BA|PI=321|ID=1|RO=1|AM=255|AO=-180|AD=2|AU=dB|NM=Volume|VC=22|~"));
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
    }

    @Test
    public void testReceiveRemoteBootCommand() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=BR|PI=2|RO=1|NM=menuName|ID=1|RN=1|VC=No Link|~"));
        assertEquals(MenuCommandType.REMOTE_BOOT_ITEM,  cmd.getCommandType());
        MenuRemoteBootCommand remoteCmd = (MenuRemoteBootCommand) cmd;
        assertEquals("No Link", remoteCmd.getCurrentValue());
        assertEquals(1, remoteCmd.getMenuItem().getRemoteNum());
        assertEquals("menuName", remoteCmd.getMenuItem().getName());
        assertEquals(1, remoteCmd.getMenuItem().getId());
        assertEquals(2, remoteCmd.getSubMenuId());
        assertTrue(remoteCmd.getMenuItem().isReadOnly());
    }

    @Test
    public void testReceiveFloatBootCommand() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=BF|PI=2|RO=1|NM=menuName|ID=1|FD=5|VC=12.3456|~"));
        assertEquals(MenuCommandType.FLOAT_BOOT_ITEM,  cmd.getCommandType());
        MenuFloatBootCommand floatCmd = (MenuFloatBootCommand) cmd;
        assertEquals((float)12.3456, floatCmd.getCurrentValue(), 0.00001);
        assertEquals(5, floatCmd.getMenuItem().getNumDecimalPlaces());
        assertEquals("menuName", floatCmd.getMenuItem().getName());
        assertEquals(1, floatCmd.getMenuItem().getId());
        assertEquals(2, floatCmd.getSubMenuId());
        assertTrue(floatCmd.getMenuItem().isReadOnly());
    }

    @Test
    public void testReceiveRuntimeListBootCommand() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=BL|PI=2|RO=1|NM=runList|ID=1|NC=3|CA=abc|CB=def|CC=ghi|~"));
        assertEquals(MenuCommandType.RUNTIME_LIST_BOOT,  cmd.getCommandType());
        MenuRuntimeListBootCommand ipCmd = (MenuRuntimeListBootCommand) cmd;
        assertEquals("runList", ipCmd.getMenuItem().getName());
        assertEquals(1, ipCmd.getMenuItem().getId());
        assertEquals(2, ipCmd.getSubMenuId());
        assertEquals(3, ipCmd.getCurrentValue().size());
        assertEquals("abc", ipCmd.getCurrentValue().get(0));
        assertEquals("def", ipCmd.getCurrentValue().get(1));
        assertEquals("ghi", ipCmd.getCurrentValue().get(2));
        assertTrue(ipCmd.getMenuItem().isReadOnly());
    }

    @Test
    public void testReceiveTextBootCommand() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=BT|PI=2|RO=0|NM=menuName|ID=1|ML=10|ET=1|VC=12345678|~"));
        assertEquals(MenuCommandType.TEXT_BOOT_ITEM,  cmd.getCommandType());
        MenuTextBootCommand textCmd = (MenuTextBootCommand) cmd;
        assertEquals("12345678", textCmd.getCurrentValue());
        assertEquals(10, textCmd.getMenuItem().getTextLength());
        assertEquals("menuName", textCmd.getMenuItem().getName());
        assertEquals(EditItemType.IP_ADDRESS, textCmd.getMenuItem().getItemType());
        assertEquals(1, textCmd.getMenuItem().getId());
        assertEquals(2, textCmd.getSubMenuId());
        assertFalse(textCmd.getMenuItem().isReadOnly());
    }

    @Test
    public void testReceiveTextBootCommandInvalidEditMode() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=BT|PI=2|RO=0|NM=menuName|ID=1|ML=10|ET=99999|VC=12345678|~"));
        assertEquals(MenuCommandType.TEXT_BOOT_ITEM,  cmd.getCommandType());
        MenuTextBootCommand textCmd = (MenuTextBootCommand) cmd;
        // the edit mode in the message was corrupt, should be set to plain text by default.
        assertEquals(EditItemType.PLAIN_TEXT, textCmd.getMenuItem().getItemType());
    }

    @Test
    public void testReceiveEnumItem() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=BE|PI=42|RO=1|ID=21|NM=Choices|NC=3|CA=Choice1|CB=Choice2|CC=Choice3|VC=2|~"));
        assertTrue(cmd instanceof MenuEnumBootCommand);
        MenuEnumBootCommand enumItem = (MenuEnumBootCommand) cmd;
        assertEquals(21, enumItem.getMenuItem().getId());
        assertEquals("Choices", enumItem.getMenuItem().getName());
        assertEquals(42, enumItem.getSubMenuId());
        assertThat(enumItem.getMenuItem().getEnumEntries(), is(Arrays.asList("Choice1", "Choice2", "Choice3")));
        assertTrue(enumItem.getMenuItem().isReadOnly());
    }

    @Test
    public void testReceiveSubMenuItem() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=BM|RO=0|PI=0|ID=1|NM=SubMenu|~"));
        assertTrue(cmd instanceof MenuSubBootCommand);
        MenuSubBootCommand subMenu = (MenuSubBootCommand) cmd;
        assertEquals(1, subMenu.getMenuItem().getId());
        assertEquals("SubMenu", subMenu.getMenuItem().getName());
        assertEquals(0, subMenu.getSubMenuId());
    }

    @Test
    public void testReceiveActionMenuItem() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=BC|RO=0|PI=0|ID=1|NM=Action|~"));
        assertTrue(cmd instanceof MenuActionBootCommand);
        MenuActionBootCommand actMenu = (MenuActionBootCommand) cmd;
        assertEquals(1, actMenu.getMenuItem().getId());
        assertEquals("Action", actMenu.getMenuItem().getName());
        assertEquals(0, actMenu.getSubMenuId());
    }

    @Test
    public void testReceiveBooleanMenuItem() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=BB|PI=0|RO=1|ID=1|BN=1|NM=BoolItem|VC=1|~"));
        checkBooleanCmdFields(cmd, true, BooleanNaming.ON_OFF);
        cmd = protocol.fromChannel(toBuffer("MT=BB|PI=0|RO=1|ID=1|BN=0|NM=BoolItem|VC=0|~"));
        checkBooleanCmdFields(cmd, false, BooleanNaming.TRUE_FALSE);
        cmd = protocol.fromChannel(toBuffer("MT=BB|PI=0|ID=1|RO=1|BN=2|NM=BoolItem|VC=0|~"));
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
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=PR|NM=someUI|UU=575d327e-fe76-4e68-b0b8-45eea154a126|~"));
        assertTrue(cmd instanceof MenuPairingCommand);
        MenuPairingCommand pairing = (MenuPairingCommand) cmd;
        assertEquals("someUI", pairing.getName());
        assertEquals("575d327e-fe76-4e68-b0b8-45eea154a126", pairing.getUuid().toString());
    }

    @Test
    public void testReceiveAcknowledgementCases() throws IOException {
        // success
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=AK|IC=FDE05CAD|ST=0"));
        checkAckFields(cmd, AckStatus.SUCCESS);
        assertFalse(AckStatus.SUCCESS.isError());

        // warning.
        cmd = protocol.fromChannel(toBuffer("MT=AK|IC=FDE05CAD|ST=-1"));
        checkAckFields(cmd, AckStatus.VALUE_RANGE_WARNING);
        assertFalse(AckStatus.VALUE_RANGE_WARNING.isError());

        // error.
        cmd = protocol.fromChannel(toBuffer("MT=AK|IC=FDE05CAD|ST=1"));
        checkAckFields(cmd, AckStatus.ID_NOT_FOUND);
        assertTrue(AckStatus.ID_NOT_FOUND.isError());

        // bad login id.
        cmd = protocol.fromChannel(toBuffer("MT=AK|IC=FDE05CAD|ST=2"));
        checkAckFields(cmd, AckStatus.INVALID_CREDENTIALS);
        assertTrue(AckStatus.INVALID_CREDENTIALS.isError());

        // unknown error.
        cmd = protocol.fromChannel(toBuffer("MT=AK|IC=FDE05CAD|ST=222"));
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
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=VC|IC=CA039424|ID=22|TC=0|VC=1|~"));
        verifyChangeFields(cmd, ChangeType.DELTA, 1, "ca039424");
    }

    @Test
    public void testReceiveAbsoluteChange() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=VC|IC=ca039424|ID=22|TC=1|VC=-10000|~"));
        verifyChangeFields(cmd, ChangeType.ABSOLUTE, -10000, "ca039424");
    }

    @Test
    public void testReceiveListChange() throws IOException {
        fail();
    }

    private void verifyChangeFields(MenuCommand cmd, ChangeType chType, int value, String correlationId) {
        assertTrue(cmd instanceof MenuChangeCommand);
        MenuChangeCommand chg = (MenuChangeCommand) cmd;

        assertEquals(chType, chg.getChangeType());
        assertEquals(value, Integer.parseInt(chg.getValue()));
        assertEquals(22, chg.getMenuItemId());
        assertEquals(MenuCommandType.CHANGE_INT_FIELD, chg.getCommandType());
        assertEquals(correlationId, chg.getCorrelationId().toString());
    }


    @Test(expected = IOException.class)
    public void testReceivingUnknownMessageThrowsException() throws IOException {
        protocol.fromChannel(toBuffer("MT=???|~"));
        // should throw exception.
    }

    @Test
    public void testWritingHeartbeat() {
        protocol.toChannel(bb, newHeartbeatCommand(10000));
        testBufferAgainstExpected("MT=HB|HI=10000|~");
    }

    @Test
    public void testWritingJoin() {
        var uuid = UUID.fromString("07cd8bc6-734d-43da-84e7-6084990becfc");
        protocol.toChannel(bb, new MenuJoinCommand(uuid,"dave", ApiPlatform.ARDUINO, 101));
        testBufferAgainstExpected("MT=NJ|NM=dave|UU=07cd8bc6-734d-43da-84e7-6084990becfc|VE=101|PF=0|~");
    }

    @Test
    public void testWritingBootstrap() {
        protocol.toChannel(bb, new MenuBootstrapCommand(MenuBootstrapCommand.BootType.START));
        testBufferAgainstExpected("MT=BS|BT=START|~");
    }

    @Test
    public void testWritingAnalogItem() {
        protocol.toChannel(bb, new MenuAnalogBootCommand(321,
                DomainFixtures.anAnalogItem("Test", 123),
                25));
        testBufferAgainstExpected("MT=BA|PI=321|ID=123|IE=104|NM=Test|RO=0|AO=102|AD=2|AM=255|AU=dB|VC=25|~");
    }

    @Test
    public void testWritingEnumItem() {
        protocol.toChannel(bb, new MenuEnumBootCommand(22,
                DomainFixtures.anEnumItem("Test", 2),
                1));
        testBufferAgainstExpected("MT=BE|PI=22|ID=2|IE=101|NM=Test|RO=0|VC=1|NC=2|CA=Item1|CB=Item2|~");
    }

    @Test
    public void testWritingSubMenu() {
        protocol.toChannel(bb, new MenuSubBootCommand(22,
                DomainFixtures.aSubMenu("Sub", 1),
                false));
        testBufferAgainstExpected("MT=BM|PI=22|ID=1|IE=102|NM=Sub|RO=0|VC=0|~");
    }

    @Test
    public void testWritingBooleanItem() {
        protocol.toChannel(bb, new MenuBooleanBootCommand(22,
                DomainFixtures.aBooleanMenu("Bool", 1, BooleanNaming.TRUE_FALSE),
                false));
        testBufferAgainstExpected("MT=BB|PI=22|ID=1|IE=102|NM=Bool|RO=0|BN=0|VC=0|~");
    }

    @Test
    public void testWritingFloatItem() {
        protocol.toChannel(bb, new MenuFloatBootCommand(22,
                DomainFixtures.aFloatMenu("FloatMenu", 1), (float)12.0));
        testBufferAgainstExpected("MT=BF|PI=22|ID=1|IE=105|NM=FloatMenu|RO=0|FD=3|VC=12.0|~");
    }

    @Test
    public void testWritingRemoteItem() {
        protocol.toChannel(bb, new MenuRemoteBootCommand(22,
                DomainFixtures.aRemoteMenuItem("Remo", 1), "ABC"));
        testBufferAgainstExpected("MT=BR|PI=22|ID=1|IE=110|NM=Remo|RO=0|RN=2|VC=ABC|~");
    }

    @Test
    public void testWritingRuntimeListItem() {
        protocol.toChannel(bb, new MenuRuntimeListBootCommand(22,
                DomainFixtures.aRuntimeListMenu("List", 1, 2),
                List.of("ABC", "DEF")));
        testBufferAgainstExpected("MT=BL|PI=22|ID=1|IE=88|NM=List|RO=0|NC=2|CA=ABC|CB=DEF|~");
    }

    @Test
    public void testWritingActionItem() {
        protocol.toChannel(bb, new MenuActionBootCommand(22,
                DomainFixtures.anActionMenu("Action", 1), false));
        testBufferAgainstExpected("MT=BC|PI=22|ID=1|IE=20|NM=Action|RO=0|VC=|~");
    }

    @Test
    public void testWritingTextItem() {
        protocol.toChannel(bb, new MenuTextBootCommand(22,
                DomainFixtures.aTextMenu("TextItem", 1), "ABC"));
        testBufferAgainstExpected("MT=BT|PI=22|ID=1|IE=101|NM=TextItem|RO=0|ML=10|ET=0|VC=ABC|~");
    }

    @Test
    public void testWritingBooleanItemOnOff() {
        protocol.toChannel(bb, new MenuBooleanBootCommand(22,
                DomainFixtures.aBooleanMenu("Bool", 1, BooleanNaming.ON_OFF),
                true));
        testBufferAgainstExpected("MT=BB|PI=22|ID=1|IE=102|NM=Bool|RO=0|BN=1|VC=1|~");
    }

    @Test
    public void testWritingBooleanItemYesNo() {
        protocol.toChannel(bb, new MenuBooleanBootCommand(22,
                DomainFixtures.aBooleanMenu("Bool", 1, BooleanNaming.YES_NO),
                true));
        testBufferAgainstExpected("MT=BB|PI=22|ID=1|IE=102|NM=Bool|RO=0|BN=2|VC=1|~");
    }

    @Test
    public void testWritingAnAbsoluteChange() {
        protocol.toChannel(bb, newAbsoluteMenuChangeCommand(new CorrelationId("00134654"), 2, 1));
        testBufferAgainstExpected("MT=VC|IC=00134654|ID=2|TC=1|VC=1|~");
    }

    @Test
    public void testWritingADeltaChange() {
        protocol.toChannel(bb, newDeltaChangeCommand(new CorrelationId("C04239"), 2, 1));
        testBufferAgainstExpected("MT=VC|IC=00c04239|ID=2|TC=0|VC=1|~");
    }

    @Test
    public void testWritingListChange() {
        fail();
    }

    @Test
    public void testWritingAck() {
        protocol.toChannel(bb, newAcknowledgementCommand(new CorrelationId("1234567a"), AckStatus.ID_NOT_FOUND));
        testBufferAgainstExpected("MT=AK|IC=1234567a|ST=1|~");
    }

    @Test
    public void testWritingPairing() {
        protocol.toChannel(bb, newPairingCommand("pairingtest", UUID.fromString("575d327e-fe76-4e68-b0b8-45eea154a126")));
        testBufferAgainstExpected("MT=PR|NM=pairingtest|UU=575d327e-fe76-4e68-b0b8-45eea154a126|~");
    }

    @Test
    public void testWritingDialogUpdate() throws IOException {

        protocol.toChannel(bb, newDialogCommand(DialogMode.SHOW, "Hello", "Buffer", MenuButtonType.NONE,
                MenuButtonType.CLOSE, CorrelationId.EMPTY_CORRELATION));
        testBufferAgainstExpected("MT=DM|MO=S|HF=Hello|BU=Buffer|B1=0|B2=4|IC=00000000|~");
    }


    private void testBufferAgainstExpected(String s2) {
        bb.flip();
        String s = new String(msgData, 0, bb.limit());
        assertEquals(s2, s);
    }


    private ByteBuffer toBuffer(String s) {
        return ByteBuffer.wrap(s.getBytes());
    }
}