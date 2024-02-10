package com.thecoderscorner.menu.editorui.gfxui.pixmgr;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.domain.util.PortablePalette;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.function.Consumer;

public class BitmapImportPopup {
    public static final PortablePalette EMPTY_PALETTE = new PortablePalette(
            new PortableColor[] { PortableColor.BLACK, PortableColor.WHITE}, PortablePalette.PaletteMode.ONE_BPP
    );
    private Image loadedImage;
    private PortablePalette palette = EMPTY_PALETTE;
    private int tolerance;
    private boolean applyAlpha;
    private NativePixelFormat pixelFormat;

    public BitmapImportPopup(Image loadedImage) {
        this.loadedImage = loadedImage;
    }

    public BitmapImportPopup() {
        this.loadedImage = null;
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

    public void showNewBitmap(Stage where, Consumer<BitmapImportPopup> importContinuation) {
        var popup = new Popup();
        var grid = new GridPane();
        grid.setVgap(4);
        grid.setHgap(4);
        grid.getColumnConstraints().add(priorityColConstraint(Priority.NEVER));
        grid.getColumnConstraints().add(priorityColConstraint(Priority.SOMETIMES));
        for(int i=0; i<10; i++) {
            grid.getRowConstraints().add(priorityRowConstraint(Priority.NEVER));
        }

        int row = 0;

        grid.add(new Label("Width Pixels"), 0, row);
        Spinner<Integer> widthSpinner = new Spinner<>(1, 256, 32);
        grid.add(widthSpinner, 1, row++);
        grid.add(new Label("Height Pixels"), 0, row);
        Spinner<Integer> heightSpinner = new Spinner<>(1, 256, 32);
        grid.add(heightSpinner, 1, row++);

        grid.add(new Label("Palette Mode"), 0, row);
        var pixelFormatCombo = new ComboBox<NativePixelFormat>();
        pixelFormatCombo.setItems(FXCollections.observableArrayList(NativePixelFormat.values()));
        pixelFormatCombo.getSelectionModel().select(0);
        grid.add(pixelFormatCombo, 1, row++);

        Button createButton = createButtonBar("Create", grid, row, popup);

        createButton.setOnAction(event -> {
            tolerance = 0;
            applyAlpha = false;
            pixelFormat = pixelFormatCombo.getValue();
            palette = new UIColorPaletteControl().createPaletteFor(pixelFormatCombo.getValue());
            loadedImage = generateImage(widthSpinner.getValue(), heightSpinner.getValue(), palette.getColorAt(0));
            importContinuation.accept(this);
            popup.hide();
        });

        popup.getContent().add(grid);
        popup.show(where);
    }

    private Image generateImage(Integer width, Integer height, PortableColor color) {
        WritableImage img = new WritableImage(width, height);
        PixelWriter pw = img.getPixelWriter();

        int[] pixels = new int[width * height];
        Arrays.fill(pixels, color.asArgb());

        pw.setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
        return img ;
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
        if(loadedImage != null) {
            grid.add(new ImageView(loadedImage), 0, row, 1, 8);
        }
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

        Button importButton = createButtonBar("Import", grid, row, popup);

        popup.getContent().add(grid);
        popup.show(where);

        importButton.setOnAction(event -> {
            tolerance = cbxTolerance.getValue();
            applyAlpha = useAlphaCheck.isSelected();
            pixelFormat = pixelFormatCombo.getValue();
            importContinuation.accept(this);
            popup.hide();
        });

    }

    private static Button createButtonBar(String actionTxt, GridPane grid, int row, Popup popup) {
        ButtonBar buttonBar = new ButtonBar();
        Button importButton = new Button(actionTxt);
        importButton.setDefaultButton(true);
        buttonBar.getButtons().add(importButton);
        Button cancelButton = new Button("Cancel");
        cancelButton.setCancelButton(true);
        buttonBar.getButtons().add(cancelButton);
        grid.add(buttonBar, 0, row, 2, 1);
        grid.setStyle("-fx-background-color: #1f1a1a;-fx-border-style: solid;-fx-border-color: black;-fx-border-width: 2;-fx-background-insets: 6;-fx-padding: 10;-fx-font-size: " + GlobalSettings.defaultFontSize());
        cancelButton.setOnAction(event -> popup.hide());
        return importButton;
    }

    public Image getImage() {
        return loadedImage;
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
