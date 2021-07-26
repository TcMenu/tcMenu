package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.ThreadMarshaller;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;

import static com.thecoderscorner.embedcontrol.jfx.dialog.GeneralSettingsController.asFxColor;
import static com.thecoderscorner.embedcontrol.jfx.dialog.GeneralSettingsController.fromFxColor;

public class RgbColorEditorComponent extends JfxTextEditorComponentBase<PortableColor> {
    private ColorPicker picker;

    protected RgbColorEditorComponent(MenuItem item, RemoteMenuController controller, ComponentSettings settings, ThreadMarshaller threadMarshaller) {
        super(controller, settings, item, threadMarshaller);
    }

    public Node createComponent() {
        picker = new ColorPicker();
        picker.setDisable(item.isReadOnly());
        picker.setOnAction(e-> {
            var newCol = fromFxColor(picker.getValue());
            validateAndSend(newCol.toString());
        });
        return picker;
    }

    @Override
    public void changeControlSettings(RenderingStatus status, String text) {
        picker.setValue(asFxColor(currentVal));
    }

    @Override
    public String getControlText() {
        return null;
    }
}
