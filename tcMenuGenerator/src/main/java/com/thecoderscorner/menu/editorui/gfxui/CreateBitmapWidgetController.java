package com.thecoderscorner.menu.editorui.gfxui;

import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.domain.util.PortablePalette;
import com.thecoderscorner.menu.editorui.dialog.AppInformationPanel;
import com.thecoderscorner.menu.editorui.gfxui.pixmgr.*;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.editorui.util.SafeNavigator;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.thecoderscorner.menu.editorui.util.AlertUtil.showAlertAndWait;

public class CreateBitmapWidgetController {
    public final System.Logger logger = System.getLogger(getClass().getSimpleName());

    public Button pasteImgButton;
    public CheckBox clipboardCheckBox;
    public TextField variableField;
    public Button addImgButton;
    public Button removeImgButton;

    public Button createWidgetButton;
    public Button createBitmapButton;
    public ToggleButton imageToggle1;
    public ToggleButton imageToggle2;
    public ToggleButton imageToggle3;
    public ToggleButton imageToggle4;
    public ToggleButton imageToggle5;
    public ToggleButton imageToggle6;
    public ToggleButton imageToggle7;
    public ToggleButton imageToggle8;
    private CurrentProjectEditorUI editorUI;
    private String homeDirectory;
    private final Map<Integer, LoadedImage> loadedImages = new HashMap<>();

    private ToggleButton[] toggleArray;

    public void initialise(CurrentProjectEditorUI editorUI, String homeDirectory) {
        this.editorUI = editorUI;
        this.homeDirectory = homeDirectory;

        toggleArray = new ToggleButton[] { imageToggle1,imageToggle2,imageToggle3,imageToggle4,imageToggle5,imageToggle6,imageToggle7,imageToggle8 };

        variableField.textProperty().addListener((observable, oldValue, newValue) -> refreshButtonStates());

        resetAllStates();
    }

    private void resetAllStates() {
        loadedImages.clear();
        int counter = 1;
        for(var t : toggleArray) {
            t.setText("Empty Image " + counter);
            t.setSelected(false);
            t.setDisable(true);
            t.setOnAction(event -> refreshButtonStates());
            counter++;
        }
        refreshButtonStates();
    }

    public void onKeyReleased(KeyEvent keyEvent) {
        if(keyEvent.isShortcutDown() && !keyEvent.isShiftDown() && !keyEvent.isAltDown() && keyEvent.getCode() == KeyCode.V) {
            onPasteImage(new ActionEvent(keyEvent, pasteImgButton));
        }
    }

    public void onPasteImage(ActionEvent actionEvent) {
        var clipboard = Clipboard.getSystemClipboard();
        if(clipboard.hasContent(DataFormat.IMAGE)) {
            Image image = clipboard.getImage();
            putImageIntoAvailableSlot(image);
        } else {
            editorUI.alertOnError("No image on clipboard", "Please ensure there is an image on the clipboard first.");
        }
    }

    private void putImageIntoAvailableSlot(Image image) {
        int blankImage = findBlankImageIndex();
        if (blankImage == -1) return;

        BitmapImportPopup popup = new BitmapImportPopup(image, blankImage);
        popup.showConfigSetup((Stage) addImgButton.getScene().getWindow(), this::importImage);
    }
    private void importImage(BitmapImportPopup popup) {
        var blankImage = popup.getSlot();
        var image = popup.getImage();
        toggleArray[blankImage].setContentDisplay(ContentDisplay.TOP);
        var img = createBitmap(popup);
        ImageView imgView = new ImageView(img.bmpData().createImageFromBitmap(popup.getPalette()));
        double ratio = image.getWidth() / image.getHeight();
        double maxWid = createWidgetButton.getScene().getHeight() * 0.2;
        if(image.getWidth() > image.getHeight()) {
            imgView.setFitWidth(maxWid);
            imgView.setFitHeight(maxWid / ratio);
        } else {
            imgView.setFitWidth(maxWid * ratio);
            imgView.setFitHeight(maxWid);
        }

        toggleArray[blankImage].setGraphic(imgView);
        toggleArray[blankImage].setText(String.format("%s %.0fx%.0f", shortFmtText(popup.getPixelFormat()),
                image.getWidth(), image.getHeight()));
        toggleArray[blankImage].setSelected(true);
        toggleArray[blankImage].setDisable(false);
        loadedImages.put(blankImage, img);

        refreshButtonStates();
    }

