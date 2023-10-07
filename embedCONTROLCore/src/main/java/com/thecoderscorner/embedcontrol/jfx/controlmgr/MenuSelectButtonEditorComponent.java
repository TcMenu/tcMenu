package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.BaseBoolEditorComponent;
import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.MenuComponentControl;
import com.thecoderscorner.embedcontrol.core.controlmgr.ThreadMarshaller;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.menu.domain.MenuItem;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;

import java.util.function.Consumer;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.ColorComponentType;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.asFxColor;

public class MenuSelectButtonEditorComponent extends BaseBoolEditorComponent<Node> {
    private final String text;
    private final Consumer<MenuItem> itemConsumer;
    private Button button;

    public MenuSelectButtonEditorComponent(MenuItem item, String text, MenuComponentControl remote, ComponentSettings settings,
                                           ThreadMarshaller threadMarshaller, Consumer<MenuItem> itemConsumer) {
        super(remote, settings, item, threadMarshaller);
        this.text = text;
        this.itemConsumer = itemConsumer;
    }

    @Override
    public Node createComponent() {
        button = new Button(text);
        button.setMaxWidth(9999);
        button.setFont(toFont(getDrawingSettings().getFontInfo(), button.getFont()));

        drawingColorHandler.setPaintFor(button, currentVal, ColorComponentType.BUTTON, RenderingStatus.NORMAL);
        button.setOnAction(evt -> itemConsumer.accept(item));

        return button;
    }

    @Override
    public void changeControlSettings(RenderingStatus status, String str) {
        drawingColorHandler.setPaintFor(button, currentVal, ColorComponentType.BUTTON, status);
        button.setText(str);
    }
}