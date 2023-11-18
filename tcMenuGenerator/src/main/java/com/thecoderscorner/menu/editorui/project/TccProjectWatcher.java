package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.persist.LocaleMappingHandler;

import java.nio.file.Path;
import java.util.function.Consumer;

public interface TccProjectWatcher {
    void setProjectName(Path emfFile);
    void clear();
    void registerNotifiers(Consumer<String> emfHandler, LocaleMappingHandler handler);

    void close();
}
