/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.editorui.generator.arduino.CallbackRequirement;

import java.io.IOException;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface SketchFileAdjuster {
    void makeAdjustments(BiConsumer<System.Logger.Level, String> logger, String inoFile, String projectName,
                         Collection<CallbackRequirement> callbacks) throws IOException;
}
