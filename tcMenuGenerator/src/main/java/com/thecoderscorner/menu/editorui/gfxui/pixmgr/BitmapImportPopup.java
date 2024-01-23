package com.thecoderscorner.menu.editorui.gfxui.pixmgr;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.domain.util.PortablePalette;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class BitmapImportPopup {
    public static final PortablePalette EMPTY_PALETTE = new PortablePalette(
            new PortableColor[] { PortableColor.BLACK, PortableColor.WHITE}, PortablePalette.PaletteMode.ONE_BPP
    );
    private final Image loadedImage;
    private final int slot;
    private PortablePalette palette = EMPTY_PALETTE;
    private int tolerance;
    private boolean applyAlpha;
    private NativePixelFormat pixelFormat;

    public BitmapImportPopup(Image loadedImage, int slot) {
        this.loadedImage = loadedImage;
        this.slot = slot;
    }

    private RowConstraints priorityRowConstraint(Priority priority) {
        var c = new RowConstraints();
        c.setVgrow(priority);
        return c;
    }

    private ColumnConstraints priorityColConstraint(Priority priority) {
        var c = new ColumnConstraints();
        c.setHgrow(priority);
        return c;
    }

    public void showConfigSetup(Stage where, Consumer<BitmapImportPopup> importContinuation) {
        var popup = new Popup();
        var grid = new GridPane();
        grid.setVgap(4);
        grid.setHgap(4);
        grid.getColumnConstraints().add(priorityColConstraint(Priority.SOMETIMES));
        grid.getColumnConstraints().add(priorityColConstraint(Priority.NEVER));
        for(int i=0; i<10; i++) {
            grid.getRowConstraints().add(priorityRowConstraint(Priority.NEVER));
        }

        int row = 0;
        UIColorPaletteControl paletteControl = new UIColorPaletteControl();
        grid.add(new Label("Settings for Image Import"), 0, row++);
        grid.add(new ImageView(loadedImage), 0, row, 1, 8);

        grid.add(new Label("Output Format"), 1, row++);
        var pixelFormatCombo = new ComboBox<NativePixelFormat>();
        pixelFormatCombo.setItems(FXCollections.observableArrayList(NativePixelFormat.values()));
        pixelFormatCombo.getSelectionModel().select(0);
        grid.add(pixelFormatCombo, 1, row++);

        var cbxTolerance = new ComboBox<>(FXCollections.observableArrayList(1, 5, 10, 15, 20));

        pixelFormatCombo.setOnAction(event -> {
            if(pixelFormatCombo.getValue() == NativePixelFormat.PALETTE_2BPP || pixelFormatCombo.getValue() == NativePixelFormat.PALETTE_4BPP) {
                NativePixelFormat fmt = pixelFormatCombo.getValue() == NativePixelFormat.PALETTE_2BPP ? NativePixelFormat.PALETTE_2BPP : NativePixelFormat.PALETTE_4BPP;
                palette = paletteControl.paletteFromImage(loadedImage, fmt, cbxTolerance.getValue() / 100.0);

            }
            else {
                palette = paletteControl.createPaletteFor(pixelFormatCombo.getValue());
            }
            paletteControl.initializePaletteEntries(palette, 350);
        });

        grid.add(new Label("Tolerance %"), 1, row++);
        cbxTolerance.setMaxWidth(99999);
        cbxTolerance.getSelectionModel().select(2);
        grid.add(cbxTolerance, 1, row++);

        var useAlphaCheck = new CheckBox("Alpha channel");
        grid.add(useAlphaCheck, 1, row++);

        paletteControl.initializePaletteEntries(palette, 350);
        grid.add(paletteControl.getControl(), 1, row++);

        ButtonBar buttonBar = new ButtonBar();
        Button importButton = new Button("Import");
        importButton.setDefaultButton(true);
        buttonBar.getButtons().add(importButton);
        Button cancelButton = new Button("Cancel");
        cancelButton.setCancelButton(true);
        buttonBar.getButtons().add(cancelButton);
        grid.add(buttonBar, 0, row, 2, 1);
        grid.setStyle("-fx-background-color: #1f1a1a;-fx-border-style: solid;-fx-border-color: black;-fx-border-width: 2;-fx-background-insets: 6;-fx-padding: 10;-fx-font-size: " + GlobalSettings.defaultFontSize());

        popup.getContent().add(grid);
        popup.show(where);

        importButton.setOnAction(event -> {
            tolerance = cbxTolerance.getValue();
            applyAlpha = useAlphaCheck.isSelected();
            pixelFormat = pixelFormatCombo.getValue();
            importContinuation.accept(this);
            popup.hide();
        });

        cancelButton.setOnAction(event -> popup.hide());
    }

    public Image getImage() {
        return loadedImage;
    }

    public int getSlot() {
        return slot;
    }

    public PortablePalette getPalette() {
        return palette;
    }

    public int getTolerance() {
        return tolerance;
    }

    public boolean isApplyAlpha() {
        return applyAlpha;
    }

    public NativePixelFormat getPixelFormat() {
        return pixelFormat;
    }
}
