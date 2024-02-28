package com.thecoderscorner.menu.editorui.gfxui.font;

import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import com.thecoderscorner.menu.persist.XMLDOMHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.thecoderscorner.menu.editorui.gfxui.font.FontGlyphGenerator.FontDimensionInformation;
import static java.lang.System.Logger.Level.WARNING;

public class EmbeddedFont {
    public final System.Logger logger = System.getLogger(getClass().getSimpleName());

    public enum EmbeddedFontType {
        XML_LOADED, IMPORTED, CREATED_FONT, NOT_A_FONT
    }

    public static final String EMBEDDED_FONT_ROOT = "embeddedFont";

    public static final long APPROX_ADA_SIZE = 8;
    public static final long ADA_OVERHEAD = 16;
    public static final long APPROX_TCUNICODE_SIZE = 10;
    public static final long TC_UNI_OVERHEAD = 16; // for each block
    private final Set<UnicodeBlockMapping> blockMappings;
    private final Map<UnicodeBlockMapping, List<EmbeddedFontGlyph>> mapBlockToGlyph = new HashMap<>();

    private final Map<Integer, EmbeddedFontGlyph> mapOfGlyph = new HashMap<>();
    private EmbeddedFontType fontType;
    private Path fontPath;

    private String fontName;
    private final int size;
    private int yAdvance;
    private int belowBaseline;

    public EmbeddedFont() {
        blockMappings = Set.of(UnicodeBlockMapping.BASIC_LATIN, UnicodeBlockMapping.LATIN1_SUPPLEMENT);
        size = 8;
        fontName = "";
        yAdvance = 0;
        belowBaseline = 0;
        fontType = EmbeddedFontType.NOT_A_FONT;
        fontPath = null;
    }

    public EmbeddedFont(FontGlyphGenerator loadedFont, Set<UnicodeBlockMapping> chosenMappings, Path fileName, int size) {
        this.blockMappings = chosenMappings;
        this.fontPath = fileName;
        this.fontName = fileName.getFileName().toString();
        this.fontType = EmbeddedFontType.IMPORTED;
        this.size = size;
        for (var blockRange : UnicodeBlockMapping.values()) {
            if (!chosenMappings.contains(blockRange)) continue;
            for (int i = minimumStartingCode(blockRange); i < blockRange.getEndingCode(); i++) {
                var maybeGlyph = loadedFont.getConvertedGlyph(i);
                if (maybeGlyph.isPresent()) {
                    var glyph = maybeGlyph.get();
                    var list = mapBlockToGlyph.computeIfAbsent(blockRange, _ -> new ArrayList<>(256));
                    mapOfGlyph.put(glyph.code(), glyph);
                    glyph.setFont(this);
                    list.add(glyph);
                    if(glyph.toBaseLine() + glyph.fontDims().pixelsBelowBaseline() > yAdvance) {
                        yAdvance = glyph.toBaseLine() + glyph.fontDims().pixelsBelowBaseline();
                    }
                    if(glyph.belowBaseline() > belowBaseline) {
                        belowBaseline = glyph.belowBaseline();
                    }
                }
            }
        }
    }

