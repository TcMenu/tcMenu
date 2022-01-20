/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uitests;

import com.thecoderscorner.menu.editorui.generator.ui.UICodePluginItem;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.Objects;

public class UICodePluginItemMatcher extends BaseMatcher<UICodePluginItem> {
    private final String pluginId;
    private final String description;
    private final String descriptionExtended;

    public UICodePluginItemMatcher(String pluginId, String description, String descriptionExtended) {
        this.pluginId = pluginId;
        this.description = description;
        this.descriptionExtended = descriptionExtended;
    }

    @Override
    public void describeTo(Description description) {
        description.appendValue("PluginMatcher for " + pluginId);
    }

    @Override
    public boolean matches(Object o) {
        if(o instanceof UICodePluginItem item) {
            return Objects.equals(item.getItem().getId(), pluginId) &&
                    Objects.equals(item.getItem().getDescription(), description) &&
                    Objects.equals(item.getItem().getExtendedDescription(), descriptionExtended);
        }
        return false;
    }
}
