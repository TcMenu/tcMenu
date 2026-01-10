package com.thecoderscorner.menu.editorui.generator.plugin.theme;

import com.thecoderscorner.menu.editorui.generator.core.CodeConversionContext;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.parameters.FontDefinition;
import com.thecoderscorner.menu.editorui.generator.plugin.BaseJavaPluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.CodeReplacement;
import com.thecoderscorner.menu.editorui.generator.validation.CannedPropertyValidators;
import com.thecoderscorner.menu.editorui.generator.validation.ChoiceDescription;
import com.thecoderscorner.menu.editorui.util.StringHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.parameters.FontDefinition.FontMode.ADAFRUIT;
import static com.thecoderscorner.menu.editorui.generator.parameters.FontDefinition.FontMode.ADAFRUIT_LOCAL;

public abstract class BaseJavaThemePluginItem extends BaseJavaPluginItem {
    public final static FontDefinition defaultForTcUnicode = new FontDefinition(ADAFRUIT, "OpenSansRegular7pt", 0);

    public String unicodeAndIconsCode(boolean iconsOn) {
        var unicode = findPropOrFail("USE_TC_UNICODE").equals("true");
        var str = "";
        if(iconsOn) {
            str += "themeBuilder.withStandardLowResCursorIcons();" + System.lineSeparator();
        }

        if(unicode) {
            str += "themeBuilder.enableTcUnicode();" + System.lineSeparator();
        }
        return str;
    }


    public CreatorProperty fontProperty(String id, String name, String desc, String defValue) {
        return new CreatorProperty(id, name, desc, defValue, SubSystem.THEME, CreatorProperty.PropType.VARIABLE,
                CannedPropertyValidators.fontValidator(), ALWAYS_APPLICABLE);
    }

    public Collection<CreatorProperty> defFontProperties() {
        return List.of(
                CreatorProperty.separatorTheme("FONT", "Font Settings"),
                fontProperty("THEME_ITEM_FONT", "Font for menu items", "The default Font that menu items draw with", "def:,1"),
                fontProperty("THEME_TITLE_FONT", "Font for titles", "The Font that will be used to draw titles", "def:,1")
        );

    }

    public String fontDeclarationCode(String name) {
        var prop = getRequiredProperties().stream().filter(p -> p.getName().equals(name))
                .findFirst().orElseThrow();
        var fdOpt = FontDefinition.fromString(prop.getLatestValue());
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

    public Collection<CreatorProperty> defDrawingProperties() {
        return List.of(
                CreatorProperty.separatorTheme("SPACING", "Spacing and Drawing Options"),
                CreatorProperty.uintProperty("TITLE_PADDING", "Padding around the title", "Padding that is applied around all sides of title", SubSystem.THEME, 2, 10),
                CreatorProperty.uintProperty("ITEM_PADDING", "Padding around each item", "Padding that is applied around all sides of title", SubSystem.THEME, 2, 10),
                CreatorProperty.uintProperty("TITLE_TO_ITEM_SPACING", "Title to first item gap", "Space to be left between title and first item", SubSystem.THEME, 2, 10),
                new CreatorProperty("TITLE_SHOW_MODE", "How to present the title", "Choose how the title will be displayed", "TITLE_ALWAYS", SubSystem.THEME, CreatorProperty.PropType.VARIABLE, CannedPropertyValidators.choicesValidator(List.of(
                        new ChoiceDescription("NO_TITLE", "No title"),
                        new ChoiceDescription("TITLE_FIRST_ROW", "Title on first row (scrolls with menu)"),
                        new ChoiceDescription("TITLE_ALWAYS", "Title always at top")
                        ), "TITLE_ALWAYS"),
                        ALWAYS_APPLICABLE),
                new CreatorProperty("USE_TC_UNICODE", "Use TcUnicode/UTF-8 for text", "TcUnicode features UTF-8 over a wide range of fonts with font editor", "false",
                        SubSystem.THEME, CreatorProperty.PropType.VARIABLE, CannedPropertyValidators.boolValidator(), ALWAYS_APPLICABLE)
        );
    }

    public List<CodeReplacement> replacementsWithExtras(CodeReplacement... codeReplacement) {
        List<CodeReplacement> defaultReplacements = List.of(
                new CodeReplacement("__ITEM_PADDING__", findPropOrFail("ITEM_PADDING"), ALWAYS_APPLICABLE),
                new CodeReplacement("__TITLE_PADDING__", findPropOrFail("TITLE_PADDING"), ALWAYS_APPLICABLE),
                new CodeReplacement("__TITLE_SPACING__", findPropOrFail("TITLE_TO_ITEM_SPACING"), ALWAYS_APPLICABLE),
                new CodeReplacement("__TITLE_MODE__", findPropOrFail("TITLE_SHOW_MODE"), ALWAYS_APPLICABLE),
                new CodeReplacement("__TITLE_FONT_DECL__", fontDeclarationCode("THEME_TITLE_FONT"), ALWAYS_APPLICABLE),
                new CodeReplacement("__ITEM_FONT_DECL__", fontDeclarationCode("THEME_ITEM_FONT"), ALWAYS_APPLICABLE),
                new CodeReplacement("__UNICODE_AND_ICONS__", unicodeAndIconsCode(true), ALWAYS_APPLICABLE)
        );

        var extras = new ArrayList<>(Arrays.asList(codeReplacement));
        extras.addAll(defaultReplacements);
        return List.copyOf(extras);
    }

    @Override
    public void beforeGenerationStarts(CodeConversionContext context) {
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
}
