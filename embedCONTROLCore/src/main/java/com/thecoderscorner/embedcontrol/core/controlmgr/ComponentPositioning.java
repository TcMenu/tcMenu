package com.thecoderscorner.embedcontrol.core.controlmgr;

public class ComponentPositioning {
    private final int row;
    private final int col;
    private final int rowSpan;
    private final int colSpan;

    public ComponentPositioning(int row, int col) {
        this(row, col, 1, 1);
    }

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