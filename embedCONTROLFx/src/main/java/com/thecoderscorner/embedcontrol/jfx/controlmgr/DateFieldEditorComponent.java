package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.ThreadMarshaller;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateFieldEditorComponent extends JfxTextEditorComponentBase<String>
    {
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        private DatePicker dateField;

        public DateFieldEditorComponent(RemoteMenuController remote, ComponentSettings settings, MenuItem item, ThreadMarshaller threadMarshaller) {
            super(remote, settings, item, threadMarshaller);
        }

        public Node createComponent()
        {
            dateField = new DatePicker(LocalDate.now());
            dateField.setDisable(item.isReadOnly());
            return makeTextComponent(dateField, this::dateSendToRemote, isItemEditable(item));
        }

        private void dateSendToRemote(ActionEvent evt)
        {
            var dateStr = dateField.getValue().format(formatter);
            validateAndSend(dateStr);
        }

        @Override
        public void changeControlSettings(RenderingStatus status, String str)
        {
            try {
                var date = formatter.parse(str);
                dateField.setValue(LocalDate.from(date));
                setNodeConditionalColours(dateField, getDrawingSettings().getColors(), ConditionalColoring.ColorComponentType.TEXT_FIELD, status);
            }
            catch(Exception ex) {
                logger.log(System.Logger.Level.WARNING, "Unrecognised string in " + item + " str = " + str, ex);
            }
        }
    }