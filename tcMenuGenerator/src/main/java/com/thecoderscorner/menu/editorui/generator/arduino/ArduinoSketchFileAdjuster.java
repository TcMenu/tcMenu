/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import java.util.List;

public class ArduinoSketchFileAdjuster {
    private final List<String> callbacks;

    public ArduinoSketchFileAdjuster(List<String> callbacks) {
        this.callbacks = callbacks;
    }

    private void addMenuInitToSetupIfNeeded() {

    }

    private void addTaskMgrToLoopIfNeeded() {

    }

    private void addCallbackIfNotExisting(String callbackName) {

    }
}
