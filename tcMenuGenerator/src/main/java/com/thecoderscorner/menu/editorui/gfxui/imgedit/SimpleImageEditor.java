package com.thecoderscorner.menu.editorui.gfxui.imgedit;

import com.thecoderscorner.menu.domain.util.PortablePalette;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

import static com.thecoderscorner.menu.editorui.gfxui.imgedit.ImageDrawingGrid.DrawingMode;
import static com.thecoderscorner.menu.editorui.gfxui.imgedit.SimpleImagePane.shortFmtText;

public class SimpleImageEditor {
    private final BmpDataManager bitmap;
    private final PortablePalette palette;
    private final NativePixelFormat format;

    public SimpleImageEditor(BmpDataManager bitmap, NativePixelFormat format, PortablePalette palette) {
        this.bitmap = bitmap;
        this.palette = palette;
        this.format = format;
    }

    public boolean presentUI(CurrentProjectEditorUI editorUI) {
        BorderPane pane = new BorderPane();
        pane.setOpaqueInsets(new Insets(10));

        HBox hbox = new HBox(4);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.getChildren().add(new Label("Toolbar"));
        pane.setTop(hbox);
        ImageDrawingGrid canvas = new ImageDrawingGrid(bitmap, palette, true);
        var modeComboBox = new ComboBox<>(FXCollections.observableArrayList(
                DrawingMode.LINE, DrawingMode.OUTLINE_RECT, DrawingMode.FILLED_RECT
        ));
        modeComboBox.getSelectionModel().select(0);
        modeComboBox.setOnAction(_ -> canvas.setCurrentShape(modeComboBox.getValue()));
        hbox.getChildren().add(modeComboBox);
        UIColorPaletteControl paletteControl = new UIColorPaletteControl();
        hbox.getChildren().add(paletteControl.swatchControl(palette, canvas::setCurrentColor));

        var saveButton = new Button("Save");
        saveButton.setOnAction(_ -> {
            var file = editorUI.findFileNameFromUser(Optional.empty(), false, "*.png");
            if(file.isEmpty()) return;
            try {
                var img = bitmap.createImageFromBitmap(palette);
                ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", Paths.get(file.get()).toFile());
            } catch (IOException e) {
                editorUI.alertOnError("Error Saving Image", "An error occurred while saving the image.");
            }
        });
        hbox.getChildren().add(saveButton);


        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty().multiply(0.9));

        pane.widthProperty().addListener((_) -> canvas.onPaintSurface(canvas.getGraphicsContext2D()));
        pane.heightProperty().addListener((_) -> canvas.onPaintSurface(canvas.getGraphicsContext2D()));

        pane.setCenter(canvas);

        Scene scene = new Scene(pane);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle(STR."Bitmap Editor \{shortFmtText(format)} \{bitmap.getPixelWidth()} x \{bitmap.getPixelHeight()}");
        stage.showAndWait();
        return canvas.isModified();
    }
}
