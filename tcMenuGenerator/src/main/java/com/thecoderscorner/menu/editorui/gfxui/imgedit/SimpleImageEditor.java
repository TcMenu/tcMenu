package com.thecoderscorner.menu.editorui.gfxui.imgedit;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.domain.util.PortablePalette;
import com.thecoderscorner.menu.editorui.dialog.BaseDialogSupport;
import com.thecoderscorner.menu.editorui.gfxui.pixmgr.BmpDataManager;
import com.thecoderscorner.menu.editorui.gfxui.pixmgr.NativePixelFormat;
import com.thecoderscorner.menu.editorui.gfxui.pixmgr.SwatchPaletteControl;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

import static com.thecoderscorner.menu.editorui.gfxui.imgedit.ImageDrawingGrid.DrawingMode.*;
import static com.thecoderscorner.menu.editorui.gfxui.imgedit.SimpleImagePane.shortFmtText;

public class SimpleImageEditor {
    private final BmpDataManager bitmap;
    private final PortablePalette palette;
    private final NativePixelFormat format;
    private final EditingMode editingMode;
    private CurrentProjectEditorUI editorUI;
    private ComboBox<TextDrawingMode> modeComboBox;
    private ImageDrawingGrid canvas;

    public enum EditingMode {
        FONT_EDITOR, BITMAP_EDITOR
    }

    public SimpleImageEditor(BmpDataManager bitmap, NativePixelFormat format, PortablePalette palette, EditingMode editingMode) {
        this.bitmap = bitmap;
        this.palette = palette;
        this.format = format;
        this.editingMode = editingMode;
    }

    public boolean presentUI(CurrentProjectEditorUI editorUI) {
        BorderPane pane = new BorderPane();
        pane.setOpaqueInsets(new Insets(10));
        this.editorUI = editorUI;

        HBox hbox = new HBox(4);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.getChildren().add(new Label("Function"));
        pane.setTop(hbox);
        canvas = new ImageDrawingGrid(bitmap, palette, true);
        modeComboBox = new ComboBox<>(FXCollections.observableArrayList(
                new TextDrawingMode("Pixel - D", DOT),
                new TextDrawingMode("Line - L", LINE),
                new TextDrawingMode("Box Outline - R", OUTLINE_RECT),
                new TextDrawingMode("Box Filled - B", FILLED_RECT),
                new TextDrawingMode("Circle - I", OUTLINE_CIRCLE),
                new TextDrawingMode("Flood Fill - F", FLOOD_FILL),
                new TextDrawingMode("Selection - E", SELECTION)
        ));
        modeComboBox.getSelectionModel().select(0);
        modeComboBox.setOnAction(_ -> canvas.setCurrentShape(modeComboBox.getValue().mode()));
        hbox.getChildren().add(modeComboBox);

        hbox.getChildren().add(new Label("Palette"));
        var paletteControl = new SwatchPaletteControl();
        boolean paletteIsEditable = editingMode == EditingMode.BITMAP_EDITOR;
        hbox.getChildren().add(paletteControl.swatchControl(palette, canvas::setCurrentColor, paletteIsEditable));

        var cutButton = new Button("", new ImageView(getClass().getResource("/img/tree-cut.png").toString()));
        cutButton.setOnAction(_ -> copyContents(true));
        var copyButton = new Button("", new ImageView(getClass().getResource("/img/tree-copy.png").toString()));
        copyButton.setOnAction(_ -> copyContents(false));
        var pasteButton = new Button("", new ImageView(getClass().getResource("/img/tree-paste.png").toString()));
        pasteButton.setOnAction(_ -> pasteSelection());
        var saveButton = new Button("", new ImageView(getClass().getResource("/img/disk-save.png").toString()));
        saveButton.setOnAction(_ -> saveContents());
        var closeButton = new Button("Close");
        closeButton.setOnAction(_ -> {
            if(editingMode == EditingMode.BITMAP_EDITOR &&  canvas.isDirty()) {
                if(editorUI.questionYesNo("Image not saved?", "The image has been changed, do you want to save?")) {
                    saveContents();
                }
            }
            ((Stage)canvas.getScene().getWindow()).close();
        });
        var xyLabel = new Label("");
        canvas.setPositionUpdateListener((x, y) -> xyLabel.setText(STR."X=\{x}, Y=\{y}"));
        hbox.getChildren().addAll(cutButton, copyButton, pasteButton, saveButton, closeButton, xyLabel);

        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty().multiply(0.9));

        pane.widthProperty().addListener((_) -> canvas.onPaintSurface(canvas.getGraphicsContext2D()));
        pane.heightProperty().addListener((_) -> canvas.onPaintSurface(canvas.getGraphicsContext2D()));

        pane.setCenter(canvas);
        pane.getStyleClass().add("background");
        pane.setStyle(STR."-fx-font-size: \{GlobalSettings.defaultFontSize()}");

        Scene scene = new Scene(pane);

