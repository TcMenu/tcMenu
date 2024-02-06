package com.thecoderscorner.menu.editorui.gfxui.imgedit;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class ImageDrawingGrid extends Canvas {
    private final Image image;
    private final boolean editMode;
    private double fitWidth;
    private double fitHeight;

    public ImageDrawingGrid(Image image, boolean editMode) {
        this.image = image;
        this.editMode = editMode;
    }

    public boolean isResizable() {
        return true;
    }

    void prepareRatios() {
        double ratio = image.getWidth() / image.getHeight();
        if (image.getWidth() > image.getHeight()) {
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
        var perSquareX = fitWidth / image.getWidth();
        var perSquareY = fitHeight / image.getHeight();

        // first render the image as it appears.
        var pixReader = image.getPixelReader();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                gc.setFill(pixReader.getColor(x, y));
                gc.fillRect(x * perSquareX, y * perSquareY, perSquareX, perSquareY);
            }
        }

        // only use grid squares if the magnification is sufficient.
        if(perSquareX > 3 && perSquareY > 3) {
            // now draw the grid ines into the image
            gc.setFill(Color.GRAY);
            for (int y = 0; y < image.getHeight(); y++) {
                gc.moveTo(0, y * perSquareY);
                gc.lineTo(this.getHeight(), y * perSquareY);
            }

            for (int x = 0; x < image.getWidth(); x++) {
                gc.moveTo(x * perSquareX, 0);
                gc.lineTo(x * perSquareX, this.getWidth());
            }
        }
    }
}