    public EmbeddedFont(Path pathName) throws Exception {
        var data = Files.readString(pathName);
        var doc = XMLDOMHelper.loadDocumentFromData(data);
        var root = doc.getDocumentElement();
        if(!root.getNodeName().equals(EMBEDDED_FONT_ROOT)) {
            throw new IOException("Document root element should be 'embeddedFont'");
        }

        fontName = XMLDOMHelper.getAttributeOrDefault(root, "fontName", "Undefined");
        size = XMLDOMHelper.getAttributeAsIntWithDefault(root, "size", 0);
        belowBaseline = XMLDOMHelper.getAttributeAsIntWithDefault(root, "belowBaseline", 0);
        yAdvance = XMLDOMHelper.getAttributeAsIntWithDefault(root, "yAdvance", 0);
        fontType = EmbeddedFontType.XML_LOADED;
        fontPath = pathName;

        blockMappings = Set.copyOf(XMLDOMHelper.transformElements(root, "blockMappings", "blockMapping",
                b -> UnicodeBlockMapping.valueOf(b.getTextContent().trim())));

        var glyphs = XMLDOMHelper.transformElements(root, "glyphs", "glyph", (e) -> {
            var glyphRaw = Base64.getDecoder().decode(e.getTextContent());
            var code = XMLDOMHelper.getAttributeAsIntWithDefault(e, "code", 0);
            var aboveBaseline = XMLDOMHelper.getAttributeAsIntWithDefault(e, "aboveBase", 0);
            var belowBaseline = XMLDOMHelper.getAttributeAsIntWithDefault(e, "belowBase", 0);
            var xAdvance = XMLDOMHelper.getAttributeAsIntWithDefault(e, "xAdvance", 0);
            var dims = new FontDimensionInformation(
                    XMLDOMHelper.getAttributeAsIntWithDefault(e, "startX", 0),
                    XMLDOMHelper.getAttributeAsIntWithDefault(e, "startY", 0),
                    XMLDOMHelper.getAttributeAsIntWithDefault(e, "width", 0),
                    XMLDOMHelper.getAttributeAsIntWithDefault(e, "height", 0),
                    belowBaseline
            );
            var sel = XMLDOMHelper.getAttributeAsBool(e, "selected");
            return new EmbeddedFontGlyph(code, dims, glyphRaw, aboveBaseline, belowBaseline, xAdvance, sel, this);
        });

        for(var glyph : glyphs) {
            var bm = findUnicodeBlockMapping(glyph);
            if (bm.isEmpty()) {
                logger.log(WARNING, STR."Corrupt font, no block mapping for \{glyph.code()} so skipping.");
                continue;
            }
            var list = mapBlockToGlyph.computeIfAbsent(bm.get(), _ -> new ArrayList<>(256));
            mapOfGlyph.put(glyph.code(), glyph);
            list.add(glyph);
        }
    }

    public void convertToXmlLoaded(Path path) {
        fontPath = path;
        fontType = EmbeddedFontType.XML_LOADED;
    }

    public void saveFont() throws Exception {
        if(fontType != EmbeddedFontType.XML_LOADED) throw new IllegalArgumentException("Must be an XML font");

        // write out the core information for the font.
        var doc = XMLDOMHelper.newDocumentRoot(EMBEDDED_FONT_ROOT);
        var root = doc.getDocumentElement();
        root.setAttribute("fontName", fontName);
        root.setAttribute("size", String.valueOf(size));
        root.setAttribute("belowBaseline", String.valueOf(belowBaseline));
        root.setAttribute("yAdvance", String.valueOf(yAdvance));

        // We now write out each of the unicode blocks that we have glyphs for ordered by starting code
        var xmlBlocks = XMLDOMHelper.appendElementWithNameValue(root, "blockMappings", null);
        for(var bm : blockMappings.stream().sorted(Comparator.comparingInt(UnicodeBlockMapping::getStartingCode)).toList()) {
            XMLDOMHelper.appendElementWithNameValue(xmlBlocks, "blockMapping", bm.name());
        }

        // get all the glyphs in order by their code
        var list = mapOfGlyph.values().stream()
                .sorted(Comparator.comparingInt(EmbeddedFontGlyph::code))
                .toList();

        // we now write out each glyph in turn, they must be in numeric order or problems will occur later. The numeric
        // values within the glyph become attributes, and the bytes are base64 encoded. For a moderately sized font,
        // with a couple of hundred glyphs this comes to about 32K.
        var xmlGlyphs = XMLDOMHelper.appendElementWithNameValue(root, "glyphs", null);
        for(var gl : list) {
            var glyphRaw = Base64.getEncoder().encodeToString(gl.rawData());
            var xmlGl = XMLDOMHelper.appendElementWithNameValue(xmlGlyphs, "glyph", glyphRaw);
            xmlGl.setAttribute("code", String.valueOf(gl.code()));
            xmlGl.setAttribute("aboveBase", String.valueOf(gl.toBaseLine()));
            xmlGl.setAttribute("belowBase", String.valueOf(gl.belowBaseline()));
            xmlGl.setAttribute("xAdvance", String.valueOf(gl.totalWidth()));
            xmlGl.setAttribute("startX", String.valueOf(gl.fontDims().startX()));
            xmlGl.setAttribute("startY", String.valueOf(gl.fontDims().startY()));
            xmlGl.setAttribute("width", String.valueOf(gl.fontDims().width()));
            xmlGl.setAttribute("height", String.valueOf(gl.fontDims().height()));
            xmlGl.setAttribute("selected", String.valueOf(gl.selected()));
        }

        try(var os = new FileOutputStream(fontPath.toFile())) {
            XMLDOMHelper.writeXml(doc, os, true);
        }
    }

