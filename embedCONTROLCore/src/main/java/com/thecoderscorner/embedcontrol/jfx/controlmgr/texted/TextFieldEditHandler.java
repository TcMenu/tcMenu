package com.thecoderscorner.embedcontrol.jfx.controlmgr.texted;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.util.MenuItemFormatter;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.asFxColor;

public class TextFieldEditHandler implements FieldEditHandler {
    private final MenuItem item;
    private final String startingValue;
    private final TextField textField;

    public TextFieldEditHandler(MenuItem item, String startingValue) {
        this.item = item;
        this.startingValue = startingValue;
        this.textField = new TextField(startingValue);
    }

    @Override
    public Node getEditorComponent() {
        return textField;
    }

    @Override
    public boolean isCurrentlyValid() {
        return validateContents(textField.getText());
    }

    private boolean validateContents(String text) {
        try {
            if (item instanceof AnalogMenuItem anItem) {
                var value = Double.parseDouble(text);
                value = value + anItem.getOffset();
                value = value * anItem.getDivisor();
                return value > 0 && value < anItem.getMaxValue();
            }
            else if(item instanceof EnumMenuItem enItem) {
                var value = Integer.parseInt(text);
                return value > 0 && value < enItem.getEnumEntries().size();
            }
            else if(item instanceof BooleanMenuItem) {
                return text.equalsIgnoreCase("true") || text.equalsIgnoreCase("false");
            }
            else if(item instanceof EditableLargeNumberMenuItem item) {
                var value = Double.parseDouble(text);
                if(value < 0.0 && !item.isNegativeAllowed()) return false;
                return true;
            }
            else if(item instanceof EditableTextMenuItem editable && editable.getItemType() == EditItemType.PLAIN_TEXT) {
                return text.length() < ((EditableTextMenuItem) item).getTextLength();
            }
            else if(item instanceof EditableTextMenuItem editable && editable.getItemType() == EditItemType.IP_ADDRESS) {
                return text.matches("\\d+\\.\\d+\\.\\d+\\.\\d+");
            }
        }
        catch (Exception e) {
        }
        return false;
    }

    @Override
    public String getValueAsString() {
        var fmt = new MenuItemFormatter();
        return fmt.formatToWire(item, textField.getText());
    }

    @Override
    public void markInvalid() {
        var errorPaint = asFxColor(ControlColor.CORAL);
        textField.setBackground(new Background(new BackgroundFill(errorPaint, new CornerRadii(0), new Insets(0))));
    }
}
