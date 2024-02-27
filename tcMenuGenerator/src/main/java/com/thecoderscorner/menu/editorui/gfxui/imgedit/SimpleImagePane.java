package com.thecoderscorner.menu.editorui.gfxui.imgedit;

import com.thecoderscorner.menu.domain.util.PortablePalette;
import com.thecoderscorner.menu.editorui.gfxui.pixmgr.BmpDataManager;
import com.thecoderscorner.menu.editorui.gfxui.pixmgr.NativePixelFormat;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.List;

public class SimpleImagePane extends BorderPane {
    private final ImageDrawingGrid canvas;

    public SimpleImagePane(BmpDataManager bitmap, NativePixelFormat format, boolean editable, PortablePalette palette,
                           List<Button> actionButtons) {
        Image image = bitmap.createImageFromBitmap(palette);
        setMaxSize(99999, 99999);

        canvas = new ImageDrawingGrid(bitmap, palette, editable);
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty().multiply(0.9));

        widthProperty().addListener((_) -> canvas.onPaintSurface(canvas.getGraphicsContext2D()));
        heightProperty().addListener((_) -> canvas.onPaintSurface(canvas.getGraphicsContext2D()));

        this.setCenter(canvas);
        var hbox = new HBox(4);
        hbox.getChildren().add(new Label(STR."\{shortFmtText(format)}: \{image.getWidth()} x \{image.getHeight()}"));
        hbox.getChildren().addAll(actionButtons);
        this.setTop(hbox);

        canvas.onPaintSurface(canvas.getGraphicsContext2D());
    }

    public static String shortFmtText(NativePixelFormat fmtCode) {
        return switch (fmtCode) {
            case XBM_LSB_FIRST -> "XBMP";
            case MONO_BITMAP -> "MONO";
            case PALETTE_2BPP -> "2BPP(4)";
            case PALETTE_4BPP -> "4BPP(16)";
        };
    }

    public void invalidate() {
        canvas.onPaintSurface(canvas.getGraphicsContext2D());
    }

    public ImageDrawingGrid getDrawingGrid() {
        return canvas;
    }
}
