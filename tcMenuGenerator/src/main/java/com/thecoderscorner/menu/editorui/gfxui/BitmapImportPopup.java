package com.thecoderscorner.menu.editorui.gfxui;

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
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.util.function.Consumer;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.fromFxColor;

public class BitmapImportPopup {
    private final Image loadedImage;
    private final int slot;
    private PortablePalette palette;
    private int tolerance;
    private boolean applyAlpha;

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
        grid.getRowConstraints().add(priorityRowConstraint(Priority.NEVER));
        grid.getRowConstraints().add(priorityRowConstraint(Priority.NEVER));
        grid.getRowConstraints().add(priorityRowConstraint(Priority.NEVER));
        grid.getRowConstraints().add(priorityRowConstraint(Priority.NEVER));
        grid.getRowConstraints().add(priorityRowConstraint(Priority.NEVER));
        grid.getRowConstraints().add(priorityRowConstraint(Priority.NEVER));
        grid.getRowConstraints().add(priorityRowConstraint(Priority.NEVER));
        grid.getRowConstraints().add(priorityRowConstraint(Priority.NEVER));

        grid.add(new Label("Settings for Image Import"), 0, 0);
        grid.add(new ImageView(loadedImage), 0, 1, 1, 6);

        grid.add(new Label("Foreground:"), 1, 1);
        ColorPicker fgColorPicker = new ColorPicker(Color.WHITE);
        grid.add(fgColorPicker, 1, 2);
        grid.add(new Label("Background:"), 1, 3);
        ColorPicker bgColorPicker = new ColorPicker(Color.BLACK);
        grid.add(bgColorPicker, 1, 4);
        grid.add(new Label("Tolerance %"), 1, 5);
        var cbx = new ComboBox<>(FXCollections.observableArrayList(1, 5, 10, 15, 20));
        cbx.setMaxWidth(99999);
        cbx.getSelectionModel().select(3);
        grid.add(cbx, 1, 6);
        var useAlphaCheck = new CheckBox("Alpha channel");
        grid.add(useAlphaCheck, 1, 7);

        ButtonBar buttonBar = new ButtonBar();
        Button importButton = new Button("Import");
        importButton.setDefaultButton(true);
        buttonBar.getButtons().add(importButton);
        Button cancelButton = new Button("Cancel");
        cancelButton.setCancelButton(true);
        buttonBar.getButtons().add(cancelButton);
        grid.add(buttonBar, 0, 8, 2, 1);
        grid.setStyle("-fx-background-color: #1f1a1a;-fx-border-style: solid;-fx-border-color: black;-fx-border-width: 2;-fx-background-insets: 6;-fx-padding: 10;-fx-font-size: " + GlobalSettings.defaultFontSize());

        popup.getContent().add(grid);
        popup.show(where);

        importButton.setOnAction(event -> {
            palette = new PortablePalette(new PortableColor[] {fromFxColor(bgColorPicker.getValue()), fromFxColor(fgColorPicker.getValue())}, PortablePalette.PaletteMode.ONE_BPP);
            tolerance = cbx.getValue();
            applyAlpha = useAlphaCheck.isSelected();
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
}
