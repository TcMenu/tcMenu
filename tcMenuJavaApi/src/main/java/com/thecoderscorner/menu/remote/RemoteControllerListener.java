/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote;

import com.thecoderscorner.menu.domain.MenuItem;

public interface RemoteControllerListener {
    void menuItemChanged(MenuItem item, boolean valueOnly);
    void treeFullyPopulated();
    void connectionState(RemoteInformation remoteInformation, boolean connected);
}
