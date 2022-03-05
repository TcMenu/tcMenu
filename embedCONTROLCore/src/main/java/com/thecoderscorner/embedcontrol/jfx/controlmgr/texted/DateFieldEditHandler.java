package com.thecoderscorner.embedcontrol.jfx.controlmgr.texted;

import com.thecoderscorner.menu.domain.MenuItem;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class DateFieldEditHandler implements FieldEditHandler {
    private final MenuItem item;
    private final DatePicker datePicker;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public DateFieldEditHandler(MenuItem item, Object startingValue) {
        this.item = item;

        if(startingValue instanceof TemporalAccessor ta) {
            this.datePicker = new DatePicker(LocalDate.from(ta));
        }
        else {
            var strippedStr = startingValue.toString().replace("[", "");
            strippedStr = strippedStr.replace("]", "");
            var current = formatter.parse(startingValue.toString());
            this.datePicker = new DatePicker(LocalDate.from(current));
        }
    }

    @Override
    public Node getEditorComponent() {
        return datePicker;
    }

    @Override
    public boolean isCurrentlyValid() {
        return true;
    }

    @Override
    public String getValueAsString() {
        return formatter.format(datePicker.getValue());
    }

    @Override
    public void markInvalid() {
        // not supported for this case.
    }
}
