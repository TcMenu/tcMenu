package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.ThreadMarshaller;
import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.AnyMenuState;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemFormatter;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Text;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.ColorComponentType;
import static com.thecoderscorner.embedcontrol.jfx.dialog.GeneralSettingsController.asFxColor;

public class HorizontalSliderAnalogComponent extends JfxTextEditorComponentBase<Integer> {
    private RenderingStatus lastStatus = RenderingStatus.NORMAL;
    private String lastStr = "";
    private final MenuTree tree;
    private int displayWidth;
    private Canvas canvas;

    public HorizontalSliderAnalogComponent(RemoteMenuController controller, ComponentSettings settings, MenuItem item, MenuTree tree, ThreadMarshaller marshaller) {
        super(controller, settings, item, marshaller);
        this.tree = tree;
    }

    public Node createComponent() {
        canvas = new Canvas();
        if(isItemEditable(item)) {
            canvas.setOnMouseReleased(mouseEvent -> sendItemAbsolute());
            canvas.setOnMouseDragged(mouseEvent -> onMouseAdjusted((int)mouseEvent.getX()));
        }
        return makeTextComponent(canvas, null,false);
    }


    private void onMouseAdjusted(int newPositionInControl) {
        AnyMenuState menuState = tree.getMenuState(item);
        if ((menuState == null) || !(item instanceof AnalogMenuItem analog))return;

        var oneTick = displayWidth / analog.getMaxValue();
        var value = Math.max(0, Math.min(analog.getMaxValue(), newPositionInControl / oneTick));
        AnyMenuState newState = MenuItemHelper.stateForMenuItem(item, value, true, menuState.isActive());
        tree.changeItem(item, newState);
        onItemUpdated((MenuState<?>) newState);
        currentVal = value;

        onPaintSurface(canvas.getGraphicsContext2D());
    }

    private void sendItemAbsolute() {
        if (status == RenderingStatus.EDIT_IN_PROGRESS) return;
        try {
            if (tree.getMenuState(item) != null)
            {
                var correlation = remoteController.sendAbsoluteUpdate(item, currentVal);
                editStarted(correlation);
            }
        } catch (Exception ex) {
            logger.log(System.Logger.Level.ERROR, "State problem in slider", ex);
        }
    }

    protected void onPaintSurface(GraphicsContext gc) {

        var analog = (AnalogMenuItem) item;

        displayWidth = (int) canvas.getWidth();

        var currentPercentage = currentVal / (float) analog.getMaxValue();

        gc.setFill(asFxColor(getDrawingSettings().getColors().backgroundFor(RenderingStatus.NORMAL, ColorComponentType.HIGHLIGHT)));
        gc.fillRect(0, 0, displayWidth * currentPercentage, canvas.getHeight());

        gc.setFill(asFxColor(getDrawingSettings().getColors().backgroundFor(lastStatus, ColorComponentType.BUTTON)));
        gc.fillRect(displayWidth * currentPercentage, 0, displayWidth, canvas.getHeight());

        gc.setFill(asFxColor(getDrawingSettings().getColors().foregroundFor(lastStatus, ColorComponentType.HIGHLIGHT)));

        String toDraw = "";
        if(controlTextIncludesName()) toDraw = item.getName();
        if(controlTextIncludesValue()) toDraw += " " + MenuItemFormatter.formatForDisplay(item, currentVal);
        final Text textObj = new Text(toDraw);
        var bounds = textObj.getLayoutBounds();
        gc.fillText(toDraw, (displayWidth - bounds.getWidth()) / 2.0, (canvas.getHeight() - (bounds.getHeight() / 2.0)));
    }

    @Override
    public void changeControlSettings(RenderingStatus status, String text) {
        lastStatus = status;
        lastStr = text;
        onPaintSurface(canvas.getGraphicsContext2D());
    }
}