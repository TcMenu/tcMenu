package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.BaseUpDownIntEditorComponent;
import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.ThreadMarshaller;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import static com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxTextEditorComponentBase.setNodeConditionalColours;

public abstract class UpDownEditorComponentBase<T> extends BaseUpDownIntEditorComponent<T> {
    private static final int REDUCE = -1;
    private static final int INCREASE = 1;
    private long lastRepeatStart = 0;
    private Button minusButton = new Button();
    private Button plusButton = new Button();
    private Label itemLabel;

    enum RepeatTypes {REPEAT_NONE, REPEAT_UP, REPEAT_DOWN, REPEAT_UP_WAIT, REPEAT_DOWN_WAIT}

    private RepeatTypes repeating;

    public UpDownEditorComponentBase(MenuItem item, RemoteMenuController remote, ComponentSettings settings, ThreadMarshaller marshaller) {
        super(remote, settings, item, marshaller);
    }

    public Node createComponent() {
        itemLabel = new Label();
        itemLabel.setPadding(new Insets(3, 0, 3, 0));

        minusButton = new Button("<");
        plusButton = new Button(">");
        minusButton.setDisable(item.isReadOnly());
        plusButton.setDisable(item.isReadOnly());

        minusButton.setOnAction(e -> bumpCount(REDUCE));

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
        plusButton.setOnAction(e -> bumpCount(INCREASE));

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