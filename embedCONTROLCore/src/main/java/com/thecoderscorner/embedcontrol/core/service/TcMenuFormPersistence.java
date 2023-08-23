package com.thecoderscorner.embedcontrol.core.service;

import com.thecoderscorner.embedcontrol.core.util.FieldMapping;
import com.thecoderscorner.embedcontrol.core.util.FieldType;
import com.thecoderscorner.embedcontrol.core.util.TableMapping;

@TableMapping(tableName = "TC_FORM", uniqueKeyField = "FORM_ID")
public class TcMenuFormPersistence {
    @FieldMapping(fieldName = "FORM_ID", fieldType = FieldType.INTEGER)
    private int formId;
    @FieldMapping(fieldName = "FORM_UUID", fieldType = FieldType.VARCHAR)
    private String uuid;
    @FieldMapping(fieldName = "FORM_NAME", fieldType = FieldType.VARCHAR)
    private String formName;
    @FieldMapping(fieldName = "XML_DATA", fieldType = FieldType.BLOB)
    private String xmlData;

    public TcMenuFormPersistence() {

    }

    public TcMenuFormPersistence(int formId, String uuid, String name, String txt) {
        this.formId = formId;
        this.uuid = uuid;
        this.formName = name;
        this.xmlData = txt;
    }

    public static TcMenuFormPersistence anEmptyFormPersistence(String uuid) {
        var formData = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<EmbedControl boardUuid=\""+ uuid + "\" layoutName=\"Untitled\"><MenuLayouts/><ColorSets/></EmbedControl>";
        return new TcMenuFormPersistence(-1, uuid, "Untitled", formData);
    }

    public int getFormId() {
        return formId;
    }

    public String getUuid() {
        return uuid;
    }

    public String getFormName() {
        return formName;
    }

    public String getXmlData() {
        return xmlData;
    }
}
