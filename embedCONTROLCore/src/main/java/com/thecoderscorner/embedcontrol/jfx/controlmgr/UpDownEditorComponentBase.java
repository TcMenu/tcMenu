package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.BaseUpDownIntEditorComponent;
import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.MenuComponentControl;
import com.thecoderscorner.embedcontrol.core.controlmgr.ThreadMarshaller;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.customization.FontInformation;
import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

import static com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxTextEditorComponentBase.setNodeConditionalColours;

public abstract class UpDownEditorComponentBase<T> extends BaseUpDownIntEditorComponent<T, Node> {
    private static final int REDUCE = -1;
    private static final int INCREASE = 1;
    private long lastRepeatStart = 0;
    private Button minusButton = new Button();
    private Button plusButton = new Button();
    private Label itemLabel;

    enum RepeatTypes {REPEAT_NONE, REPEAT_UP, REPEAT_DOWN, REPEAT_UP_WAIT, REPEAT_DOWN_WAIT}

    private RepeatTypes repeating;

    public UpDownEditorComponentBase(MenuItem item, MenuComponentControl remote, ComponentSettings settings, ThreadMarshaller marshaller) {
        super(remote, settings, item, marshaller);
    }

    @Override
    public Node createComponent() {
        itemLabel = new Label();
        itemLabel.setPadding(new Insets(3, 0, 3, 0));

        minusButton = new Button("<");
        plusButton = new Button(">");
        minusButton.setDisable(item.isReadOnly());
        plusButton.setDisable(item.isReadOnly());
        plusButton.setFont(toFont(getDrawingSettings().getFontInfo(), plusButton.getFont()));
        minusButton.setFont(toFont(getDrawingSettings().getFontInfo(), minusButton.getFont()));
        itemLabel.setFont(toFont(getDrawingSettings().getFontInfo(), itemLabel.getFont()));

        minusButton.setOnMousePressed(e -> {
            repeating = RepeatTypes.REPEAT_DOWN_WAIT;
            lastRepeatStart = System.currentTimeMillis();
        });
        minusButton.setOnMouseReleased(e -> repeating = RepeatTypes.REPEAT_NONE);
        plusButton.setOnMousePressed(e -> {
            repeating = RepeatTypes.REPEAT_UP_WAIT;
            lastRepeatStart = System.currentTimeMillis();
        });
        plusButton.setOnMouseReleased(e -> repeating = RepeatTypes.REPEAT_NONE);

        if(item instanceof AnalogMenuItem analogItem) {
            minusButton.setOnAction(e -> bumpCount(REDUCE * analogItem.getStep()));
            plusButton.setOnAction(e -> bumpCount(INCREASE * analogItem.getStep()));
        } else {
            minusButton.setOnAction(e -> bumpCount(REDUCE));
            plusButton.setOnAction(e -> bumpCount(INCREASE));
        }

        var border = new BorderPane();
        border.setLeft(minusButton);
        border.setRight(plusButton);
        border.setCenter(itemLabel);
        return border;
    }

    @Override
    public void changeControlSettings(RenderingStatus status, String str) {
        var condCol = getDrawingSettings().getColors();

        setNodeConditionalColours(minusButton, condCol, ConditionalColoring.ColorComponentType.BUTTON, status);
        setNodeConditionalColours(plusButton, condCol, ConditionalColoring.ColorComponentType.BUTTON, status);
        setNodeConditionalColours(itemLabel, condCol, ConditionalColoring.ColorComponentType.TEXT_FIELD, status);
        itemLabel.setText(str);
    }

    @Override
    public void tick() {
        super.tick();

        if(repeating == RepeatTypes.REPEAT_DOWN_WAIT && (System.currentTimeMillis() - lastRepeatStart) > 500 ) {
            repeating = RepeatTypes.REPEAT_DOWN;
        }

        if(repeating == RepeatTypes.REPEAT_UP_WAIT && (System.currentTimeMillis() - lastRepeatStart) > 500 ) {
            repeating = RepeatTypes.REPEAT_UP;
        }

        if(repeating == RepeatTypes.REPEAT_UP) {
            Platform.runLater(()->plusButton.fire());
        }
        else if(repeating == RepeatTypes.REPEAT_DOWN) {
            Platform.runLater(()->minusButton.fire());
        }
    }

}