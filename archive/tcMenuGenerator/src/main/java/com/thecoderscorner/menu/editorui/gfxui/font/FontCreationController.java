package com.thecoderscorner.menu.editorui.gfxui.font;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.menu.editorui.MenuEditorApp;
import com.thecoderscorner.menu.editorui.dialog.BaseDialogSupport;
import com.thecoderscorner.menu.editorui.dialog.SelectUnicodeRangesDialog;
import com.thecoderscorner.menu.editorui.gfxui.NativeFreeFontGlyphGenerator;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import static com.thecoderscorner.menu.editorui.dialog.BaseDialogSupport.createDialogStateAndShow;
import static com.thecoderscorner.menu.editorui.gfxui.font.UnicodeBlockMapping.BASIC_LATIN;
import static com.thecoderscorner.menu.editorui.gfxui.font.UnicodeBlockMapping.LATIN_EXTENDED_A;

public class FontCreationController {
    public static final int EMBEDDED_FONT_DPI = 100;

    private static Optional<EmbeddedFont> result = Optional.empty();
    private CurrentProjectEditorUI editorUI;
    ResourceBundle bundle = MenuEditorApp.getBundle();
    private Stage stage;
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

    public Optional<EmbeddedFont> createDialog(Stage stage, Path startingDir, CurrentProjectEditorUI editorUI) {
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
        var cancelButton = new Button(bundle.getString("core.cancel.text"));
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(_ -> ((Stage)sizeSpinner.getScene().getWindow()).close());
        buttonBar.getButtons().addAll(cancelButton, uniBlocksBtn, fontChooseBtn);

        BaseDialogSupport.getJMetro().setScene(stage.getScene());
        createDialogStateAndShow(stage, grid, "Create Font", true);
        return result;
    }

    private void setBlockText() {
        blockText.setText(String.join(", ", blockMappings.stream().map(Enum::name).toList()));
    }

    private void blockChooser(ActionEvent actionEvent) {
        var dlg = new SelectUnicodeRangesDialog(stage, blockMappings);
        dlg.getBlockMappings().ifPresent(unicodeBlockMappings -> {
            blockMappings = unicodeBlockMappings;
            setBlockText();
        });
    }

    private void fontChooser(ActionEvent actionEvent) {
        var fileChoice = editorUI.findFileNameFromUser(Optional.of(startingDir), true, "Fonts|*.ttf");
        fileChoice.ifPresent(file -> {
            Path path = Paths.get(file);
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
