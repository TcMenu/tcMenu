/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.protocol;

import com.thecoderscorner.menu.domain.DomainFixtures;
import com.thecoderscorner.menu.remote.commands.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TagValMenuCommandProtocolTest {
    private TagValMenuCommandProtocol protocol = new TagValMenuCommandProtocol();
    private byte[] msgData;
    private ByteBuffer bb;

    @Before
    public void setUp() throws Exception {
        msgData = new byte[2048];
        bb = ByteBuffer.wrap(msgData);
    }

    @Test
    public void testReceiveJoinCommand() throws IOException {
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=NJ|CV=ard8_1.0|NM=IoTdevice|~"));
        assertTrue(cmd instanceof MenuJoinCommand);
        MenuJoinCommand join = (MenuJoinCommand) cmd;
        assertEquals("IoTdevice", join.getMyName());
        assertEquals("ard8_1.0", join.getApiVersion());
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
        MenuCommand cmd = protocol.fromChannel(toBuffer("MT=BA|PI=321|ID=1|AM=255|AO=-180|AD=2|AU=dB|NM=Volume|~"));
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

    @Test(expected = IOException.class)
    public void testReceivingUnknownMessageThrowsException() throws IOException {
        protocol.fromChannel(toBuffer("MT=???|~"));
        // should throw exception.
    }

    @Test
    public void testWritingHeartbeat() {
        protocol.toChannel(bb, new MenuHeartbeatCommand());
        testBufferAgainstExpected("MT=HB|~");
    }

    @Test
    public void testWritingJoin() {
        protocol.toChannel(bb, new MenuJoinCommand("dave", "ard8_V1.0"));
        testBufferAgainstExpected("MT=NJ|NM=dave|CV=ard8_V1.0|~");
    }

    @Test
    public void testWritingBootstrap() {
        protocol.toChannel(bb, new MenuBootstrapCommand(MenuBootstrapCommand.BootType.START));
        testBufferAgainstExpected("MT=BS|BT=START|~");
    }

    @Test
    public void testWritingAnalogItem() {
        protocol.toChannel(bb, new MenuAnalogBootCommand(321, DomainFixtures.anAnalogItem("Test", 123)));
        testBufferAgainstExpected("MT=BA|PI=321|ID=123|NM=Test|AO=102|AD=2|AM=255|AU=dB|~");
    }

    private void testBufferAgainstExpected(String s2) {
        bb.flip();
        String s = new String(msgData, 0, bb.limit());
        assertEquals(s2, s);
    }


    private ByteBuffer toBuffer(String s) throws IOException {
        return ByteBuffer.wrap(s.getBytes());
    }
}