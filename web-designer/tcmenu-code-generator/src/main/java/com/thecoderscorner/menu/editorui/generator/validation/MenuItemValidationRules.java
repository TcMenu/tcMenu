package com.thecoderscorner.menu.editorui.generator.validation;

import com.thecoderscorner.menu.domain.*;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MenuItemValidationRules extends ChoicesPropertyValidationRules {
    private final Class<? extends MenuItem> filter;

    /**
     * Create an instance with an array of values, generally from an Enum.
     *
     * @param filter the filter to use for selecting a menuitem.
     */
    public MenuItemValidationRules(Class<? extends MenuItem> filter) {
        super("-1");
        this.filter = filter;
    }

    public void initialise(List<MenuItem> allItems) {
        var selectedItems = allItems.stream()
                .filter(it -> filter.equals(MenuItem.class) || it.getClass().equals(filter))
                .toList();
        enumValues.clear();
        enumNaturalOrder.clear();
        enumNaturalOrder.add(new ChoiceDescription("-1", "None"));
        enumValues.put("-1", new ChoiceDescription("-1", "None"));
        for(var item : selectedItems) {
            var idStr = Integer.toString(item.getId());
            enumValues.put(idStr, new ChoiceDescription(idStr, item.getName()));
            enumNaturalOrder.add(new ChoiceDescription(idStr, item.getName()));
        }
    }

    @Override
    public PropValidationInfo getValidationInfo() {
        return new PropValidationInfo(PropValidationInfo.PropertyValidationMode.FONT, 0, 0, itemTypeText(filter), List.of());
    }

    private String itemTypeText(Class<? extends MenuItem> filter) {
        if(SubMenuItem.class.equals(filter)) return "sub";
        else if(EnumMenuItem.class.equals(filter)) return "enum";
        else if(RuntimeListMenuItem.class.equals(filter)) return "list";
        else if(AnalogMenuItem.class.equals(filter)) return "analog";
        else if(ActionMenuItem.class.equals(filter)) return "action";
        else if(BooleanMenuItem.class.equals(filter)) return "boolean";
        else if(FloatMenuItem.class.equals(filter)) return "float";
        else return "*";
    }

    @Override
    public String toString() {
        return "MenuItem validator " + filter.getSimpleName();
    }
}
