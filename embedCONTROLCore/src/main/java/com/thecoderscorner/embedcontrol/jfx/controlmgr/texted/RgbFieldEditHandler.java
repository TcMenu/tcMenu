package com.thecoderscorner.embedcontrol.jfx.controlmgr.texted;

import com.thecoderscorner.menu.domain.Rgb32MenuItem;
import com.thecoderscorner.menu.domain.state.PortableColor;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.asFxColor;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.fromFxColor;

public class RgbFieldEditHandler implements FieldEditHandler {
    private final Rgb32MenuItem item;
    private final ColorPicker rgbPicker;

    public RgbFieldEditHandler(Rgb32MenuItem item, Object currentValue) {
        this.item = item;

        Color startingColor;
        if(currentValue instanceof PortableColor pc) {
            startingColor = asFxColor(pc);
        }
        else if(currentValue != null) {
            startingColor = asFxColor(new PortableColor(currentValue.toString()));
        } else {
            startingColor = new Color(0, 0, 0, 0);
        }
        this.rgbPicker = new ColorPicker(startingColor);
    }

    @Override
    public Node getEditorComponent() {
        return rgbPicker;
    }

    @Override
    public boolean isCurrentlyValid() {
        return true;
    }

    @Override
    public String getValueAsString() {
        return fromFxColor(rgbPicker.getValue()).toString();
    }

    @Override
    public void markInvalid() {
        // not supported on rgb picker
    }
}
