package com.thecoderscorner.menu.editorui.gfxui.imgedit;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.menu.domain.util.PortablePalette;
import com.thecoderscorner.menu.editorui.gfxui.pixmgr.BmpDataManager;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.function.BiConsumer;

/**
 * Heavily based on Adafruit_GFX code, this edits a bitmap in a way similar to how Adafruit GFX draws onto native
 * displays. It is done this way as we must avoid any aliasing whatsoever in graphics libraries given these bitmaps
 * are going to be used later potentially on native displays.
 */
public class ImageDrawingGrid extends Canvas {
    public enum DrawingMode { NONE, DOT, LINE, OUTLINE_RECT, FILLED_RECT, OUTLINE_CIRCLE, FLOOD_FILL }
    private final BmpDataManager bitmap;
    private final PortablePalette palette;
    private final boolean editMode;
    private BiConsumer<Integer, Integer> positionConsumer;
    private DrawingMode mode = DrawingMode.NONE;
    private DrawingMode currentShape = DrawingMode.LINE;
    private int colorIndex = 0;
    private double fitWidth;
    private double fitHeight;
    private int xStart, yStart;
    private int xNow, yNow;
    private boolean dirty = false;

    public ImageDrawingGrid(BmpDataManager bitmap, PortablePalette palette, boolean editMode) {
        this.bitmap = bitmap;
        this.palette = palette;
        this.editMode = editMode;

        if(editMode) {
            setOnMousePressed(event -> {
                xStart = (int) (event.getX() / fitWidth * bitmap.getPixelWidth());
                yStart = (int) (event.getY() / fitHeight * bitmap.getPixelHeight());
                mode = currentShape == DrawingMode.FLOOD_FILL ? DrawingMode.FLOOD_FILL : DrawingMode.DOT;
            });

            setOnMouseDragged(event -> {
                mode = currentShape;

                xNow = (int) (event.getX() / fitWidth * bitmap.getPixelWidth());
                yNow = (int) (event.getY() / fitHeight * bitmap.getPixelHeight());
                if (xNow  >= bitmap.getPixelWidth() || yNow >= bitmap.getPixelHeight()) return;
                onPaintSurface(getGraphicsContext2D());
            });

            setOnMouseMoved(event -> {
                int xEnd = (int) (event.getX() / fitWidth * bitmap.getPixelWidth());
                int yEnd = (int) (event.getY() / fitHeight * bitmap.getPixelHeight());
                if(positionConsumer != null) positionConsumer.accept(xEnd, yEnd);
            });

            setOnMouseReleased(event -> {
                int xEnd = (int) (event.getX() / fitWidth * bitmap.getPixelWidth());
                int yEnd = (int) (event.getY() / fitHeight * bitmap.getPixelHeight());
                if (xEnd >= bitmap.getPixelWidth() || yEnd >= bitmap.getPixelHeight()) return;
                if(mode == DrawingMode.DOT) {
                    recordChange();
                    bitmap.setDataAt(xEnd, yEnd, colorIndex);
                    onPaintSurface(getGraphicsContext2D());
                } else if(mode == DrawingMode.FILLED_RECT) {
                    recordChange();
                    filledRectangle(bitmap, xEnd, yEnd);
                } else if(mode == DrawingMode.OUTLINE_CIRCLE) {
                    recordChange();
                    int halfX = (xEnd - xStart) / 2;
                    drawCircle(xStart + halfX, yStart + halfX, halfX);
                } else if(mode == DrawingMode.OUTLINE_RECT) {
                    recordChange();
                    drawBoxOutline(xEnd, yEnd);
                } else if(mode == DrawingMode.LINE) {
                    recordChange();
                    drawLine(xStart, yStart, xEnd, yEnd);
                } else if(mode == DrawingMode.FLOOD_FILL) {
                    recordChange();
                    floodFill(xEnd, yEnd, bitmap.getDataAt(xEnd, yEnd));
                }
                mode = DrawingMode.NONE;
                onPaintSurface(getGraphicsContext2D());
            });
        }
    }

    private void floodFill(int x, int y, int startingCol) {
        // make sure we are inside bounds, and still able to fill, IE colour is still the starting colour
        if(x < 0 || y < 0 || x >= bitmap.getPixelWidth() || y >= bitmap.getPixelHeight()) return;
        if(bitmap.getDataAt(x, y) != startingCol) return;

        bitmap.setDataAt(x, y, colorIndex);
        floodFill(x, y + 1, startingCol);
        floodFill(x, y - 1, startingCol);
        floodFill(x  - 1, y, startingCol);
        floodFill(x  + 1, y, startingCol);
    }

    private void drawBoxOutline(int xEnd, int yEnd) {
        drawLine(xStart, yStart, xEnd, yStart);
        drawLine(xEnd, yStart, xEnd, yEnd);
        drawLine(xStart, yEnd, xEnd, yEnd);
        drawLine(xStart, yStart, xStart, yEnd);
    }

