package com.thecoderscorner.tcmenu.plugins.baseinput;

import com.thecoderscorner.menu.pluginapi.AbstractCodeCreator;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;

import java.util.List;

import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.pluginapi.SubSystem.INPUT;

public class RotaryEncoderInputCreator extends AbstractCodeCreator {
    private final List<CreatorProperty> creatorProperties = List.of(
            new CreatorProperty("PULLUP_LOGIC", "Use Pull Up switch logic (true/false)", "true", INPUT, TEXTUAL),
            new CreatorProperty("INTERRUPT_SWITCHES", "Use interrupts for switches (true/false)", "false", INPUT, TEXTUAL),
            new CreatorProperty("SWITCH_IODEVICE", "Advanced: IoAbstractionRef (default is Arduino pins)", "", INPUT, TEXTUAL),
            new CreatorProperty("ENCODER_PIN_A", "A pin from rotary encoder - must be interrupt capable", "0", INPUT),
            new CreatorProperty("ENCODER_PIN_B", "B pin from rotary encoder", "0", INPUT),
            new CreatorProperty("ENCODER_PIN_OK", "OK button pin connector", "0", INPUT));

    public RotaryEncoderInputCreator() {
    }

    @Override
    public List<CreatorProperty> properties() {
        return creatorProperties;
    }
}
