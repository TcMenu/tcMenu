package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.BaseUpDownIntEditorComponent;
import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.MenuComponentControl;
import com.thecoderscorner.embedcontrol.core.controlmgr.ThreadMarshaller;
import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.CurrentScrollPosition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.ColorComponentType;

/**
 * Abstract class providing a base for editor components with up and down functionality be it numeric
 * or a list of options. Even for lists of options such an EnumMenuItem their choice is based on an int.
 * Extends the `BaseUpDownIntEditorComponent` and provides UI components with buttons for incrementing and decrementing values.
 *
 * @param <T> The type of the value managed by this editor component.
 */
public abstract class UpDownEditorComponentBase<T> extends BaseUpDownIntEditorComponent<T, Node> {
    private static final int REDUCE = -1;
    private static final int INCREASE = 1;
    private long lastRepeatStart = 0;
    private Button minusButton = new Button();
    private Button plusButton = new Button();
    private Label itemLabel;
    private final JfxTextEditorComponentBase.DrawingColorHandler drawingColorHandler;

    enum RepeatTypes {REPEAT_NONE, REPEAT_UP, REPEAT_DOWN, REPEAT_UP_WAIT, REPEAT_DOWN_WAIT}

    private RepeatTypes repeating;

    public UpDownEditorComponentBase(MenuItem item, MenuComponentControl remote, ComponentSettings settings, ThreadMarshaller marshaller) {
        super(remote, settings, item, marshaller);
        drawingColorHandler = new JfxTextEditorComponentBase.DrawingColorHandler(settings);
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
        drawingColorHandler.setPaintFor(minusButton, false, ColorComponentType.BUTTON, status);
        drawingColorHandler.setPaintFor(plusButton, false, ColorComponentType.BUTTON, status);
        drawingColorHandler.setPaintFor(itemLabel, ensureNumeric(currentVal), ColorComponentType.TEXT_FIELD, status);
        itemLabel.setText(str);
    }

    private Number ensureNumeric(T currentVal) {
        if(currentVal instanceof Number n) {
            return n;
        } else if(currentVal instanceof CurrentScrollPosition sc) {
            return sc.getPosition();
        } else return 0;
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