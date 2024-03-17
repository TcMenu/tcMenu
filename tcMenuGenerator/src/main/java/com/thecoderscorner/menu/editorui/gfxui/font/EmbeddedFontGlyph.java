package com.thecoderscorner.menu.editorui.gfxui.font;

import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.domain.util.PortablePalette;
import com.thecoderscorner.menu.editorui.gfxui.imgedit.ImageDrawingGrid;
import com.thecoderscorner.menu.editorui.gfxui.pixmgr.NBppBitPacker;
import com.thecoderscorner.menu.editorui.gfxui.pixmgr.NativeBmpBitPacker;

import java.util.Objects;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.*;
import static com.thecoderscorner.menu.domain.util.PortablePalette.PaletteMode;
import static com.thecoderscorner.menu.editorui.gfxui.font.FontGlyphGenerator.FontDimensionInformation;

public class EmbeddedFontGlyph {
    private final int code;
    private FontDimensionInformation fontDims;
    private NativeBmpBitPacker data;
    private int toBaseLine;
    private int belowBaseline;
    private int totalWidth;
    private EmbeddedFont font;
    private boolean selected;

    public EmbeddedFontGlyph(int code, FontDimensionInformation fontDims, byte[] data, int toBaseLine, int belowBaseline, int totalWidth, boolean selected, EmbeddedFont font) {
        this.code = code;
        this.fontDims = fontDims;
        this.font = font;
        this.data = new NativeBmpBitPacker(data, fontDims.width(), fontDims.height(), true);
        this.toBaseLine = toBaseLine;
        this.belowBaseline = belowBaseline;
        this.totalWidth = totalWidth;
        this.selected = selected;
    }

    public int calculatedWidth() {
        return Math.max(fontDims.startX() + fontDims.width(), totalWidth);
    }

    public int code() {
        return code;
    }

    public FontDimensionInformation fontDims() {
        return fontDims;
    }

    public byte[] rawData() {
        return data.getData();
    }

    public NativeBmpBitPacker data() {
        return data;
    }

    public int toBaseLine() {
        return toBaseLine;
    }

    public int belowBaseline() {
        return belowBaseline;
    }

    public int totalWidth() {
        return totalWidth;
    }

    public boolean selected() {
        return selected;
    }

    public void setFont(EmbeddedFont font) {
        this.font = font;
    }

    public void setGlyphData(NativeBmpBitPacker newBits, FontDimensionInformation fontDims) {
        this.data = newBits;
        this.fontDims = fontDims;
    }

    public void setFontSizing(int belowBaseline, int aboveBaseline, int totalWidth) {
        this.toBaseLine = aboveBaseline;
        this.belowBaseline = belowBaseline;
        this.totalWidth = totalWidth;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (EmbeddedFontGlyph) obj;
        return this.code == that.code &&
                Objects.equals(this.fontDims, that.fontDims) &&
                Objects.equals(this.data, that.data) &&
                this.toBaseLine == that.toBaseLine &&
                this.belowBaseline == that.belowBaseline &&
                this.totalWidth == that.totalWidth &&
                this.selected == that.selected;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, fontDims, data, toBaseLine, belowBaseline, totalWidth, selected);
    }

    @Override
    public String toString() {
        return STR."ConvertedFontGlyph[code=\{code}, fontDims=\{fontDims}, data=\{data}, toBaseLine=\{toBaseLine}, belowBaseline=\{belowBaseline}, totalWidth=\{totalWidth}, selected=\{selected}\{']'}";
    }

    /**
     * Retrieves a display bitmap for the glyph as a canvas that can be presented onto a JavaFX pane. It will also
     * contain the baseline within the returned component.
     *
     * @return The display bitmap for the glyph as a Canvas object.
     */
    public ImageDrawingGrid getDisplayBitmapForGlyph() {
        var palette = new PortablePalette(new PortableColor[]{BLACK, WHITE, GREY, BLUE}, PaletteMode.TWO_BPP);
        int advance = font.getYAdvance() + 1;
        var bmp = new NBppBitPacker(totalWidth(), advance, 2);
        ImageDrawingGrid drawingGrid = new ImageDrawingGrid(bmp, palette, false);
        drawingGrid.setCurrentColor(2);
        int baseLinePoint = advance - font.getBelowBaseline();
        drawingGrid.drawLine(0, baseLinePoint, bmp.getPixelWidth() - 1, baseLinePoint);
        // The below calc is as follows. First we calculate what the ascent is, IE above the baseline, then we subtract
        // from that the height of the bitmap, finally we then add back in the below baseline amount for this glyph.
        int startingPositionY = ((advance - font.getBelowBaseline()) - fontDims.startY());
        bmp.pushBitsOn(fontDims().startX(), startingPositionY, data, 1);
        return drawingGrid;
    }

    public void setSelected(boolean b) {
        selected = b;
    }

    public void reDimensionToSmallest() {
        int firstYLocation = -1;
        int lastYLocation = 1;
        int firstXLocation = -1;
        int lastXLocation = 1;
        for (int y = 0; y < data.getPixelHeight(); y++) {
            boolean lineHadData = false;
            int xStartThisGo = -1;
            int xEndThisGo = -1;
            for (int x = 0; x < data.getPixelWidth(); x++) {
                int idx = data.getDataAt(x, y);
                if (firstYLocation == -1 && idx != 0) {
                    firstYLocation = y;
                }
                lineHadData = lineHadData || idx != 0;

                if (xStartThisGo == -1 && idx != 0) {
                    xStartThisGo = x;
                }
                if (idx != 0) {
                    xEndThisGo = x;
                }
            }
            if (lineHadData) {
                lastYLocation = y;
            }

            if ((xStartThisGo != -1 && xStartThisGo < firstXLocation) || firstXLocation == -1)
                firstXLocation = xStartThisGo;
            if (xEndThisGo != -1 && xEndThisGo > lastXLocation) lastXLocation = xEndThisGo;
        }

        // do not re-dimension if nothing found
        if (firstYLocation == -1 || firstXLocation == -1) return;

        data = (NativeBmpBitPacker) data.segmentOf(firstXLocation, firstYLocation, lastXLocation, lastYLocation);
        fontDims = new FontDimensionInformation(firstXLocation, firstYLocation, data.getPixelWidth(), data.getPixelHeight(), fontDims.pixelsBelowBaseline());
    }

}
