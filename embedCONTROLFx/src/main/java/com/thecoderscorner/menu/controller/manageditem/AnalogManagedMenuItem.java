/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.controller.manageditem;

import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.state.AnyMenuState;
import com.thecoderscorner.menu.domain.state.IntegerMenuState;

public class AnalogManagedMenuItem extends IntegerBaseManagedMenuItem<AnalogMenuItem, Integer> {
    public AnalogManagedMenuItem(AnalogMenuItem item) {
        super(item);
    }


    @Override
    public void internalChangeItem(AnyMenuState state) {
        if (state instanceof IntegerMenuState intState) {
            var value = intState.getValue();
            var divisor = item.getDivisor();
            String str;
            if (divisor < 2) {
                // in this case divisor was 0 or 1, this means treat as integer.
                str = value + item.getUnitName();
            } else {
                // so we can display as decimal, work out the nearest highest unit for 2dp, 3dp and 4dp.
                int fractionMax = (divisor > 1000) ? 10000 : (divisor > 100) ? 1000 : (divisor > 10) ? 100 : 10;

                // when divisor is greater than 10 we need to deal with both parts as if they were 2 ints
                int whole = (item.getOffset() + value) / divisor;
                int fraction = Math.abs((value % divisor)) * (fractionMax / divisor);

                str = whole + "." + fraction + item.getUnitName();
            }
            itemLabel.setText(str);
        }
    }
}