    public static int minimumStartingCode(UnicodeBlockMapping blockMapping) {
        return Math.max(31, blockMapping.getStartingCode());
    }

    private Optional<UnicodeBlockMapping> findUnicodeBlockMapping(EmbeddedFontGlyph glyph) {
        for(var bm : blockMappings) {
            if(glyph.code() >= bm.getStartingCode() && glyph.code() <= bm.getEndingCode()) {
                return Optional.of(bm);
            }
        }
        return Optional.empty();
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public int getSize() {
        return size;
    }

    public EmbeddedFontType getFontType() {
        return fontType;
    }

    public List<EmbeddedFontGlyph> getGlyphsForBlock(UnicodeBlockMapping block) {
        var glyphs = mapBlockToGlyph.get(block);
        return (glyphs != null) ? glyphs : List.of();
    }

    public FontSizeInfo getFontSizeInfo() {
        var selGlyphs = mapOfGlyph.values().stream().filter(EmbeddedFontGlyph::selected).toList();
        var count = selGlyphs.size();
        long byteSize = selGlyphs.stream().map(e-> e.rawData().length).reduce(0, Integer::sum);

        int min = selGlyphs.stream().map(EmbeddedFontGlyph::code).min(Integer::compareTo).orElse(0);
        int max = selGlyphs.stream().map(EmbeddedFontGlyph::code).max(Integer::compareTo).orElse(0);

        var adaSize = ((max - min) * APPROX_ADA_SIZE) + ADA_OVERHEAD + byteSize;
        var tcUniSize = (count * APPROX_TCUNICODE_SIZE) + (blockMappings.size() + TC_UNI_OVERHEAD) + byteSize;
        return new FontSizeInfo(adaSize, tcUniSize, byteSize, count);
    }

    public boolean isAnythingSelected() {
        return mapBlockToGlyph.values().stream().flatMap(Collection::stream).anyMatch(EmbeddedFontGlyph::selected);
    }

    public boolean isPopulated() {
        return !StringHelper.isStringEmptyOrNull(fontName);
    }

    public long fontSizeInBytes(FontEncoder.FontFormat fmt) {
        long bitmapSizes = mapBlockToGlyph.values().stream()
                .flatMap(Collection::stream)
                .filter(EmbeddedFontGlyph::selected)
                .map(g -> g.rawData().length)
                .reduce(0, Integer::sum);

        if(fmt == FontEncoder.FontFormat.ADAFRUIT) {
            var allGlyphs = mapBlockToGlyph.values().stream().flatMap(Collection::stream).toList();
            int min = allGlyphs.getFirst().code();
            int max = allGlyphs.stream().map(EmbeddedFontGlyph::code).reduce(0, Integer::max);
            long fontSizes = (((max - min) + 1) * APPROX_ADA_SIZE) + ADA_OVERHEAD;
            return fontSizes + bitmapSizes;
        } else {
            long blockSize = blockMappings.size() * TC_UNI_OVERHEAD;
            long glyphSize = mapBlockToGlyph.values().stream().mapToLong(List::size).sum() * APPROX_TCUNICODE_SIZE;
            return blockSize + glyphSize + bitmapSizes + APPROX_TCUNICODE_SIZE;
        }
    }

    public Set<UnicodeBlockMapping> getAvailableMappings() {
        return blockMappings;
    }

    public int getYAdvance() {
        return yAdvance;
    }

    public int getBelowBaseline() {
        return belowBaseline;
    }

    public EmbeddedFontGlyph getGlyph(int code) {
        return mapOfGlyph.get(code);
    }

    public Path getFontPath() {
        return fontPath;
    }

    public String getDefaultFontVariableName() {
        var file = fontName;
        int extensionIndex = file.lastIndexOf('.');
        if(extensionIndex != -1) {
            file = file.substring(0, extensionIndex);
        }
        var outputName = STR."\{file}_\{size}pt";
        return VariableNameGenerator.makeNameFromVariable(outputName);
    }

    public record FontSizeInfo(long adafruitSize, long tcUnicodeSize, long byteSize, int count){
    }
}
