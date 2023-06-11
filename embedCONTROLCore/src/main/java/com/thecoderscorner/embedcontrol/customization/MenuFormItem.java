package com.thecoderscorner.embedcontrol.customization;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentPositioning;

/**
 * A form item represents the actual entry at a position in the row, along with its calculated drawing information and
 * its position.
 */
public abstract class MenuFormItem {
    public static final FontInformation FONT_100_PERCENT = new FontInformation(100, FontInformation.SizeMeasurement.PERCENT);
    public static MenuFormItem NO_FORM_ITEM = new NoFormItem();
    private ColorCustomizable settings;
    private ComponentPositioning positioning;
    private FontInformation fontInfo = FONT_100_PERCENT;

    public MenuFormItem(ColorCustomizable settings, ComponentPositioning positioning) {
        this.settings = settings;
        this.positioning = positioning;
    }

    public abstract boolean isValid();
    public abstract String getDescription();

    public FontInformation getFontInfo() {
        return fontInfo;
    }
    public void setFontInfo(FontInformation fontInfo) {
        this.fontInfo = fontInfo;
    }

    public ColorCustomizable getSettings() {return settings;}
    public void setSettings(ColorCustomizable settings) { this.settings = settings;}

    public ComponentPositioning getPositioning() {
        return positioning;
    }

    public void setColSpan(int newSpan) {
        positioning = new ComponentPositioning(positioning.getRow(), positioning.getCol(), positioning.getRowSpan(), newSpan);
    }

    public void setPositioning(ComponentPositioning positioning) {
        this.positioning = positioning;
    }

    /**
     * Creates a blank form item that is not ready for saving, it effectively has no settings.
     */
    public static class NoFormItem extends MenuFormItem {
        private NoFormItem() {
            super(null, new ComponentPositioning(0,0));
        }

        @Override
        public boolean isValid() {return false;}

        @Override
        public String getDescription() {
            return "Empty";
        }
    }
}
