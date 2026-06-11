package com.thecoderscorner.menu.editorui.project;

import java.nio.file.Path;

public interface TccProjectWatcher {
    void setProjectName(Path emfFile);
    void clear();
    void registerWatchListener(ProjectWatchListener watchListener);

    void close();

    void fileWasSaved(Path path, String data);

    public interface ProjectWatchListener {
        void externalChangeToProject();
        void projectRefreshRequired();
        void i18nFileUpdated(String context);
    }
}
