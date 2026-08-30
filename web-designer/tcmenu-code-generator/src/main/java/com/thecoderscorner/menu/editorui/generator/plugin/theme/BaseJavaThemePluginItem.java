package com.thecoderscorner.menu.editorui.generator.plugin.theme;

import com.thecoderscorner.menu.editorui.generator.applicability.EqualityApplicability;
import com.thecoderscorner.menu.editorui.generator.core.CodeConversionContext;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeParameter;
import com.thecoderscorner.menu.editorui.generator.parameters.FontDefinition;
import com.thecoderscorner.menu.editorui.generator.plugin.*;
import com.thecoderscorner.menu.editorui.generator.validation.CannedPropertyValidators;
import com.thecoderscorner.menu.editorui.generator.validation.ChoiceDescription;
import com.thecoderscorner.menu.editorui.util.StringHelper;

import java.util.*;

import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.PRIORITY_MIN;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.PRIORITY_NORMAL;
import static com.thecoderscorner.menu.editorui.generator.parameters.FontMode.*;

public abstract class BaseJavaThemePluginItem extends BaseJavaPluginItem {
    public final static FontDefinition defaultForTcUnicode = new FontDefinition(ADAFRUIT, "OpenSansRegular7pt", 0);
    public enum IconEditMode { NONE, LO_RES, MED_RES }

    protected BaseJavaThemePluginItem(SubSystem subsystem, String imagePath) {
        super(subsystem, imagePath);
    }

    public String unicodeAndIconsCode() {
        var iconEditMode = IconEditMode.valueOf(findPropOrDefault("THEME_EDIT_ICONS", "LO_RES"));
        var unicode = findPropOrFail("USE_TC_UNICODE").equals("true");
        var str = "";
        if(iconEditMode == IconEditMode.LO_RES) {
            str += System.lineSeparator() + "            .withStandardLowResCursorIcons()";
        } else if (iconEditMode == IconEditMode.MED_RES) {
            str += System.lineSeparator() + "            .withStandardMedResCursorIcons()";
        }

        if(unicode) {
            str += System.lineSeparator() + "            .enableTcUnicode()";
        }
        str += ";";
        return str;
    }


    public CreatorProperty fontProperty(String id, String name, String desc, String defValue) {
        return new CreatorProperty(id, name, desc, defValue, SubSystem.THEME, CreatorProperty.PropType.VARIABLE,
                CannedPropertyValidators.fontValidator(), ALWAYS_APPLICABLE);
    }

    public Collection<CreatorProperty> defFontProperties() {
        return List.of(
                separatorProperty("FONT", "Font Settings"),
                fontProperty("THEME_ITEM_FONT", "Font for menu items", "The default Font that menu items draw with", "def:,1"),
                fontProperty("THEME_TITLE_FONT", "Font for titles", "The Font that will be used to draw titles", "def:,1"),
                new CreatorProperty("USE_TC_UNICODE", "Use TcUnicode/UTF-8 for text", "Use TcUnicode for font drawing. Features UTF-8 fonts with font editor", "false",
                        SubSystem.THEME, CreatorProperty.PropType.VARIABLE, CannedPropertyValidators.boolValidator(), ALWAYS_APPLICABLE)

        );

    }

    private String fontExportHeader() {
        var f1 = fontIncludeHeader("THEME_ITEM_FONT");
        var f2 = fontIncludeHeader("THEME_TITLE_FONT");
        if(f1.isEmpty() && f2.isEmpty()) return "";
        if(f1.equals(f2)) f2 = "";

        return "#include <UnicodeFontDefs.h>" + System.lineSeparator() + f1 + f2 + System.lineSeparator();
    }

    private String fontIncludeHeader(String propName) {
        var fdOpt = getFontDefinition(propName);
        if(fdOpt.isEmpty()) return "";
        var fd = fdOpt.get();
        if(fd.fontMode() == ADAFRUIT || fd.fontMode() == ADAFRUIT_LOCAL) {
            return "extern GFXfont* " + fd.fontName() + ";" + System.lineSeparator();
        } else if(fd.fontMode() == TCUNICODE || fd.fontMode() == TCUNICODE_LOCAL) {
            return "extern const UnicodeFont " + fd.fontName() + "[];" + System.lineSeparator();
        }
        else {
            return "";
        }
    }

