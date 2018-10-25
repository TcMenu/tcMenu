package com.thecoderscorner.menu.editorui.generator.input;

import com.thecoderscorner.menu.editorui.generator.EmbeddedCodeCreator;
import com.thecoderscorner.menu.editorui.generator.EmbeddedPlatform;
import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty.PropType.USE_IN_DEFINE;
import static com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty.SubSystem.INPUT;
import static com.thecoderscorner.menu.editorui.util.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.assertions.api.Assertions.assertThat;

public class InputTypeTest {
    @Test
    public void testStaticInputValues() {
        assertEquals(2, InputType.values.size());
        assertEquals("Rotary encoder", InputType.values.get(1).getDescription());
        assertTrue(InputType.values.get(1).isApplicableFor(EmbeddedPlatform.ARDUINO));

        assertEquals("Up/Down/OK switches", InputType.values.get(2).getDescription());
        assertTrue(InputType.values.get(2).isApplicableFor(EmbeddedPlatform.ARDUINO));
    }

    @Test
    public void testRotaryEncoderInput() {
        EmbeddedCodeCreator creator = InputType.values.get(1).makeCreator(makeEditorProject());
        assertEquals(6, creator.properties().size());

        findAndCheckProperty(creator,"PULLUP_LOGIC", INPUT, TEXTUAL, "false");
        findAndCheckProperty(creator,"INTERRUPT_SWITCHES", INPUT, TEXTUAL, "false");
        findAndCheckProperty(creator,"SWITCH_IODEVICE", INPUT, TEXTUAL, "io8574");
        findAndCheckProperty(creator,"ENCODER_PIN_A", INPUT, USE_IN_DEFINE, "1");
        findAndCheckProperty(creator,"ENCODER_PIN_B", INPUT, USE_IN_DEFINE, "2");
        findAndCheckProperty(creator,"ENCODER_PIN_OK", INPUT, USE_IN_DEFINE, "3");

        assertEqualsIgnoringCRLF("extern IoAbstractionRef io8574;\n" +
                "#define ENCODER_PIN_A 1\n" +
                "#define ENCODER_PIN_B 2\n" +
                "#define ENCODER_PIN_OK 3\n", creator.getExportDefinitions());

        assertEquals("    switches.initialise(io8574, false);\n" +
                "    menuMgr.initForEncoder(&renderer, &root, ENCODER_PIN_A, ENCODER_PIN_B, ENCODER_PIN_OK);",
                creator.getSetupCode("root"));
        assertEquals("", creator.getGlobalVariables());
        assertThat(creator.getRequiredFiles()).isEmpty();
        assertThat(creator.getIncludes()).isEmpty();
    }

    @Test
    public void testUpDownEncoder() {
        EmbeddedCodeCreator creator = InputType.values.get(2).makeCreator(makeEditorProject());
        assertEquals(6, creator.properties().size());

        findAndCheckProperty(creator,"PULLUP_LOGIC", INPUT, TEXTUAL, "false");
        findAndCheckProperty(creator,"INTERRUPT_SWITCHES", INPUT, TEXTUAL, "false");
        findAndCheckProperty(creator,"SWITCH_IODEVICE", INPUT, TEXTUAL, "io8574");
        findAndCheckProperty(creator,"ENCODER_UP_PIN", INPUT, USE_IN_DEFINE, "1");
        findAndCheckProperty(creator,"ENCODER_DOWN_PIN", INPUT, USE_IN_DEFINE, "2");
        findAndCheckProperty(creator,"ENCODER_OK_PIN", INPUT, USE_IN_DEFINE, "3");

        assertEqualsIgnoringCRLF("extern IoAbstractionRef io8574;\n" +
                "#define ENCODER_UP_PIN 1\n" +
                "#define ENCODER_DOWN_PIN 2\n" +
                "#define ENCODER_OK_PIN 3\n", creator.getExportDefinitions());

        assertEquals("    switches.initialise(io8574, false);\n" +
                        "    menuMgr.initForUpDownOk(&renderer, &root, ENCODER_PIN_UP, ENCODER_PIN_DOWN, ENCODER_BUTTON_PIN);",
                creator.getSetupCode("root"));
        assertEquals("", creator.getGlobalVariables());
        assertThat(creator.getRequiredFiles()).isEmpty();
        assertThat(creator.getIncludes()).isEmpty();
    }
}