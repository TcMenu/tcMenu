package com.thecoderscorner.embedcontrol.customization;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentPositioning;
import javafx.scene.image.Image;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.*;

public class TextFormItem extends MenuFormItem {
    private String text;
    private Image img;
    private PortableAlignment alignment;

    public TextFormItem(String txt, ColorCustomizable settings, ComponentPositioning positioning, PortableAlignment align) {
        super(settings, positioning);
        this.text = txt;
        this.img = null;
        this.alignment = align;
    }

    public TextFormItem(String txt, Image img, ColorCustomizable settings, ComponentPositioning positioning, PortableAlignment align) {
        super(settings, positioning);
        this.text = txt;
        this.img = img;
        this.alignment = align;
    }

    @Override
    public boolean isValid() {
        return text != null;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Image getImg() {
        return img;
    }

    public void setImg(Image img) {
        this.img = img;
    }

    @Override
    public String getDescription() {
        return "Edit Text";
    }

    public PortableAlignment getAlignment() {
        return alignment;
    }

    public void setAlignment(PortableAlignment alignment) {
        this.alignment = alignment;
    }
}
