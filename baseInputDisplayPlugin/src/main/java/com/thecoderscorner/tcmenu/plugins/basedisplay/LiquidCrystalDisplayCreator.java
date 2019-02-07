package com.thecoderscorner.tcmenu.plugins.basedisplay;

import com.thecoderscorner.menu.pluginapi.AbstractCodeCreator;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.model.CodeVariableBuilder;
import com.thecoderscorner.menu.pluginapi.model.FunctionCallBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.pluginapi.SubSystem.DISPLAY;

public class LiquidCrystalDisplayCreator extends AbstractCodeCreator {

    private List<CreatorProperty> creatorProperties = new ArrayList<>(Arrays.asList(
            new CreatorProperty("LCD_RS", "RS connection to display", "1", DISPLAY),
            new CreatorProperty("LCD_EN", "EN connection to display", "2", DISPLAY),
            new CreatorProperty("LCD_D4", "D4 connection to display", "4", DISPLAY),
            new CreatorProperty("LCD_D5", "D5 connection to display", "5", DISPLAY),
            new CreatorProperty("LCD_D6", "D6 connection to display", "6", DISPLAY),
            new CreatorProperty("LCD_D7", "D7 connection to display", "7", DISPLAY),
            new CreatorProperty("LCD_WIDTH", "Number of chars across", "20", DISPLAY),
            new CreatorProperty("LCD_HEIGHT", "Number of chars down", "4", DISPLAY),
            new CreatorProperty("LCD_BACKLIGHT", "Controls the backlight (-1 no backlight)", "-1", DISPLAY),
            new CreatorProperty("LCD_PWM_PIN", "Advanced: PWM control contrast (-1 off)", "-1", DISPLAY),
            new CreatorProperty("IO_DEVICE", "Advanced: IoDevice to use (default blank)", "", DISPLAY, TEXTUAL))
    );

    public LiquidCrystalDisplayCreator() {
        addVariable(new CodeVariableBuilder().variableName("lcd").variableType("LiquidCrystal")
                               .requiresHeader("LiquidCrystalIO.h", false)
                               .exportNeeded().param("LCD_RS").param("LCD_EN")
                               .param("LCD_D4").param("LCD_D5").param("LCD_D6").param("LCD_D7")
                               .paramFromPropertyWithDefault("IO_DEVICE", "ioUsingArduino()"));

        addVariable(new CodeVariableBuilder().variableName("renderer").variableType("LiquidCrystalRenderer")
                                .requiresHeader("LiquidCrystalIO.h", false)
                                .exportNeeded().param("&lcd").param(16).param(2));

        addFunctionCall(new FunctionCallBuilder().functionName("begin").objectName("lcd")
                                .param("LCD_WIDTH").param("LCD_HEIGHT"));

        addLibraryFiles("renderers/liquidcrystal/tcMenuLiquidCrystal.cpp",
                        "renderers/liquidcrystal/tcMenuLiquidCrystal.h");
    }

    @Override
    public String getSetupCode(String rootItem) {
        if(findPropertyValueAsIntWithDefault("LCD_BACKLIGHT", -1) != -1) {
            addFunctionCall(new FunctionCallBuilder().functionName("configureBacklightPin").objectName("lcd")
                           .param("LCD_BACKLIGHT"));
            addFunctionCall(new FunctionCallBuilder().functionName("backlight").objectName("lcd"));
        }

        if(findPropertyValueAsIntWithDefault("LCD_PWM_PIN", -1) != -1) {
            addFunctionCall(new FunctionCallBuilder().functionName("pinMode").param("LCD_PWM_PIN").param("OUTPUT"));
            addFunctionCall(new FunctionCallBuilder().functionName("analogWrite").param("LCD_PWM_PIN").param(10));
        }

        return super.getSetupCode(rootItem);
    }

    @Override
    public List<CreatorProperty> properties() {
        return creatorProperties;
    }
}
