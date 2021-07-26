package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.*;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.jfx.dialog.GeneralSettingsController;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.util.MenuItemFormatter;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.function.Consumer;

import static com.thecoderscorner.embedcontrol.jfx.dialog.GeneralSettingsController.*;

public abstract class JfxTextEditorComponentBase<T> extends BaseTextEditorComponent<T> {

    protected JfxTextEditorComponentBase(RemoteMenuController controller, ComponentSettings settings, MenuItem item, ThreadMarshaller threadMarshaller) {
        super(controller, settings, item, threadMarshaller);
    }

    protected Node makeTextComponent(Node entryField, EventHandler<ActionEvent> sendHandler, boolean needSendBtn) {
        var needLabel = getDrawingSettings().getDrawMode() == RedrawingMode.SHOW_LABEL_NAME_VALUE || getDrawingSettings().getDrawMode() == RedrawingMode.SHOW_NAME_IN_LABEL;
        if (!needLabel && !needSendBtn) return entryField;

        var grid = new GridPane();

        var entryRow = 0;
        if (needLabel) {
            var lbl = new Label(item.getName());
            lbl.setTextFill(asFxColor(getDrawingSettings().getColors().foregroundFor(RenderingStatus.NORMAL, ConditionalColoring.ColorComponentType.TEXT_FIELD)));
            lbl.setPadding(new Insets(2));
            grid.add(lbl, 0, 0);
            entryRow = 1;
        }

        grid.add(entryField, 0, entryRow);

        if (needSendBtn) {
            var sendButton = new Button("Send");
            if (sendHandler != null) sendButton.setOnAction(sendHandler);
            grid.add(sendButton, 1, entryRow);
        }

        return grid;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onItemUpdated(MenuState<?> newValue)
    {
        var state = (MenuState<T>) newValue;
        {
            currentVal = state.getValue();
            markRecentlyUpdated(RenderingStatus.RECENT_UPDATE);
        }
    }

    @Override
    public String getControlText()
    {
        String str = "";
        if (controlTextIncludesName()) str = item.getName() + " ";
        if (controlTextIncludesValue()) str += MenuItemFormatter.formatForDisplay(item, currentVal);
        return str;
    }

    public static void setNodeConditionalColours(Node node, ConditionalColoring condColor, ConditionalColoring.ColorComponentType ty) {
        setNodeConditionalColours(node, condColor, ty, RenderingStatus.NORMAL);
    }

    public static void setNodeConditionalColours(Node node, ConditionalColoring condColor, ConditionalColoring.ColorComponentType ty, RenderingStatus status) {
        var bgPaint = asFxColor(condColor.backgroundFor(status, ty));
        var fgPaint = asFxColor(condColor.foregroundFor(status, ty));
        if(node instanceof Region r) r.setBackground(new Background(new BackgroundFill(bgPaint, new CornerRadii(0.0), new Insets(0))));
        if(node instanceof Labeled l) l.setTextFill(fgPaint);
    }

}