/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.remote;

import com.thecoderscorner.menu.pluginapi.AbstractCodeCreator;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.model.CodeVariableBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.pluginapi.SubSystem.REMOTE;
import static com.thecoderscorner.menu.pluginapi.validation.CannedPropertyValidators.textValidator;

public class NoRemoteCapability extends AbstractCodeCreator {
    private List<CreatorProperty> creatorProperties = new ArrayList<>(Collections.singletonList(
            new CreatorProperty("DEVICE_NAME", "Name of this device", "New Device", REMOTE, TEXTUAL, textValidator())
    ));

    @Override
    protected void initCreator(String root) {
        String deviceName = findPropertyValue("DEVICE_NAME").getLatestValue();

        addVariable(new CodeVariableBuilder().variableName("applicationName[]").variableType("char")
                            .quoted(deviceName).progmem().byAssignment().exportNeeded()
                            .requiresHeader("RemoteConnector.h", true));
    }

    @Override
    public List<CreatorProperty> properties() {
        return creatorProperties;
    }
}
