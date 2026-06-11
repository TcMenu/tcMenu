package com.thecoderscorner.bmped.gfxui;

import com.thecoderscorner.bmped.gfxui.font.*;
import com.thecoderscorner.bmped.util.BmpEditorUI;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.SafeNavigator;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.domain.util.PortablePalette;
import com.thecoderscorner.bmped.gfxui.imgedit.ImageDrawingGrid;
import com.thecoderscorner.bmped.gfxui.imgedit.SimpleImageEditor;
import com.thecoderscorner.bmped.gfxui.imgedit.SimpleImagePane;
import com.thecoderscorner.bmped.gfxui.pixmgr.BitmapImportPopup;
import com.thecoderscorner.bmped.gfxui.pixmgr.BmpDataManager;
import com.thecoderscorner.bmped.gfxui.pixmgr.NativeBmpBitPacker;
import com.thecoderscorner.bmped.gfxui.pixmgr.NativePixelFormat;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.*;

import static com.thecoderscorner.menu.domain.util.PortablePalette.PaletteMode;
import static com.thecoderscorner.bmped.gfxui.font.EmbeddedFontExporter.FontFormat;
import static com.thecoderscorner.bmped.gfxui.imgedit.SimpleImageEditor.EditingMode.FONT_EDITOR;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class CreateFontUtilityController {
    public static final String FONTS_GUIDE_URL = "https://tcmenu.github.io/documentation/arduino-libraries/tc-menu/using-custom-fonts-in-menu/";

    public final System.Logger logger = System.getLogger(getClass().getSimpleName());

    public TextField fontFileField;
    public TextField outputStructNameField;
    public GridPane fontRenderArea;
    public Label fontSizeField;
    public MenuButton generateButton;
    private BmpEditorUI editorUI;
    private Path currentDir;
    private Set<UnicodeBlockMapping> chosenMappings = new HashSet<>(Set.of(UnicodeBlockMapping.BASIC_LATIN));
    private final Map<UnicodeBlockMapping,List<FontGlyphDataControl>> controlsByBlock = new HashMap<>();
    private EmbeddedFont embeddedFont = new EmbeddedFont();
    Popup popup;
    private boolean dirty = false;
    private boolean clipboardExport = false;
    private Path lastExportDir;

    public void initialise(BmpEditorUI editorUI) {
        this.editorUI = editorUI;
        currentDir = Path.of(System.getProperty("user.dir"));
        lastExportDir = currentDir;

        var clipboardItem = new CheckMenuItem("Export To Clipboard");
        clipboardItem.setSelected(false);
        clipboardItem.setOnAction(_ -> clipboardExport = clipboardItem.isSelected());

        generateButton.getItems().addAll(
                clipboardItem,
                bundleMenuItemWithAction("Adafruit Font", _ -> internalGenerate(FontFormat.ADAFRUIT)),
                bundleMenuItemWithAction("TcUnicode Font", _ -> internalGenerate(FontFormat.TC_UNICODE))
        );

        checkButtons();
    }

    private void internalGenerate(FontFormat format) {
        if(clipboardExport) {
            logger.log(INFO, "Convert font {} to clipboard", format);
            try(var outStream = new ByteArrayOutputStream()) {
                EmbeddedFontExporter exporter = new EmbeddedFontExporter(embeddedFont, outputStructNameField.getText());
                exporter.encodeFontToStream(outStream, format);
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(outStream.toString());
                clipboard.setContent(content);
            } catch (Exception ex) {
                editorUI.alertOnError("Clipboard export failed", "The font was not converted due to the following." + ex.getMessage());
            }
        } else {
            logger.log(INFO, "Show font conversion save dialog");
            try {
                String varName = outputStructNameField.getText();
                EmbeddedFontExporter exporter = new EmbeddedFontExporter(embeddedFont, varName);
                var bos = new ByteArrayOutputStream();
                exporter.encodeFontToStream(bos, format);
                var file = editorUI.saveFileWithChooser("*.h", Optional.of(varName), bos.toByteArray());
                if(file.isPresent()) {
                    logger.log(INFO, "Convert font {}, name {}", format, file);
                    lastExportDir = Path.of(file.get()).getParent();

                }
            } catch (Exception ex) {
                editorUI.alertOnError("File export failed ", "The font was not converted due to the following. " + ex.getMessage());
            }
        }
    }

    private MenuItem bundleMenuItemWithAction(String text, EventHandler<ActionEvent> act) {
        var m = new MenuItem(text);
        m.setOnAction(act);
        return m;
    }

    public void onOpenExistingFont(ActionEvent ignoredEvent) {
        if(!shouldOverwrite()) return;
        var fileChoice = editorUI.openFileWithChooser("*.xml");
        fileChoice.ifPresent(fileAndData -> {
            try {
                currentDir = Path.of(fileAndData.fileName()).getParent();
                embeddedFont = new EmbeddedFont(fileAndData.data());
                recalcFont();
            } catch (Exception e) {
                editorUI.alertOnError("File load problem", "The font file was not loaded - " + e);
            }
        });
    }

    public void saveFont(ActionEvent ignored) {
        try {
            var baos = new ByteArrayOutputStream();
            embeddedFont.saveFontToStream(baos);
            var fileChoice = editorUI.saveFileWithChooser("*.xml", Optional.of(embeddedFont.getDefaultFontVariableName()), baos.toByteArray());
            if(fileChoice.isEmpty()) return;
            embeddedFont.convertToXmlLoaded(Path.of(fileChoice.get()));
            dirty = false;
            recalcFont();
        } catch(Exception ex) {
            editorUI.alertOnError("Font Save Failed", ex.getMessage());
            logger.log(ERROR, "Font save failed with exception", ex);
        }
    }

    public void importFont(ActionEvent ignoredActionEvent) {
        if(!shouldOverwrite()) return;
        var createFontDlg = new FontCreationController();
        var p = Path.of(System.getProperty("user.home"));
        var font = createFontDlg.createDialog((Stage)fontFileField.getScene().getWindow(), p, editorUI);
        if(font.isEmpty()) return;
        embeddedFont = font.get();
        chosenMappings = createFontDlg.getChosenMappings();
        recalcFont();
        checkButtons();
    }

    private void recalcFont() {
        fontRenderArea.getChildren().clear();
        controlsByBlock.clear();
        int gridRow = 0;
        for(var blockRange : UnicodeBlockMapping.values()) {
            if(!chosenMappings.contains(blockRange)) continue;

            Label title = new Label(blockRange.toString());
            title.setPadding(new Insets(15, 0, 6, 0));
            title.setStyle(String.format("-fx-font-size: %d;", GlobalSettings.defaultFontSize() * 2));
            fontRenderArea.add(title, 0, gridRow, 5, 1);
            CheckBox selAllCheck = new CheckBox("Select/Clear All");
            selAllCheck.setOnAction(_ -> {
                var allItems = controlsByBlock.get(blockRange);
                if(allItems != null && !allItems.isEmpty()) {
                    for(var item : allItems) {
                        item.setSelected(selAllCheck.isSelected());
                    }
                    markDirty();
                }
                recalcSize();
            });
            Button preview = new Button("Preview");
            preview.setOnAction(_ -> previewNativeFont(blockRange, controlsByBlock.get(blockRange)));
            fontRenderArea.add(selAllCheck, 6, gridRow, 2, 1);
            fontRenderArea.add(preview, 8, gridRow);

            fontFileField.setText("%s %s".formatted(embeddedFont.getFontType(), embeddedFont.getFontPath()));
            outputStructNameField.setText(embeddedFont.getDefaultFontVariableName());

            gridRow++;

            var allButtons = new ArrayList<FontGlyphDataControl>();
            int gridCol = 0;
            for(var glyph : embeddedFont.getGlyphsForBlock(blockRange)) {
                try {
                    var fontGlyphView = new FontGlyphDataControl(glyph);
                    Pane ui = fontGlyphView.getUI();
                    fontRenderArea.add(ui, gridCol, gridRow);
                    allButtons.add(fontGlyphView);
                    GridPane.setMargin(ui, new Insets(4));
                    gridCol++;
                    if (gridCol > 9) {
                        gridCol = 0;
                        gridRow++;
                    }
                } catch(Exception ex) {
                    logger.log(ERROR, "Create control has failed at %s %s".formatted(new String(Character.toChars(glyph.code())), glyph), ex);
                }
            }
            gridRow++;
            controlsByBlock.put(blockRange, allButtons);
        }
        recalcSize();
    }

    private void recalcSize() {
        var fsi = embeddedFont.getFontSizeInfo();
        var txt = String.format(" Characters selected = %d, yAdvance = %d, baseline = %d, approx size Adafruit = %d, TcUnicode = %d",
                fsi.count(), embeddedFont.getYAdvance(), embeddedFont.getBelowBaseline(), fsi.adafruitSize(), fsi.tcUnicodeSize());
        fontSizeField.setText(txt);

        checkButtons();
    }

    private void checkButtons() {
        boolean atLeastOneSelected = embeddedFont.isAnythingSelected();
        generateButton.setDisable(!atLeastOneSelected);
        outputStructNameField.setDisable(!embeddedFont.isPopulated());
    }

    public void onOnlineHelp(ActionEvent ignored) {
        SafeNavigator.safeNavigateTo(FONTS_GUIDE_URL);
    }

    private boolean shouldOverwrite() {
        if(!dirty) return true;
        if(editorUI.questionYesNo("%font.create.changed.title", "%font.create.changed.content")) {
            dirty = false;
            return true;
        } else return false;
    }

    private void previewNativeFont(UnicodeBlockMapping mapping, List<FontGlyphDataControl> glyphs) {
        int totalWidth = Math.max(1, glyphs.stream().filter(FontGlyphDataControl::isSelected)
                .map(d -> d.getData().getPixelWidth()).reduce(0, Integer::sum));
        int maxHeight = glyphs.stream().map(d -> d.getData().getPixelHeight()).max(Integer::compareTo).orElse(1);
        NativeBmpBitPacker packer = new NativeBmpBitPacker(320, maxHeight * ((totalWidth / 320) + 1), false);
        SimpleImagePane e = new SimpleImagePane(packer, NativePixelFormat.MONO_BITMAP, false, BitmapImportPopup.EMPTY_PALETTE, List.of());
        e.getDrawingGrid().setTextCursor(0,0);
        e.getDrawingGrid().setFont(embeddedFont);
        e.getDrawingGrid().setCurrentColor(1);
        for(var g : glyphs) {
            if(!g.isSelected()) continue;
            e.getDrawingGrid().printChar(g.glyph.code());
        }

        var popup = new Popup();
        var vbox = new VBox(4);
        vbox.setStyle("-fx-background-color: #1f1a1a;-fx-border-style: solid;-fx-border-color: black;-fx-border-width: 2;-fx-background-insets: 6;-fx-padding: 10;-fx-font-size: " + GlobalSettings.defaultFontSize());

        e.setPrefWidth(640);
        e.setPrefHeight(maxHeight * 2);

        var title = new Label("Glyphs in %s range %d-%d".formatted(mapping.name(), mapping.getStartingCode(), mapping.getEndingCode()));
        var closeBtn = new Button("Close");
        closeBtn.setMaxWidth(99999);
        closeBtn.setOnAction(_ -> popup.hide());
        vbox.getChildren().addAll(title, e, closeBtn);
        popup.getContent().add(vbox);
        popup.show(generateButton.getScene().getWindow());
    }

    void markDirty() {
        dirty = true;
        checkButtons();
    }

    class FontGlyphDataControl {
        private static final PortablePalette FONT_PALETTE = new PortablePalette(new PortableColor[]{PortableColor.BLACK, PortableColor.WHITE}, PaletteMode.ONE_BPP);
        final private EmbeddedFontGlyph glyph;
        private BorderPane pane;
        private ImageDrawingGrid imgView;

        public FontGlyphDataControl(EmbeddedFontGlyph glyph) {
            this.glyph = glyph;
        }

        public Pane getUI() {
            pane = new BorderPane();
            createAnImageView();
            setFooterText(pane);
            return pane;
        }

        private void createAnImageView() {
            imgView = glyph.getDisplayBitmapForGlyph();
            pane.setCenter(imgView);
            imgView.widthProperty().bind(pane.widthProperty());
            imgView.heightProperty().bind(pane.heightProperty().multiply(0.9));
            imgView.setOnMouseClicked(this::imgViewMouseClick);
            pane.widthProperty().addListener((_) -> imgView.onPaintSurface(imgView.getGraphicsContext2D()));
            pane.heightProperty().addListener((_) -> imgView.onPaintSurface(imgView.getGraphicsContext2D()));
        }

        public BmpDataManager getData() {
            return glyph.data();
        }

        private void setFooterText(BorderPane pane) {
            var strSel = glyph.selected() ? "[X] " : "[ ]";
            char[] theChar = Character.toChars(glyph.code());
            pane.setBottom(new Label("%sU%d : %s".formatted(strSel, glyph.code(), new String(theChar))));
        }

        public void setSelected(boolean selected) {
            glyph.setSelected(selected);
            // protect against the UI not being generated yet.
            if(pane != null) {
                setFooterText(pane);
            }
        }

        public boolean isSelected() {
            return glyph.selected();
        }

        private void imgViewMouseClick(MouseEvent mouseEvent) {
            if (popup != null) popup.hide();
            popup = new Popup();

            var box = new VBox(4);
            box.setFillWidth(true);
            box.setStyle("-fx-font-size: " + GlobalSettings.defaultFontSize());
            var editButton = new Button("Edit Glyph U" + glyph.code());
            editButton.setMaxWidth(99999);
            editButton.setOnAction(_ -> {
                var editor = new SimpleImageEditor(glyph.data(), NativePixelFormat.MONO_BITMAP, FONT_PALETTE, FONT_EDITOR);
                popup.hide();
                if(editor.presentUI(editorUI)) {
                    markDirty();
                    createAnImageView();
                    imgView.onPaintSurface(imgView.getGraphicsContext2D());
                } else popup.hide();
            });
            var selectButton = new Button(glyph.selected() ? "Exclude Glyph" : "Include Glyph");
            selectButton.setMaxWidth(99999);
            selectButton.setOnAction(_ -> {
                markDirty();
                glyph.setSelected(!glyph.selected());
                setFooterText(pane);
                recalcSize();
                popup.hide();
            });
            var closeButton = new Button("Close");
            closeButton.setMaxWidth(99999);
            closeButton.setOnAction(_ -> popup.hide());
            box.setStyle("-fx-background-color: #1f1a1a;-fx-border-style: solid;-fx-border-color: black;-fx-border-width: 2;-fx-background-insets: 6;-fx-padding: 10;-fx-font-size: " + GlobalSettings.defaultFontSize());
            box.getChildren().addAll(selectButton, editButton, closeButton);
            popup.getContent().add(box);
            popup.show(pane.getScene().getWindow());
            popup.setX(mouseEvent.getScreenX());
            popup.setY(mouseEvent.getScreenY());
        }
    }
}