    public String fontDeclarationCode(String name) {
        var fdOpt = getFontDefinition(name);
        var tcUnicode = findPropOrFail("USE_TC_UNICODE").equals("true");
        if(fdOpt.isEmpty()) {
            return "withNativeFont(nullptr, 1)";
        }
        var fd = fdOpt.get();

        if (tcUnicode && fd.fontNumber() == 0) {
            return "withTcUnicodeFont(" + expandToNull(fd.fontName()) + ")";
        } else if ((fd.fontMode() == ADAFRUIT || fd.fontMode() == ADAFRUIT_LOCAL) &&  fd.fontNumber() > 0) {
            return "withAdaFont(" + expandToNull(fd.fontName()) + ", " + fd.fontNumber() + ")";
        } else {
            return "withNativeFont(" + expandToNull(fd.fontName()) + ", " + fd.fontNumber() + ")";
        }
    }

    private Optional<FontDefinition> getFontDefinition(String name) {
        var prop = getRequiredProperties().stream().filter(p -> p.getName().equals(name))
                .findFirst().orElseThrow();
        return FontDefinition.fromString(prop.getLatestValue());
    }

    public Collection<CreatorProperty> defDrawingProperties() {
        return List.of(
                separatorProperty("SPACING", "Spacing and Drawing Options"),
                CreatorProperty.uintProperty("TITLE_PADDING", "Padding around the title", "Padding that is applied around all sides of title", SubSystem.THEME, 2, 10),
                CreatorProperty.uintProperty("ITEM_PADDING", "Padding around each item", "Padding that is applied around all sides menu items", SubSystem.THEME, 2, 10),
                CreatorProperty.uintProperty("TITLE_TO_ITEM_SPACING", "Title to first item gap", "Space between title and first item", SubSystem.THEME, 2, 10),
                new CreatorProperty("TITLE_SHOW_MODE", "How to present the title", "Choose how the title will be displayed", "TITLE_ALWAYS", SubSystem.THEME, CreatorProperty.PropType.VARIABLE, CannedPropertyValidators.choicesValidator(List.of(
                        new ChoiceDescription("NO_TITLE", "No title"),
                        new ChoiceDescription("TITLE_FIRST_ROW", "Title on first row (scrolls with menu)"),
                        new ChoiceDescription("TITLE_ALWAYS", "Title always at top")
                        ), "TITLE_ALWAYS"),
                        ALWAYS_APPLICABLE),
                CreatorProperty.uintProperty("THEME_ACTION_ROUND_CORNER", "Use rounded corners on actionable items (0=off)", "Use rounded corners on actionable items, 0 is off all corners same", SubSystem.THEME, 4, 15),
                CreatorProperty.uintProperty("THEME_TITLE_ROUND_CORNER", "Use rounded corners on the title (0=off)", "Use rounded corners on title, 0 is off, all corners same", SubSystem.THEME, 4, 15)
        );
    }

    public List<CodeReplacement> replacementsWithExtras(CodeReplacement... codeReplacement) {
        List<CodeReplacement> defaultReplacements = List.of(
                new CodeReplacement("__ITEM_PADDING__", findPropOrFail("ITEM_PADDING"), ALWAYS_APPLICABLE),
                new CodeReplacement("__TITLE_PADDING__", findPropOrFail("TITLE_PADDING"), ALWAYS_APPLICABLE),
                new CodeReplacement("__ACTION_PADDING__", calculateActionPadding(), ALWAYS_APPLICABLE),
                new CodeReplacement("__TITLE_SPACING__", findPropOrFail("TITLE_TO_ITEM_SPACING"), ALWAYS_APPLICABLE),
                new CodeReplacement("__MENU_BORDER_CODE_ACTION__", handleTheBorder("THEME_ACTION_ROUND_CORNER"), ALWAYS_APPLICABLE),
                new CodeReplacement("__MENU_BORDER_CODE_TITLE__", handleTheBorder("THEME_TITLE_ROUND_CORNER"), ALWAYS_APPLICABLE),
                new CodeReplacement("__TITLE_MODE__", findPropOrFail("TITLE_SHOW_MODE"), ALWAYS_APPLICABLE),
                new CodeReplacement("__TITLE_FONT_DECL__", fontDeclarationCode("THEME_TITLE_FONT"), ALWAYS_APPLICABLE),
                new CodeReplacement("__ITEM_FONT_DECL__", fontDeclarationCode("THEME_ITEM_FONT"), ALWAYS_APPLICABLE),
                new CodeReplacement("__UNICODE_AND_ICONS__", unicodeAndIconsCode(), ALWAYS_APPLICABLE),
                new CodeReplacement("__FONT_EXPORT_STATEMENTS__", fontExportHeader(), ALWAYS_APPLICABLE),
                new CodeReplacement("__USE_SLIDERS__", findPropOrDefault("THEME_USE_SLIDERS_ANALOG", "false"), ALWAYS_APPLICABLE)
        );

        var extras = new ArrayList<>(Arrays.asList(codeReplacement));
        extras.addAll(defaultReplacements);
        return List.copyOf(extras);
    }

