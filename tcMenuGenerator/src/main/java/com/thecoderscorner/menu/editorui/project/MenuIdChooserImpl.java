/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class MenuIdChooserImpl implements MenuIdChooser {
    private static final int MAGIC_EEPROM_OFFSET = 2;
    private final MenuTree menuTree;
    private final TreeSet<Integer> idSet;

    public MenuIdChooserImpl(MenuTree menuTree) {
        this.menuTree = menuTree;
        idSet = menuTree.getAllSubMenus().stream()
                .flatMap((subMenu) -> menuTree.getMenuItems(subMenu).stream())
                .map(MenuItem::getId)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public int nextHighestId() {
        return idSet.stream().max(Integer::compareTo).orElse(0) + 1;
    }

    @Override
    public int nextHighestEeprom() {
        List<MenuItem> list = getItemsSortedByEeprom();
        if(list.isEmpty()) {
            return MAGIC_EEPROM_OFFSET;
        }

        MenuItem item = list.get(list.size() - 1);
        if(item.getEepromAddress() < 0) {
            return MAGIC_EEPROM_OFFSET;
        }

        return item.getEepromAddress() + MenuItemHelper.eepromSizeForItem(item);
    }

    @Override
    public List<MenuItem> getItemsSortedById() {
        return getItemsSortedByFunction(MenuItem::getId);
    }

    @Override
    public List<MenuItem> getItemsSortedByEeprom() {
        return getItemsSortedByFunction(MenuItem::getEepromAddress);
    }

    private List<MenuItem> getItemsSortedByFunction(ToIntFunction<MenuItem> intFunction) {
        return menuTree.getAllSubMenus().stream()
                .flatMap((subMenu) -> menuTree.getMenuItems(subMenu).stream())
                .sorted(Comparator.comparingInt(intFunction))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isIdUnique(int id) {
        return !idSet.contains(id);
    }
}
