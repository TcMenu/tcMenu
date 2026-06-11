/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.MenuItem;

import java.util.List;

public interface MenuIdChooser {
    int nextHighestId();

    int nextHighestEeprom();

    List<MenuItem> getItemsSortedById();

    List<MenuItem> getItemsSortedByEeprom();

    boolean isIdUnique(int id);
}
