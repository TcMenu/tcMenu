package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.MenuComponentControl;
import com.thecoderscorner.embedcontrol.core.controlmgr.ThreadMarshaller;
import com.thecoderscorner.embedcontrol.customization.customdraw.CustomDrawingConfiguration;
import com.thecoderscorner.embedcontrol.customization.customdraw.NumberCustomDrawingConfiguration;
import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.FloatMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.ColorComponentType;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.asFxColor;

public class AnalogMeterComponent<T extends Number> extends JfxTextEditorComponentBase<T> implements CanvasDrawableContext  {
    private RenderingStatus lastStatus = RenderingStatus.NORMAL;
    private final MenuTree tree;
    private AnalogMeterCanvas canvas;
    private BorderPane borderPane;

    public AnalogMeterComponent(MenuComponentControl controller, ComponentSettings settings, MenuItem item, MenuTree tree, ThreadMarshaller marshaller) {
        super(controller, settings, item, marshaller);
        this.tree = tree;
    }

    @Override
    public Node createComponent() {
        canvas = new AnalogMeterCanvas();
        borderPane = new BorderPane(canvas);
        borderPane.setMaxSize(9999, 9999);
        borderPane.setPrefSize(150, 150);

        canvas.widthProperty().bind(borderPane.widthProperty());
        canvas.heightProperty().bind(borderPane.heightProperty());

        return borderPane;
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

    private class AnalogMeterCanvas extends ResizableCanvas {
        AnalogMeterCanvas() {
            super(AnalogMeterComponent.this);
        }

        @Override
        public boolean isResizable() {
            return true;
        }

        protected void onPaintSurface(GraphicsContext gc) {
            int displayWidth = (int) borderPane.getWidth();
            int displayHeight = (int) borderPane.getHeight();

            double protectedCurrent;
            double max;
            CustomDrawingConfiguration customDrawing = getDrawingSettings().getCustomDrawing();
            if(item instanceof AnalogMenuItem analog) {
                protectedCurrent = (double)currentVal;
                max = analog.getMaxValue();
            } else if(item instanceof FloatMenuItem flt) {
                max = 100.0;
                if(customDrawing instanceof NumberCustomDrawingConfiguration numCust) {
                    var ranges = numCust.getColorRanges();
                    max = ranges.get(ranges.size() - 1).end();
                }
                protectedCurrent = currentVal.doubleValue();
            } else {
                throw new UnsupportedOperationException("Not able to show meter for " + item.getClass().getSimpleName());
            }

            gc.setFill(asFxColor(getDrawingSettings().getColors().backgroundFor(status, ColorComponentType.TEXT_FIELD)));
            gc.fillRect(0, 0, displayWidth, displayHeight);

            gc.setFill(Color.TRANSPARENT);
            gc.setLineWidth(10);

            if(customDrawing instanceof NumberCustomDrawingConfiguration numCust) {
                for(var r : numCust.getColorRanges()) {
                    if (r.start() < protectedCurrent) {
                        gc.setStroke(asFxColor(r.bg()));
                        if(getDrawingSettings().getJustification() == PortableAlignment.LEFT) {
                            var startOffs = -180 + ((r.start() / max) * -270);
                            var extent = ((Math.min(r.end(), protectedCurrent)-r.start()) / max) * -270;
                            gc.strokeArc(8, 8, displayWidth - 16, displayHeight - 16, startOffs, extent, ArcType.OPEN);
                        } else {
                            var startOffs = 270 + ((r.start() / max) * -270);
                            var extent = ((Math.min(r.end(), protectedCurrent)-r.start()) / max) * -270;
                            gc.strokeArc(8, 8, displayWidth - 16, displayHeight - 16,  startOffs, extent, ArcType.OPEN);
                        }

                    }
                }

            } else {
                gc.setStroke(asFxColor(getDrawingSettings().getColors().backgroundFor(lastStatus, ColorComponentType.HIGHLIGHT)));
                double extent = Math.min(2, (protectedCurrent / max)) * -270;
                if (getDrawingSettings().getJustification() == PortableAlignment.LEFT) {
                    gc.strokeArc(8, 8, displayWidth - 16, displayHeight - 16, -180, extent, ArcType.OPEN);
                } else {
                    gc.strokeArc(8, 8, displayWidth - 16, displayHeight - 16, 270, extent, ArcType.OPEN);
                }
            }

            drawTextUsingSettings(gc, protectedCurrent, displayWidth, displayHeight, true);
        }
    }
}