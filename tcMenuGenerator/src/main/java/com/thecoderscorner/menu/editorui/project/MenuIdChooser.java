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
