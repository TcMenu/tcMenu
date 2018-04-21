package com.thecoderscorner.menu.editorui.generator.display;

import java.util.Collections;
import java.util.List;

public class DisplayNotUsedCreator implements DisplayCreator{

    @Override
    public List<String> getIncludes() {
        return Collections.emptyList();
    }

    @Override
    public String getGlobalVariables() {
        return "";
    }

    @Override
    public String getSetupCode() {
        return "";
    }
}
