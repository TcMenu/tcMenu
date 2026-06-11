package com.thecoderscorner.embedcontrol.jfx.controlmgr.texted;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.menu.domain.EditItemType;
import com.thecoderscorner.menu.domain.EditableTextMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;

import java.time.format.DateTimeFormatter;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.asFxColor;

public class TimeFieldEditHandler implements FieldEditHandler {
    private final MenuItem item;
    private final TextField timeField;
    private final DateTimeFormatter formatter;

    public TimeFieldEditHandler(MenuItem item, Object startingValue) {
        this.item = item;
        var strippedStr = startingValue.toString().replace("[", "");
        strippedStr = strippedStr.replace("]", "");
        timeField = new TextField(strippedStr);
        formatter = DateTimeFormatter.ofPattern(getFormat());
    }

    private String getFormat() {
        if (item instanceof EditableTextMenuItem timeItem) {
            return (timeItem.getItemType() == EditItemType.TIME_12H) ? "hh:mm:sstt" :
                    (timeItem.getItemType() == EditItemType.TIME_24H) ? "HH:mm:ss" : "HH:mm:ss.ff";
        }

        return "T";
    }

    @Override
    public Node getEditorComponent() {
        return timeField;
    }

    @Override
    public boolean isCurrentlyValid() {

        try {
            formatter.parse(timeField.getText());
            return true;
        }
        catch(Exception ex) {

        }
        return false;
    }

    @Override
    public String getValueAsString() {
        return timeField.getText();
    }

    @Override
    public void markInvalid() {
        var errorPaint = asFxColor(ControlColor.CORAL);
        timeField.setBackground(new Background(new BackgroundFill(errorPaint, new CornerRadii(0), new Insets(0))));
    }
}