    private String calculateActionPadding() {
        var actRounding = Integer.parseInt(findPropOrDefault("THEME_ACTION_ROUND_CORNER", "0"));
        var itemPadding = Integer.parseInt(findPropOrDefault("ITEM_PADDING", "0"));
        return String.valueOf(Math.min(15, actRounding + itemPadding));
    }

    private String handleTheBorder(String propName) {
        var prop = Integer.parseInt(findPropOrFail(propName));
        if(prop == 0) {
            return "";
        } else {
            return "%n            .withBorder(MenuBorder(%d, BORD_FILL_ROUNDED))".formatted(prop);
        }
    }

    @Override
    public void beforeGenerationStarts(CodeConversionContext context) {
        super.beforeGenerationStarts(context);
        var tcUnicode = findPropOrFail("USE_TC_UNICODE").equals("true");
        if(!tcUnicode) return;

        checkThatFontIsValidTcUnicode("THEME_TITLE_FONT");
        checkThatFontIsValidTcUnicode("THEME_ITEM_FONT");
    }

    private void checkThatFontIsValidTcUnicode(String name) {
        var prop = getRequiredProperties().stream().filter(p -> p.getName().equals(name))
                .findFirst().orElseThrow();

        var fd = FontDefinition.fromString(prop.getLatestValue());
        if(fd.isEmpty() || StringHelper.isStringEmptyOrNull(fd.get().fontName())) {
            prop.setLatestValue(defaultForTcUnicode.toString());
        }
    }

    @Override
    public List<FunctionDefinition> getFunctions() {
        return List.of(new FunctionDefinition("applyTheme", "", false, false, List.of(
                CodeParameter.unNamedValue("renderer")
        ), ALWAYS_APPLICABLE));
    }

    @Override
    public List<CodeVariable> getVariables() {
        return List.of(
                CodeVariable.fontExport("THEME_ITEM_FONT"),
                CodeVariable.fontExport("THEME_TITLE_FONT")
        );
    }

    protected String buildPalette(String ty) {
        return findPropOrFail("THEME_COLOR_" + ty + "_FG") + ", " + findPropOrFail("THEME_COLOR_" + ty + "_BG") +
                ", " + findPropOrFail("THEME_COLOR_" + ty + "_HL") + ", " + findPropOrFail("THEME_COLOR_" + ty + "_EX");
    }


    protected Collection<CreatorProperty> colorThemeEntries() {
        var props = new ArrayList<CreatorProperty>();
        props.add(separatorProperty("COLORS", "Choose Theme Colors"));
        props.addAll(colorPropertiesFor("ITEM", "regular item", new String[] { "GxEPD_BLACK", "GxEPD_WHITE", "GxEPD_BLACK", "GxEPD_BLACK"}));
        props.addAll(colorPropertiesFor("TITLE", "menu title", new String[] { "GxEPD_WHITE", "GxEPD_BLACK", "GxEPD_WHITE", "GxEPD_WHITE"}));
        props.addAll(selectedColorProperties("GxEPD_WHITE", "GxEPD_BLACK"));
        return props;
    }

    protected List<CreatorProperty> selectedColorProperties(String bgCol, String fgCol) {
        return List.of(
                CreatorProperty.rgbProperty("THEME_SELECTED_BG", "Selected background color", "Selected item background color", bgCol),
                CreatorProperty.rgbProperty("THEME_SELECTED_FG", "Selected text color", "Selected item text color", fgCol)
        );
    }

