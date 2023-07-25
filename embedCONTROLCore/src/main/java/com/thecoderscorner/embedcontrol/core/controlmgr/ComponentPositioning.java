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

    public String toWire() {
        if(colSpan <= 1 && rowSpan <=1) {
            return String.format("%d,%d", row, col);
        } else {
            return String.format("%d,%d,%d,%d", row, col, rowSpan, colSpan);
        }
    }

    public static ComponentPositioning fromWire(String in) {
        var parts = in.split(",\\s*");
        if(parts.length == 2) {
            return new ComponentPositioning(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), 1, 1);
        } else if(parts.length == 4) {
            return new ComponentPositioning(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
        }
        throw new IllegalArgumentException("Position not in the right format " + in);
    }
}