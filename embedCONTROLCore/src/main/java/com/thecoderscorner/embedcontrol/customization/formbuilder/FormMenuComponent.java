package com.thecoderscorner.embedcontrol.customization.formbuilder;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentPositioning;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.ColorCustomizable;
import com.thecoderscorner.embedcontrol.customization.GlobalColorCustomizable;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.menu.domain.SubMenuItem;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import static com.thecoderscorner.embedcontrol.customization.formbuilder.MenuFormItem.NO_FORM_ITEM;

/**
 * This represents an item in an embedCONTROL pre-made form.
 */
public class FormMenuComponent extends BorderPane {
    private final GlobalSettings settings;
    private final MenuItemStore store;
    private final JfxNavigationManager navMgr;
    private final Button editTopBtn;
    private final Button removeButton;
    private final ComponentPositioning myPosition;
    private final Label editBottomLabel;
    private MenuFormItem formItem;

    public FormMenuComponent(MenuFormItem item, GlobalSettings settings, ComponentPositioning positioning,
                             JfxNavigationManager navMgr, MenuItemStore store) {
        this.navMgr = navMgr;
        this.myPosition = positioning;
        this.settings = settings;
        this.store = store;
        this.formItem = item;
        editTopBtn = new Button("Empty");
        editTopBtn.setMaxWidth(999);
        editTopBtn.setOnAction(event -> showEditingForm());
        editBottomLabel = new Label("");
        editTopBtn.setMaxWidth(999);
        setTop(editTopBtn);
        removeButton = new Button("X");
        removeButton.setMaxHeight(999);
        removeButton.setOnAction(event -> {
            formItem = NO_FORM_ITEM;
            evaluateFormItem();
        });
        setRight(removeButton);
        setBottom(editBottomLabel);
        getStyleClass().add("menu-edit-item");
        setPadding(new Insets(2));

        evaluateFormItem();
        setOnDragDropped(event -> {
            var dragged = FormEditorController.GridPositionCell.getCurrentDragItem();
            if(dragged != null) {
                setFormItem( dragged.createComponent(myPosition));
            }
            event.consume();
            event.setDropCompleted(true);
            setEffect(null);
        });

        setOnDragExited(event -> {
            event.consume();
            setEffect(null);
        });

        setOnDragOver(event -> {
            var dragged = FormEditorController.GridPositionCell.getCurrentDragItem();
            if(dragged != null) {
                event.acceptTransferModes(TransferMode.MOVE);
                InnerShadow shadow = new InnerShadow();
                shadow.setColor(Color.web("#888888"));
                shadow.setOffsetX(1.0);
                shadow.setOffsetY(1.0);
                setEffect(shadow);
                event.consume();
            }
        });

    }

    private void setFormItem(MenuFormItem component) {
        this.formItem = component;
        store.setFormItemAt(myPosition.getRow(), myPosition.getCol(), formItem);
        evaluateFormItem();
    }

    private void showEditingForm() {
        if(formItem instanceof MenuItemFormItem || formItem instanceof TextFormItem) {
            var presentable = new EditFormComponentPresentable(settings, this);
            navMgr.pushNavigation(presentable);
        }
    }

    public void evaluateFormItem() {
        editTopBtn.setDisable(formItem instanceof MenuFormItem.NoFormItem);
        removeButton.setDisable(formItem instanceof MenuFormItem.NoFormItem);

        editTopBtn.setText(formItem.getDescription());

        var colorScheme = "none";
        if(formItem.getSettings() != null) {
            colorScheme = formItem.getSettings().getColorSchemeName();
        }
        editBottomLabel.setText("Font: " + formItem.getFontInfo().toWire() + ", Col: " + colorScheme);

        if(formItem instanceof TextFormItem tfi){
            setCenter(new Label(tfi.getText()));
        } else if(formItem instanceof SpaceFormItem sfi) {
            var spinner = new Spinner<Integer>(1, 50, sfi.getVerticalSpace());
            spinner.valueProperty().addListener((observable, oldValue, newValue) -> sfi.setVerticalSpace(newValue));
            setCenter(spinner);
        } else if(formItem instanceof MenuItemFormItem mfi) {
            setCenter(new Label("Fmt: " + mfi.getControlType() + " - " + mfi.getAlignment() + " - " + mfi.getRedrawingMode()));
        } else {
            setCenter(new Label(""));
        }

    }

    public ColorCustomizable getColorCustomizable() {
        return formItem.getSettings();
    }

    public void setColorCustomizable(ColorCustomizable colorCustomizable) {
        this.getFormItem().setSettings(colorCustomizable);
        formItem.setSettings(colorCustomizable);
    }

    public MenuFormItem getFormItem() {
        return formItem;
    }

    public MenuItemStore getStore() {
        return store;
    }
}
