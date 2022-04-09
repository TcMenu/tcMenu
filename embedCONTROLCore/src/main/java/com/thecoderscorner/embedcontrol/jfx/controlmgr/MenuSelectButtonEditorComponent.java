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
import javafx.scene.text.Font;

import java.util.function.Consumer;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.ColorComponentType;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.asFxColor;
import static com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxTextEditorComponentBase.setNodeConditionalColours;

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
        button.setFont(Font.font(getDrawingSettings().getFontSize()));
        setNodeConditionalColours(button, getDrawingSettings().getColors(), ColorComponentType.BUTTON);
        button.setOnAction(evt -> itemConsumer.accept(item));

        return button;
    }

    @Override
    public void changeControlSettings(RenderingStatus status, String str) {
        ConditionalColoring condColor = getDrawingSettings().getColors();
        var bgPaint = asFxColor(condColor.backgroundFor(status, ColorComponentType.BUTTON));
        var fgPaint = asFxColor(condColor.foregroundFor(status, ColorComponentType.BUTTON));
        button.setBackground(new Background(new BackgroundFill(bgPaint, new CornerRadii(0), new Insets(0))));
        button.setTextFill(fgPaint);
        button.setText(str);
    }
}