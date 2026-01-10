package com.thecoderscorner.menu.editorui.generator.plugin.display;

import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.validation.CannedPropertyValidators;
import com.thecoderscorner.menu.editorui.generator.validation.ChoiceDescription;

import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.plugin.JavaPluginItem.ALWAYS_APPLICABLE;

public class CommonDisplayPluginHelper {
    public static CreatorProperty updatesPerSecond() {
        return new CreatorProperty("UPDATES_PER_SEC", "Updates per second", "How many times the screen is updated per second",
                "2", SubSystem.DISPLAY, CreatorProperty.PropType.VARIABLE, CannedPropertyValidators.uintValidator(10), ALWAYS_APPLICABLE);
    }

    public static CreatorProperty displayRotation0to3() {
        return new CreatorProperty("DISPLAY_ROTATION", "Display Rotation", "Use to rotate the display",
                "0", SubSystem.DISPLAY, CreatorProperty.PropType.TEXTUAL, CannedPropertyValidators.choicesValidator(List.of(
                new ChoiceDescription("0", "Not rotated"),
                new ChoiceDescription("1", "90 degrees"),
                new ChoiceDescription("2", "180 degrees"),
                new ChoiceDescription("3", "270 degrees")
        ), "0"), ALWAYS_APPLICABLE);
    }
}
