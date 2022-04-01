package com.thecoderscorner.embedcontrol.customization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ColumnLayoutHolder {
    private final int gridSize;
    private final boolean rowIsAuto;
    private final List<ScreenLayoutPersistence.MenuIdWithSpace> positionsInRow;

    public ColumnLayoutHolder(int gridSize, boolean auto) {
        this.gridSize = gridSize;
        this.rowIsAuto = auto;
        this.positionsInRow = new ArrayList<>();
    }

    public Optional<ScreenLayoutPersistence.MenuIdWithSpace> configuredColumn(int id, int start, int spaces) {
        ScreenLayoutPersistence.MenuIdWithSpace idWithSpace = new ScreenLayoutPersistence.MenuIdWithSpace(id, start, spaces);
        positionsInRow.add(idWithSpace);
        return Optional.of(idWithSpace);
    }

    public Optional<ScreenLayoutPersistence.MenuIdWithSpace> nextColumn(int id, int spaces) {
        if (!rowIsAuto) return Optional.empty();

        int start = 0;
        for (var pos : positionsInRow) {
            start += pos.colsTaken();
        }
        if ((start + spaces) > gridSize) return Optional.empty();

        var newItem = new ScreenLayoutPersistence.MenuIdWithSpace(id, start, spaces);
        positionsInRow.add(newItem);
        return Optional.of(newItem);
    }

    List<ScreenLayoutPersistence.MenuIdWithSpace> menuItemsOnLine() {
        return Collections.unmodifiableList(positionsInRow);
    }

    public boolean isAuto() {
        return rowIsAuto;
    }
}
