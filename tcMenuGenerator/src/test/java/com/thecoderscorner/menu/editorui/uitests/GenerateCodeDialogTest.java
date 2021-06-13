package com.thecoderscorner.menu.editorui.uitests;

import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.plugin.*;
import com.thecoderscorner.menu.editorui.generator.ui.CodeGeneratorRunner;
import com.thecoderscorner.menu.editorui.generator.ui.GenerateCodeDialog;
import com.thecoderscorner.menu.editorui.generator.validation.*;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.project.FileBasedProjectPersistor;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import javafx.application.Platform;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static com.thecoderscorner.menu.editorui.generator.parameters.FontDefinition.fromString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;

@ExtendWith(ApplicationExtension.class)
public class GenerateCodeDialogTest {
    public static final String UNITTEST_DEFAULT_DISPLAY_UUID = "20409bb8-b8a1-4d1d-b632-2cf9b5739888";
    public static final String UNITTEST_DEFAULT_INPUT_UUID = "20409bb8-b8a1-4d1d-b632-2cf9b57353e3";
    public static final String UNITTEST_DEFAULT_REMOTE_UUID = "850b889b-fb15-4d9b-a589-67d5ffe3488d";
    private GenerateCodeDialog genDialog;
    private Stage stage;
    private CodeGeneratorRunner generatorRunner;
    private CurrentEditorProject project;
    private CodePluginManager pluginManager;
    private Path pluginTemp;
    private CurrentProjectEditorUI editorUI;

    @Start
    public void onStart(Stage stage) throws Exception {
        this.stage = stage;
        var embeddedPlatforms = new PluginEmbeddedPlatformsImpl();

        pluginTemp = Files.createTempDirectory("gennyTest");
        DefaultXmlPluginLoaderTest.makeStandardPluginInPath(pluginTemp, true);
        var storage = mock(ConfigurationStorage.class);
        when(storage.getVersion()).thenReturn("2.2.0");
        when(storage.getAdditionalPluginPaths()).thenReturn(Collections.singletonList(pluginTemp.toString()));
        pluginManager = new DefaultXmlPluginLoader(embeddedPlatforms, storage, false);
        pluginManager.reload();

        generatorRunner = mock(CodeGeneratorRunner.class);
        editorUI = mock(CurrentProjectEditorUI.class);

        createTheProject();

        genDialog = new GenerateCodeDialog(pluginManager, editorUI, project, generatorRunner, embeddedPlatforms);
        genDialog.showCodeGenerator(stage, false);
    }

    private void createTheProject() throws IOException {
        var prjDir = pluginTemp.resolve("myProject");
        Files.createDirectory(prjDir);
        var projectFile = prjDir.resolve("myProject.emf");
        var prj = Objects.requireNonNull(GenerateCodeDialogTest.class.getResourceAsStream("/cannedProject/unitTestProject.emf")).readAllBytes();
        Files.write(projectFile, prj);
        project = new CurrentEditorProject(editorUI, new FileBasedProjectPersistor());
        project.openProject(projectFile.toString());
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.walk(pluginTemp)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);

