package com.thecoderscorner.menu.editorui.gfxui;

import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.domain.util.PortablePalette;
import com.thecoderscorner.menu.editorui.dialog.AppInformationPanel;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.editorui.util.SafeNavigator;
import javafx.collections.FXCollections;
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
import java.util.*;

import static com.thecoderscorner.menu.editorui.util.AlertUtil.showAlertAndWait;
import static com.thecoderscorner.menu.editorui.util.StringHelper.printArrayToStream;

public class CreateBitmapWidgetController {
    public final System.Logger logger = System.getLogger(getClass().getSimpleName());
    public enum NativePixelFormat { XBITMAP} // PALETTE_4_COL, PALETTE_2COL

    public Button pasteImgButton;
    public CheckBox clipboardCheckBox;
    public TextField variableField;
    public Button addImgButton;
    public Button removeImgButton;

    public Button createWidgetButton;
    public Button createBitmapButton;
    public ComboBox<NativePixelFormat> pixelFormatCombo;
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

        pixelFormatCombo.setItems(FXCollections.observableArrayList(NativePixelFormat.values()));
        pixelFormatCombo.getSelectionModel().select(0);
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
        Optional<LoadedImage> maybeImage = createBitmap(NativePixelFormat.XBITMAP, popup);
        if(maybeImage.isEmpty()) {
            editorUI.alertOnError("Image conversion error", "Couldn't convert the image to the desired format");
            return;
        }
        ImageView imgView = new ImageView(maybeImage.get().bmpData().createImageFromBitmap(popup.getPalette()));
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
        toggleArray[blankImage].setText(String.format("%s %.0fx%.0f", shortFmtText(pixelFormatCombo.getSelectionModel().getSelectedItem()),
                image.getWidth(), image.getHeight()));
        toggleArray[blankImage].setSelected(true);
        toggleArray[blankImage].setDisable(false);
        loadedImages.put(blankImage, maybeImage.get());

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
            case XBITMAP -> "XBMP";
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

    private void writeOutTitleWidget(PrintStream fileOut) {
        String name = VariableNameGenerator.makeNameFromVariable(variableField.getText());

        StringBuilder arrayOfNames = new StringBuilder();
        var items = new ArrayList<LoadedImage>();
        for(int i=0;i<8;i++) {
            if(loadedImages.containsKey(i) && toggleArray[i].isSelected()) {
                if(!items.isEmpty()) {
                    arrayOfNames.append(", ");
                }
                arrayOfNames.append(name).append("WidIcon").append(i);
                items.add(loadedImages.get(i));
            }
        }

        writeOutBitmaps(fileOut, name, items, "WidIcon");

        fileOut.printf("const uint8_t* const %sWidIcons[] PROGMEM = { %s };", name, arrayOfNames);
        fileOut.println();

        fileOut.println();

        fileOut.println("// Widget Generator " + variableField.getText());
        fileOut.printf("TitleWidget %sWidget(%1$sWidIcons, %d, %d, %d, nullptr);", name, items.size(), items.get(0).width(), items.get(0).height());
        fileOut.println();
    }

    private static void writeOutBitmaps(PrintStream fileOut, String name, ArrayList<LoadedImage> items, String extraName) {
        int count = 0;
        for(var img : items) {
            fileOut.printf("// %s icon=%d, width=%d, height=%d, size=%d", name, count, img.width(), img.height(), ((img.width() + 7)/8) * img.height());
            fileOut.println();
            fileOut.printf("const uint8_t %s%s%d[] PROGMEM = {", name, extraName, count++);
            fileOut.println();
            printArrayToStream(fileOut, img.bmpData().getData(), 20);
            fileOut.println("};");
        }
    }

    public void onCreateBitmaps(ActionEvent actionEvent) {
        String name = VariableNameGenerator.makeNameFromVariable(variableField.getText());

        var items = new ArrayList<LoadedImage>();
        for(int i=0;i<8;i++) {
            if(loadedImages.containsKey(i) && toggleArray[i].isSelected()) {
                items.add(loadedImages.get(i));
            }
        }

        if(clipboardCheckBox.isSelected()) {
            try(var os = new ByteArrayOutputStream(10240); var fileOut = new PrintStream(os);) {
                writeOutBitmaps(fileOut, name, items, "Bitmap");
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
                writeOutBitmaps(fileOut, name, items, "Bitmap");
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

    Optional<LoadedImage> createBitmap(NativePixelFormat fmt, BitmapImportPopup popup) {
        Image image = popup.getImage();
        PixelReader reader = image.getPixelReader();
        boolean applyAlpha = popup.isApplyAlpha();
        if(fmt == NativePixelFormat.XBITMAP) {
            var bitmapProcessor = new NativeBmpBitPacker((int) image.getWidth(), (int) image.getHeight(), false);
            PortablePalette palette = popup.getPalette();
            bitmapProcessor.convertToBits((x, y) -> {
                var col = PortableColor.asPortableColor(reader.getArgb(x, y));
                return palette.getClosestColorIndex(col, popup.getTolerance() / 100.0, applyAlpha) != 0; // in this palette zero is background
            });

            return Optional.of(new LoadedImage(bitmapProcessor, NativePixelFormat.XBITMAP, (int) image.getWidth(), (int) image.getHeight(), palette));
        }
        return Optional.empty();
    }

    public record LoadedImage(
            NativeBmpBitPacker bmpData,
            NativePixelFormat pixelFormat,
            int width, int height,
            PortablePalette palette) {
    }

}
