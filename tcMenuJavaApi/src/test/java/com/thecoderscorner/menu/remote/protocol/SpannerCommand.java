package com.thecoderscorner.menu.remote.protocol;

import com.thecoderscorner.menu.remote.commands.MenuCommand;
import com.thecoderscorner.menu.remote.protocol.MessageField;

public class SpannerCommand implements MenuCommand {
    public static final MessageField SPANNER_MSG_TYPE = new MessageField('S', 'Z');

    public int metricSize;
    public String make;

    public SpannerCommand(int metricSize, String make) {
        this.metricSize = metricSize;
        this.make = make;
    }

    @Override
    public MessageField getCommandType() {
        return SPANNER_MSG_TYPE;
    }

    public int getMetricSize() {
        return metricSize;
    }

    public String getMake() {
        return make;
    }
}
