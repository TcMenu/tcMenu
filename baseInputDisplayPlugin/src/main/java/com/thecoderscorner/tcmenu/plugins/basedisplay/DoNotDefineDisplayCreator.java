/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

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