        KeyCombination cutPressed = new KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN);
        KeyCombination copyPressed = new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN);
        KeyCombination pastePressed = new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN);
        KeyCombination savePressed = new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN);
        KeyCombination dotPressed = new KeyCodeCombination(KeyCode.D);
        KeyCombination linePressed = new KeyCodeCombination(KeyCode.L);
        KeyCombination circlePressed = new KeyCodeCombination(KeyCode.I);
        KeyCombination boxPressed = new KeyCodeCombination(KeyCode.B);
        KeyCombination rectPressed = new KeyCodeCombination(KeyCode.R);
        KeyCombination fillPressed = new KeyCodeCombination(KeyCode.F);
        KeyCombination selectionPressed = new KeyCodeCombination(KeyCode.E);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (cutPressed.match(ke)) copyContents(true);
            else if (copyPressed.match(ke)) copyContents(false);
            else if (pastePressed.match(ke)) pasteSelection();
            else if(savePressed.match(ke)) saveContents();
            else if(dotPressed.match(ke)) changeShape(DOT);
            else if(linePressed.match(ke)) changeShape(LINE);
            else if(circlePressed.match(ke)) changeShape(OUTLINE_CIRCLE);
            else if(boxPressed.match(ke)) changeShape(FILLED_RECT);
            else if(rectPressed.match(ke)) changeShape(OUTLINE_RECT);
            else if(fillPressed.match(ke)) changeShape(FLOOD_FILL);
            else if(selectionPressed.match(ke)) changeShape(SELECTION);
            else if(ke.getCode().getCode() >= KeyCode.DIGIT0.getCode() && ke.getCode().getCode() <= KeyCode.DIGIT9.getCode()) {
                int palEnt = ke.getCode().getCode() - KeyCode.DIGIT0.getCode();
                paletteControl.onExternalPaletteChange(palEnt);
            }
        });
        Stage stage = new Stage();
        stage.setMaximized(true);
        stage.setWidth(800);
        stage.setWidth(600);
        stage.setScene(scene);
        stage.setTitle(STR."Bitmap Editor \{shortFmtText(format)} \{bitmap.getPixelWidth()} x \{bitmap.getPixelHeight()}");
        BaseDialogSupport.getJMetro().setScene(scene);
        stage.setOnCloseRequest(_ -> {
            if(editingMode == EditingMode.BITMAP_EDITOR && canvas.isDirty()) {
                if(editorUI.questionYesNo("Save Image?", "The image is dirty do you want to save?")) {
                    saveContents();
                }
            }
        });
        stage.showAndWait();
        return canvas.isChanged();
    }

    private void changeShape(ImageDrawingGrid.DrawingMode drawingMode) {
        for(int i=0;i<modeComboBox.getItems().size(); i++) {
            if (modeComboBox.getItems().get(i).mode() == drawingMode) {
                modeComboBox.getSelectionModel().select(i);
                break;
            }
        }
    }

    private void saveContents() {
        var file = editorUI.findFileNameFromUser(Optional.empty(), false, "*.png");
        if(file.isEmpty()) return;
        try {
            var img = bitmap.createImageFromBitmap(palette);
            canvas.markAsSaved();
            ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", Paths.get(file.get()).toFile());
        } catch (IOException e) {
            editorUI.alertOnError("Error Saving Image", "An error occurred while saving the image.");
        }
    }

    private void copyContents(boolean cut) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        if(canvas.getImageSelection() != null) {
            var sel = canvas.getImageSelection();
            BmpDataManager newBitmap = bitmap.segmentOf(sel.getXMin(), sel.getYMin(), sel.getXMax(), sel.getYMax());
            content.putImage(newBitmap.createImageFromBitmap(palette));
            clipboard.setContent(content);

            if(cut) {
                canvas.filledRectangle(bitmap, sel.getXMin(), sel.getYMin(), sel.getXMax() - 1, sel.getYMax() - 1);
                canvas.onPaintSurface(canvas.getGraphicsContext2D());
            }

        } else {
            // copy the whole image to the clipboard, no selection
            content.putImage(bitmap.createImageFromBitmap(palette));

            if(cut) {
                canvas.filledRectangle(bitmap, 0, 0, bitmap.getPixelWidth() - 1, bitmap.getPixelHeight() - 1);
                canvas.onPaintSurface(canvas.getGraphicsContext2D());
            }
        }
        clipboard.setContent(content);
    }

    private void pasteSelection() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasImage() && canvas.getImageSelection() != null) {
            var image = clipboard.getImage();
            PixelReader reader = image.getPixelReader();

            BmpDataManager bitmapProcessor = bitmap.createNew((int) image.getWidth(), (int) image.getHeight());
            bitmapProcessor.convertToBits((x, y) -> {
                var col = PortableColor.asPortableColor(reader.getArgb(x, y));
                return palette.getClosestColorIndex(col, 1, false);
            });

            var sel = canvas.getImageSelection();
            bitmap.pushBitsRaw(sel.getXMin(), sel.getYMin(), bitmapProcessor);
            canvas.onPaintSurface(canvas.getGraphicsContext2D());
        }
    }

    record TextDrawingMode(String name, ImageDrawingGrid.DrawingMode mode) {
        @Override
        public String toString() {
            return name;
        }
    }
}
