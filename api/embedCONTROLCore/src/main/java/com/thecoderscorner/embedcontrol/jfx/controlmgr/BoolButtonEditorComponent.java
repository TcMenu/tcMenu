package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.BaseBoolEditorComponent;
import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.MenuComponentControl;
import com.thecoderscorner.embedcontrol.core.controlmgr.ThreadMarshaller;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.util.MenuItemFormatter;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.ColorComponentType;

/**
 * BoolButtonEditorComponent is a concrete implementation of BaseBoolEditorComponent that uses a JavaFX Button
 * to represent and control a boolean state in a UI.
 */
public class BoolButtonEditorComponent extends BaseBoolEditorComponent<Node> {
    private Button button;

    public BoolButtonEditorComponent(MenuItem item, MenuComponentControl remote, ComponentSettings settings, ThreadMarshaller threadMarshaller) {
        super(remote, settings, item, threadMarshaller);
    }

    @Override
    public Node createComponent() {
        button = new Button(MenuItemFormatter.defaultInstance().getItemName(item));
        button.setMaxWidth(9999);
        button.setFont(toFont(getDrawingSettings().getFontInfo(), button.getFont()));
        drawingColorHandler.setPaintFor(button, currentVal, ColorComponentType.BUTTON, status);

        if (item.isReadOnly()) {
            button.setDisable(true);
        } else {
            button.setOnAction(this::buttonWasClicked);
        }

        return button;
    }

    public void buttonWasClicked(ActionEvent e) {
        toggleState();
    }

    @Override
    public void changeControlSettings(RenderingStatus status, String str) {
        drawingColorHandler.setPaintFor(button, currentVal, ColorComponentType.BUTTON, status);
        button.setText(str);
    }
}