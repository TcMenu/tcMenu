/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.controller.manageditem;

import com.thecoderscorner.menu.domain.EnumMenuItem;
import com.thecoderscorner.menu.domain.state.AnyMenuState;
import com.thecoderscorner.menu.domain.state.IntegerMenuState;

public class EnumManagedMenuItem extends IntegerBaseManagedMenuItem<EnumMenuItem, Integer> {
    public EnumManagedMenuItem(EnumMenuItem item) {
        super(item);
    }

    @Override
    public void internalChangeItem(AnyMenuState state) {
        if(state instanceof IntegerMenuState intState) {
            itemLabel.setText(item.getEnumEntries().get(intState.getValue()));
        }
    }
}
