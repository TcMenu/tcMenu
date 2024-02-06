package com.thecoderscorner.menu.editorui.gfxui;

import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.domain.util.PortablePalette;
import com.thecoderscorner.menu.editorui.dialog.AppInformationPanel;
import com.thecoderscorner.menu.editorui.gfxui.imgedit.SimpleImagePane;
import com.thecoderscorner.menu.editorui.gfxui.pixmgr.*;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.editorui.util.SafeNavigator;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.thecoderscorner.menu.editorui.util.AlertUtil.showAlertAndWait;

public class CreateBitmapWidgetController {
    public final System.Logger logger = System.getLogger(getClass().getSimpleName());

    public Button pasteImgButton;
    public CheckBox clipboardCheckBox;
    public TextField variableField;
    public Button addImgButton;
    public Button createWidgetButton;
    public Button createBitmapButton;
    public GridPane imageGridPane;

    private CurrentProjectEditorUI editorUI;
    private String homeDirectory;
    private final List<LoadedImage> loadedImages = new ArrayList<>();

    public void initialise(CurrentProjectEditorUI editorUI, String homeDirectory) {
        this.editorUI = editorUI;
        this.homeDirectory = homeDirectory;

        variableField.textProperty().addListener((_, _, _) -> refreshButtonStates());

        resetAllStates();
    }

    private void resetAllStates() {
        loadedImages.clear();
        refreshGridComponents();
        refreshButtonStates();
    }

    private void refreshGridComponents() {
        imageGridPane.getChildren().clear();
        imageGridPane.getRowConstraints().clear();
        imageGridPane.getColumnConstraints().clear();

        int cols = switch(loadedImages.size()) {
            case 1 -> 1;
            case 2, 3, 4 -> 2;
            case 5, 6 -> 3;
            default -> 4;
        };
        int rows = (loadedImages.size() < 3) ? 1 : 2;

        for(int i=0; i<cols; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(98.0 / cols);
            col.setHgrow(Priority.SOMETIMES);
            imageGridPane.getColumnConstraints().add(col);
        }

        for(int i=0; i<rows; i++) {
            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.SOMETIMES);
            imageGridPane.getRowConstraints().add(row);
        }

