package com.thecoderscorner.embedcontrol.customization.formbuilder;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentPositioning;
import com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.GlobalColorCustomizable;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.panels.ColorSettingsPresentable;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.PortableAlignment.*;

public class FormEditorController {
    public GridPane editGrid;
    public ListView<GridPositionChoice> selectionList;
    public ComboBox<String> gridSizeCombo;
    public CheckBox recursiveCheck;
    public ComboBox<MenuItem> subItemCombo;
    public ComboBox<String> colorSetCombo;
    private GlobalSettings settings;
    private JfxNavigationManager navMgr;
    private MenuItemStore itemStore;
    private MenuTree tree;
    private UUID boardUuid;
    private boolean autosizingInProgress = false;

    public void initialise(GlobalSettings settings, MenuTree tree, UUID boardUuid, JfxNavigationManager navMgr,
                           MenuItemStore itemStore) {
        this.settings = settings;
        this.itemStore = itemStore;
        this.navMgr = navMgr;
        this.tree = tree;
        this.boardUuid = boardUuid;
        selectionList.setCellFactory(param -> new GridPositionCell());

        gridSizeCombo.setItems(FXCollections.observableArrayList("1 column", "2 Column", "3 Column", "4 Column"));
        subItemCombo.setItems(FXCollections.observableArrayList(tree.getAllSubMenus()));

        rebuildSelections();
        rebuildColumns();
        rebuildColorSelections();
    }

    private void rebuildColorSelections() {
        colorSetCombo.setItems(FXCollections.observableArrayList(itemStore.getAllColorSetNames()));
        colorSetCombo.getSelectionModel().select(itemStore.getTopLevelColorSet().getColorSchemeName());
    }

