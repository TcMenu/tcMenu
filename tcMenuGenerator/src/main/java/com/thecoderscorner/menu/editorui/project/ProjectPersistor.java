package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.state.MenuTree;

import java.io.IOException;

public interface ProjectPersistor {

    MenuTree open(String fileName) throws IOException;
    void save(String fileName, MenuTree tree) throws IOException;
}
