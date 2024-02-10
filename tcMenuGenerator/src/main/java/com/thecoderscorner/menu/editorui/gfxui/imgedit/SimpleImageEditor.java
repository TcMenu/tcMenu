package com.thecoderscorner.menu.editorui.gfxui.imgedit;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.menu.domain.util.PortablePalette;
import com.thecoderscorner.menu.editorui.dialog.BaseDialogSupport;
import com.thecoderscorner.menu.editorui.gfxui.pixmgr.BmpDataManager;
import com.thecoderscorner.menu.editorui.gfxui.pixmgr.NativePixelFormat;
import com.thecoderscorner.menu.editorui.gfxui.pixmgr.UIColorPaletteControl;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
    private CurrentProjectEditorUI editorUI;
    private ComboBox<TextDrawingMode> modeComboBox;

    public SimpleImageEditor(BmpDataManager bitmap, NativePixelFormat format, PortablePalette palette) {
        this.bitmap = bitmap;
        this.palette = palette;
        this.format = format;
    }

    public boolean presentUI(CurrentProjectEditorUI editorUI) {
        BorderPane pane = new BorderPane();
        pane.setOpaqueInsets(new Insets(10));
        this.editorUI = editorUI;

        HBox hbox = new HBox(4);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.getChildren().add(new Label("Function"));
        pane.setTop(hbox);
        ImageDrawingGrid canvas = new ImageDrawingGrid(bitmap, palette, true);
        modeComboBox = new ComboBox<>(FXCollections.observableArrayList(
                new TextDrawingMode("Pixel - D", DOT),
                new TextDrawingMode("Line - L", LINE),
                new TextDrawingMode("Box Outline - R", OUTLINE_RECT),
                new TextDrawingMode("Box Filled - B", FILLED_RECT),
                new TextDrawingMode("Circle - I", OUTLINE_CIRCLE),
                new TextDrawingMode("Flood Fill - F", FLOOD_FILL)
        ));
        modeComboBox.getSelectionModel().select(0);
        modeComboBox.setOnAction(_ -> canvas.setCurrentShape(modeComboBox.getValue().mode()));
        hbox.getChildren().add(modeComboBox);

        hbox.getChildren().add(new Label("Palette"));
        UIColorPaletteControl paletteControl = new UIColorPaletteControl();
        hbox.getChildren().add(paletteControl.swatchControl(palette, canvas::setCurrentColor));

        var copyButton = new Button("Copy");
        copyButton.setOnAction(_ -> copyContents());
        var saveButton = new Button("Save");
        saveButton.setOnAction(_ -> saveContents());
        var xyLabel = new Label("");
        canvas.setPositionUpdateListener((x, y) -> xyLabel.setText(STR."X=\{x}, Y=\{y}"));
        hbox.getChildren().addAll(copyButton, saveButton, xyLabel);

        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty().multiply(0.9));

        pane.widthProperty().addListener((_) -> canvas.onPaintSurface(canvas.getGraphicsContext2D()));
        pane.heightProperty().addListener((_) -> canvas.onPaintSurface(canvas.getGraphicsContext2D()));

        pane.setCenter(canvas);
        pane.getStyleClass().add("background");
        pane.setStyle(STR."-fx-font-size: \{GlobalSettings.defaultFontSize()}");

        Scene scene = new Scene(pane);

        KeyCombination copyPressed = new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN);
        KeyCombination savePressed = new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN);
        KeyCombination dotPressed = new KeyCodeCombination(KeyCode.D);
        KeyCombination linePressed = new KeyCodeCombination(KeyCode.L);
        KeyCombination circlePressed = new KeyCodeCombination(KeyCode.I);
        KeyCombination boxPressed = new KeyCodeCombination(KeyCode.B);
        KeyCombination rectPressed = new KeyCodeCombination(KeyCode.R);
        KeyCombination fillPressed = new KeyCodeCombination(KeyCode.F);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (copyPressed.match(ke)) {
                copyContents();
                ke.consume(); // <-- stops passing the event to next node
            } else if(savePressed.match(ke)) {
                saveContents();
                ke.consume(); // <-- stops passing the event to next node
            } else if(dotPressed.match(ke)) changeShape(DOT);
            else if(linePressed.match(ke)) changeShape(LINE);
            else if(circlePressed.match(ke)) changeShape(OUTLINE_CIRCLE);
            else if(boxPressed.match(ke)) changeShape(FILLED_RECT);
            else if(rectPressed.match(ke)) changeShape(OUTLINE_RECT);
            else if(fillPressed.match(ke)) changeShape(FLOOD_FILL);
        });
        Stage stage = new Stage();
        stage.setMaximized(true);
        stage.setWidth(800);
        stage.setWidth(600);
        stage.setScene(scene);
        stage.setTitle(STR."Bitmap Editor \{shortFmtText(format)} \{bitmap.getPixelWidth()} x \{bitmap.getPixelHeight()}");
        BaseDialogSupport.getJMetro().setScene(scene);
        stage.showAndWait();
        return canvas.isModified();
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
            ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", Paths.get(file.get()).toFile());
        } catch (IOException e) {
            editorUI.alertOnError("Error Saving Image", "An error occurred while saving the image.");
        }
    }

    private void copyContents() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putImage(bitmap.createImageFromBitmap(palette));
        clipboard.setContent(content);
    }

    record TextDrawingMode(String name, ImageDrawingGrid.DrawingMode mode) {
        @Override
        public String toString() {
            return name;
        }
    }
}