    private void rebuildSelections() {
        var allItems = recursiveCheck.isSelected() ? tree.getAllMenuItems() : tree.getMenuItems(tree.getMenuById(itemStore.getRootItemId()).orElseThrow());
        var list = new ArrayList<GridPositionChoice>();
        for(var item : allItems) {
            list.add(new MenuItemPositionChoice(item));
        }
        list.add(new TextGridPositionChoice());
        list.add(new SpacingGridPositionChoice());
        selectionList.setItems(FXCollections.observableArrayList(list));

        recursiveCheck.setSelected(itemStore.isRecursive());
        gridSizeCombo.getSelectionModel().select(itemStore.getGridSize() - 1);


        subItemCombo.setDisable(itemStore.isRecursive());
        if(!itemStore.isRecursive()) {
            subItemCombo.getSelectionModel().select(tree.getMenuById(itemStore.getRootItemId()).orElseThrow());
        }

        colorSetCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null && itemStore.getColorSet(newValue) != null && !autosizingInProgress) {
                itemStore.setTopLevelColorSet(itemStore.getColorSet(newValue));
            }
        });
    }

    public void closePressed() {
    }

    public void onGridSizeChange(ActionEvent actionEvent) {
        int selCols = gridSizeCombo.getSelectionModel().getSelectedIndex() + 1;
        if(itemStore.getGridSize() != selCols) {
            var alert = new Alert(Alert.AlertType.CONFIRMATION, "Changing grid size may lose some data, select YES to continue", ButtonType.NO, ButtonType.YES);
            alert.setTitle("Really change grid");
            alert.setHeaderText("Really change grid");
            if(alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.YES) {
                return;
            }
        }
        itemStore.setGridSize(selCols);
        rebuildColumns();
    }

    private void rebuildColumns() {
        editGrid.getColumnConstraints().clear();
        editGrid.getRowConstraints().clear();
        editGrid.getChildren().clear();
        for(int i=0; i < itemStore.getGridSize(); i++) {
            editGrid.getColumnConstraints().add(new ColumnConstraints(10, 100, 999, Priority.SOMETIMES, HPos.CENTER, true));
        }

        for(int i=0; i < itemStore.getMaximumRow(); i++) {
            editGrid.getRowConstraints().add(new RowConstraints(10, 30, 999, Priority.SOMETIMES, VPos.CENTER, true));
        }

        for(int row = 0; row < editGrid.getRowConstraints().size(); row++) {
            for(int col = 0; col < itemStore.getGridSize(); col++) {
                var formComp = new FormMenuComponent(itemStore.getFormItemAt(row, col), settings,
                        new ComponentPositioning(row, col), navMgr, itemStore);
                editGrid.add(formComp, col, row);
            }
        }
    }

    public void onOnlineHelp(ActionEvent actionEvent) {
    }

    public void onColorConfig(ActionEvent actionEvent) {
        var colorPresentable = new ColorSettingsPresentable(settings, navMgr, GlobalColorCustomizable.KEY_NAME, itemStore);
        navMgr.pushNavigation(colorPresentable);
        rebuildColorSelections();
    }

    public void onAddNewRow(ActionEvent actionEvent) {
        editGrid.getRowConstraints().add(new RowConstraints(10, 30, 999, Priority.SOMETIMES, VPos.CENTER, true));
        for(int col = 0; col < editGrid.getColumnCount(); col++) {
            int row = editGrid.getRowConstraints().size() - 1;
            var formComp = new FormMenuComponent(itemStore.getFormItemAt(row, col), settings,
                    new ComponentPositioning(row, col), navMgr, itemStore);
            editGrid.add(formComp, col, row);
        }
    }

    public Optional<String> showFileChooser(boolean open) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a Layout File");

        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Layouts", "*.xml"));
        File f;
        if (open) {
            f = fileChooser.showOpenDialog(getStage());
        } else {
            f = fileChooser.showSaveDialog(getStage());
        }

        return  (f != null) ? Optional.of(f.getPath()) :  Optional.empty();
    }

    public Stage getStage() {
        return (Stage)recursiveCheck.getScene().getWindow();
    }

    public void onLoadLayout(ActionEvent actionEvent) {
        var maybeFile = showFileChooser(true);
        if(maybeFile.isEmpty()) return;
        var file = maybeFile.get();

        itemStore.loadLayout(file, boardUuid);
        try {
            autosizingInProgress = true;
            rebuildColorSelections();
            rebuildSelections();
            rebuildColumns();
        } finally {
            autosizingInProgress = false;
        }
    }

    public void onSaveLayout(ActionEvent actionEvent) {
        var maybeFile = showFileChooser(false);
        if(maybeFile.isEmpty()) return;
        var file = maybeFile.get();

        itemStore.saveLayout(file, boardUuid);
    }

    public void onRecursiveChanged(ActionEvent actionEvent) {
        if(autosizingInProgress) return;

        itemStore.setRecursive(recursiveCheck.isSelected());
        rebuildSelections();
    }

    public void subMenuHasChanged(ActionEvent actionEvent) {
        if(autosizingInProgress) return;
    }

    public class GridPositionCell extends ListCell<GridPositionChoice> {
        private static GridPositionChoice currentDragItem = null;

        public GridPositionCell() {
            setOnDragOver(event -> {
                // do nothing, this is not allowed
            });

            setOnDragDetected(event -> {
                ClipboardContent content = new ClipboardContent();
                content.putString("FormEditComponent");

                Dragboard dragboard = selectionList.startDragAndDrop(TransferMode.MOVE);
                dragboard.setContent(content);

                currentDragItem = getItem();
                event.consume();
            });

            setOnDragDropped(event -> {

            });

            setOnDragExited(event -> {

            });
        }

        public static GridPositionChoice getCurrentDragItem() {
            return currentDragItem;
        }

        public static void setCurrentDragItem(GridPositionChoice currentDragItem) {
            GridPositionCell.currentDragItem = currentDragItem;
        }

        @Override
        protected void updateItem(GridPositionChoice item, boolean empty) {
            super.updateItem(item, empty);
            if(empty) {
                setText("");
            } else {
                setText(item.getName());
            }
        }

        public String toString() {
            return "GridPos List Cell " + getItem();
        }
    }

    public interface GridPositionChoice {
        MenuFormItem createComponent(ComponentPositioning myPosition);
        String getName();
    }

    public class TextGridPositionChoice implements GridPositionChoice {
        @Override
        public MenuFormItem createComponent(ComponentPositioning myPosition) {
            return new TextFormItem("Change me", itemStore.getTopLevelColorSet(), myPosition, LEFT);
        }

        @Override
        public String getName() {
            return "Add Static Text";
        }
    }
    public class SpacingGridPositionChoice implements GridPositionChoice {
        @Override
        public MenuFormItem createComponent(ComponentPositioning myPosition) {
            return new SpaceFormItem(itemStore.getTopLevelColorSet(), myPosition, 10);
        }

        public String getName() {
            return "Add Spacing";
        }
    }

    public class MenuItemPositionChoice implements GridPositionChoice {
        private final MenuItem item;

        public MenuItemPositionChoice(MenuItem item) {
            this.item = item;
        }

        @Override
        public MenuFormItem createComponent(ComponentPositioning myPosition) {
            return new MenuItemFormItem(item, itemStore.getTopLevelColorSet(), myPosition);
        }

        @Override
        public String getName() {
            return "Add " + item;
        }
    }
}