    private int findBlankImageIndex() {
        int blankImage = -1;
        for(int i=0; i< 8; i++) {
            if(!loadedImages.containsKey(i)) {
                blankImage = i;
                break;
            }
        }

        if(blankImage == -1) {
            editorUI.alertOnError("No image spaces available", "No images spaces are left available for the paste operation, remove at least one image");
        }
        return blankImage;
    }

    private String shortFmtText(NativePixelFormat fmtCode) {
        return switch (fmtCode) {
            case XBM_LSB_FIRST -> "XBMP";
            case MONO_BITMAP -> "MONO";
            case PALETTE_2BPP -> "2BPP(4)";
            case PALETTE_4BPP -> "4BPP(16)";
        };
    }

    private void refreshButtonStates() {
        boolean nothingSelected = Arrays.stream(toggleArray).noneMatch(ToggleButton::isSelected);
        removeImgButton.setDisable(nothingSelected);
        addImgButton.setDisable(loadedImages.size() == 8);
        var variableEmpty = variableField.getText().isEmpty();
        createWidgetButton.setDisable(nothingSelected || variableEmpty);
        createBitmapButton.setDisable(nothingSelected || variableEmpty);
    }

    public void onClose(ActionEvent actionEvent) {
        ((Stage)createBitmapButton.getScene().getWindow()).close();
    }

    public void onPixelFormatChange(ActionEvent actionEvent) {
        resetAllStates();
    }

    public void onOnlineHelp(ActionEvent actionEvent) {
        SafeNavigator.safeNavigateTo(AppInformationPanel.CREATE_USE_BITMAP_PAGE);
    }

    public void onAddImage(ActionEvent actionEvent) {
        var maybeFile = editorUI.findFileNameFromUser(getInitialDir(), true, "*");
        if(maybeFile.isPresent()) {
            try(var is = new BufferedInputStream(new FileInputStream(maybeFile.get()))) {
                Image img = new Image(is);
                putImageIntoAvailableSlot(img);
            } catch (Exception ex) {
                logger.log(System.Logger.Level.ERROR, "Image load from file failure " + maybeFile.get(), ex);
                editorUI.alertOnError("Error loading image", ex.getMessage());
            }
        }
    }

    public void onRemoveImage(ActionEvent actionEvent) {
        for(int i=0;i<8;i++) {
            if(loadedImages.containsKey(i) && toggleArray[i].isSelected()) {
                toggleArray[i].setText("Empty Image" + (i + 1));
                toggleArray[i].setDisable(true);
                toggleArray[i].setGraphic(null);
                toggleArray[i].setSelected(false);
                loadedImages.remove(i);
            }
        }
        refreshButtonStates();
    }

    public void onCreateWidget(ActionEvent actionEvent) {
        if(clipboardCheckBox.isSelected()) {
            try(var os = new ByteArrayOutputStream(10240); var fileOut = new PrintStream(os)) {
                writeOutTitleWidget(fileOut);
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
        for(int i=0;i<8;i++) {
            if(loadedImages.containsKey(i) && toggleArray[i].isSelected()) {
                exporter.addImageToExport(loadedImages.get(i));
            }
        }
        exporter.exportBitmapDataAsWidget(fileOut, variableField.getText());
    }

    public void onCreateBitmaps(ActionEvent actionEvent) {
        String name = variableField.getText();
        if(name.isEmpty() || name.matches(".*[\\s.].*")) {
            editorUI.alertOnError("Invalid name", "Not a variable name");
            return;
        }

        var exporter = new NativeBitmapExporter();
        for(int i=0;i<8;i++) {
            if(loadedImages.containsKey(i) && toggleArray[i].isSelected()) {
                exporter.addImageToExport(loadedImages.get(i));
            }
        }

        if(clipboardCheckBox.isSelected()) {
            try(var os = new ByteArrayOutputStream(10240); var fileOut = new PrintStream(os);) {
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
        showAlertAndWait(Alert.AlertType.INFORMATION, variableField.getText() + " successfully exported",
                variableField.getText() + " was successfully exported to " + where, ButtonType.CLOSE);
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
