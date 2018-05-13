/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.protocol;

import com.thecoderscorner.menu.domain.BooleanMenuItem;
import com.thecoderscorner.menu.domain.DomainFixtures;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.remote.commands.*;
import com.thecoderscorner.menu.remote.commands.MenuChangeCommand.ChangeType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.thecoderscorner.menu.domain.BooleanMenuItem.*;
import static com.thecoderscorner.menu.remote.commands.CommandFactory.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=NJ|NM=IoTdevice|VE=1223|PF=1|~"));
        assertTrue(cmd instanceof MenuJoinCommand);
        MenuJoinCommand join = (MenuJoinCommand) cmd;
        assertEquals("IoTdevice", join.getMyName());
        assertEquals(1223, join.getApiVersion());
        assertEquals(ApiPlatform.JAVA_API, join.getPlatform());
        assertEquals(MenuCommandType.JOIN, join.getCommandType());
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
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=BA|PI=321|ID=1|AM=255|AO=-180|AD=2|AU=dB|NM=Volume|VC=22|~"));
        assertTrue(cmd instanceof MenuAnalogBootCommand);
        MenuAnalogBootCommand analog = (MenuAnalogBootCommand) cmd;
        assertEquals(-180, analog.getMenuItem().getOffset());
        assertEquals(255, analog.getMenuItem().getMaxValue());
        assertEquals(2, analog.getMenuItem().getDivisor());
        assertEquals("dB", analog.getMenuItem().getUnitName());
        assertEquals(1, analog.getMenuItem().getId());
        assertEquals("Volume", analog.getMenuItem().getName());
        assertEquals(321, analog.getSubMenuId());
    }

    @Test
    public void testReceiveEnumItem() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=BE|PI=42|ID=21|NM=Choices|NC=3|CA=Choice1|CB=Choice2|CC=Choice3|VC=2|~"));
        assertTrue(cmd instanceof MenuEnumBootCommand);
        MenuEnumBootCommand enumItem = (MenuEnumBootCommand) cmd;
        assertEquals(21, enumItem.getMenuItem().getId());
        assertEquals("Choices", enumItem.getMenuItem().getName());
        assertEquals(42, enumItem.getSubMenuId());
        assertThat(enumItem.getMenuItem().getEnumEntries(), is(Arrays.asList("Choice1", "Choice2", "Choice3")));
    }

    @Test
    public void testReceiveSubMenuItem() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=BM|PI=0|ID=1|NM=SubMenu|~"));
        assertTrue(cmd instanceof MenuSubBootCommand);
        MenuSubBootCommand subMenu = (MenuSubBootCommand) cmd;
        assertEquals(1, subMenu.getMenuItem().getId());
        assertEquals("SubMenu", subMenu.getMenuItem().getName());
        assertEquals(0, subMenu.getSubMenuId());
    }

    @Test
    public void testReceiveBooleanMenuItem() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=BB|PI=0|ID=1|BN=1|NM=BoolItem|VC=1|~"));
        checkBooleanCmdFields(cmd, true, BooleanNaming.ON_OFF);
        cmd = protocol.fromChannel(toBuffer("MT=BB|PI=0|ID=1|BN=0|NM=BoolItem|VC=0|~"));
        checkBooleanCmdFields(cmd, false, BooleanNaming.TRUE_FALSE);
        cmd = protocol.fromChannel(toBuffer("MT=BB|PI=0|ID=1|BN=2|NM=BoolItem|VC=0|~"));
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
    }

    @Test
    public void testReceiveDeltaChange() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=VC|PI=11|ID=22|TC=0|VC=1|~"));
        verifyChangeFields(cmd, ChangeType.DELTA, 1);
    }

    @Test
    public void testReceiveAbsoluteChange() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=VC|PI=11|ID=22|TC=1|VC=-10000|~"));
        verifyChangeFields(cmd, ChangeType.ABSOLUTE, -10000);
    }

    private void verifyChangeFields(MenuCommand cmd, ChangeType chType, int value) {
        assertTrue(cmd instanceof MenuChangeCommand);
        MenuChangeCommand chg = (MenuChangeCommand) cmd;

        assertEquals(chType, chg.getChangeType());
        assertEquals(value, chg.getValue());
        assertEquals(11, chg.getParentItemId());
        assertEquals(22, chg.getMenuItemId());
        assertEquals(MenuCommandType.CHANGE_INT_FIELD, chg.getCommandType());
    }


    @Test(expected = IOException.class)
    public void testReceivingUnknownMessageThrowsException() throws IOException {
        protocol.fromChannel(toBuffer("MT=???|~"));
        // should throw exception.
    }

    @Test
    public void testWritingHeartbeat() {
        protocol.toChannel(bb, newHeartbeatCommand());
        testBufferAgainstExpected("MT=HB|~");
    }

    @Test
    public void testWritingJoin() {
        protocol.toChannel(bb, new MenuJoinCommand("dave", ApiPlatform.ARDUINO_8, 101));
        testBufferAgainstExpected("MT=NJ|NM=dave|VE=101|PF=0|~");
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
        testBufferAgainstExpected("MT=BA|PI=321|ID=123|NM=Test|AO=102|AD=2|AM=255|AU=dB|VC=25|~");
    }

    @Test
    public void testWritingEnumItem() {
        protocol.toChannel(bb, new MenuEnumBootCommand(22,
                DomainFixtures.anEnumItem("Test", 2),
                1));
        testBufferAgainstExpected("MT=BE|PI=22|ID=2|NM=Test|VC=1|NC=2|CA=Item1|CB=Item2|~");
    }

    @Test
    public void testWritingSubMenu() {
        protocol.toChannel(bb, new MenuSubBootCommand(22,
                DomainFixtures.aSubMenu("Sub", 1),
                false));
        testBufferAgainstExpected("MT=BM|PI=22|ID=1|NM=Sub|VC=0|~");
    }

    @Test
    public void testWritingBooleanItem() {
        protocol.toChannel(bb, new MenuBooleanBootCommand(22,
                DomainFixtures.aBooleanMenu("Bool", 1, BooleanNaming.TRUE_FALSE),
                false));
        testBufferAgainstExpected("MT=BB|PI=22|ID=1|NM=Bool|BN=0|VC=0|~");
    }

    @Test
    public void testWritingBooleanItemOnOff() {
        protocol.toChannel(bb, new MenuBooleanBootCommand(22,
                DomainFixtures.aBooleanMenu("Bool", 1, BooleanNaming.ON_OFF),
                true));
        testBufferAgainstExpected("MT=BB|PI=22|ID=1|NM=Bool|BN=1|VC=1|~");
    }

    @Test
    public void testWritingBooleanItemYesNo() {
        protocol.toChannel(bb, new MenuBooleanBootCommand(22,
                DomainFixtures.aBooleanMenu("Bool", 1, BooleanNaming.YES_NO),
                true));
        testBufferAgainstExpected("MT=BB|PI=22|ID=1|NM=Bool|BN=2|VC=1|~");
    }

    @Test
    public void testWritingAnAbsoluteChange() {
        protocol.toChannel(bb, newAbsoluteMenuChangeCommand(1, 2, 1));
        testBufferAgainstExpected("MT=VC|PI=1|ID=2|TC=1|VC=1|~");
    }

    @Test
    public void testWritingADeltaChange() {
        protocol.toChannel(bb, newDeltaChangeCommand(1, 2, 1));
        testBufferAgainstExpected("MT=VC|PI=1|ID=2|TC=0|VC=1|~");
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