    private void drawCircle(int x0, int y0, int r) {
        int f = 1 - r;
        int ddF_x = 1;
        int ddF_y = -2 * r;
        int x = 0;
        int y = r;

        bitmap.setDataAt(x0, y0 + r, colorIndex);
        bitmap.setDataAt(x0, y0 - r, colorIndex);
        bitmap.setDataAt(x0 + r, y0, colorIndex);
        bitmap.setDataAt(x0 - r, y0, colorIndex);

        while (x < y) {
            if (f >= 0) {
                y--;
                ddF_y += 2;
                f += ddF_y;
            }
            x++;
            ddF_x += 2;
            f += ddF_x;

            bitmap.setDataAt(x0 + x, y0 + y, colorIndex);
            bitmap.setDataAt(x0 - x, y0 + y, colorIndex);
            bitmap.setDataAt(x0 + x, y0 - y, colorIndex);
            bitmap.setDataAt(x0 - x, y0 - y, colorIndex);
            bitmap.setDataAt(x0 + y, y0 + x, colorIndex);
            bitmap.setDataAt(x0 - y, y0 + x, colorIndex);
            bitmap.setDataAt(x0 + y, y0 - x, colorIndex);
            bitmap.setDataAt(x0 - y, y0 - x, colorIndex);
        }
    }

    private void recordChange() {
        dirty = true;
    }

    void setCurrentShape(DrawingMode shape) {
        currentShape = shape;
    }

    private void filledRectangle(BmpDataManager bitmap, int xEnd, int yEnd) {
        int xMin = Math.min(xStart, xEnd);
        int yMin = Math.min(yStart, yEnd);
        int xMax = Math.max(xStart, xEnd);
        int yMax = Math.max(yStart, yEnd);
        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                bitmap.setDataAt(x, y, colorIndex);
            }
        }
    }

    private void drawLine(int x0, int y0, int x1, int y1) {
        // roughly copied from Adafruit_GFX, Thanks!
        boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);
        if (steep) {
            var tmp = y0;
            y0 = x0;
            x0 = tmp;

            tmp = y1;
            y1 = x1;
            x1 = tmp;
        }

        if (x0 > x1) {
            var tmp = x0;
            x0 = x1;
            x1 = tmp;

            tmp = y0;
            y0 = y1;
            y1 = tmp;
        }

        int dx, dy;
        dx = x1 - x0;
        dy = Math.abs(y1 - y0);

        int err = dx / 2;
        int ystep;

        if (y0 < y1) {
            ystep = 1;
        } else {
            ystep = -1;
        }

        for (; x0 <= x1; x0++) {
            if (steep) {
                bitmap.setDataAt(y0, x0, colorIndex);
            } else {
                bitmap.setDataAt(x0, y0, colorIndex);
            }
            err -= dy;
            if (err < 0) {
                y0 += ystep;
                err += dx;
            }
        }
    }

    public void setCurrentColor(int paletteIdx) {
        colorIndex = paletteIdx;
    }

    public boolean isResizable() {
        return true;
    }

    void prepareRatios() {
        double ratio = (double) bitmap.getPixelWidth() / (double) bitmap.getPixelHeight();
        if (bitmap.getPixelWidth() > bitmap.getPixelHeight()) {
            double maxWid = this.getWidth();
            fitWidth = maxWid;
            fitHeight = (maxWid / ratio);
        } else {
            double maxHei = this.getHeight();
            fitWidth = (maxHei * ratio);
            fitHeight = maxHei;
        }
    }

    protected void onPaintSurface(GraphicsContext gc) {
        prepareRatios();
        var perSquareX = fitWidth / bitmap.getPixelWidth();
        var perSquareY = fitHeight / bitmap.getPixelHeight();

        // first render the image as it appears.
        for (int y = 0; y < bitmap.getPixelHeight(); y++) {
            for (int x = 0; x < bitmap.getPixelWidth(); x++) {
                gc.setFill(ControlColor.asFxColor(palette.getColorAt(bitmap.getDataAt(x, y))));
                gc.fillRect(x * perSquareX, y * perSquareY, perSquareX + 0.6, perSquareY + 0.6);
            }
        }

        if(!editMode) return;

        gc.setStroke(Color.BLUE);
        gc.setLineWidth(3);
        gc.setLineDashes(10, 3);

        double wid = ((xNow - xStart) + 1) * perSquareX;
        double hei = ((yNow - yStart) + 1) * perSquareY;
        if(mode == DrawingMode.FILLED_RECT || mode == DrawingMode.OUTLINE_RECT || mode == DrawingMode.OUTLINE_CIRCLE) {
            gc.strokeRect(xStart * perSquareX, yStart * perSquareY, wid, hei);
        } else if(mode == DrawingMode.LINE) {
            gc.strokeLine(xStart * perSquareX, yStart * perSquareY, xNow * perSquareX, yNow * perSquareY);
        }

        // only use grid squares if the magnification is sufficient.
        if(perSquareX > 3 && perSquareY > 3) {
            gc.setLineDashes();
            gc.setStroke(Color.LIGHTGRAY);
            gc.setLineWidth(1);
            gc.setLineDashes(4, 2);

            // now draw the grid ines into the image
            for (int y = 0; y < bitmap.getPixelHeight(); y++) {
                gc.strokeLine(0, y * perSquareY, fitWidth, y * perSquareY);
            }

            for (int x = 0; x < bitmap.getPixelWidth(); x++) {
                gc.strokeLine(x * perSquareX, 0, x * perSquareX, fitHeight);
            }
        }
    }

    public boolean isModified() {
        return dirty;
    }

    public void setPositionUpdateListener(BiConsumer<Integer, Integer> consumer) {
        positionConsumer = consumer;
    }
}
