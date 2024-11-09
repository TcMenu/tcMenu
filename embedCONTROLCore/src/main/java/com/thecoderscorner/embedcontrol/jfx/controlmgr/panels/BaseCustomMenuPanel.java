package com.thecoderscorner.embedcontrol.jfx.controlmgr.panels;

import com.thecoderscorner.embedcontrol.core.controlmgr.*;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.UpdatablePanel;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;

import java.util.HashMap;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.RenderingStatus.NORMAL;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.ColorComponentType.TEXT_FIELD;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.asFxColor;

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
}
