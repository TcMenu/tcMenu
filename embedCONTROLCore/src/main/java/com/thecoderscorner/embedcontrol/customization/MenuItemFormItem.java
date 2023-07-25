package com.thecoderscorner.embedcontrol.customization;

import com.thecoderscorner.embedcontrol.core.controlmgr.*;
import com.thecoderscorner.menu.domain.*;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.*;

public class MenuItemFormItem extends MenuFormItem {
    private final MenuItem item;
    private PortableAlignment alignment;
    private ControlType controlType;
    private RedrawingMode redrawingMode;

    public MenuItemFormItem(MenuItem mi, ColorCustomizable settings, ComponentPositioning positioning, ControlType ty,
                            PortableAlignment alignment, RedrawingMode redrawingMode) {
        super(settings, positioning);
        this.item = mi;
        this.controlType = ty;
        this.alignment = alignment;
        this.redrawingMode = redrawingMode;

    }

    public MenuItemFormItem(MenuItem mi, ColorCustomizable settings, ComponentPositioning positioning) {
        super(settings, positioning);
        this.item = mi;
        this.controlType = MenuGridComponent.defaultControlForType(mi);
        this.alignment = defaultAlignForType(mi, controlType);
        this.redrawingMode = defaultDrawingModeForType(mi);
    }

    private PortableAlignment defaultAlignForType(MenuItem mi, ControlType controlType) {
        return controlType == ControlType.BUTTON_CONTROL ? PortableAlignment.CENTER : PortableAlignment.LEFT;
    }

    private RedrawingMode defaultDrawingModeForType(MenuItem mi) {
        if (mi instanceof SubMenuItem || mi instanceof ActionMenuItem) return RedrawingMode.SHOW_NAME;
        else return RedrawingMode.SHOW_NAME_VALUE;
    }

    @Override
    public boolean isValid() {
        return item != null;
    }

    @Override
    public String getDescription() {
        return "Edit " + item.toString();
    }

    public MenuItem getItem() {
        return item;
    }

    public PortableAlignment getAlignment() {
        return alignment;
    }

    public void setAlignment(PortableAlignment alignment) {
        this.alignment = alignment;
    }

    public ControlType getControlType() {
        return controlType;
    }

    public void setControlType(ControlType controlType) {
        this.controlType = controlType;
    }

    public RedrawingMode getRedrawingMode() {
        return redrawingMode;
    }

    public void setRedrawingMode(RedrawingMode redrawingMode) {
        this.redrawingMode = redrawingMode;
    }
}
