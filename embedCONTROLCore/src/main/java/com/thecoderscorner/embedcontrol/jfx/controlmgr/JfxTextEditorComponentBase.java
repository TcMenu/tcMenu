package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.BaseTextEditorComponent;
import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.MenuComponentControl;
import com.thecoderscorner.embedcontrol.core.controlmgr.ThreadMarshaller;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.util.MenuItemFormatter;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.asFxColor;


public abstract class JfxTextEditorComponentBase<T> extends BaseTextEditorComponent<T, Node> {

    protected JfxTextEditorComponentBase(MenuComponentControl controller, ComponentSettings settings, MenuItem item, ThreadMarshaller threadMarshaller) {
        super(controller, settings, item, threadMarshaller);
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
        if (controlTextIncludesName()) str = MenuItemFormatter.defaultInstance().getItemName(item) + " ";
        if (controlTextIncludesValue()) str += MenuItemFormatter.defaultInstance().formatForDisplay(item, currentVal);
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