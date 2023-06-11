package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.*;
import com.thecoderscorner.embedcontrol.customization.FontInformation;
import com.thecoderscorner.embedcontrol.customization.MenuItemStore;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemFormatter;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.concurrent.ScheduledExecutorService;

public class JfxMenuPresentable implements PanelPresentable<Node> {
    private final SubMenuItem subMenuItem;
    private final MenuGridComponent<Node> gridComponent;
    private final MenuEditorFactory<Node> editorFactory;
    private final MenuItemStore store;
    private final MenuComponentControl componentControl;
    private double presentableWidth = 999;
    private GridPane gridPane;

    public JfxMenuPresentable(SubMenuItem subMenuItem, MenuItemStore store, JfxNavigationManager navMgr,
                              ScheduledExecutorService executor, ThreadMarshaller marshaller,
                              MenuEditorFactory<Node> factory, MenuComponentControl control) {
        this.subMenuItem = subMenuItem;
        this.store = store;
        this.editorFactory = factory;
        this.componentControl = control;
        this.gridComponent = new JfxGridComponent(store, navMgr, executor, marshaller);
    }

    @Override
    public Node getPanelToPresent(double width) throws Exception {
        if(gridPane != null) {
            // empty it if it already exists to make GC easier
            gridPane.getChildren().clear();
            gridPane.getColumnConstraints().clear();
            gridPane.getRowConstraints().clear();
        }
        gridPane = new GridPane();
        presentableWidth = width;
        gridComponent.clearGrid();
        gridComponent.renderMenuRecursive(editorFactory, subMenuItem, store.isRecursive(), 0);
        return gridPane;
    }

    @Override
    public String getPanelName() {
        if(componentControl == null) return "empty";
        return subMenuItem == MenuTree.ROOT ? componentControl.getConnectionName() : MenuItemFormatter.defaultInstance().getItemName(subMenuItem);
    }

    @Override
    public boolean canBeRemoved() {
        return true;
    }

    @Override
    public boolean canClose() {
        return subMenuItem != MenuTree.ROOT;
    }

    @Override
    public void closePanel() {
    }

    public void connectionIsUp(boolean up) {
        Platform.runLater(()->gridPane.setDisable(!up));
    }

    class JfxGridComponent extends MenuGridComponent<Node> {

        public JfxGridComponent(MenuItemStore store, JfxNavigationManager navMgr, ScheduledExecutorService executor, ThreadMarshaller marshaller) {
            super(store, navMgr, executor, marshaller);
        }

        @Override
        public void clearGrid() {
            super.clearGrid();
            gridPane.setHgap(5);
            gridPane.setVgap(5);
            gridPane.setMaxWidth(9999);
            gridPane.setPrefWidth(presentableWidth);
            gridPane.getChildren().clear();

            gridPane.getColumnConstraints().clear();
            gridPane.getRowConstraints().clear();
            for(int i=0; i<store.getGridSize(); i++) {
                var cc = new ColumnConstraints(10, presentableWidth / store.getGridSize(), 9999, Priority.SOMETIMES, HPos.CENTER, true);
                gridPane.getColumnConstraints().add(cc);
            }
        }

        @Override
        protected void addToGrid(ComponentPositioning where, EditorComponent<Node> item) {
            if(where.getRow() >= gridPane.getRowConstraints().size()) {
                gridPane.getRowConstraints().add(new RowConstraints(10, 30, 999, Priority.SOMETIMES, VPos.CENTER, true));
            }
            var comp = item.createComponent();
            GridPane.setColumnIndex(comp, where.getCol());
            GridPane.setRowIndex(comp, where.getRow());
            GridPane.setColumnSpan(comp, where.getColSpan());
            gridPane.getChildren().add(comp);
        }

        @Override
        protected void addTextToGrid(ComponentSettings settings, String item) {
            var comp = new Label(item);
            comp.setFont(calculateFont(settings.getFontInfo(), comp.getFont()));
            var where = settings.getPosition();
            comp.setTextAlignment(toTextAlign(settings.getJustification()));
            GridPane.setColumnIndex(comp, where.getCol());
            GridPane.setRowIndex(comp, where.getRow());
            GridPane.setColumnSpan(comp, where.getColSpan());
            gridPane.getChildren().add(comp);
        }

        private Font calculateFont(FontInformation fontInfo, Font current) {
            if(fontInfo.sizeMeasurement() == FontInformation.SizeMeasurement.PERCENT) {
                return Font.font(current.getSize() * (fontInfo.fontSize() / 100.0));
            } else {
                return Font.font(fontInfo.fontSize());
            }
        }

        private TextAlignment toTextAlign(EditorComponent.PortableAlignment justification) {
            return switch (justification) {
                case LEFT -> TextAlignment.LEFT;
                case RIGHT -> TextAlignment.RIGHT;
                case CENTER -> TextAlignment.CENTER;
            };
        }

        @Override
        protected void addSpaceToGrid(ComponentPositioning where, int amount) {

        }
    }
}
