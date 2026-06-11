package com.thecoderscorner.bmped.gfxui.font;

import com.thecoderscorner.bmped.controller.SelectUnicodeRangesController;
import com.thecoderscorner.bmped.gfxui.NativeFreeFontGlyphGenerator;
import com.thecoderscorner.bmped.util.BmpEditorUI;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;

import java.io.IOException;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static com.thecoderscorner.bmped.gfxui.font.UnicodeBlockMapping.BASIC_LATIN;
import static com.thecoderscorner.bmped.gfxui.font.UnicodeBlockMapping.LATIN_EXTENDED_A;
import static com.thecoderscorner.embedcontrol.jfx.controlmgr.panels.BaseDialogSupport.createDialogStateAndShow;

public class FontCreationController {
    public static final int EMBEDDED_FONT_DPI = 100;

    private static Optional<EmbeddedFont> result = Optional.empty();
    private Stage stage;
    private BmpEditorUI editorUI;
    private Path startingDir;
    private Spinner<Integer> dpiSpinner;
    private Spinner<Integer> sizeSpinner;
    private Set<UnicodeBlockMapping> blockMappings = Set.of(BASIC_LATIN, LATIN_EXTENDED_A);
    private TextField blockText;

    private ColumnConstraints priorityColConstraint(Priority priority) {
        var c = new ColumnConstraints();
        c.setHgrow(priority);
        return c;
    }

    public Optional<EmbeddedFont> createDialog(Stage stage, Path startingDir, BmpEditorUI editorUI) {
        this.stage = stage;
        this.startingDir = startingDir;
        this.editorUI = editorUI;

        int row = 0;
        var grid = new GridPane(4, 4);
        grid.setStyle("-fx-font-size: " + GlobalSettings.defaultFontSize());
        grid.getColumnConstraints().addAll(
            priorityColConstraint(Priority.NEVER),
            priorityColConstraint(Priority.SOMETIMES),
            priorityColConstraint(Priority.NEVER)
        );
        grid.getStyleClass().add("background");
        grid.setPadding(new Insets(15));
        grid.add(new Label("Font Size"), 0, row);
        sizeSpinner = new Spinner<>(5, 100, 12);
        sizeSpinner.setEditable(true);
        grid.add(sizeSpinner, 1, row);
        grid.add(new Label("points"), 3, row);
        row++;

        grid.add(new Label("Display DPI"), 0, row);
        dpiSpinner = new Spinner<>(50, 400, EMBEDDED_FONT_DPI);
        dpiSpinner.setEditable(true);
        grid.add(dpiSpinner, 1, row);
        grid.add(new Label("px/inch"), 3, row);
        row++;

        grid.add(new Label("Unicode Blocks"), 0, row);
        blockText = new TextField();
        blockText.setMinWidth(500);
        blockText.setEditable(false);
        setBlockText();
        grid.add(blockText, 1, row, 3, 1);
        row++;

        var buttonBar = new ButtonBar();
        grid.add(buttonBar, 0, row, 4, 1);
        var uniBlocksBtn = new Button("Unicode Blocks");
        uniBlocksBtn.setOnAction(this::blockChooser);
        var fontChooseBtn = new Button("Choose File");
        fontChooseBtn.setOnAction(this::fontChooser);
        var cancelButton = new Button("Cancel");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(_ -> ((Stage)sizeSpinner.getScene().getWindow()).close());
        buttonBar.getButtons().addAll(cancelButton, uniBlocksBtn, fontChooseBtn);

        createDialogStateAndShow(stage, grid, "Create Font", true);
        return result;
    }

    private void setBlockText() {
        blockText.setText(String.join(", ", blockMappings.stream().map(Enum::name).toList()));
    }

    private void blockChooser(ActionEvent actionEvent) {
        try {
            var loader = new FXMLLoader(getClass().getResource("/fxui/selectUnicodeRanges.fxml"));
            Parent root = loader.load();
            SelectUnicodeRangesController controller = loader.getController();
            controller.initialise(blockMappings);

            var dlg = new Stage();
            dlg.initModality(Modality.APPLICATION_MODAL);
            dlg.initOwner(stage);
            dlg.setScene(new Scene(root));
            dlg.setMinWidth(500);
            dlg.setTitle("Select Unicode Blocks");
            dlg.showAndWait();

            controller.getBlockMappings().ifPresent(unicodeBlockMappings -> {
                blockMappings = unicodeBlockMappings;
                setBlockText();
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void fontChooser(ActionEvent actionEvent) {
        var fileChoice = editorUI.openFileWithChooser("*.ttf");
        fileChoice.ifPresent(file -> {
            Path path = Path.of(file.fileName());
            try (var loadedFont = new NativeFreeFontGlyphGenerator(path, dpiSpinner.getValue())) {
                loadedFont.deriveFont(sizeSpinner.getValue(), blockMappings, FontGlyphGenerator.AntiAliasMode.NO_ANTI_ALIAS);
                result = Optional.of(new EmbeddedFont(loadedFont, blockMappings, path, sizeSpinner.getValue()));
                ((Stage)sizeSpinner.getScene().getWindow()).close();
            }
        });
    }

    public Set<UnicodeBlockMapping> getChosenMappings() {
        return blockMappings;
    }
}
