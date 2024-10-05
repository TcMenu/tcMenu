package com.thecoderscorner.menu.remote.protocol;

import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.commands.MenuCommand;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * An implementation of the menu command protocol interface that is configurable, by default it can create all the regular
 * tag value message processors so that regular embedCONTROL messages can be parsed and written. It is also possible to
 * add extra command handlers for both TagVal protocol and also for binary format.
 */
public class ConfigurableProtocolConverter implements MenuCommandProtocol {
    private static final boolean DEBUG_ALL_MESSAGES = false;
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());

    private final Map<MessageField, TagValProtocolIncomingMsgConverter> tagValIncomingParsers = new HashMap<>(128);
    private final Map<MessageField, OutputMsgConverterWithType<? extends MenuCommand, StringBuilder>> tagValOutputWriters = new HashMap<>(128);
    private final Map<MessageField, RawProtocolIncomingMsgConverter> rawIncomingParsers = new HashMap<>();
    private final Map<MessageField, OutputMsgConverterWithType<? extends MenuCommand, ByteBuffer>> rawOutputWriters = new HashMap<>();

    public ConfigurableProtocolConverter(boolean includeDefaultProcessors) {
        if(includeDefaultProcessors) {
            var tagValProcessors = new TagValMenuCommandProcessors();
            tagValProcessors.addHandlersToProtocol(this);
        }
    }

    /**
     * This method adds an additional tag value message processor that can convert an incoming wire message into a
     * command. In this case the processor will take a {@link TagValTextParser} and convert that into a
     * {@link MenuCommand}.
     *
     * @param field the message type to convert
     * @param processor the processor that can do the conversion
     */
    public void addTagValInProcessor(MessageField field, TagValProtocolIncomingMsgConverter processor) {
        tagValIncomingParsers.put(field, processor);
    }

    /**
     * This methods adds an additional tag value message processor that can convert a {@link MenuCommand} into the
     * appropriate wire format for sending.
     * @param field the type to convert
     * @param processor the processor that can do the conversion
     * @param clazz the specific message class
     * @param <T> specialization of the message class extending from MenuCommand.
     */
    public <T extends MenuCommand> void addTagValOutProcessor(MessageField field, ProtocolOutgoingMsgConverter<T, StringBuilder> processor, Class<T> clazz) {
        tagValOutputWriters.put(field, new OutputMsgConverterWithType<>(processor, clazz));
    }

    /**
     * This method adds an additional binary message processor that can convert an incoming wire message into a
     * command. In this case the processor will take a byte buffer and length of the message in the buffer, it should
     * convert this into a {@link MenuCommand}.
     *
     * @param field the message type to convert
     * @param processor the processor that can do the conversion
     */
    public void addRawInProcessor(MessageField field, RawProtocolIncomingMsgConverter processor) {
        rawIncomingParsers.put(field, processor);
    }

    /**
     * This method adds an addition binary message processor that can convert a {@link MenuCommand} into binary wire
     * format, you must write 4 bytes containing the length first.
     * @param field the message type to convert
     * @param processor the processor for this conversion
     * @param clazz the specific message class
     * @param <T> specialization of the message class extending from MenuCommand.
     */
    public  <T extends MenuCommand> void addRawOutProcessor(MessageField field, ProtocolOutgoingMsgConverter<T, ByteBuffer> processor, Class<T> clazz) {
        rawOutputWriters.put(field, new OutputMsgConverterWithType<>(processor, clazz));
    }

    @Override
    public MenuCommand fromChannel(ByteBuffer buffer) throws IOException {
        byte protoId = buffer.get();
        CommandProtocol protocol = CommandProtocol.fromProtocolId(protoId);

        String ty = getMsgTypeFromBuffer(buffer);
        MessageField cmdType = MessageField.fromId(ty);
        if(cmdType == null) throw new TcUnknownMessageException("Received unexpected message: " + ty);

        if(protocol == CommandProtocol.TAG_VAL_PROTOCOL && tagValIncomingParsers.containsKey(cmdType)) {
            TagValTextParser parser = new TagValTextParser(buffer);
            if (DEBUG_ALL_MESSAGES) logger.log(DEBUG, "Protocol convert in: {0}", parser);
            return tagValIncomingParsers.get(cmdType).apply(parser);

        } else if(protocol == CommandProtocol.RAW_BIN_PROTOCOL && rawIncomingParsers.containsKey(cmdType)){
            buffer.order(ByteOrder.BIG_ENDIAN);
            int len = buffer.getShort();
            return rawIncomingParsers.get(cmdType).apply(buffer, len);
        } else {
            throw new TcProtocolException("Unknown protocol used in message" + protocol);
        }
    }

    @Override
    public void toChannel(ByteBuffer buffer, MenuCommand cmd) throws TcProtocolException {
        var rawProcessor = rawOutputWriters.get(cmd.getCommandType());
        if(rawProcessor != null) {
            writeStandardHeader(buffer, cmd, CommandProtocol.RAW_BIN_PROTOCOL);
            rawProcessor.apply(buffer, cmd);
        } else if(tagValOutputWriters.containsKey(cmd.getCommandType())) {
            var tagWriter = tagValOutputWriters.get(cmd.getCommandType());
            writeStandardHeader(buffer, cmd, CommandProtocol.TAG_VAL_PROTOCOL);
            StringBuilder sb = new StringBuilder(256);
            tagWriter.apply(sb, cmd);
            buffer.put(sb.toString().getBytes(StandardCharsets.UTF_8));
            buffer.put((byte)0x02);

        }
        else throw new TcProtocolException("Message not processed" + cmd.getCommandType());
    }

    @Override
    public CommandProtocol getProtocolForCmd(MenuCommand command) {
        return (tagValOutputWriters.containsKey(command.getCommandType()) ? CommandProtocol.TAG_VAL_PROTOCOL : CommandProtocol.RAW_BIN_PROTOCOL);
    }

    private void writeStandardHeader(ByteBuffer buffer, MenuCommand cmd, CommandProtocol protocol) {
        buffer.put(PROTO_START_OF_MSG);
        buffer.put(protocol.getProtoNum());
        buffer.put((byte) cmd.getCommandType().getHigh());
        buffer.put((byte) cmd.getCommandType().getLow());
    }

    private String getMsgTypeFromBuffer(ByteBuffer buffer) {
        return String.valueOf((char) buffer.get()) + (char) buffer.get();
    }

    private static class OutputMsgConverterWithType<T extends MenuCommand, B> {
        private final ProtocolOutgoingMsgConverter<T, B> converter;
        private final Class<T> theClazz;

        public OutputMsgConverterWithType(ProtocolOutgoingMsgConverter<T, B> converter, Class<T> theClazz) {
            this.converter = converter;
            this.theClazz = theClazz;
        }

        @SuppressWarnings("unchecked")
        void apply(B buffer, MenuCommand cmd) throws TcProtocolException {
            if(!(cmd.getClass().equals(theClazz))) throw new IllegalArgumentException("Wrong type of command provided");
            converter.apply(buffer, (T)cmd);
        }
    }

}
