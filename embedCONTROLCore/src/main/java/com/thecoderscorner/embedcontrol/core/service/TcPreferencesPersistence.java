package com.thecoderscorner.embedcontrol.core.service;

import com.thecoderscorner.embedcontrol.core.util.FieldMapping;
import com.thecoderscorner.embedcontrol.core.util.FieldType;
import com.thecoderscorner.embedcontrol.core.util.TableMapping;
import com.thecoderscorner.menu.domain.state.PortableColor;

@TableMapping(tableName = "GLOBAL_SETTINGS", uniqueKeyField = "SETTING_ID")
public class TcPreferencesPersistence {
    @FieldMapping(fieldName = "SETTING_ID", fieldType = FieldType.INTEGER, primaryKey = true)
    private int settingId;

    @FieldMapping(fieldName = "LOCAL_NAME", fieldType = FieldType.VARCHAR)
    private String localName;
    @FieldMapping(fieldName = "LOCAL_UUID", fieldType = FieldType.VARCHAR)
    private String localUuid;
    @FieldMapping(fieldName = "RECURSE_SUB", fieldType = FieldType.BOOLEAN)
    private boolean recurseSub;
    @FieldMapping(fieldName = "FONT_SIZE", fieldType = FieldType.INTEGER)
    private int fontSize;

    @FieldMapping(fieldName = "BUTTON_FG", fieldType = FieldType.VARCHAR)
    private String fgButton;
    @FieldMapping(fieldName = "BUTTON_BG", fieldType = FieldType.VARCHAR)
    private String bgButton;

    @FieldMapping(fieldName = "UPDATE_FG", fieldType = FieldType.VARCHAR)
    private String fgUpdate;
    @FieldMapping(fieldName = "UPDATE_BG", fieldType = FieldType.VARCHAR)
    private String bgUpdate;

    @FieldMapping(fieldName = "HIGHLIGHT_FG", fieldType = FieldType.VARCHAR)
    private String fgHighlight;
    @FieldMapping(fieldName = "HIGHLIGHT_BG", fieldType = FieldType.VARCHAR)
    private String bgHighlight;

    @FieldMapping(fieldName = "TEXT_FG", fieldType = FieldType.VARCHAR)
    private String fgText;
    @FieldMapping(fieldName = "TEXT_BG", fieldType = FieldType.VARCHAR)
    private String bgText;

    @FieldMapping(fieldName = "ERROR_FG", fieldType = FieldType.VARCHAR)
    private String fgError;
    @FieldMapping(fieldName = "ERROR_BG", fieldType = FieldType.VARCHAR)
    private String bgError;
    @FieldMapping(fieldName = "DIALOG_FG", fieldType = FieldType.VARCHAR)
    private String fgDialog;
    @FieldMapping(fieldName = "DIALOG_BG", fieldType = FieldType.VARCHAR)
    private String bgDialog;


    public void populateGlobalSettings(GlobalSettings settings) {
        settings.setAppName(localName);
        settings.setAppUuid(localUuid);
        settings.setDefaultRecursiveRendering(recurseSub);
        settings.setDefaultFontSize(fontSize);
        settings.getButtonColor().setBg(new PortableColor(bgButton));
        settings.getButtonColor().setFg(new PortableColor(fgButton));
        settings.getUpdateColor().setBg(new PortableColor(bgUpdate));
        settings.getUpdateColor().setFg(new PortableColor(fgUpdate));
        settings.getHighlightColor().setBg(new PortableColor(bgHighlight));
        settings.getHighlightColor().setFg(new PortableColor(fgHighlight));
        settings.getTextColor().setBg(new PortableColor(bgText));
        settings.getTextColor().setFg(new PortableColor(fgText));
        settings.getErrorColor().setBg(new PortableColor(bgError));
        settings.getErrorColor().setFg(new PortableColor(fgError));
        settings.getDialogColor().setBg(new PortableColor(bgDialog));
        settings.getDialogColor().setFg(new PortableColor(fgDialog));
    }

    public TcPreferencesPersistence(GlobalSettings settings) {
        localName = settings.getAppName();
        localUuid = settings.getAppUuid();
        recurseSub = settings.isDefaultRecursiveRendering();
        fontSize = settings.getDefaultFontSize();

        bgButton = settings.getButtonColor().getBg().toString();
        fgButton = settings.getButtonColor().getFg().toString();

        bgUpdate = settings.getUpdateColor().getBg().toString();
        fgUpdate = settings.getUpdateColor().getFg().toString();

        bgHighlight = settings.getHighlightColor().getBg().toString();
        fgHighlight = settings.getHighlightColor().getFg().toString();

        bgText = settings.getTextColor().getBg().toString();
        fgText = settings.getTextColor().getFg().toString();

        bgError = settings.getErrorColor().getBg().toString();
        fgError = settings.getErrorColor().getFg().toString();

        bgDialog = settings.getDialogColor().getBg().toString();
        fgDialog = settings.getDialogColor().getFg().toString();
    }

    public TcPreferencesPersistence() {
    }
}
