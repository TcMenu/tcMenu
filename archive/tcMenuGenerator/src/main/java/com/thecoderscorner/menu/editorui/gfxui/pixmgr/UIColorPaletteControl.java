package com.thecoderscorner.menu.editorui.gfxui.pixmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.domain.util.PortablePalette;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.*;
import static com.thecoderscorner.menu.domain.util.PortablePalette.PaletteMode;
import static com.thecoderscorner.menu.editorui.gfxui.pixmgr.BitmapImportPopup.EMPTY_PALETTE;


public class UIColorPaletteControl {
    private VBox paletteEntries;
    ScrollPane scrollArea = new ScrollPane();
    private final List<ColorPicker> pickers = new ArrayList<>();

    public Node getControl() {
        return scrollArea;
    }

    public void initializePaletteEntries(PortablePalette palette, int maxSize) {
        paletteEntries = new VBox();
        pickers.clear();
        scrollArea.setContent(paletteEntries);
        scrollArea.setMaxHeight(maxSize);
        scrollArea.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        for(int i = 0; i < palette.getColorArray().length; i++) {
            var col = palette.getColorArray()[i];
            ColorPicker picker = new ColorPicker();
            picker.setValue(ControlColor.asFxColor(col));
            picker.setOnAction(e -> {
                var index = pickers.indexOf(picker);
                PortableColor newColor = ControlColor.fromFxColor(picker.getValue());
                palette.setColorAt(index, newColor);
            });
            if(i == 0) {
                paletteEntries.getChildren().add(new Label("Background"));
            } else if(i == 1) {
                paletteEntries.getChildren().add(new Label("Palette"));
            }
            pickers.add(picker);
            paletteEntries.getChildren().add(picker);
        }
    }
    public PortablePalette paletteFromImage(Image img, NativePixelFormat fmt, double tolerance) {
        // when null return the default
        if(img == null) return createPaletteFor(fmt);

        // otherwise get the palette based on the colors in the image and the tolerance.
        var width = (int) img.getWidth();
        var height = (int) img.getHeight();
        var pixelReader = img.getPixelReader();
        var maxColors = fmt == NativePixelFormat.PALETTE_2BPP ? 4 : 16;
        var colors = new PortableColor[maxColors];
        Arrays.fill(colors, BLACK);
        colors[0] = fromFxColorWithCorrectedOpacity(pixelReader.getColor(0, 0));
        var nextColor = 1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                var color = fromFxColorWithCorrectedOpacity(pixelReader.getColor(x, y));
                if(!hasColorCloseTo(color, colors, nextColor, tolerance) && nextColor < maxColors) {
                    colors[nextColor] = color;
                    nextColor += 1;
                }
            }
            if(nextColor >= maxColors) break;
        }
        return new PortablePalette(colors, maxColors == 4 ? PaletteMode.TWO_BPP : PaletteMode.FOUR_BPP);
    }

    private PortableColor fromFxColorWithCorrectedOpacity(Color color) {
        if(color.getOpacity() != 0) {
            return fromFxColor(color);
        } else {
            return new PortableColor((short)(color.getRed() * 255.0), (short)(color.getGreen() * 255.0), (short)(color.getBlue() * 255.0));
        }
    }

    private boolean hasColorCloseTo(PortableColor color, PortableColor[] colors, int currMax, double tolerance) {
        for(int i=0; i<currMax; i++) {
            var col = colors[i];
            double distance = Math.sqrt(Math.pow(col.getRed() - color.getRed(), 2) +
                    Math.pow(col.getGreen() - color.getGreen(), 2) +
                    Math.pow(col.getBlue() - color.getBlue(), 2));
            if (distance < tolerance) {
                return true;
            }
        }
        return false; // nothing close
    }

    public PortablePalette createPaletteFor(NativePixelFormat fmt) {
        return switch (fmt) {
            case MONO_BITMAP, XBM_LSB_FIRST -> EMPTY_PALETTE;
            case PALETTE_2BPP -> new PortablePalette(new PortableColor[] { PortableColor.BLACK, PortableColor.WHITE, RED, BLUE}, PaletteMode.TWO_BPP);
            case PALETTE_4BPP -> new PortablePalette(new PortableColor[] {
                    PortableColor.BLACK, PortableColor.WHITE, RED, BLUE,
                    GREEN, INDIGO, CORAL, CORNFLOWER_BLUE,
                    ANTIQUE_WHITE, CRIMSON, LIGHT_GRAY, GREY,
                    DARK_SLATE_BLUE, DARK_BLUE, new PortableColor(255, 255, 0), new PortableColor(20, 20, 20)
            }, PaletteMode.FOUR_BPP);
        };
    }

    public void showPopup(PortablePalette pal, HBox hBox, Runnable onClose) {
        var popup = new Popup();
        initializePaletteEntries(pal, 999);

        var close = new Button("Apply");
        close.setOnAction(_ -> {
            popup.hide();
            onClose.run();
        });
        close.setCancelButton(true);
        var vbox = new VBox(4);
        vbox.getChildren().add(paletteEntries);
        vbox.getChildren().add(close);
        vbox.setOpacity(1);
        vbox.setBackground(Background.fill(Color.BLACK));
        vbox.setOpaqueInsets(new Insets(10));
        vbox.setStyle("-fx-background-color: #1f1a1a;-fx-border-style: solid;-fx-border-color: black;-fx-border-width: 2;-fx-background-insets: 6;-fx-padding: 10;-fx-font-size: " + GlobalSettings.defaultFontSize());

        popup.getContent().add(vbox);
        popup.show(hBox.getScene().getWindow());
    }

}
