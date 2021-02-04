package com.thecoderscorner.menu.editorui.generator.validation;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.editorui.util.StringHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MenuItemValidationRules implements PropertyValidationRules {
    private static final SubMenuItem NONE = new SubMenuItem("NONE", "NONE", -1, -1, false, false, false);
    private final Predicate<MenuItem> filter;
    private List<MenuItem> menuItems = List.of();

    /**
     * Create an instance with an array of values, generally from an Enum.
     *
     * @param filter the filter to use for selecting a menuitem.
     */
    public MenuItemValidationRules(Predicate<MenuItem> filter) {
        this.filter = filter;
    }

    public void initialise(List<MenuItem> allItems) {
        var selectedItems = allItems.stream()
                .filter(filter).collect(Collectors.toList());
        menuItems = new ArrayList<>();
        menuItems.add(NONE);
        menuItems.addAll(selectedItems);
    }

    public String valueFor(int selectedIndex) {
        if(selectedIndex == -1) return "-1";
        return Integer.toString(menuItems.get(selectedIndex).getId());
    }

    public int valueToIndex(String lastValue) {
        if(StringHelper.isStringEmptyOrNull(lastValue)) return 0;
        var lastId = Integer.parseInt(lastValue);
        for(int i = 0; i < menuItems.size(); i++) {
            if(menuItems.get(i).getId() == lastId) return i;
        }
        return 0;
    }

    @Override
    public boolean isValueValid(String value) {
        if(StringHelper.isStringEmptyOrNull(value)) return false;
        try {
            if(value.indexOf(':') != -1) {
                value = value.split(":")[0];
            }
            var id = Integer.parseInt(value);
            return menuItems.stream().anyMatch(item -> id == item.getId());
        }
        catch(Exception ex) {
            return false;
        }
    }

    @Override
    public boolean hasChoices() {
        return true;
    }

    @Override
    public List<String> choices() {
        return menuItems.stream()
                .map(item -> item.getId() + ": " + item.getName())
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "MenuItem validator";
    }
}
