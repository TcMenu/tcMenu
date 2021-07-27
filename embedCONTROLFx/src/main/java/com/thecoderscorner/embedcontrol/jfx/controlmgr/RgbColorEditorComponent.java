package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.ThreadMarshaller;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import static com.thecoderscorner.embedcontrol.jfx.dialog.GeneralSettingsController.asFxColor;
import static com.thecoderscorner.embedcontrol.jfx.dialog.GeneralSettingsController.fromFxColor;

public class RgbColorEditorComponent extends JfxTextEditorComponentBase<PortableColor> {
    private ColorPicker picker;

    protected RgbColorEditorComponent(MenuItem item, RemoteMenuController controller, ComponentSettings settings, ThreadMarshaller threadMarshaller) {
        super(controller, settings, item, threadMarshaller);
    }

    public Node createComponent() {
        RedrawingMode drawMode = getDrawingSettings().getDrawMode();
        picker = new ColorPicker();
        picker.setDisable(item.isReadOnly());
        picker.setOnAction(e-> {
            var newCol = fromFxColor(picker.getValue());
            validateAndSend(newCol.toString());
        });

        if(drawMode == RedrawingMode.SHOW_LABEL_NAME_VALUE || drawMode == RedrawingMode.SHOW_NAME_IN_LABEL) {
            BorderPane bp = new BorderPane();
            Label lbl = new Label(item.getName());
            bp.setLeft(lbl);
            bp.setCenter(picker);
            BorderPane.setAlignment(lbl, Pos.CENTER_LEFT);
            return bp;
        }
        else {
            return picker;
        }
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
