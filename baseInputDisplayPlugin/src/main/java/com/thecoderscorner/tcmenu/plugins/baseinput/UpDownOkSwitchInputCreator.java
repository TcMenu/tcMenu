package com.thecoderscorner.tcmenu.plugins.baseinput;

import com.thecoderscorner.menu.pluginapi.AbstractCodeCreator;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;

import java.util.List;

public class UpDownOkSwitchInputCreator extends AbstractCodeCreator {
    private final List<CreatorProperty> creatorProperties = List.of();

    public UpDownOkSwitchInputCreator() {
    }

    @Override
    public List<CreatorProperty> properties() {
        return creatorProperties;
    }
}
