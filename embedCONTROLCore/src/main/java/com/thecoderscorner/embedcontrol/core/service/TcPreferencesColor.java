package com.thecoderscorner.embedcontrol.core.service;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.embedcontrol.core.util.FieldMapping;
import com.thecoderscorner.embedcontrol.core.util.FieldType;
import com.thecoderscorner.embedcontrol.core.util.TableMapping;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.ColorComponentType;

@TableMapping(tableName = "GLOBAL_SETTING_COLORS", uniqueKeyField = "COLOR_ID")
public class TcPreferencesColor {
    @FieldMapping(fieldName = "COLOR_ID", fieldType = FieldType.INTEGER, primaryKey = true)
    private int id;

    @FieldMapping(fieldName = "COL_FG", fieldType = FieldType.VARCHAR)
    private String fg;
    @FieldMapping(fieldName = "COL_BG", fieldType = FieldType.VARCHAR)
    private String bg;
    @FieldMapping(fieldName = "COMP_TYPE", fieldType = FieldType.VARCHAR)
    private String compType;
    @FieldMapping(fieldName = "IN_USE", fieldType = FieldType.BOOLEAN)
    private boolean inUse;

    public TcPreferencesColor() {
    }

    public TcPreferencesColor(ColorComponentType colType, ControlColor col) {
        id = colType.ordinal();
        fg = col.getFg().toString();
        bg = col.getBg().toString();
        compType = colType.toString();
        inUse = col.isInUse();
    }

    public ControlColor getControlColor() {
        var cc = new ControlColor(fg, bg);
        cc.setInUse(inUse);
        return cc;
    }

    public ColorComponentType getCompType() {
        return ColorComponentType.valueOf(compType);
    }

    @Override
    public String toString() {
        return "TcPreferencesColor{" +
                "id=" + id +
                ", fg='" + fg + '\'' +
                ", bg='" + bg + '\'' +
                ", compType='" + compType + '\'' +
                ", inUse=" + inUse +
                '}';
    }
}
