/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.util;

/**
 * Defines the status of the standard set of libraries needed by TcMenu.
 * Returned by certain calls in the Arduino installer class
 */
public class LibraryStatus {
    private final boolean tcMenuUpToDate;
    private final boolean ioAbstractionUpToDate;
    private final boolean liquidCrystalIoUpToDate;
    private final boolean taskManagerIOUpToDate;

    public LibraryStatus(boolean tcMenuUpToDate, boolean ioAbstractionUpToDate, boolean liquidCrystalIoUpToDate, boolean taskManagerIOUpToDate) {
        this.tcMenuUpToDate = tcMenuUpToDate;
        this.ioAbstractionUpToDate = ioAbstractionUpToDate;
        this.liquidCrystalIoUpToDate = liquidCrystalIoUpToDate;
        this.taskManagerIOUpToDate = taskManagerIOUpToDate;
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

    public boolean isTaskManagerIOUpToDate() { return taskManagerIOUpToDate; }

    public boolean isUpToDate() {
        return tcMenuUpToDate && liquidCrystalIoUpToDate && ioAbstractionUpToDate && taskManagerIOUpToDate;
    }
}
