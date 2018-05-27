/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.input;

import com.thecoderscorner.menu.editorui.generator.EmbeddedCodeCreator;
import com.thecoderscorner.menu.editorui.generator.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.EnumWithApplicability;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.thecoderscorner.menu.editorui.generator.EmbeddedPlatformMappings.ALL_DEVICES;

public class InputType extends EnumWithApplicability  {
    public static Map<Integer, InputType> values = new HashMap<>();

    static {
        addValue(1, ALL_DEVICES, "Rotary encoder", RotaryEncoderInputCreator.class);
        addValue(2, ALL_DEVICES, "Up/Down/OK switches", UpDownOkInputCreator.class);
    }

    public InputType(Set<EmbeddedPlatform> platformApplicability, String description,
                     Class<? extends EmbeddedCodeCreator> creator, int key) {
        super(platformApplicability, description, creator, key);
    }

    private static void addValue(int key, Set<EmbeddedPlatform> applicability, String description,
                                 Class<? extends EmbeddedCodeCreator> creator) {
        values.put(key, new InputType(applicability, description, creator, key));
    }

}
