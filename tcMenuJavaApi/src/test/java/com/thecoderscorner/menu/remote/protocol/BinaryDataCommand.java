package com.thecoderscorner.menu.remote.protocol;

import com.thecoderscorner.menu.remote.commands.MenuCommand;

public class BinaryDataCommand implements MenuCommand {
    public static final MessageField BIN_DATA_COMMAND = new MessageField('S', 'B');

    public byte[] binData;

    public BinaryDataCommand(byte[] binData) {
        this.binData = binData;
    }

    @Override
    public MessageField getCommandType() {
        return BIN_DATA_COMMAND;
    }

    public byte[] getBinData() {
        return binData;
    }
}
