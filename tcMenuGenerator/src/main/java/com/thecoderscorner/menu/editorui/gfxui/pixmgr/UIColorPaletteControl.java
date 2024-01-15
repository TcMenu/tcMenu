package com.thecoderscorner.menu.editorui.gfxui.pixmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.domain.util.PortablePalette;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.*;
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

    public Node swatchControl(PortablePalette pal) {
        HBox hBox = new HBox(2);
        for(var col : pal.getColorArray()) {
            hBox.getChildren().add(new Rectangle(20, 20, ControlColor.asFxColor(col)));
        }
        return hBox;
    }

    public PortablePalette createPaletteFor(NativePixelFormat fmt) {
        return switch (fmt) {
            case MONO_BITMAP, XBM_LSB_FIRST -> EMPTY_PALETTE;
            case PALETTE_2BPP -> new PortablePalette(new PortableColor[] { PortableColor.BLACK, PortableColor.WHITE, RED, BLUE}, PortablePalette.PaletteMode.TWO_BPP);
            case PALETTE_4BPP -> new PortablePalette(new PortableColor[] {
                    PortableColor.BLACK, PortableColor.WHITE, RED, BLUE,
                    GREEN, INDIGO, CORAL, CORNFLOWER_BLUE,
                    ANTIQUE_WHITE, CRIMSON, LIGHT_GRAY, GREY,
                    DARK_SLATE_BLUE, DARK_BLUE, new PortableColor(255, 255, 0), new PortableColor(20, 20, 20)
            }, PortablePalette.PaletteMode.FOUR_BPP);
        };
    }
}
