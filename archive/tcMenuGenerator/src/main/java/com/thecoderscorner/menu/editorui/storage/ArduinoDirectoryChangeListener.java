package com.thecoderscorner.menu.editorui.storage;

import java.util.Optional;

@FunctionalInterface
public interface ArduinoDirectoryChangeListener {
    void arduinoDirectoryHasChanged(Optional<String> newArduinoDir, Optional<String> newLibsDir, boolean libsChanged);
}
