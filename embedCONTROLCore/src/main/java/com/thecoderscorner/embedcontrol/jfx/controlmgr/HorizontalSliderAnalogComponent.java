package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.MenuComponentControl;
import com.thecoderscorner.embedcontrol.core.controlmgr.ThreadMarshaller;
import com.thecoderscorner.embedcontrol.customization.customdraw.CustomDrawingConfiguration;
import com.thecoderscorner.embedcontrol.customization.customdraw.NumberCustomDrawingConfiguration;
import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.FloatMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.AnyMenuState;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.ColorComponentType;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.asFxColor;

/**
 * The `HorizontalSliderAnalogComponent` class encapsulates an abstract JavaFX-based slider component that allows for horizontal
 * adjustment of analog values. It integrates with a menu system, rendering dynamic slider states and handling user interaction
 * for adjustable menu items.
 *
 * @param <T> the type of the analog value, constrained to subclasses of Number.
 */
public abstract class HorizontalSliderAnalogComponent<T extends Number> extends JfxTextEditorComponentBase<T> implements CanvasDrawableContext {
    private RenderingStatus lastStatus = RenderingStatus.NORMAL;
    private final MenuTree tree;
    private HorizScrollCanvas canvas;
    private BorderPane borderPane;

    public HorizontalSliderAnalogComponent(MenuComponentControl controller, ComponentSettings settings, MenuItem item, MenuTree tree, ThreadMarshaller marshaller) {
        super(controller, settings, item, marshaller);
        this.tree = tree;
    }

    @Override
    public Node createComponent() {
        canvas = new HorizScrollCanvas();
        if(isItemEditable(item)) {
            canvas.setOnMouseReleased(mouseEvent -> {
                onMouseAdjusted(mouseEvent.getX());
                sendItemAbsolute();
            });
            canvas.setOnMouseDragged(mouseEvent -> onMouseAdjusted(mouseEvent.getX()));
        }
        borderPane = new BorderPane(canvas);
        borderPane.setMaxSize(9999, 9999);

        canvas.widthProperty().bind(borderPane.widthProperty());
        canvas.heightProperty().bind(borderPane.heightProperty());
        borderPane.widthProperty().addListener((e) -> canvas.onPaintSurface(canvas.getGraphicsContext2D()));
        borderPane.heightProperty().addListener((e) -> canvas.onPaintSurface(canvas.getGraphicsContext2D()));

        return borderPane;
    }


    private void onMouseAdjusted(double newPositionInControl) {
        AnyMenuState menuState = tree.getMenuState(item);
        if ((menuState == null) || !(item instanceof AnalogMenuItem analog))return;

        var oneTick = borderPane.getWidth() / (double)analog.getMaxValue();
        var value = Math.max(0, Math.min(analog.getMaxValue(), newPositionInControl / oneTick));
        AnyMenuState newState = MenuItemHelper.stateForMenuItem(item, value, true, menuState.isActive());
        tree.changeItem(item, newState);
        onItemUpdated(item, (MenuState<?>) newState);
        setCurrentVal(value);

        canvas.onPaintSurface(canvas.getGraphicsContext2D());
    }

    protected abstract void setCurrentVal(double value);

    private void sendItemAbsolute() {
        if (status == RenderingStatus.EDIT_IN_PROGRESS) return;
        try {
            if (tree.getMenuState(item) != null)
            {
                var correlation = componentControl.editorUpdatedItem(item, currentVal);
                editStarted(correlation);
            }
        } catch (Exception ex) {
            logger.log(System.Logger.Level.ERROR, "State problem in slider", ex);
        }
    }

    @Override
    public void changeControlSettings(RenderingStatus status, String text) {
        lastStatus = status;
        canvas.onPaintSurface(canvas.getGraphicsContext2D());
    }

    @Override
    public MenuItem getItem() {
        return item;
    }

    @Override
    public RenderingStatus getStatus() {
        return status;
    }

    @Override
    public Object getValue() {
        return currentVal;
    }

    private class HorizScrollCanvas extends ResizableCanvas {
        HorizScrollCanvas() {
            super(HorizontalSliderAnalogComponent.this);
        }

        @Override
        public boolean isResizable() {
            return true;
        }

        protected void onPaintSurface(GraphicsContext gc) {

            int displayWidth = (int) canvas.getWidth();
            int displayHeight = (int) canvas.getHeight();

            double protectedCurrent;
            double screenPercentage;
            double currentPercentage;
            CustomDrawingConfiguration customDrawing = getDrawingSettings().getCustomDrawing();
            if(item instanceof AnalogMenuItem analog) {
                protectedCurrent = currentVal != null ? currentVal.doubleValue() : 0.0;
                screenPercentage = displayWidth / (double) analog.getMaxValue();
                currentPercentage = protectedCurrent / (double) analog.getMaxValue();
            } else if(item instanceof FloatMenuItem flt) {
                double max = 100.0;
                if(customDrawing instanceof NumberCustomDrawingConfiguration numCust) {
                    var ranges = numCust.getColorRanges();
                    max = ranges.get(ranges.size() - 1).end();
                }
                protectedCurrent = currentVal != null ? currentVal.doubleValue() : 0.0;
                currentPercentage = protectedCurrent / max;
                screenPercentage = displayWidth / max;
            } else {
                throw new UnsupportedOperationException("Not able to show meter for " + item.getClass().getSimpleName());
            }

            if(getDrawingSettings().getCustomDrawing() instanceof NumberCustomDrawingConfiguration numCust) {
                for (CustomDrawingConfiguration.NumericColorRange r : numCust.getColorRanges()) {
                    if (r.start() < protectedCurrent) {
                        gc.setFill(asFxColor(r.bg()));
                        gc.fillRect(r.start() * screenPercentage, 0, Math.min(r.end(), protectedCurrent) * screenPercentage, displayHeight);
                    }
                }
            } else {
                gc.setFill(asFxColor(getDrawingSettings().getColors().backgroundFor(status, ColorComponentType.CUSTOM)));
                gc.fillRect(0, 0, displayWidth * currentPercentage, displayHeight);
            }

            gc.setFill(asFxColor(getDrawingSettings().getColors().backgroundFor(lastStatus, ColorComponentType.TEXT_FIELD)));
            gc.fillRect(displayWidth * currentPercentage, 0, displayWidth, displayHeight);

            gc.setFill(asFxColor(getDrawingSettings().getColors().foregroundFor(lastStatus, ColorComponentType.HIGHLIGHT)));

            drawTextUsingSettings(gc, protectedCurrent, displayWidth, displayHeight, false);
        }
    }

    public static class IntHorizontalSliderComponent extends HorizontalSliderAnalogComponent<Integer> {
        public IntHorizontalSliderComponent(MenuComponentControl controller, ComponentSettings settings, MenuItem item, MenuTree tree, ThreadMarshaller marshaller) {
            super(controller, settings, item, tree, marshaller);
        }

        @Override
        protected void setCurrentVal(double value) {
            currentVal = (int) value;
        }
    }

    public static class FloatHorizontalSliderComponent extends HorizontalSliderAnalogComponent<Float> {
        public FloatHorizontalSliderComponent(MenuComponentControl controller, ComponentSettings settings, MenuItem item, MenuTree tree, ThreadMarshaller marshaller) {
            super(controller, settings, item, tree, marshaller);
        }

        @Override
        protected void setCurrentVal(double value) {
            throw new UnsupportedOperationException("Float not editable");
        }
    }
}