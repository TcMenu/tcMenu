package com.thecoderscorner.menu.editorui.generator.validation;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MenuItemValidationRules extends ChoicesPropertyValidationRules {
    private final Predicate<MenuItem> filter;

    /**
     * Create an instance with an array of values, generally from an Enum.
     *
     * @param filter the filter to use for selecting a menuitem.
     */
    public MenuItemValidationRules(Predicate<MenuItem> filter) {
        super("-1");
        this.filter = filter;
    }

    public void initialise(List<MenuItem> allItems) {
        var selectedItems = allItems.stream()
                .filter(filter).collect(Collectors.toList());
        enumValues.clear();
        enumValues.put("-1", new ChoiceDescription("-1", "None"));
        for(var item : selectedItems) {
            var idStr = Integer.toString(item.getId());
            enumValues.put(idStr, new ChoiceDescription(idStr, item.getName()));
        }
    }

    @Override
    public String toString() {
        return "MenuItem validator";
    }
}
