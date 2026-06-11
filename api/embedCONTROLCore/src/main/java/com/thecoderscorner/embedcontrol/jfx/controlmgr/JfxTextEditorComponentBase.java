package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.BaseTextEditorComponent;
import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.MenuComponentControl;
import com.thecoderscorner.embedcontrol.core.controlmgr.ThreadMarshaller;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
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

/**
 * JfxTextEditorComponentBase provides an abstract base class for JavaFX-based text editor components.
 * Extends capabilities of BaseTextEditorComponent by implementing methods specific to JavaFX.
 *
 * @param <T> the type of the underlying data that will be edited
 */
public abstract class JfxTextEditorComponentBase<T> extends BaseTextEditorComponent<T, Node> {
    protected final DrawingColorHandler drawingColorHandler;
    protected JfxTextEditorComponentBase(MenuComponentControl controller, ComponentSettings settings, MenuItem item, ThreadMarshaller threadMarshaller) {
        super(controller, settings, item, threadMarshaller);
        drawingColorHandler = new DrawingColorHandler(settings);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onItemUpdated(MenuItem item, MenuState<?> newValue)
    {
        this.item = item;
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

    public static class DrawingColorHandler {
        private final ComponentSettings settings;

        public DrawingColorHandler(ComponentSettings settings) {
            this.settings = settings;
        }

        void setPaintFor(Node node, Object value, ConditionalColoring.ColorComponentType ty, RenderingStatus status) {
            ControlColor colors;
            if(status == RenderingStatus.NORMAL) {
                colors = settings.getCustomDrawing().getColorFor(value, settings.getColors(), status, ty);
            } else {
                colors = settings.getColors().colorFor(status, ty);
            }
            var fg = asFxColor(colors.getFg());
            var bg = asFxColor(colors.getBg());
            if(node instanceof Region r) r.setBackground(new Background(new BackgroundFill(bg, new CornerRadii(0.0), new Insets(0))));
            if(node instanceof Labeled l) l.setTextFill(fg);
        }
    }

}