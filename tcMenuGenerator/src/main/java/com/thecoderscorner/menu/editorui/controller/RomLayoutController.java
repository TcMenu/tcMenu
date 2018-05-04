/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import com.thecoderscorner.menu.editorui.project.MenuIdChooserImpl;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class RomLayoutController {
    private MenuIdChooser menuIdChooser;
    public VBox idContainer;
    public VBox eepromContainer;

    public void init(MenuTree menuTree) {
        menuIdChooser = new MenuIdChooserImpl(menuTree);

        menuIdChooser.getItemsSortedById().forEach((item)-> idContainer.getChildren().add(
                new Label(item.getId() + " - " + item.getName())
        ));

        List<MenuItem> sortedByEeprom = menuIdChooser.getItemsSortedByEeprom();
        sortedByEeprom.stream()
                .filter((it)-> it.getEepromAddress() != -1)
                .forEach((item)-> {
                    int address = item.getEepromAddress();
                    int addrSize = MenuItemHelper.eepromSizeForItem(item);
                    int addrEnd = address + addrSize - 1;
                    Label l = new Label(address + "-" + addrEnd + ": " + item.getName());

                    Optional<String> maybeOverlap = overlapDetails(item, sortedByEeprom);
                    if(maybeOverlap.isPresent()){
                        l.getStyleClass().add("brokenEeprom");
                        l.setTooltip(new Tooltip(maybeOverlap.get()));
                    }
                    else {
                        l.getStyleClass().add("eepromEntry");
                    }
                    l.setPrefWidth(eepromContainer.getPrefWidth());
                    eepromContainer.getChildren().add(l);
                }
        );
    }

    private Optional<String> overlapDetails(MenuItem itemTest, List<MenuItem> sortedByEeprom) {
        if(itemTest.getEepromAddress() < 2) {
            return Optional.of("Overlaps with magic number (less than 2)");
        }
        int startB = itemTest.getEepromAddress();
        int endB = itemTest.getEepromAddress() + MenuItemHelper.eepromSizeForItem(itemTest) - 1;

        return sortedByEeprom.stream()
                .filter(item-> !item.equals(itemTest))
                .filter(item-> item.getEepromAddress() != -1)
                .filter(item-> {
                    int startA = item.getEepromAddress();
                    int endA = item.getEepromAddress() + MenuItemHelper.eepromSizeForItem(item) - 1;
                    return (endA >= startB && startA <= endB);
                })
                .map(menuItem -> "Overlaps with " + menuItem)
                .findFirst();

    }
}
