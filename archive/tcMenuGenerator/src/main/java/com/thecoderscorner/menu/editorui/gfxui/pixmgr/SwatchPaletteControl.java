package com.thecoderscorner.menu.editorui.gfxui.pixmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.menu.domain.util.PortablePalette;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.function.Consumer;

public class SwatchPaletteControl {
    private ArrayList<Rectangle> listOfSwatches;
    private HBox hBox;
    private Consumer<Integer> colorConsumer;
    private boolean paletteIsEditable;
    private PortablePalette palette;

    public Node swatchControl(PortablePalette pal, Consumer<Integer> colorConsumer, boolean paletteIsEditable) {
        this.colorConsumer = colorConsumer;
        this.paletteIsEditable = paletteIsEditable;
        this.palette = pal;
        hBox = new HBox(2);
        listOfSwatches = new ArrayList<>();
        populateColorsIntoBox(pal, listOfSwatches, hBox);
        hBox.setOnMouseClicked(event -> {
            var swatch = (Rectangle)event.getTarget();
            presentCurrentIndex(swatch);
            var swatchIndex = listOfSwatches.indexOf(swatch);
            if(swatchIndex != -1 && swatchIndex < pal.getNumColors()) {
                if(event.getClickCount() > 1) {
                    showPaletteEditor(pal);
                } else {
                    colorConsumer.accept(swatchIndex);
                }
            }
        });
        return hBox;
    }

    private void presentCurrentIndex(Rectangle newSel) {
        for(var r : listOfSwatches) {
            r.setStrokeWidth(0);
        }
        newSel.setStroke(Color.BLACK);
        newSel.setStrokeWidth(2);
    }

    public void showPaletteEditor(PortablePalette pal) {
        if(paletteIsEditable) {
            var colorPaletteControl = new UIColorPaletteControl();
            colorPaletteControl.showPopup(pal, hBox, () -> populateColorsIntoBox(pal, listOfSwatches, hBox));
        }
    }

    public void onExternalPaletteChange(int index) {
        if(index <= 0) {
            showPaletteEditor(palette);
        } else index--;
        if(index < listOfSwatches.size()) {
            presentCurrentIndex(listOfSwatches.get(index));
            colorConsumer.accept(index);
        }
    }

    private static void populateColorsIntoBox(PortablePalette pal, ArrayList<Rectangle> listOfSwatches, HBox hBox) {
        hBox.getChildren().clear();
        listOfSwatches.clear();
        for(var col : pal.getColorArray()) {
            int size = GlobalSettings.defaultFontSize() * 2;
            var r = new Rectangle(size, size, ControlColor.asFxColor(col));
            listOfSwatches.add(r);
            hBox.getChildren().add(r);
        }
    }

}
