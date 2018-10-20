package com.thecoderscorner.menu.editorui.generator.util;

/**
 * Defines the status of the standard set of libraries needed by TcMenu.
 * Returned by certain calls in the Arduino installer class
 */
public class LibraryStatus {
    private final boolean tcMenuUpToDate;
    private final boolean ioAbstractionUpToDate;
    private final boolean liquidCrystalIoUpToDate;

    public LibraryStatus(boolean tcMenuUpToDate, boolean ioAbstractionUpToDate, boolean liquidCrystalIoUpToDate) {
        this.tcMenuUpToDate = tcMenuUpToDate;
        this.ioAbstractionUpToDate = ioAbstractionUpToDate;
        this.liquidCrystalIoUpToDate = liquidCrystalIoUpToDate;
    }

    public boolean isTcMenuUpToDate() {
        return tcMenuUpToDate;
    }

    public boolean isIoAbstractionUpToDate() {
        return ioAbstractionUpToDate;
    }

    public boolean isLiquidCrystalIoUpToDate() {
        return liquidCrystalIoUpToDate;
    }

    public boolean isUpToDate() {
        return tcMenuUpToDate && liquidCrystalIoUpToDate && ioAbstractionUpToDate;
    }
}