    protected List<CreatorProperty> colorPropertiesFor(String item, String desc, String[] defaultPalette) {
        return List.of(
                CreatorProperty.rgbProperty("THEME_COLOR_" + item + "_FG", item + " text color", "Text color of a " + desc, defaultPalette[0]),
                CreatorProperty.rgbProperty("THEME_COLOR_" + item + "_BG", item + " background color", "Background color of a " + desc, defaultPalette[1]),
                CreatorProperty.rgbProperty("THEME_COLOR_" + item + "_HL", item + " highlight color", "Highlight color of widgets, checkbox, buttons for " + desc, defaultPalette[2]),
                CreatorProperty.rgbProperty("THEME_COLOR_" + item + "_EX", item + " extra color", "Extra color of items for borders, buttons etc for " + desc, defaultPalette[3])
        );
    }

    protected List<HeaderDefinition> generatedHeaders(String hdr) {
        return List.of(
                new HeaderDefinition("tcUnicodeHelper.h", HeaderDefinition.HeaderType.GLOBAL, PRIORITY_NORMAL, new EqualityApplicability("USE_TC_UNICODE", "true", false)),
                new HeaderDefinition("${THEME_ITEM_FONT}", HeaderDefinition.HeaderType.FONT, PRIORITY_NORMAL, ALWAYS_APPLICABLE),
                new HeaderDefinition("${THEME_TITLE_FONT}", HeaderDefinition.HeaderType.FONT, PRIORITY_NORMAL, ALWAYS_APPLICABLE),
                new HeaderDefinition("${SRC_DIR_OFFSET}" + hdr + ".h", HeaderDefinition.HeaderType.CPP_SRC_FILE, PRIORITY_MIN, ALWAYS_APPLICABLE)
        );
    }

    protected CreatorProperty editIconChoice() {
        return new CreatorProperty("THEME_EDIT_ICONS", "Edit and active icon choice", "Choose which edit icons to use or turn them off",
                "LO_RES", SubSystem.THEME, CreatorProperty.PropType.VARIABLE, CannedPropertyValidators.choicesValidator(List.of(
                new ChoiceDescription("NONE", "No Edit Icons"),
                new ChoiceDescription("LO_RES", "Low resolution edit icons"),
                new ChoiceDescription("MED_RES", "Medium resolution edit icons")
        ), "MED_RES"), ALWAYS_APPLICABLE);
    }


    protected String buildThemeFile() {
        return """
                #ifndef TCMENU_THEME_BLOCK
                #define TCMENU_THEME_BLOCK
                
                #include <graphics/TcThemeBuilder.h>
                
                __FONT_EXPORT_STATEMENTS__
                
                color_t defaultItemPalette[] = { __ITEM_PALETTE__ };
                color_t defaultActionPalette[] = { __ACTION_PALETTE__ };
                color_t defaultTitlePalette[] = { __TITLE_PALETTE__ };
                
                /**
                 * This is one of the stock themes, you can modify it to meet your requirements, and it will not be updated by tcMenu
                 * Designer unless you delete it. This sets up the fonts, spacing and padding for all items.
                 * @param gr the graphical renderer
                 */
                void applyTheme(GraphicsDeviceRenderer& gr) {
                
                    // See https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/themes/rendering-with-themes-icons-grids/
                    TcThemeBuilder themeBuilder(gr);
                    themeBuilder.withSelectedColors(__SELECTED_BG__, __SELECTED_FG__)
                            .dimensionsFromRenderer()
                            .withItemPadding(MenuPadding(__ITEM_PADDING__))
                            .withRenderingSettings(BaseGraphicalRenderer::__TITLE_MODE__, __USE_SLIDERS__)
                            .withPalette(defaultItemPalette)
                            .__ITEM_FONT_DECL__
                            .withSpacing(1)__UNICODE_AND_ICONS__
                
                    themeBuilder.defaultTitleProperties()
                            .__TITLE_FONT_DECL__
                            .withPalette(defaultTitlePalette)
                            .withPadding(MenuPadding(__TITLE_PADDING__))
                            .withJustification(__TITLE_JUSTIFICATION__)
                            .withSpacing(__TITLE_SPACING__)__MENU_BORDER_CODE_TITLE__
                            .apply();
                
                    themeBuilder.defaultActionProperties()
                            .withJustification(__ACTION_JUSTIFICATION__)
                            .withPadding(MenuPadding(__ACTION_PADDING__))
                            .withPalette(defaultActionPalette)__MENU_BORDER_CODE_ACTION__
                            .apply();
                
                    themeBuilder.defaultItemProperties()
                            .withJustification(__ITEM_JUSTIFICATION__)
                            .apply();
                
                    themeBuilder.apply();
                }
                
                #endif //TCMENU_THEME_BLOCK
                
                """;
    }
}
