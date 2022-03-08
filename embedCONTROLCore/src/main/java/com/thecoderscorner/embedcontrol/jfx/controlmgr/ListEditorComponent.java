package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.BaseEditorComponent;
import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.MenuComponentControl;
import com.thecoderscorner.embedcontrol.core.controlmgr.ThreadMarshaller;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.RuntimeListMenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;

import java.util.List;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.asFxColor;

public class ListEditorComponent extends BaseEditorComponent<Node> {
    private final ObservableList<String> actualData = FXCollections.observableArrayList();
    private ListView<String> listView;

    public ListEditorComponent(MenuComponentControl remote, ComponentSettings settings, MenuItem item, ThreadMarshaller marshaller) {
        super(remote, settings, item, marshaller);
    }

    @Override
    public void changeControlSettings(RenderingStatus status, String text) {
        ConditionalColoring condColor = getDrawingSettings().getColors();
        var bgPaint = asFxColor(condColor.backgroundFor(RenderingStatus.NORMAL, ConditionalColoring.ColorComponentType.BUTTON));
        listView.setBackground(new Background(new BackgroundFill(bgPaint, new CornerRadii(0), new Insets(0))));
    }

    public Node createComponent() {
        if (item instanceof RuntimeListMenuItem) {
            listView = new ListView<>();
            ConditionalColoring condColor = getDrawingSettings().getColors();
            var bgPaint = asFxColor(condColor.backgroundFor(RenderingStatus.NORMAL, ConditionalColoring.ColorComponentType.BUTTON));
            listView.setBackground(new Background(new BackgroundFill(bgPaint, new CornerRadii(0), new Insets(0))));
            listView.setItems(actualData);
            return listView;
        } else {
            return new Label("item not a list");
        }
    }

    @Override
    public String getControlText() {
        return null;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void onItemUpdated(MenuState<?> state) {
        if (state.getValue() instanceof List) {
            updateAll((List<String>) state.getValue());
        }
    }

    private void updateAll(List<String> values) {
        threadMarshaller.runOnUiThread(() -> {
            actualData.clear();
            actualData.addAll(values);
        });
    }
}
