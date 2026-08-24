package com.thecoderscorner.menu.editorui.generator.plugin.input;

import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeParameter;
import com.thecoderscorner.menu.editorui.generator.plugin.*;

import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.core.SubSystem.INPUT;

public class BspStmTouchControllerPlugin extends BaseJavaPluginItem {
    private final List<CreatorProperty> requiredProperties;
    private final CodePluginItem pluginItem;

    public BspStmTouchControllerPlugin(JavaPluginGroup group, CodePluginManager manager) {
        super(INPUT, "/plugin/input/resistive-touch.jpg");
        requiredProperties = List.of(
                CreatorProperty.variableProperty("TOUCH_HEADER_BSP", "BSP header file containing TS (without .h)", "The BSP header file containing the touch screen BSP functions", INPUT, "stm32f429i_discovery_ts"),
                CreatorProperty.uintProperty("TOUCH_SCREEN_WIDTH", "Width of the screen", "Width of the screen in pixels", INPUT, 240, 9999),
                CreatorProperty.uintProperty("TOUCH_SCREEN_HEIGHT", "Height of the screen", "Height of the screen in pixels", INPUT, 320, 9999)
        );
        var codePlugin = new CodePluginItem();
        codePlugin.setId("62d822f4-3261-4c13-867d-573485380ca1");
        codePlugin.setDescription("StmCube BSP touch controller interface (STM32F429 DISC1)");
        codePlugin.setConfig(group.getConfig());
        codePlugin.setExtendedDescription("Use touch control of the menu using the inbuilt STM BSP touch controller.");
        codePlugin.setDocsLink("https://tcmenu.github.io/documentation/arduino-libraries/tc-menu/tcmenu-plugins/resistive-touch-screen-plugin/");
        codePlugin.setJavaImpl(this);
        codePlugin.setManager(manager);
        codePlugin.setProperties(requiredProperties);
        codePlugin.setSubsystem(INPUT);
        codePlugin.setSupportedPlatforms(PluginEmbeddedPlatformsImpl.allPlatforms);
        pluginItem = codePlugin;

    }

    @Override
    public CodePluginItem getPlugin() {
        return pluginItem;
    }

    @Override
    public List<CreatorProperty> getRequiredProperties() {
        return requiredProperties;
    }

    @Override
    public List<FunctionDefinition> getFunctions() {
        return List.of(
                FunctionDefinition.ofRegCpp("init","touchInterrogator", List.of()),
                FunctionDefinition.ofRegCpp("initWithoutInput","menuMgr", List.of(
                        CodeParameter.unNamedValue("&renderer"),
                        CodeParameter.unNamedValue("&${ROOT}")
                )),
                FunctionDefinition.ofRegCpp("start","touchScreenManager", List.of())
        );
    }

    @Override
    public List<HeaderDefinition> getHeaderDefinitions() {
        return List.of(
                new HeaderDefinition("stmCubeTouchBsp.h", HeaderDefinition.HeaderType.SOURCE,  HeaderDefinition.PRIORITY_NORMAL, ALWAYS_APPLICABLE),
                new HeaderDefinition("extras/DrawableTouchCalibrator.h", HeaderDefinition.HeaderType.GLOBAL,  HeaderDefinition.PRIORITY_NORMAL, ALWAYS_APPLICABLE)
        );
    }

    @Override
    public List<RequiredSourceFile> getRequiredSourceFiles() {
        List<CodeReplacement> replacements = List.of(
                new CodeReplacement("__HEADER_BSP__", findPropOrFail("TOUCH_HEADER_BSP"), ALWAYS_APPLICABLE)
        );
        return List.of(
                new RequiredSourceFile("stmCubeTouchBsp.cpp", TOUCH_STM_BSP_CPP, replacements, false),
                new RequiredSourceFile("stmCubeTouchBsp.h", TOUCH_STM_BSP_H, replacements, false)
        );
    }

    @Override
    public List<CodeVariable> getVariables() {
        return List.of(
                CodeVariable.globalExported("touchInterrogator", "StBspTouchInterrogator", VariableDefinitionMode.VARIABLE_AND_EXPORT, List.of(
                        CodeParameter.unNamedValue(findPropOrFail("TOUCH_SCREEN_WIDTH")),
                        CodeParameter.unNamedValue(findPropOrFail("TOUCH_SCREEN_HEIGHT"))
                )),
                //iotouch::TouchOrientationSettings touchSettings(false, false, false);
                CodeVariable.globalExported("touchSettings", "iotouch::TouchOrientationSettings", VariableDefinitionMode.VARIABLE_AND_EXPORT, List.of(
                        CodeParameter.unNamedValue("false"),
                        CodeParameter.unNamedValue("false"),
                        CodeParameter.unNamedValue("false")
                )),
                //MenuTouchScreenManager touchScreenManager(&touchInterrogator, &renderer, touchSettings);
                CodeVariable.globalExported("touchScreenManager", "MenuTouchScreenManager", VariableDefinitionMode.VARIABLE_AND_EXPORT, List.of(
                        CodeParameter.unNamedValue("&touchInterrogator"),
                        CodeParameter.unNamedValue("&renderer"),
                        CodeParameter.unNamedValue("touchSettings")
                )),
                //tcextras::IoaTouchScreenCalibrator touchCal(&touchScreenManager, &renderer, 256);
                CodeVariable.globalExported("touchCal", "tcextras::IoaTouchScreenCalibrator", VariableDefinitionMode.VARIABLE_AND_EXPORT, List.of(
                        CodeParameter.unNamedValue("&touchScreenManager"),
                        CodeParameter.unNamedValue("&renderer"),
                        CodeParameter.unNamedValue("256")
                ))
        );
    }

    private static final String TOUCH_STM_BSP_H = """
            #ifndef STM_CUBE_TOUCH_BSP_H
            #define STM_CUBE_TOUCH_BSP_H
            #include "ResistiveTouchScreen.h"
            
            /**
             * A touch screen interface for tcMenu mapping in the BSP STM32Cube HAL library functions.
             */
            class StBspTouchInterrogator : public iotouch::TouchInterrogator {
            private:
                int width, height;
            public:
                StBspTouchInterrogator(int wid, int hei);
                void init();
                ~StBspTouchInterrogator() override = default;
                iotouch::TouchState internalProcessTouch(float *ptrX, float *ptrY, const iotouch::TouchOrientationSettings& rotation,
                                                         const iotouch::CalibrationHandler& calib) override;
            };
            
            #endif
            
            """;
    private static final String TOUCH_STM_BSP_CPP = """
            
            #include "stmCubeTouchBsp.h"
            #include "__HEADER_BSP__.h"
            
            iotouch::TouchState StBspTouchInterrogator::internalProcessTouch(float *ptrX, float *ptrY, const iotouch::TouchOrientationSettings& rotation,
                                                                             const iotouch::CalibrationHandler& calibrationHandler) {
                TS_StateTypeDef tsState;
                BSP_TS_GetState(&tsState);
                if(!tsState.TouchDetected) return iotouch::NOT_TOUCHED;
            
                *ptrX = calibrationHandler.calibrateX(static_cast<float>(tsState.X) / static_cast<float>(width), rotation.isXInverted());
                *ptrY = calibrationHandler.calibrateY(static_cast<float>(height - tsState.Y) / static_cast<float>(height), rotation.isYInverted());
                return iotouch::TOUCHED;
            }
            
            StBspTouchInterrogator::StBspTouchInterrogator(const int wid, const int hei) {
                width = wid;
                height = hei;
            }
            
            void StBspTouchInterrogator::init() {
                BSP_TS_Init(width, height);
            }
            
            """;

}