        Platform.runLater(()-> stage.close());
    }

    @Test
    public void testCodeGeneratorProperties(FxRobot robot) throws Exception{
        verifyThat("#appUuidField", TextInputControlMatchers.hasText("52c779d0-0fb9-49d4-94fe-61b2bc6f9164"));
        verifyThat("#appNameField", TextInputControlMatchers.hasText("Generator integration test"));
        verifyThat("#recursiveNaming", CheckBox::isSelected);
        verifyThat("#saveToSrc", Predicate.not(CheckBox::isSelected));
        verifyThat("#useCppMain", Predicate.not(CheckBox::isSelected));
        verifyThat("#platformCombo", (ComboBox<EmbeddedPlatform> cbx) -> cbx.getSelectionModel().getSelectedItem() == EmbeddedPlatform.ARDUINO_AVR);

        robot.clickOn("#saveToSrc");
        robot.clickOn("#useCppMain");

        robot.clickOn("#appUuidButton");
        verify(editorUI).questionYesNo(eq("Really change the UUID?"), any());

        var inputPlugin = pluginManager.getPluginById(UNITTEST_DEFAULT_INPUT_UUID).orElseThrow();
        var displayPlugin = pluginManager.getPluginById(UNITTEST_DEFAULT_DISPLAY_UUID).orElseThrow();
        var remotePlugin = pluginManager.getPluginById(UNITTEST_DEFAULT_REMOTE_UUID).orElseThrow();

        assertExpectedPlugin(robot, inputPlugin, "inputPlugin");
        assertExpectedPlugin(robot, displayPlugin, "displayPlugin");

        //
        // Different input devices being selected (such as trackpad) changes the direction meaning. If
        // the generator doesn't scroll to the remote during testing, change the direction between DOWN to UP
        // on the line below.
        //
        robot.scroll(100, VerticalDirection.DOWN);
        assertExpectedPlugin(robot, remotePlugin, "remotePlugin0");

        assertTrue(robot.lookup("#themePlugin").tryQuery().isEmpty());

        robot.clickOn("#generateButton");

        // the list must be in exactly this order, DISPLAY, INPUT, REMOTE, THEME
        var expectedPlugins = List.of(displayPlugin, inputPlugin, remotePlugin);

        var previousPluinFiles = List.of(
                "src/source.h",
                "src/source.cpp",
                "src/extra.cpp",
                "src/MySpecialTransport.h"
        );

        verify(generatorRunner).startCodeGeneration(
                eq(stage), eq(EmbeddedPlatform.ARDUINO_AVR), eq(pluginTemp.resolve("myProject").toString()),
                eq(expectedPlugins), eq(previousPluinFiles), eq(true));
    }

    void assertExpectedPlugin(FxRobot robot, CodePluginItem item, String id) throws Exception {
        FxAssert.verifyThat("#" + id + "Title", LabeledMatchers.hasText(item.getDescription()));
        FxAssert.verifyThat("#" + id + "Description", LabeledMatchers.hasText(item.getExtendedDescription()));
        String moduleName = item.getConfig().getName() + " - " + item.getConfig().getVersion();
        FxAssert.verifyThat("#" + id + "WhichPlugin", LabeledMatchers.hasText(moduleName));
        FxAssert.verifyThat("#" + id + "Docs", LabeledMatchers.hasText("Click for documentation"));

        for (var prop: item.getProperties()) {
            String nodeName = "#" + id + prop.getName();
            if(prop.getValidationRules() instanceof BooleanPropertyValidationRules) {
                checkBooleanPropertyEditing(robot, prop, nodeName);
            }
            else if(prop.getValidationRules() instanceof IntegerPropertyValidationRules intVal) {
                checkIntegerPropertyEditing(robot, id, prop, nodeName, intVal);
            }
            else if(prop.getValidationRules() instanceof StringPropertyValidationRules) {
                checkStringPropertyEditing(robot, id, prop, nodeName);
            }
            else if(prop.getValidationRules() instanceof PinPropertyValidationRules) {
                checkPinPropertyEditing(robot, id, prop, nodeName);
            }
            else if(prop.getValidationRules() instanceof ChoicesPropertyValidationRules choiceVal) {
                checkChoicePropertyEditing(robot, prop, nodeName, choiceVal);
            }
            else if(prop.getValidationRules() instanceof FontPropertyValidationRules) {
                checkFontPropertyEditing(robot, prop, nodeName);
            }
        }
    }

    private void checkFontPropertyEditing(FxRobot robot, CreatorProperty prop, String nodeName) {
        // try the default font x2
        var dialogPane = compareFontDialogToProperty(robot, nodeName, prop);
        robot.clickOn("#defaultFontSelect");
        TestUtils.writeIntoField(robot, "#fontVarField", "", 10);
        TestUtils.writeIntoField(robot, "#fontNumField", 2, 4);
        TestUtils.clickOnButtonInDialog(robot, dialogPane,"Set Font");

        // try the numbered x2
        dialogPane = compareFontDialogToProperty(robot, nodeName, prop);
        robot.clickOn("#largeNumSelect");
        TestUtils.writeIntoField(robot, "#fontNumField", 9, 4);
        TestUtils.writeIntoField(robot, "#fontVarField", "", 10);
        TestUtils.clickOnButtonInDialog(robot, dialogPane,"Set Font");

        // try ada font x1
        dialogPane = compareFontDialogToProperty(robot, nodeName, prop);
        robot.clickOn("#adafruitFontSel");
        TestUtils.writeIntoField(robot, "#fontNumField", 2, 4);
        TestUtils.writeIntoField(robot, "#fontVarField", "myFont", 10);
        TestUtils.clickOnButtonInDialog(robot, dialogPane,"Set Font");

        robot.clickOn(nodeName + "_btn");
        dialogPane = compareFontDialogToProperty(robot, nodeName, prop);
        TestUtils.clickOnButtonInDialog(robot, dialogPane, "Cancel");
    }

    private Node compareFontDialogToProperty(FxRobot robot, String nodeName, CreatorProperty prop) {
        String latestValue = fromString(prop.getLatestValue()).orElseThrow().getNicePrintableName();
        FxAssert.verifyThat(nodeName, TextInputControlMatchers.hasText(latestValue));

        robot.clickOn(nodeName + "_btn");
        var def = fromString(prop.getLatestValue()).orElseThrow();
        var radioToCheck = switch(def.getFontMode()) {
            case DEFAULT_FONT -> "#defaultFontSelect";
            case ADAFRUIT -> "#adafruitFontSel";
            case ADAFRUIT_LOCAL -> "#adafruitLocalFontSel";
            case AVAILABLE -> "#staticFontSel";
            case NUMBERED -> "#largeNumSelect";
        };
        FxAssert.verifyThat("#fontNumField", TextInputControlMatchers.hasText(String.valueOf(def.getFontNumber())));
        FxAssert.verifyThat("#fontVarField", TextInputControlMatchers.hasText(def.getFontName()));
        FxAssert.verifyThat(radioToCheck, RadioButton::isSelected);

        return robot.lookup(".fontDialog").query();

    }

    private void checkChoicePropertyEditing(FxRobot robot, CreatorProperty prop, String nodeName, ChoicesPropertyValidationRules choiceVal) throws InterruptedException {
        for(var choice : choiceVal.choices()) {
            assertTrue(TestUtils.selectItemInCombo(robot, nodeName, (ChoiceDescription cd) ->
                    cd.getChoiceValue().equals(choice.getChoiceValue())
            ));
            assertEquals(choice.getChoiceValue(), prop.getLatestValue());
        }
    }

    private void checkPinPropertyEditing(FxRobot robot, String id, CreatorProperty prop, String nodeName) {
        FxAssert.verifyThat(nodeName, TextInputControlMatchers.hasText(prop.getLatestValue()));
        var originalValue = prop.getLatestValue();
        writeIntoTextFieldAndVerify(robot, prop, id, "abc123");
        writeIntoTextFieldAndVerify(robot, prop, id, "A0");
        writeIntoTextFieldAndVerify(robot, prop, id, 23);
        writeIntoTextFieldAndVerify(robot, prop, id, originalValue);
    }

    private void checkStringPropertyEditing(FxRobot robot, String id, CreatorProperty prop, String nodeName) {
        FxAssert.verifyThat(nodeName, TextInputControlMatchers.hasText(prop.getLatestValue()));
        var originalValue = prop.getLatestValue();
        writeIntoTextFieldAndVerify(robot, prop, id, "abc123");
        writeIntoTextFieldAndVerify(robot, prop, id, originalValue);
    }

    private void checkIntegerPropertyEditing(FxRobot robot, String id, CreatorProperty prop, String nodeName, IntegerPropertyValidationRules intVal) {
        FxAssert.verifyThat(nodeName, TextInputControlMatchers.hasText(prop.getLatestValue()));
        var originalValue = prop.getLatestValue();
        writeIntoTextFieldAndVerify(robot, prop, id, intVal.getMaxVal() - 1);
        writeIntoTextFieldAndVerify(robot, prop, id, originalValue);
    }

    private void checkBooleanPropertyEditing(FxRobot robot, CreatorProperty prop, String nodeName) {
        FxAssert.verifyThat(nodeName, LabeledMatchers.hasText(prop.getDescription()));
        boolean originalLatest = Boolean.parseBoolean(prop.getLatestValue());
        FxAssert.verifyThat(nodeName, (CheckBox cbx) -> cbx.isSelected() == originalLatest);
        robot.clickOn(nodeName);
        assertNotEquals(originalLatest, Boolean.parseBoolean(prop.getLatestValue()));
        robot.clickOn(nodeName);
        assertEquals(originalLatest, Boolean.parseBoolean(prop.getLatestValue()));
    }

    void writeIntoTextFieldAndVerify(FxRobot robot, CreatorProperty property, String id, Object value) {
        String nodeName = "#" + id + property.getName();
        robot.clickOn(nodeName);
        TestUtils.writeIntoField(robot, nodeName, value, 5);
        robot.clickOn("#" + id + "Title");
        assertEquals(value.toString(), property.getLatestValue());
    }
}
