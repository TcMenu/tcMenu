package com.thecoderscorner.embedcontrol.core.service;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.ColorComponentType;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.embedcontrol.core.util.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.ColorComponentType.*;

@TableMapping(tableName = "GLOBAL_SETTINGS", uniqueKeyField = "SETTING_ID")
public class TcPreferencesPersistence {
    private List<TcPreferencesColor> colorsToSave;

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
    @ProvideStore
    private TccDatabaseUtilities dataStore;

    public void populateGlobalSettings(GlobalSettings settings) {
        settings.setAppName(localName);
        settings.setAppUuid(localUuid);
        settings.setDefaultRecursiveRendering(recurseSub);
        settings.setDefaultFontSize(fontSize);

        var mapPrefColors = colorsToSave.stream().collect(Collectors.toMap(TcPreferencesColor::getCompType, TcPreferencesColor::getControlColor));
        updateColor(TEXT_FIELD, mapPrefColors, settings);
        updateColor(BUTTON, mapPrefColors, settings);
        updateColor(DIALOG, mapPrefColors, settings);
        updateColor(CUSTOM, mapPrefColors, settings);
        updateColor(ERROR, mapPrefColors, settings);
        updateColor(HIGHLIGHT, mapPrefColors, settings);
        updateColor(PENDING, mapPrefColors, settings);
    }

    private void updateColor(ColorComponentType compType, Map<ColorComponentType, ControlColor> mapPrefColors, GlobalSettings settings) {
        ControlColor color;
        if(mapPrefColors.containsKey(compType)) {
            color = mapPrefColors.get(compType);
        } else {
            color = new ControlColor(); // not in use color
        }
        settings.getUnderlyingColor(compType).copyColorsFrom(color);
    }

    public TcPreferencesPersistence(GlobalSettings settings) {
        localName = settings.getAppName();
        localUuid = settings.getAppUuid();
        recurseSub = settings.isDefaultRecursiveRendering();
        fontSize = settings.getDefaultFontSize();

        colorsToSave = settings.getColorsToSave().entrySet().stream()
                .map(colEntry -> new TcPreferencesColor(colEntry.getKey(), colEntry.getValue()))
                .toList();
    }

    public TcPreferencesPersistence() {
    }

    public List<TcPreferencesColor> getColorsToSave() {
        return colorsToSave;
    }

    public void setColorsToSave(List<TcPreferencesColor> colorsToSave) {
        this.colorsToSave = colorsToSave;
    }
}