        for (int i = 0; i < loadedImages.size(); i++) {
            LoadedImage img = loadedImages.get(i);
            var editButton = new Button("edit");
            var removeButton = new Button("remove");
            removeButton.setOnAction(_ -> {
                loadedImages.remove(img);
                refreshGridComponents();
                refreshButtonStates();
            });
            var buttons = List.of(editButton, removeButton);
            var imageView = new SimpleImagePane(img.bmpData(), img.pixelFormat(),false, img.palette(), buttons);
            GridPane.setConstraints(imageView, i % cols, i / cols);
            imageGridPane.getChildren().add(imageView);
        }
    }

    public void onKeyReleased(KeyEvent keyEvent) {
        if(keyEvent.isShortcutDown() && !keyEvent.isShiftDown() && !keyEvent.isAltDown() && keyEvent.getCode() == KeyCode.V) {
            onPasteImage(new ActionEvent(keyEvent, pasteImgButton));
        }
    }

    public void onPasteImage(ActionEvent ignoredActionEvent) {
        var clipboard = Clipboard.getSystemClipboard();
        if(clipboard.hasContent(DataFormat.IMAGE)) {
            Image image = clipboard.getImage();
            putImageIntoAvailableSlot(image);
        } else {
            editorUI.alertOnError("No image on clipboard", "Please ensure there is an image on the clipboard first.");
        }
    }

    private void putImageIntoAvailableSlot(Image image) {
        if (loadedImages.size() >= 8) return;

        BitmapImportPopup popup = new BitmapImportPopup(image);
        popup.showConfigSetup((Stage) addImgButton.getScene().getWindow(), this::importImage);
    }
    private void importImage(BitmapImportPopup popup) {
        var img = createBitmap(popup);
        loadedImages.add(img);
        refreshGridComponents();
        refreshButtonStates();
    }

    private void refreshButtonStates() {
        addImgButton.setDisable(loadedImages.size() == 8);
        var variableEmpty = variableField.getText().isEmpty();
        createWidgetButton.setDisable(loadedImages.isEmpty() || variableEmpty);
        createBitmapButton.setDisable(loadedImages.isEmpty() || variableEmpty);
    }

    public void onClose(ActionEvent ignoredActionEvent) {
        ((Stage)createBitmapButton.getScene().getWindow()).close();
    }

    public void onOnlineHelp(ActionEvent ignoredActionEvent) {
        SafeNavigator.safeNavigateTo(AppInformationPanel.CREATE_USE_BITMAP_PAGE);
    }

    public void onAddImage(ActionEvent ignoredActionEvent) {
        var maybeFile = editorUI.findFileNameFromUser(getInitialDir(), true, "*");
        if(maybeFile.isPresent()) {
            try(var is = new BufferedInputStream(new FileInputStream(maybeFile.get()))) {
                Image img = new Image(is);
                putImageIntoAvailableSlot(img);
            } catch (Exception ex) {
                logger.log(System.Logger.Level.ERROR, STR."Image load from file failure \{maybeFile.get()}", ex);
                editorUI.alertOnError("Error loading image", ex.getMessage());
            }
        }
    }

    public void onCreateWidget(ActionEvent ignoredActionEvent) {
        if(clipboardCheckBox.isSelected()) {
            try(var os = new ByteArrayOutputStream(10240); var fileOut = new PrintStream(os)) {
                writeOutTitleWidget(fileOut);
                Clipboard.getSystemClipboard().setContent(Map.of(DataFormat.PLAIN_TEXT, os.toString()));
                exportSuccessful("clipboard");
            }
            catch (Exception e) {
                logger.log(System.Logger.Level.ERROR, "Could not put file content on clipboard", e);
                editorUI.alertOnError("Not exported to Clipboard", STR."Not exported to Clipboard \{e.getMessage()}");
            }
            return;
        }
        var maybeName = editorUI.findFileNameFromUser(getInitialDir(), false, "*.h");
        if(maybeName.isPresent()) {
            try(var fileOut = new PrintStream(new FileOutputStream(maybeName.get()))) {
                writeOutTitleWidget(fileOut);
                exportSuccessful(maybeName.get());
            } catch (Exception e) {
                logger.log(System.Logger.Level.ERROR, "File could not be written", e);
                editorUI.alertOnError("Not exported to file", "Not exported to file " + e.getMessage());
            }
        }
    }

    private void writeOutTitleWidget(PrintStream fileOut) throws IOException {
        var exporter = new NativeBitmapExporter();
        for(var li : loadedImages) {
            exporter.addImageToExport(li);
        }
        exporter.exportBitmapDataAsWidget(fileOut, variableField.getText());
    }

    public void onCreateBitmaps(ActionEvent ignoredActionEvent) {
        String name = variableField.getText();
        if(name.isEmpty() || name.matches(".*[\\s.].*")) {
            editorUI.alertOnError("Invalid name", "Not a variable name");
            return;
        }

        var exporter = new NativeBitmapExporter();
        for(var li : loadedImages) {
            exporter.addImageToExport(li);
        }

        if(clipboardCheckBox.isSelected()) {
            try(var os = new ByteArrayOutputStream(10240); var fileOut = new PrintStream(os)) {
                exporter.exportBitmaps(fileOut, name, "Bitmap");
                Clipboard.getSystemClipboard().setContent(Map.of(DataFormat.PLAIN_TEXT, os.toString()));
                exportSuccessful("clipboard");
            }
            catch (Exception e) {
                logger.log(System.Logger.Level.ERROR, "Could not put file content on clipboard", e);
                editorUI.alertOnError("Not exported to Clipboard", "Not exported to Clipboard " + e.getMessage());
            }
            return;
        }
        var maybeName = editorUI.findFileNameFromUser(getInitialDir(), false, "*.h");
        if(maybeName.isPresent()) {
            try(var fileOut = new PrintStream(new FileOutputStream(maybeName.get()))) {
                exporter.exportBitmaps(fileOut, name, "Bitmap");
                exportSuccessful(maybeName.get());
            } catch (Exception e) {
                logger.log(System.Logger.Level.ERROR, "File could not be written", e);
                editorUI.alertOnError("File not written", "Error while writing file " + e.getMessage());
            }
        }
    }

    private Optional<Path> getInitialDir() {
        if(editorUI.getCurrentProject().getFileName().equals("New")) {
            return Optional.of(Path.of(homeDirectory));
        } else {
            return Optional.of(Path.of(editorUI.getCurrentProject().getFileName()).getParent());
        }
    }

    private void exportSuccessful(String where) {
        showAlertAndWait(Alert.AlertType.INFORMATION, STR."\{variableField.getText()} successfully exported",
                STR."\{variableField.getText()} was successfully exported to \{where}", ButtonType.CLOSE);
    }

    LoadedImage createBitmap(BitmapImportPopup popup) {
        NativePixelFormat fmt = popup.getPixelFormat();
        Image image = popup.getImage();
        PixelReader reader = image.getPixelReader();
        boolean applyAlpha = popup.isApplyAlpha();
        BmpDataManager bitmapProcessor = getBmpDataManager(fmt, image);
        PortablePalette palette = popup.getPalette();
        bitmapProcessor.convertToBits((x, y) -> {
            var col = PortableColor.asPortableColor(reader.getArgb(x, y));
            return palette.getClosestColorIndex(col, popup.getTolerance() / 100.0, applyAlpha); // in this palette zero is background
        });

        return new LoadedImage(bitmapProcessor, fmt, (int) image.getWidth(), (int) image.getHeight(), palette);
    }

    private static BmpDataManager getBmpDataManager(NativePixelFormat fmt, Image image) {
        if(fmt == NativePixelFormat.XBM_LSB_FIRST || fmt == NativePixelFormat.MONO_BITMAP) {
            return  new NativeBmpBitPacker((int) image.getWidth(), (int) image.getHeight(), false);
        } else if(fmt == NativePixelFormat.PALETTE_2BPP || fmt == NativePixelFormat.PALETTE_4BPP) {
            return  new NBppBitPacker((int) image.getWidth(), (int) image.getHeight(),
                    (fmt == NativePixelFormat.PALETTE_2BPP) ? 2 : 4);
        }
        else throw new IllegalArgumentException("Unknown bitmap format");
    }
}
