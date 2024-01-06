package com.thecoderscorner.menu.editorui.generator.font;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.lang.System.Logger.Level;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class AwtLoadedFont implements LoadedFont {
    private final System.Logger logger = System.getLogger(AwtLoadedFont.class.getSimpleName());
    private AntiAliasMode antiAliasMode = AntiAliasMode.ANTI_ALIAS_ON;
    public AtomicReference<Set<UnicodeBlockMapping>> enabledUnicodeGroups = new AtomicReference<>(Set.of());
    public final Map<Integer, ConvertedFontGlyph> fontGlyphCache = new HashMap<>(4096);

    private Font font;

    public AwtLoadedFont(String fontFile, FontStyle fontStyle, int size, Set<UnicodeBlockMapping> mappings, AntiAliasMode mode) {
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, new File(fontFile));
            font = font.deriveFont(toAwtStyle(fontStyle), size);
            this.antiAliasMode = mode;
            enabledUnicodeGroups.set(mappings);
            fontHasChanged();
        } catch (Exception e) {
            logger.log(Level.ERROR, "Failed creating font", e);
        }
    }

    public AwtLoadedFont(Font font, FontStyle fontStyle, int size, Set<UnicodeBlockMapping> mappings, AntiAliasMode mode) {
        try {
            this.font = font.deriveFont(toAwtStyle(fontStyle), size);
            this.antiAliasMode = mode;
            enabledUnicodeGroups.set(mappings);
            fontHasChanged();
        } catch (Exception e) {
            logger.log(Level.ERROR, "Failed creating font", e);
        }
    }

    private int toAwtStyle(FontStyle fontStyle) {
        return switch (fontStyle) {
            case PLAIN -> 0;
            case BOLD -> 1;
            case ITALICS -> 2;
            case BOLD_ITALICS -> 3;
        };
    }

    @Override
    public Optional<ConvertedFontGlyph> getConvertedGlyph(int code) {
        return Optional.ofNullable(fontGlyphCache.get(code));
    }

    @Override
    public boolean canDisplay(int code) {
        return fontGlyphCache.containsKey(code);
    }

    private void fontHasChanged() {
        fontGlyphCache.clear();
        var totalRange = enabledUnicodeGroups.get().stream()
                .map(gr -> gr.getEndingCode() - gr.getStartingCode())
                .reduce(0, Integer::sum);
        logger.log(Level.INFO, "Block change, unicode chars to process=" + totalRange);

        var latch = new CountDownLatch(totalRange);
        for (var group : this.enabledUnicodeGroups.get()) {
            for (int code = group.getStartingCode(); code <= group.getEndingCode(); code++) {
                internalConvert(code, maybeGlyph -> {
                    if (maybeGlyph.isPresent()) {
                        fontGlyphCache.put(maybeGlyph.get().code(), maybeGlyph.get());
                    }
                    latch.countDown();
                });
            }
        }

        try {
            boolean finished = latch.await(5, TimeUnit.SECONDS);
            logger.log(Level.INFO, "Processing of characters finished=" + finished);

        } catch (InterruptedException e) {
            logger.log(Level.ERROR, "Processing of characters failed in error", e);
        }
    }

    @Override
    public void deriveFont(FontStyle fontStyle, int size, Set<UnicodeBlockMapping> newGroups, AntiAliasMode mode) {
        font = font.deriveFont(toAwtStyle(fontStyle), size);
        enabledUnicodeGroups.set(newGroups);
        antiAliasMode = mode;
        fontHasChanged();
    }

    public void internalConvert(int code, Consumer<Optional<ConvertedFontGlyph>> fontGlyphConsumer) {
        SwingUtilities.invokeLater(() -> {
            try {
                if (!font.canDisplay(code)) {
                    fontGlyphConsumer.accept(Optional.empty());
                    return;
                }
                int sizeBmp = font.getSize() * 2;
                var offScreenImg = new BufferedImage(sizeBmp, sizeBmp, BufferedImage.TYPE_BYTE_BINARY);
                var strCode = new String(Character.toChars(code));
                Graphics2D g = (Graphics2D) offScreenImg.getGraphics();
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, fromAntiAliasMode(antiAliasMode));
                g.setFont(font);
                g.setColor(Color.WHITE);
                FontMetrics fontMetrics = g.getFontMetrics();
                var lm = fontMetrics.getLineMetrics(strCode, g);
                var width = fontMetrics.stringWidth(strCode);
                g.drawString(strCode, 0, (int) lm.getAscent());
                var data = ((DataBufferByte) offScreenImg.getRaster().getDataBuffer()).getData();

                var dims = getFontDimensions(data, sizeBmp, Math.round(lm.getAscent()));
                if (width == 0) {
                    logger.log(Level.DEBUG, "Empty Code " + code + "(" + strCode + ")");
                    fontGlyphConsumer.accept(Optional.empty());
                } else {
                    FontBitPacker bitSet = new FontBitPacker(dims.widthHeight());
                    int theBit = 0;
                    int perLine = (sizeBmp + 7) / 8;
                    for (int y = dims.startY(); y < dims.lastY(); y++) {
                        for (int x = dims.startX(); x < dims.lastX(); x++) {
                            int rgb = (data[(y * perLine) + (x / 8)] >> (7-(x % 8))) & 1;
                            bitSet.pushBit(rgb != 0);
                            theBit = theBit + 1;
                        }
                    }

                    byte[] fontRawData = bitSet.getData();
                    logger.log(Level.DEBUG, "Code " + code + "(" + strCode + ") width " + width + " height " + dims.height());
                    fontGlyphConsumer.accept(Optional.of(
                            new ConvertedFontGlyph(code, dims, fontRawData, Math.round(lm.getAscent()), Math.round(lm.getDescent()), width)
                    ));
                }
            } catch (Exception ex) {
                logger.log(Level.ERROR, "Processing of character failed, code=", code, ex);
            }
        });
    }

    private Object fromAntiAliasMode(AntiAliasMode antiAliasMode) {
        return switch (antiAliasMode) {
            case NO_ANTI_ALIAS -> RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
            case ANTI_ALIAS_ON -> RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
            case ANTI_ALIAS_DEFAULT -> RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT;
            case ANTI_ALIAS_GASP -> RenderingHints.VALUE_TEXT_ANTIALIAS_GASP;
        };
    }

    private static FontDimensionInformation getFontDimensions(byte[] data, int sizeBmp, int baseline) {
        int firstYLocation = -1;
        int lastYLocation = 1;
        int firstXLocation = -1;
        int lastXLocation = 1;
        int perLine = (sizeBmp + 7) / 8;
        for (int y = 0; y < sizeBmp; y++) {
            boolean lineHadData = false;
            int xStartThisGo = -1;
            int xEndThisGo = -1;
            for (int x = 0; x < sizeBmp; x++) {
                int locSrc = (y * perLine) + (x / 8);
                if (firstYLocation == -1 && (data[locSrc] >> (7-(x % 8)) & 1) != 0) {
                    firstYLocation = y;
                }
                lineHadData = lineHadData || data[locSrc] != 0;

                if (xStartThisGo == -1 && data[locSrc] != 0) {
                    xStartThisGo = x;
                }
                if (data[locSrc] != 0) {
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
        if (firstYLocation == -1 || firstXLocation == -1) return FontDimensionInformation.DIMENSION_EMPTY;
        return new FontDimensionInformation(
                firstXLocation, firstYLocation,
                (lastXLocation - firstXLocation) + 1,
                (lastYLocation - firstYLocation) + 1,
                lastYLocation > baseline ? lastYLocation - baseline : 0);
    }

    static class FontBitPacker {
        public final int bitsNeeded;
        public int bit;
        public byte[] data;

        public FontBitPacker(int bitsNeeded) {
            this.bitsNeeded = bitsNeeded;
            this.bit = 0;
            this.data = new byte[(bitsNeeded + 7) / 8];
            Arrays.fill(data, (byte)0);
        }

        public void pushBit(boolean value) {
            if(bit >= bitsNeeded) throw new ArrayIndexOutOfBoundsException("too many bits");
            byte theBit = (byte)(7 - (bit % 8));
            if(value) {
                this.data[bit / 8] |=  (byte)(1 << theBit);
            }

            bit++;
        }

        public byte[] getData() {
            return data;
        }
    }
}
