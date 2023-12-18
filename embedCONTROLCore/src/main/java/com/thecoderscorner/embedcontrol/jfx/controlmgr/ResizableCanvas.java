package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent;
import com.thecoderscorner.embedcontrol.core.controlmgr.RedrawingMode;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.customdraw.CustomDrawingConfiguration;
import com.thecoderscorner.menu.domain.util.MenuItemFormatter;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.asFxColor;

class ResizableCanvas extends Canvas {
    protected CanvasDrawableContext context;

    ResizableCanvas(CanvasDrawableContext context) {
        this.context = context;
    }

    public void drawTextUsingSettings(GraphicsContext gc, double protectedCurrent, double displayWidth, double displayHeight, boolean bottomVertical) {
            CustomDrawingConfiguration customDrawing = context.getDrawingSettings().getCustomDrawing();
            String toDraw = "";
            boolean needName = context.getDrawingSettings().getDrawMode() == RedrawingMode.SHOW_NAME || context.getDrawingSettings().getDrawMode() == RedrawingMode.SHOW_NAME_VALUE;
            boolean needValue = context.getDrawingSettings().getDrawMode() == RedrawingMode.SHOW_VALUE || context.getDrawingSettings().getDrawMode() == RedrawingMode.SHOW_NAME_VALUE;

            if(needName) toDraw = MenuItemFormatter.defaultInstance().getItemName(context.getItem());
            if(needValue) toDraw += " " + MenuItemFormatter.defaultInstance().formatForDisplay(context.getItem(), context.getValue());

            final Text textObj = new Text(toDraw);
            gc.setFill(asFxColor(customDrawing.getColorFor(protectedCurrent, context.getDrawingSettings().getColors(), context.getStatus(), ConditionalColoring.ColorComponentType.TEXT_FIELD).getFg()));
            var fontSize = context.getDrawingSettings().getFontInfo().fontSizeFromExisting(GlobalSettings.defaultFontSize());

            Font font = Font.font(gc.getFont().getFamily(), fontSize);
            gc.setFont(font);
            textObj.setFont(font);
            var bounds = textObj.getLayoutBounds();
            double yLocation;
            if(bottomVertical && context.getDrawingSettings().getJustification() != EditorComponent.PortableAlignment.CENTER) {
                yLocation = (displayHeight - (bounds.getHeight()));
            } else {
                yLocation = bounds.getHeight() + (displayHeight - bounds.getHeight()) / 2.0;
            }
            switch(context.getDrawingSettings().getJustification()) {
                case LEFT -> gc.fillText(toDraw, 8, yLocation);
                case RIGHT -> gc.fillText(toDraw, (displayWidth - bounds.getWidth()) - 8, yLocation);
                default -> gc.fillText(toDraw, (displayWidth - bounds.getWidth()) / 2.0, yLocation);
            }
        }

    }
