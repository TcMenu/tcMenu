package com.thecoderscorner.embedcontrol.customization;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.menu.domain.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record ComponentSettingsForSub(int rootItemId, boolean recursive, Optional<ControlColor> textColor,
                                      Optional<ControlColor> buttonColor, Optional<ControlColor> updateColor,
                                      Optional<ControlColor> highlightColor, int fontSize,
                                      List<ComponentSettingsWithMenuId> menuIdLevelOverrides) {

    public static ComponentSettingsForSub copyWithNewItem(ComponentSettingsForSub other, ComponentSettingsWithMenuId newSetting) {
        var items = new ArrayList<>(other.menuIdLevelOverrides.stream()
                .filter(c -> c.menuId() != newSetting.menuId())
                .toList());
        items.add(newSetting);

        return new ComponentSettingsForSub(
                other.rootItemId(), other.recursive(), other.textColor(), other.buttonColor(), other.updateColor(),
                other.highlightColor(), other.fontSize(), List.copyOf(items)
        );
    }

    public static ComponentSettingsForSub removeItemFromList(ComponentSettingsForSub other, int menuId) {
        var items = new ArrayList<>(other.menuIdLevelOverrides.stream()
                .filter(c -> c.menuId() != menuId)
                .toList());

        return new ComponentSettingsForSub(
                other.rootItemId(), other.recursive(), other.textColor(), other.buttonColor(), other.updateColor(),
                other.highlightColor(), other.fontSize(), List.copyOf(items)
        );
    }

    public Optional<ComponentSettings> overrideFor(int id) {
        return menuIdLevelOverrides.stream()
                .filter(it -> it.menuId() == id)
                .map(ComponentSettingsWithMenuId::settings)
                .findFirst();
    }

    public Optional<ControlColor> getColor(ConditionalColoring.ColorComponentType componentType) {
        return switch(componentType) {
            case BUTTON -> buttonColor;
            case HIGHLIGHT -> highlightColor;
            case TEXT_FIELD -> textColor;
            case CUSTOM -> updateColor;
            default -> Optional.empty();
        };
    }
}
