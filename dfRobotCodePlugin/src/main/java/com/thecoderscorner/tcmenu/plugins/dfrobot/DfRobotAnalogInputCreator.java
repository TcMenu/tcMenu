package com.thecoderscorner.tcmenu.plugins.dfrobot;

import com.thecoderscorner.menu.pluginapi.AbstractCodeCreator;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.model.FunctionCallBuilder;

import java.util.List;

public class DfRobotAnalogInputCreator extends AbstractCodeCreator {
    @Override
    protected void initCreator(String root) {
        addFunctionCall(new FunctionCallBuilder().functionName("initialise").objectName("switches")
                                .requiresHeader("IoAbstraction.h", false)
                                .fnparam("ioUsingArduino"));
        addFunctionCall(new FunctionCallBuilder().functionName("initForUpDownOk").objectName("menuMgr")
                                .requiresHeader("DfRobotInputAbstraction.h", false)
                                .param("DF_KEY_UP").param("DF_KEY_DOWN").param("DF_KEY_SELECT"));
    }

    @Override
    public List<CreatorProperty> properties() {
        return List.of();
    }
}
