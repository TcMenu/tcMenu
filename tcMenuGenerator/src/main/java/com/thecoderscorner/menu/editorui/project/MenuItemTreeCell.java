package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.controller.MenuEditorController.MenuItemWithDescription;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;

public class MenuItemTreeCell extends TreeCell<MenuItemWithDescription> {
    public enum MenuInsertionPoint { BEFORE, AFTER }
    private static TreeItem<MenuItemWithDescription> draggedTreeItem;
    private static MenuInsertionPoint menuInsertionPoint = MenuInsertionPoint.AFTER;

    public MenuItemTreeCell(MenuItemCellController cellController) {
        getStyleClass().add("tree-cell");

        setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY)
                cellController.itemClickedLeft(MenuItemTreeCell.this);
            else
                cellController.itemClickedRight(MenuItemTreeCell.this);
        });
        
        setOnDragOver(event -> {
            if(isDraggableToParent() && isNotAlreadyChildOfTarget(MenuItemTreeCell.this.getTreeItem()) && draggedTreeItem.getParent() != getTreeItem()) {
                // get the y coordinate within the control based on mouse position
                var sceneCoordinates = MenuItemTreeCell.this.localToScene(0d, 0d);
                double height = MenuItemTreeCell.this.getHeight();
                double y = event.getSceneY() - (sceneCoordinates.getY());

                // dropping into a submenu is always in after mode, otherwise we determine top or bottom.
                if(getItem().item() instanceof SubMenuItem) {
                    menuInsertionPoint = MenuInsertionPoint.AFTER;
                } else {
                    menuInsertionPoint = (y > (height / 2)) ? MenuInsertionPoint.AFTER : MenuInsertionPoint.BEFORE;
                }

                event.acceptTransferModes(TransferMode.MOVE);
                InnerShadow shadow = new InnerShadow();
                shadow.setColor(Color.web("#666666"));
                if(menuInsertionPoint == MenuInsertionPoint.BEFORE) {
                    shadow.setOffsetX(1.0);
                    shadow.setOffsetY(height / 3);
                }
                else {
                    shadow.setOffsetX(1.0);
                    shadow.setOffsetY(1.0);
                }
                setEffect(shadow);
            }
        });
        setOnDragDetected(event -> {
            if(getItem().item().equals(MenuTree.ROOT)) return;
            ClipboardContent content;

            content = new ClipboardContent();
            content.putString("MenuItem");

            Dragboard dragboard;

            dragboard = getTreeView().startDragAndDrop(TransferMode.MOVE);
            dragboard.setContent(content);

            draggedTreeItem = getTreeItem();

            event.consume();
        });
        setOnDragDropped(event -> {
            if(draggedTreeItem != null) {
                if(isDraggableToParent() && isNotAlreadyChildOfTarget(MenuItemTreeCell.this.getTreeItem()) && draggedTreeItem.getParent() != getTreeItem()) {
                    cellController.menuHasMovedFromTo(draggedTreeItem.getValue().item(), getItem().item(), menuInsertionPoint);
                }
                draggedTreeItem = null;
                menuInsertionPoint = MenuInsertionPoint.AFTER;
            }

            event.setDropCompleted(true);
            event.consume();
        });
        setOnDragExited(event -> {
            // remove all dnd effects
            setEffect(null);
        });
    }

    protected boolean isDraggableToParent() {
        //return draggedTreeItem.getValue().isDraggableTo(getTreeItem().getValue());
        return true;
    }

    protected boolean isNotAlreadyChildOfTarget(TreeItem<MenuItemWithDescription> treeItemParent) {
        if(draggedTreeItem == treeItemParent)
            return false;
        
        if(treeItemParent.getParent() != null)
            return isNotAlreadyChildOfTarget(treeItemParent.getParent());
        else
            return true;
    }
    
    protected void updateItem(MenuItemWithDescription item, boolean empty) {
        super.updateItem(item, empty);
        if(item != null) {
            setText(item.toString());
        } else {
            setText("");
        }
    }

    public interface MenuItemCellController {
        void itemClickedLeft(TreeCell<MenuItemWithDescription> item);
        void itemClickedRight(TreeCell<MenuItemWithDescription> item);
        void menuHasMovedFromTo(MenuItem originalLocation, MenuItem newLocation, MenuInsertionPoint where);
    }
}