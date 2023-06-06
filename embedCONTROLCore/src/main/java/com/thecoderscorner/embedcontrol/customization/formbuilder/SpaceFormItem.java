package com.thecoderscorner.embedcontrol.customization.formbuilder;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentPositioning;
import com.thecoderscorner.embedcontrol.customization.ColorCustomizable;

public class SpaceFormItem extends MenuFormItem {
    private int verticalSpace;

    public SpaceFormItem(ColorCustomizable settings, ComponentPositioning positioning, int verticalSpace) {
        super(settings, positioning);
        this.verticalSpace = verticalSpace;
    }

    public int getVerticalSpace() {
        return verticalSpace;
    }

    public void setVerticalSpace(int verticalSpace) {
        this.verticalSpace = verticalSpace;
    }

    @Override
    public boolean isValid() {
        return verticalSpace != 0;
    }

    @Override
    public String getDescription() {
        return "Edit Space";
    }
}
