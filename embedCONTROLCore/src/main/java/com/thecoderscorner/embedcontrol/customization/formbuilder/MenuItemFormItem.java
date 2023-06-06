package com.thecoderscorner.embedcontrol.customization.formbuilder;

import com.thecoderscorner.embedcontrol.core.controlmgr.*;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.ColorCustomizable;
import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.MenuItem;

import java.awt.*;

public class MenuItemFormItem extends MenuFormItem {
    private final MenuItem item;
    private EditorComponent.PortableAlignment alignment;
    private ControlType controlType;
    private RedrawingMode redrawingMode;

    public MenuItemFormItem(MenuItem mi, ColorCustomizable settings, ComponentPositioning positioning, ControlType ty,
                            EditorComponent.PortableAlignment alignment, RedrawingMode redrawingMode) {
        super(settings, positioning);
        this.item = mi;
        this.controlType = ty;
        this.alignment = alignment;
        this.redrawingMode = redrawingMode;

    }

    public MenuItemFormItem(MenuItem mi, ColorCustomizable settings, ComponentPositioning positioning) {
        super(settings, positioning);
        this.item = mi;
        this.controlType = defaultControlForType(mi);
        this.alignment = defaultAlignForType(mi);
        this.redrawingMode = defaultDrawingModeForType(mi);

    }

    private EditorComponent.PortableAlignment defaultAlignForType(MenuItem mi) {
        return EditorComponent.PortableAlignment.LEFT;
    }

    private RedrawingMode defaultDrawingModeForType(MenuItem mi) {
        if(mi instanceof SubMenuItem || mi instanceof ActionMenuItem) return RedrawingMode.SHOW_NAME;
        else return RedrawingMode.SHOW_NAME_VALUE;
    }

    private ControlType defaultControlForType(MenuItem mi) {
        if(mi instanceof ActionMenuItem || mi instanceof BooleanMenuItem) {
            return ControlType.BUTTON_CONTROL;
        } else if(mi instanceof AnalogMenuItem) {
            return ControlType.HORIZONTAL_SLIDER;
        } else if(mi instanceof EnumMenuItem || mi instanceof ScrollChoiceMenuItem) {
            return ControlType.UP_DOWN_CONTROL;
        } else if(mi instanceof EditableTextMenuItem) {
            return switch(((EditableTextMenuItem) mi).getItemType()) {
                case GREGORIAN_DATE -> ControlType.DATE_CONTROL;
                case TIME_12H, TIME_12H_HHMM, TIME_24_HUNDREDS, TIME_24H_HHMM, TIME_24H -> ControlType.TIME_CONTROL;
                default -> ControlType.TEXT_CONTROL;
            };
        } else {
            return ControlType.TEXT_CONTROL;
        }
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

    public EditorComponent.PortableAlignment getAlignment() {
        return alignment;
    }

    public void setAlignment(EditorComponent.PortableAlignment alignment) {
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
