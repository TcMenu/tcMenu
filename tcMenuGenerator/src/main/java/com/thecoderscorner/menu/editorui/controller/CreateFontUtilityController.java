package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.editorui.dialog.SelectUnicodeRangesDialog;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.generator.font.AwtLoadedFont;
import com.thecoderscorner.menu.editorui.generator.font.LoadedFont;
import com.thecoderscorner.menu.editorui.generator.font.NoLoadedFont;
import com.thecoderscorner.menu.editorui.generator.font.UnicodeBlockMapping;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.font.AwtLoadedFont.*;

public class CreateFontUtilityController {
    public TextField fontFileField;
    public Button onHelp;
    public Button onToggleSelected;
    public Button onGenerateAdafruit;
    public Button onGenerateTcUnicode;
    public Label approxSizeField;
    public Spinner<Integer> pixelSizeSpinner;
    public ComboBox<FontStyle> fontStyleCombo;
    public TextField outputStructNameField;
    public GridPane fontRenderArea;
    public Menu loadedFontsMenu;
    private CurrentProjectEditorUI editorUI;
    private String homeDirectory;
    private Path currentDir;
    private LoadedFont loadedFont = NO_LOADED_FONT;
    private Set<UnicodeBlockMapping> blockMappings = Set.of();
    private Map<UnicodeBlockMapping,List<ToggleButton>> controlsByBlock = new HashMap<>();
    private Map<Integer, Boolean> currentlySelected = new HashMap<>();

    public void initialise(CurrentProjectEditorUI editorUI, String homeDirectory) {
        this.editorUI = editorUI;
        this.homeDirectory = homeDirectory;
        var fileName = editorUI.getCurrentProject().getFileName();
        currentDir = (fileName.equals("New")) ? Path.of(homeDirectory) : Path.of(fileName).getParent();
        blockMappings = Set.of(UnicodeBlockMapping.BASIC_LATIN, UnicodeBlockMapping.LATIN1_SUPPLEMENT);

        pixelSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 255, 12));
        pixelSizeSpinner.getValueFactory().valueProperty().addListener((observable, oldValue, newValue) -> {
            changeNameField();
            recalcFont();
        });

        this.fontStyleCombo.setItems(FXCollections.observableArrayList(FontStyle.values()));
        this.fontStyleCombo.getSelectionModel().select(0);

        SwingUtilities.invokeLater(() -> {
            var ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            for(var f : ge.getAllFonts()) {
                MenuItem item = new MenuItem(f.getName() + " " + f.getFamily());
                item.setOnAction(event -> {
                    fontFileField.setText("OS " + f.getName() + " " + f.getFamily());
                    loadedFont = new AwtLoadedFont(f, fontStyleCombo.getValue(), pixelSizeSpinner.getValue(), blockMappings);
                });
                loadedFontsMenu.getItems().add(item);
            }
        });
    }

    public void onChooseFont(ActionEvent actionEvent) {
        var fileChoice = editorUI.findFileNameFromUser(Optional.of(currentDir), true, "Fonts|*.ttf");
        fileChoice.ifPresent(file -> {
            fontFileField.setText(file);
            changeNameField();
            loadedFont = new AwtLoadedFont(file, fontStyleCombo.getValue(), pixelSizeSpinner.getValue(), blockMappings);
            recalcFont();
        });
    }

    private void recalcFont() {
        fontRenderArea.getChildren().clear();
        controlsByBlock.clear();
        loadedFont.deriveFont(fontStyleCombo.getValue(), pixelSizeSpinner.getValue());
        int gridRow = 0;
        for(var blockRange : UnicodeBlockMapping.values()) {
            if(!blockMappings.contains(blockRange)) continue;

            fontRenderArea.add(new Label(blockRange.toString()), 0, gridRow, 5, 1);
            CheckBox selAllCheck = new CheckBox("Select/Clear All");
            selAllCheck.setOnAction(event -> {
                var allItems = controlsByBlock.get(blockRange);
                if(allItems != null && !allItems.isEmpty()) {
                    for(var item : allItems) {
                        item.setSelected(selAllCheck.isSelected());
                    }
                    for(int i=blockRange.getStartingCode(); i<=blockRange.getEndingCode();i++) {
                        currentlySelected.put(i, selAllCheck.isSelected());
                    }
                }
            });
            fontRenderArea.add(selAllCheck, 6, gridRow, 3, 1);

            gridRow++;

            var allButtons = new ArrayList<ToggleButton>();
            int gridCol = 0;
            for(int i=blockRange.getStartingCode(); i<blockRange.getEndingCode();i++) {
                var maybeGlyph = loadedFont.getConvertedGlyph(i);
                if (maybeGlyph.isPresent()) {
                    var glyph = maybeGlyph.get();
                    Image img = fromGlyphToImg(glyph);
                    var toggleButton = new ToggleButton("U" + glyph.code());
                    toggleButton.setGraphic(new ImageView(img));
                    toggleButton.setContentDisplay(ContentDisplay.TOP);
                    var selected = currentlySelected.get(glyph.code());
                    toggleButton.setSelected(selected != null && selected);
                    toggleButton.setOnAction(event -> currentlySelected.put(glyph.code(), toggleButton.isSelected()));
                    fontRenderArea.add(toggleButton, gridCol, gridRow);
                    allButtons.add(toggleButton);
                    GridPane.setMargin(toggleButton, new Insets(4));
                    gridCol++;
                    if(gridCol > 9) {
                        gridCol = 0;
                        gridRow++;
                    }
                }
            }
            gridRow++;
            controlsByBlock.put(blockRange, allButtons);
        }
    }

    private Image fromGlyphToImg(ConvertedFontGlyph glyph) {
        WritableImage img = new WritableImage(glyph.calculatedWidth() + 1, glyph.belowBaseline() + glyph.toBaseLine() + 1);
        var writer = img.getPixelWriter();
        int bitOffset = 0;
        for(int y=glyph.fontDims().startY();y<glyph.fontDims().lastY(); y++) {
            for(int x=glyph.fontDims().startX();x<glyph.fontDims().lastX(); x++) {
                int byteOffset = bitOffset / 8;
                if(byteOffset >= glyph.data().length) break;
                int d = glyph.data()[byteOffset];
                boolean on = (d & (1<<(bitOffset % 8))) != 0;
                if(on) {
                    writer.setColor(x, y, Color.WHITE);
                }
                bitOffset++;
            }
        }
        return img;
    }

    public void onFontStyleChanged(ActionEvent actionEvent) {
        changeNameField();
        recalcFont();
    }

    private void changeNameField() {
        var file = Path.of(fontFileField.getText()).getFileName().toString();
        file = file.replace(".ttf", "");
        var outputName = file + " " + pixelSizeSpinner.getValue() + "pt " + fontStyleCombo.getValue();
        outputStructNameField.setText(VariableNameGenerator.makeNameFromVariable(outputName));
    }

    public void onChooseUnicodeRanges(ActionEvent actionEvent) {
        if(loadedFont instanceof NoLoadedFont) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please select a font before choosing unicode ranges");
            alert.setTitle("No font Selected");
            alert.setHeaderText("No Font Selected");
            alert.showAndWait();
        } else {
            Stage mainStage = (Stage) outputStructNameField.getScene().getWindow();
            var dlg = new SelectUnicodeRangesDialog(mainStage, loadedFont, blockMappings);
            dlg.getBlockMappings().ifPresent(unicodeBlockMappings -> {
                blockMappings = unicodeBlockMappings;
                recalcFont();
            });

        }
    }
}
