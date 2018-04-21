package com.thecoderscorner.menu.editorui.generator.display;

import java.util.List;

public interface DisplayCreator {
    List<String> getIncludes();
    String getGlobalVariables();
    String getSetupCode();
}
