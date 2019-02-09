package com.thecoderscorner.tcmenu.plugins.basedisplay;

import com.thecoderscorner.menu.pluginapi.AbstractCodeCreator;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;

import java.util.Collections;
import java.util.List;

public class DoNotDefineDisplayCreator extends AbstractCodeCreator {

    @Override
    protected void initCreator(String root) {
        // nothing to do
    }

    @Override
    public List<CreatorProperty> properties() {
        return Collections.emptyList();
    }
}
