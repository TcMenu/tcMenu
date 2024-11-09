package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.*;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.FontInformation;
import com.thecoderscorner.embedcontrol.customization.MenuItemStore;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemFormatter;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.concurrent.ScheduledExecutorService;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.RenderingStatus.NORMAL;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.ColorComponentType.TEXT_FIELD;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.asFxColor;
import static com.thecoderscorner.embedcontrol.customization.FontInformation.SizeMeasurement;

public class JfxMenuPresentable implements PanelPresentable<Node>, UpdatablePanel {
    private final SubMenuItem subMenuItem;
    protected final MenuGridComponent<Node> gridComponent;
    private final MenuEditorFactory<Node> editorFactory;
    private final MenuItemStore store;
    private final MenuComponentControl componentControl;
    private double presentableWidth = 999;
    private GridPane gridPane;
    private double lastWidth = 999;

    public JfxMenuPresentable(SubMenuItem subMenuItem, MenuItemStore store, JfxNavigationManager navMgr,
                              ScheduledExecutorService executor, ThreadMarshaller marshaller,
                              MenuEditorFactory<Node> factory, MenuComponentControl control) {
        this.subMenuItem = subMenuItem;
        this.store = store;
        this.editorFactory = factory;
        this.componentControl = control;
        this.gridComponent = new JfxGridComponent(store, navMgr, executor, marshaller);
    }

    public MenuGridComponent<Node> getGridComponent() { return gridComponent; }

    @Override
    public Node getPanelToPresent(double width) throws Exception {
        lastWidth = width;
        if(gridPane != null) {
            // empty it if it already exists to make GC easier
            gridPane.getChildren().clear();
            gridPane.getColumnConstraints().clear();
            gridPane.getRowConstraints().clear();
        }
        gridPane = new GridPane();
        presentableWidth = width;
        gridComponent.clearGrid();
        gridPane.setBackground(new Background(new BackgroundFill(
                asFxColor(store.getColorSet("").getColorFor(TEXT_FIELD).getBg()), null, null
        )));
        gridComponent.renderMenuRecursive(editorFactory, subMenuItem, store.isRecursive(), 0);
        return gridPane;
    }

    public void entirelyRebuildGrid() {
        gridComponent.clearGrid();
        gridComponent.renderMenuRecursive(editorFactory, subMenuItem, store.isRecursive(), 0);
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

    @Override
    public void connectionIsUp(boolean up) {
        gridPane.setDisable(!up);
    }

    @Override
    public void acknowledgedCorrelationId(CorrelationId correlationId, AckStatus status) {
        getGridComponent().acknowledgementReceived(correlationId, status);
    }

    @Override
    public void tickAll() {
        getGridComponent().tickAll();
    }

    @Override
    public void itemHasUpdated(MenuItem item) {
        getGridComponent().itemHasUpdated(item);
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
            var sizeMeasure = store.getGlobalFontInfo().sizeMeasurement() == SizeMeasurement.ABS_SIZE ? "px" : "%";
            gridPane.setStyle("-fx-font-size: " + store.getGlobalFontInfo().fontSize() + sizeMeasure + ";");

            gridPane.getColumnConstraints().clear();
            gridPane.getRowConstraints().clear();
            for(int i=0; i<store.getGridSize(); i++) {
                var cc = new ColumnConstraints(10, presentableWidth / store.getGridSize(), 9999, Priority.SOMETIMES, HPos.CENTER, true);
                gridPane.getColumnConstraints().add(cc);
            }
        }

        @Override
        protected void addToGrid(ComponentPositioning where, EditorComponent<Node> item, FontInformation fontInfo) {
            while(where.getRow() >= gridPane.getRowConstraints().size()) {
                var computedFontSize = GlobalSettings.defaultFontSize();
                computedFontSize = fontInfo.fontSizeFromExisting(computedFontSize);
                gridPane.getRowConstraints().add(new RowConstraints(10, computedFontSize * 2, 999, Priority.SOMETIMES, VPos.CENTER, true));
            }
            var comp = item.createComponent();
            GridPane.setColumnIndex(comp, where.getCol());
            GridPane.setRowIndex(comp, where.getRow());
            GridPane.setColumnSpan(comp, where.getColSpan());
            if(item instanceof AnalogMeterComponent) {
                gridPane.getRowConstraints().get(where.getRow()).setPrefHeight(100);
            }
            gridPane.getChildren().add(comp);
        }

        @Override
        protected void addTextToGrid(ComponentSettings settings, String item) {
            var comp = new Label(item);
            comp.setFont(calculateFont(settings.getFontInfo(), comp.getFont()));
            var where = settings.getPosition();
            comp.setTextAlignment(toTextAlign(settings.getJustification()));
            comp.setTextFill(asFxColor(settings.getColors().foregroundFor(NORMAL, TEXT_FIELD)));
            GridPane.setColumnIndex(comp, where.getCol());
            GridPane.setRowIndex(comp, where.getRow());
            GridPane.setColumnSpan(comp, where.getColSpan());
            gridPane.getChildren().add(comp);
        }

        private Font calculateFont(FontInformation fontInfo, Font current) {
            if(fontInfo.sizeMeasurement() == SizeMeasurement.PERCENT) {
                return Font.font(current.getSize() * (fontInfo.fontSize() / 100.0));
            } else {
                return Font.font(fontInfo.fontSize());
            }
        }

        private TextAlignment toTextAlign(EditorComponent.PortableAlignment justification) {
            return switch (justification) {
                case LEFT, LEFT_VAL_RIGHT -> TextAlignment.LEFT;
                case RIGHT -> TextAlignment.RIGHT;
                case CENTER -> TextAlignment.CENTER;
            };
        }

        @Override
        protected void addSpaceToGrid(ComponentPositioning where, int amount) {

        }
    }
}
