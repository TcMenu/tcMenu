package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.ControlType;
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
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.ColorComponentType;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.asFxColor;

public class AnalogMeterComponent<T extends Number> extends JfxTextEditorComponentBase<T> implements CanvasDrawableContext  {
    private RenderingStatus lastStatus = RenderingStatus.NORMAL;
    private final MenuTree tree;
    private AnalogCanvas canvas;
    private BorderPane borderPane;

    public AnalogMeterComponent(MenuComponentControl controller, ComponentSettings settings, MenuItem item, MenuTree tree, ThreadMarshaller marshaller) {
        super(controller, settings, item, marshaller);
        this.tree = tree;
    }

    @Override
    public Node createComponent() {
        if(getDrawingSettings().getControlType() == ControlType.VU_METER) {
            canvas = new VuMeterCanvas();
        } else {
            canvas = new AnalogMeterCanvas();
        }
        borderPane = new BorderPane(canvas);
        borderPane.setMaxSize(9999, 9999);

        canvas.widthProperty().bind(borderPane.widthProperty());
        canvas.heightProperty().bind(borderPane.heightProperty());

        borderPane.widthProperty().addListener((e) -> canvas.onPaintSurface(canvas.getGraphicsContext2D()));
        borderPane.heightProperty().addListener((e) -> canvas.onPaintSurface(canvas.getGraphicsContext2D()));

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

    abstract class AnalogCanvas extends ResizableCanvas {
        AnalogCanvas() {
            super(AnalogMeterComponent.this);
        }

        protected void onPaintSurface(GraphicsContext gc) {
            double protectedCurrent;
            double max;
            double displayWidth = canvas.getWidth();
            double displayHeight = canvas.getHeight();

            CustomDrawingConfiguration customDrawing = getDrawingSettings().getCustomDrawing();
            if (item instanceof AnalogMenuItem analog) {
                protectedCurrent = currentVal != null ? currentVal.doubleValue() : 0.0;
                max = analog.getMaxValue();
            } else if (item instanceof FloatMenuItem) {
                max = 100.0;
                if (customDrawing instanceof NumberCustomDrawingConfiguration numCust) {
                    var ranges = numCust.getColorRanges();
                    max = ranges.get(ranges.size() - 1).end();
                }
                protectedCurrent = currentVal != null ? currentVal.doubleValue() : 0.0;
            } else {
                throw new UnsupportedOperationException("Not able to show meter for " + item.getClass().getSimpleName());
            }

            gc.setFill(asFxColor(customDrawing.getColorFor(protectedCurrent, getDrawingSettings().getColors(), status,
                    ColorComponentType.TEXT_FIELD).getBg()));
            gc.fillRect(0, 0, displayWidth, displayHeight);

            internalPaint(gc, max, protectedCurrent, displayWidth, displayHeight);
        }

        @Override
        public boolean isResizable() {
            return true;
        }

        protected abstract void internalPaint(GraphicsContext gc, double maxVal, double currentVal, double displayWidth, double displayHeight);
    }

    private class VuMeterCanvas extends AnalogCanvas {
        @Override
        protected void internalPaint(GraphicsContext gc, double maxVal, double protectedCurrent,
                                     double displayWidth, double displayHeight) {
            var customDrawing = getDrawingSettings().getCustomDrawing();
            gc.setLineWidth(8);
            if (customDrawing instanceof NumberCustomDrawingConfiguration numCust) {
                for(var r : numCust.getColorRanges()) {
                    gc.setStroke(asFxColor(r.fg()));
                    double startOffs;
                    var extent = -((r.end()-r.start()) / maxVal) * 120;
                    startOffs = 150 - ((((r.start()) / maxVal) * 120));
                    gc.strokeArc(8, 8, displayWidth - 16, displayHeight - 16, startOffs, extent, ArcType.OPEN);
                }

            } else {
                gc.setStroke(asFxColor(getDrawingSettings().getColors().backgroundFor(lastStatus, ColorComponentType.HIGHLIGHT)));
                gc.setLineWidth(8);
                gc.strokeArc(0, 10, displayWidth, displayHeight, 30, 120, ArcType.OPEN);

            }

            //range of -60 to 60 degrees

            double deg = -60.0 + ((protectedCurrent / maxVal) * 120);
            double angRads = Math.toRadians(deg);
            double x = (Math.sin(angRads) * (displayWidth / 2)) + (displayWidth / 2);
            double yMag = ((displayHeight - 10) / 2);
            double y = (displayHeight / 2) - (Math.cos(angRads) * yMag);

            gc.setLineWidth(2);
            gc.setStroke(asFxColor(getDrawingSettings().getColors().foregroundFor(RenderingStatus.NORMAL, ColorComponentType.TEXT_FIELD)));
            gc.strokeLine(displayWidth / 2 , displayHeight - 4, x, y);

            drawTextUsingSettings(gc, protectedCurrent, displayWidth, displayHeight, true);
        }
    }

    private class AnalogMeterCanvas extends AnalogCanvas {
        @Override
        protected void internalPaint(GraphicsContext gc, double maxVal, double protectedCurrent, double displayWidth, double displayHeight) {
            var customDrawing = getDrawingSettings().getCustomDrawing();

            gc.setFill(Color.TRANSPARENT);
            gc.setLineWidth(10);

            if(customDrawing instanceof NumberCustomDrawingConfiguration numCust) {
                for(var r : numCust.getColorRanges()) {
                    if (r.start() < protectedCurrent) {
                        gc.setStroke(asFxColor(r.fg()));
                        double startOffs;
                        var extent = ((Math.min(r.end(), protectedCurrent)-r.start()) / maxVal) * -270;
                        if(getDrawingSettings().getJustification() == PortableAlignment.LEFT) {
                            startOffs = -180 + ((r.start() / maxVal) * -270);
                        } else {
                            startOffs = 270 + ((r.start() / maxVal) * -270);
                        }
                        gc.strokeArc(8, 8, displayWidth - 16, displayHeight - 16, startOffs, extent, ArcType.OPEN);

                    }
                }
            } else {
                gc.setStroke(asFxColor(getDrawingSettings().getColors().backgroundFor(lastStatus, ColorComponentType.HIGHLIGHT)));
                double extent = Math.min(2, (protectedCurrent / maxVal)) * -270;
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