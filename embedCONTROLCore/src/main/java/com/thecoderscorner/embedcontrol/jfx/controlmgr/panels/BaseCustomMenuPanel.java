package com.thecoderscorner.embedcontrol.jfx.controlmgr.panels;

import com.thecoderscorner.embedcontrol.core.controlmgr.*;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.customization.FontInformation;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.UpdatablePanel;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.RenderingStatus.NORMAL;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.ColorComponentType.TEXT_FIELD;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.asFxColor;

/// This class is the starting point for building your own panel to present a sub menu. It has
/// the main functionality to easily add your own components that will update automatically
/// as menu items change. It already implements `UpdatablePanel` so gets updates as menu items
/// change, and also a 1/10sec tick to handle any animations such as update highlighting.
/// Although it is not mandated that you use this to implement custom panels it provides a lot
/// of helper functions.
public abstract class BaseCustomMenuPanel implements PanelPresentable<Node>, UpdatablePanel {
    protected final boolean panelCanBeClosed;
    protected final MenuEditorFactory<Node> editorFactory;
    protected final ConditionalColoring conditionalColoring;
    protected final MenuTree menuTree;
    protected final HashMap<Integer, EditorComponent<Node>> controlsBeingManaged = new HashMap<>();
    protected GridPane gridPane;
    protected double presentableWidth = 999;

    public BaseCustomMenuPanel(MenuEditorFactory<Node> editorFactory,
                               ConditionalColoring conditionalColoring,
                               MenuTree menuTree,
                               boolean canClose) {
        this.editorFactory = editorFactory;
        this.conditionalColoring = conditionalColoring;
        this.menuTree = menuTree;
        this.panelCanBeClosed = canClose;
    }

    @Override
    public Node getPanelToPresent(double width) throws Exception {
        if (gridPane != null) {
            // empty it if it already exists to make GC easier
            gridPane.getChildren().clear();
            gridPane.getColumnConstraints().clear();
            gridPane.getRowConstraints().clear();
        }
        presentableWidth = width;
        makeBasicGridLayout();
        populateGrid();

        return gridPane;
    }
    protected abstract void populateGrid();

    private Font calculateFont(FontInformation fontInfo, Font current) {
        if(fontInfo.sizeMeasurement() == FontInformation.SizeMeasurement.PERCENT) {
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

    protected void putIntoGrid(ComponentSettingsBuilder builder) {
        if(builder.getMode() == ComponentSettingsBuilder.BuildingMode.TEXT) {
            var settings = builder.build();
            var comp = new Label(builder.getText());
            comp.setFont(calculateFont(settings.getFontInfo(), comp.getFont()));
            comp.setTextAlignment(toTextAlign(settings.getJustification()));
            comp.setTextFill(asFxColor(settings.getColors().foregroundFor(NORMAL, TEXT_FIELD)));
            var pos = settings.getPosition();
            gridPane.add(comp, pos.getCol(), pos.getRow(), pos.getColSpan(), pos.getRowSpan());
        } else if(builder.getMode() == ComponentSettingsBuilder.BuildingMode.MENU){
            putIntoGrid(builder.getItem(), builder.build());
        } else {
            throw new IllegalArgumentException("Unsupported mode " + builder.getMode());
        }
    }

    protected void putIntoGrid(MenuItem item, ComponentSettings componentSettings) {
        ComponentPositioning pos = componentSettings.getPosition();
        var component = editorFactory.getComponentEditorItem(item, componentSettings, this::noAction);
        component.ifPresent(comp -> {
            controlsBeingManaged.put(item.getId(), comp);
            gridPane.add(comp.createComponent(), pos.getCol(), pos.getRow(), pos.getColSpan(), pos.getRowSpan());
        });
    }

    protected void makeBasicGridLayout() {
        gridPane = new GridPane();
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridPane.setMaxWidth(9999);
        gridPane.setPrefWidth(presentableWidth);
        gridPane.getChildren().clear();

        // the grid will be 4 across by three down.
        gridPane.getColumnConstraints().clear();
        gridPane.getRowConstraints().clear();
        gridPane.setBackground(new Background(new BackgroundFill(
                asFxColor(conditionalColoring.colorFor(NORMAL, TEXT_FIELD).getBg()), null, null
        )));

    }

    protected void noAction(MenuItem menuItem) {
    }


    @Override
    public boolean canClose() {
        return panelCanBeClosed;
    }


    @Override
    public void closePanel() {
        controlsBeingManaged.clear();
    }

    @Override
    public void itemHasUpdated(MenuItem item) {
        if(controlsBeingManaged.containsKey(item.getId())) {
            controlsBeingManaged.get(item.getId()).onItemUpdated(item, menuTree.getMenuState(item));
        }
    }

    @Override
    public void connectionIsUp(boolean isUp) {
        gridPane.setDisable(!isUp);
    }

    @Override
    public void acknowledgedCorrelationId(CorrelationId correlationId, AckStatus status) {
        for(var component : controlsBeingManaged.values()) {
            component.onCorrelation(correlationId, status);
        }
    }

    @Override
    public void tickAll() {
        for(var component : controlsBeingManaged.values()) {
            component.tick();
        }
    }

    @Override
    public boolean canBeRemoved() {
        return true;
    }
}
