package com.thecoderscorner.embedcontrol.core.service;

public class TcMenuFormPersistence {
    private final int formId;
    private final String uuid;
    private final String formName;
    private final AppDataStore dataStore;
    private String xmlData;


    public TcMenuFormPersistence(int formId, String uuid, String formName, AppDataStore dataStore) {
        this.formId = formId;
        this.uuid = uuid;
        this.formName = formName;
        this.dataStore = dataStore;
    }

    public TcMenuFormPersistence(int formId, String uuid, String name, String txt) {
        this.formId = formId;
        this.uuid = uuid;
        this.formName = name;
        this.xmlData = txt;
        this.dataStore = null;
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
        if(xmlData == null) {
            xmlData = dataStore.getUniqueFormData(formId);
        }
        return xmlData;
    }
}
