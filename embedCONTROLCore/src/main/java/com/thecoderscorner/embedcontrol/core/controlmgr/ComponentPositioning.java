package com.thecoderscorner.embedcontrol.core.controlmgr;

/**
 * Represents an abstract way of positioning menu item controls for display in a grid. Containing the row, column
 * and span of each.
 */
public class ComponentPositioning {
    private final int row;
    private final int col;
    private final int rowSpan;
    private final int colSpan;

    /**
     * Create a component at row, col with span set to 1
     * @param row the row zero based
     * @param col the column zero based
     */
    public ComponentPositioning(int row, int col) {
        this(row, col, 1, 1);
    }

    /**
     * Create a component at row, col with spans
     * @param row the row zero based
     * @param col the column zero based
     * @param rowSpan the row span - 1 or more
     * @param colSpan the column span - 1 or more.
     */
    public ComponentPositioning(int row, int col, int rowSpan, int colSpan) {
        this.row = row;
        this.col = col;
        this.rowSpan = rowSpan;
        this.colSpan = colSpan;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getRowSpan() {
        return rowSpan;
    }

    public int getColSpan() {
        return colSpan;
    }
}