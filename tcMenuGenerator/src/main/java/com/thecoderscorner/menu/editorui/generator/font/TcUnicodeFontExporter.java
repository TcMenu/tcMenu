package com.thecoderscorner.menu.editorui.generator.font;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator.LINE_BREAK;
import static com.thecoderscorner.menu.editorui.util.StringHelper.printArrayToStream;

public record TcUnicodeFontExporter(String fontName, List<TcUnicodeFontBlock> blocks, int yAdvance) implements FontEncoder {
    List<TcUnicodeFontGlyph> itemsFromAllBlocks() {
        return blocks.stream().flatMap(b -> b.glyphs().stream()).collect(Collectors.toList());
    }
    @Override
    public void encodeFontToStream(OutputStream stream, FontFormat fmt) {
        var ps = new PrintStream(stream);
        ps.println("// Font file generated by theCodersCorner.com Font Generator on " + LocalDateTime.now());
        ps.println();
        switch(fmt) {
            case ADAFRUIT -> encodeAdafruit(ps);
            case TC_UNICODE -> encodeTcUnicode(ps);
        }
    }

    private void encodeAdafruit(PrintStream ps) {
        ps.println("const uint8_t " + fontName + "Bitmaps[] PROGMEM {");
        List<TcUnicodeFontGlyph> allGlyphs = itemsFromAllBlocks();
        printByteArray(ps, allGlyphs);
        ps.println("};");
        ps.println();

        ps.println("const GFXglyph " + fontName + "Glyphs[] PROGMEM = {");
        allGlyphs.sort(Comparator.comparing(TcUnicodeFontGlyph::charNum));
        int min = allGlyphs.get(0).charNum();
        int max = allGlyphs.stream().map(TcUnicodeFontGlyph::charNum).reduce(0, Integer::max);

        int bmpOffset = 0;
        boolean first = true;
        int lastCode = -1;
        for(var item : allGlyphs) {
            if(!first) {
                ps.println(",");
            }
            first = false;
            while(lastCode != -1 && item.charNum != (lastCode + 1)) {
                // adafruit cannot have gaps, we need to fill in any holes in the range. Usually OK for ASCII
                ps.printf("    { %d, %d, %d, %d, %d, %d }, /* empty fill %d */", bmpOffset, 0,  0, 0, 0, 0, lastCode + 1);
                ps.println();
                lastCode = lastCode + 1;
            }
            lastCode = item.charNum();
            ps.printf("    { %d, %d, %d, %d, %d, %d } /* %s %d */", bmpOffset, item.width(),
                    item.height(), item.xAdvance(), item.xOffset(), item.yOffset(),
                    Arrays.toString(Character.toChars(item.charNum())), item.charNum());
            bmpOffset += item.bitmapData().length;
        }
        ps.println();
        ps.println("};");
        ps.println();
        ps.println("const GFXfont " + fontName + " PROGMEM = {");
        ps.println("    (uint8_t*)" + fontName + "Bitmaps,");
        ps.println("    (GFXglyph*)" + fontName + "Glyphs,");
        ps.println("    " + min + ", " + max + ",");
        ps.println("    " + yAdvance);
        ps.println("};");
        ps.println();
    }

    private void encodeTcUnicode(PrintStream ps) {
        ps.println("#include <graphics/UnicodeFontHandler.h>");
        ps.println();

        var blockData = new ArrayList<String>();
        for(var block : blocks) {
            ps.println("// Bitmaps for " + block.mapping());
            ps.printf("const uint8_t %sBitmaps_%d[] PROGMEM {", fontName, block.mapping().ordinal());
            ps.println();
            printByteArray(ps, block.glyphs());
            ps.println("};");
            ps.println();

            blockData.add(String.format("    {%d, %sBitmaps_%d, %2$sGlyphs_%3$d, %d} /* %s */",
                    block.mapping().getStartingCode(),  fontName, block.mapping().ordinal(),
                    block.mapping().getEndingCode() - block.mapping().getStartingCode(), block.mapping()));

            ps.println("// Glyphs for " + block.mapping());
            ps.printf("const UnicodeFontGlyph %sGlyphs_%d[] PROGMEM = {", fontName, block.mapping().ordinal());
            ps.println();

            int bmpOffset = 0;
            boolean first = true;
            for(var item : block.glyphs()) {
                if(!first) {
                    ps.println(",");
                }
                first = false;
                ps.printf("    { %d, %d, %d, %d, %d, %d, %d} /* %s %d*/ ", item.charNum(), bmpOffset, item.width(),
                        item.height(), item.xAdvance(), item.xOffset(), item.yOffset(),
                        Arrays.toString(Character.toChars(item.charNum())), item.charNum());
                bmpOffset += item.bitmapData().length;
            }
            ps.println();
            ps.println("};");
            ps.println();
        }

        ps.println("const UnicodeFontBlock " + fontName + "Blocks[] PROGMEM = {");
        ps.println(blockData.stream().collect(Collectors.joining("," + LINE_BREAK)));
        ps.println("};");
        ps.println();

        ps.printf("const UnicodeFont " + fontName + "[] PROGMEM = {%sBlocks, %d, %d};",  fontName, blocks.size(), yAdvance);
        ps.println();
        ps.println();
    }

    private void printByteArray(PrintStream ps, List<TcUnicodeFontGlyph> glyphs) {
        int dataSize = glyphs.stream().map(i -> i.bitmapData().length).reduce(0, Integer::sum);
        byte[] dataBytes = new byte[dataSize];
        int current = 0;
        for(var item : glyphs) {
            System.arraycopy(item.bitmapData(), 0, dataBytes, current, item.bitmapData().length);
            current += item.bitmapData().length;
        }

        printArrayToStream(ps, dataBytes, 20);
    }

    public ByteBuffer encodeFontAsByteArray(FontFormat fmt) {
        throw new UnsupportedOperationException();
    }

    public record TcUnicodeFontGlyph(int charNum, byte[] bitmapData, int width, int height, int xAdvance, int xOffset, int yOffset) {
    }

    public record TcUnicodeFontBlock(UnicodeBlockMapping mapping, List<TcUnicodeFontGlyph> glyphs) {
    }
}
