package com.thecoderscorner.embedcontrol.core.service;

import com.thecoderscorner.embedcontrol.core.util.FieldMapping;
import com.thecoderscorner.embedcontrol.core.util.FieldType;
import com.thecoderscorner.embedcontrol.core.util.TableMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@TableMapping(tableName = "TC_FORM", uniqueKeyField = "FORM_ID")
public class TcMenuFormPersistence {
    private final static System.Logger logger = System.getLogger(TcMenuFormPersistence.class.getSimpleName());

    @FieldMapping(fieldName = "FORM_ID", fieldType = FieldType.INTEGER, primaryKey = true)
    private int formId;
    @FieldMapping(fieldName = "FORM_UUID", fieldType = FieldType.VARCHAR)
    private String uuid;
    @FieldMapping(fieldName = "FORM_NAME", fieldType = FieldType.VARCHAR)
    private String formName;
    @FieldMapping(fieldName = "XML_DATA", fieldType = FieldType.LARGE_TEXT)
    private String xmlData;
    @FieldMapping(fieldName = "FORM_MODE", fieldType = FieldType.ENUM)
    private FormPersistMode formMode;

    public TcMenuFormPersistence() {
    }

    public TcMenuFormPersistence(int formId, FormPersistMode mode, String uuid, String name, String txt) {
        this.formId = formId;
        this.uuid = uuid;
        this.formName = name;
        this.xmlData = txt;
        this.formMode = mode;
    }

    public static TcMenuFormPersistence createProjectFileFormPersistence(String name, String uuid, String fileName) {
        return new TcMenuFormPersistence(-1, FormPersistMode.WITHIN_PROJECT, uuid, name, fileName);
    }

    public TcMenuFormPersistence projectFormLayoutUpdate(String formName) {
        // when the form represents a project form, then it should be no more than a link to a file in the project
        return new TcMenuFormPersistence(formId, FormPersistMode.WITHIN_PROJECT, uuid, formName, xmlData);
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
        if(formMode == FormPersistMode.WITHIN_PROJECT) {
            try {
                return Files.readString(Path.of(xmlData));
            } catch (IOException e) {
                logger.log(System.Logger.Level.ERROR, "Project Form load failed on " + xmlData, e);
                return "";
            }
        } else {
            return xmlData;
        }
    }

    public Optional<Path> getFileNameIfPresent() {
        if(formMode == FormPersistMode.WITHIN_PROJECT) {
            return Optional.of(Path.of(xmlData));
        } else {
            return Optional.empty();
        }
    }

    public FormPersistMode getFormMode() {
        return formMode;
    }
}
