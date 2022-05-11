package com.thecoderscorner.menu.editorui.uitests;

import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinitionCollection;
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
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.thecoderscorner.menu.editorui.controller.EepromTypeSelectionController.ROM_PAGE_SIZES;
import static com.thecoderscorner.menu.editorui.generator.parameters.FontDefinition.fromString;
import static com.thecoderscorner.menu.editorui.generator.parameters.auth.ReadOnlyAuthenticatorDefinition.FlashRemoteId;
import static com.thecoderscorner.menu.editorui.util.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;

@ExtendWith(ApplicationExtension.class)
public class GenerateCodeDialogTest {
    public static final String UNITTEST_DEFAULT_DISPLAY_UUID = "20409bb8-b8a1-4d1d-b632-2cf9b5739888";
    public static final String UNITTEST_DEFAULT_INPUT_UUID = "20409bb8-b8a1-4d1d-b632-2cf9b57353e3";
    public static final String UNITTEST_DEFAULT_REMOTE_UUID = "850b889b-fb15-4d9b-a589-67d5ffe3488d";
    private static PluginEmbeddedPlatformsImpl embeddedPlatforms;
    private GenerateCodeDialog genDialog;
    private Stage stage;
    private static CodeGeneratorRunner generatorRunner;
    private static CurrentEditorProject project;
    private static CodePluginManager pluginManager;
    private static Path pluginTemp;
    private static CurrentProjectEditorUI editorUI;

    @BeforeAll
    public static void initialiseProjectFiles() throws IOException {
        embeddedPlatforms = new PluginEmbeddedPlatformsImpl();

        pluginTemp = Files.createTempDirectory("gennyTest");
        DefaultXmlPluginLoaderTest.makeStandardPluginInPath(pluginTemp, true);
        var storage = mock(ConfigurationStorage.class);
        when(storage.getVersion()).thenReturn("2.2.0");
        when(storage.getAdditionalPluginPaths()).thenReturn(Collections.singletonList(pluginTemp.toString()));
        pluginManager = new DefaultXmlPluginLoader(embeddedPlatforms, storage, false);
        pluginManager.reload();

        generatorRunner = mock(CodeGeneratorRunner.class);
        editorUI = mock(CurrentProjectEditorUI.class);
        var prjDir = pluginTemp.resolve("myProject");
        project = createTheProject(Files.createDirectory(prjDir), editorUI);
        when(editorUI.getCurrentProject()).thenReturn(project);
    }

    @Start
    public void onStart(Stage stage) {
        this.stage = stage;

        assertEquals(1, pluginManager.getLoadedPlugins().size());

        genDialog = new GenerateCodeDialog(pluginManager, editorUI, project, generatorRunner, embeddedPlatforms);
        genDialog.showCodeGenerator(stage, false);
    }

    public static CurrentEditorProject createTheProject(Path prjDir, CurrentProjectEditorUI editorUI) throws IOException {
        var projectFile = prjDir.resolve("myProject.emf");
        var prj = Objects.requireNonNull(GenerateCodeDialogTest.class.getResourceAsStream("/cannedProject/unitTestProject.emf")).readAllBytes();
        Files.write(projectFile, prj);
        var project = new CurrentEditorProject(editorUI, new FileBasedProjectPersistor(), mock(ConfigurationStorage.class));
        project.openProject(projectFile.toString());
        return project;
    }

    @AfterEach
    public void closeDialog() {
        Platform.runLater(() -> stage.close());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @AfterAll
    public static void tearDownProjectFiles() throws IOException {
        Files.walk(pluginTemp)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    public void testCodeGeneratorProperties(FxRobot robot) throws Exception {
        verifyThat("#appNameLabel", LabeledMatchers.hasText("Generator integration test - 52c779d0-0fb9-49d4-94fe-61b2bc6f9164"));
        verifyThat("#platformCombo", (ComboBox<EmbeddedPlatform> cbx) -> cbx.getSelectionModel().getSelectedItem() == EmbeddedPlatform.ARDUINO_AVR);

        var inputPlugin = getPlugin(UNITTEST_DEFAULT_INPUT_UUID, SubSystem.INPUT);
        var displayPlugin = pluginManager.getPluginById(UNITTEST_DEFAULT_DISPLAY_UUID).orElseThrow();
        var remotePlugin = pluginManager.getPluginById(UNITTEST_DEFAULT_REMOTE_UUID).orElseThrow();

        assertExpectedPlugin(robot, inputPlugin, "inputPlugin");
        assertExpectedPlugin(robot, displayPlugin, "displayPlugin");

        //
        // Different input devices being selected (such as trackpad) changes the direction meaning. If
        // the generator doesn't scroll to the remote during testing, change the direction between DOWN to UP
        // on the line below.
        //
        var dir = VerticalDirection.DOWN;

        robot.scroll(100, dir);
        assertExpectedPlugin(robot, remotePlugin, "remotePlugin0");

        assertTrue(robot.lookup("#themePlugin").tryQuery().isEmpty());

        robot.clickOn("#generateButton");

        // the list must be in exactly this order, DISPLAY, INPUT, REMOTE, THEME
        var expectedPlugins = List.of(displayPlugin, inputPlugin, remotePlugin);

        var previousPluinFiles = List.of(
                "20409bb8-b8a1-4d1d-b632-2cf9b57353e3",
                "20409bb8-b8a1-4d1d-b632-2cf9b5739888",
                "0BB96E3C-192F-4A29-97E1-A5004E9816F7",
                "850b889b-fb15-4d9b-a589-67d5ffe3488d"
        );

        verify(generatorRunner).startCodeGeneration(
                eq(stage), eq(EmbeddedPlatform.ARDUINO_AVR), eq(pluginTemp.resolve("myProject").toString()),
                eq(expectedPlugins), eq(previousPluinFiles), eq(true));
    }

    private CodePluginItem getPlugin(String id, SubSystem ty) {
        return genDialog.getAllPluginsForConversion().stream()
                .filter(pl -> pl.getId().equals(id) && pl.getSubsystem().equals(ty))
                .findFirst().orElseThrow();
    }

    @Test
    public void checkAuthenticationEditing(FxRobot robot)  throws Exception {
        verifyThat("#authModeLabel", LabeledMatchers.hasText("EEPROM Authenticator, offset=100"));
        robot.clickOn("#authModeButton");

        verifyThat("#eepromAuthRadio", RadioButton::isSelected);
        verifyThat("#eepromStartField", TextInputControlMatchers.hasText("100"));
        verifyThat("#eepromNumRemotes", TextInputControlMatchers.hasText("6"));
        writeIntoField(robot, "#eepromStartField", 250, 10);
        robot.clickOn("#okButton");
        withRetryOnFxThread(new TextFieldPredicate(robot, "#authModeLabel", "EEPROM Authenticator, offset=250"));
        robot.clickOn("#authModeButton");

        robot.clickOn("#flashAuthRadio");
        verifyThat("#okButton", Node::isDisabled);
        writeIntoField(robot, "#pinFlashField", "1234", 5);

        robot.clickOn("#addButton");
        var generatedUuid = UUID.randomUUID().toString();
        verifyThat("#addRemoteButton", Node::isDisabled);
        writeIntoField(robot, "#uuidField", generatedUuid, 1);
        writeIntoField(robot, "#nameField", "unit123", 1);
        robot.clickOn("#addRemoteButton");

        FxAssert.verifyThat("#flashVarList", (ListView<FlashRemoteId> lv) ->
                lv.getItems().size() == 1 && lv.getItems().get(0).name().equals("unit123"));

        robot.clickOn("#okButton");

        withRetryOnFxThread(new TextFieldPredicate(robot, "#authModeLabel", "FLASH Authenticator, remotes=1"));
    }

    @Test
    public void checkEepromEditing(FxRobot robot) throws InterruptedException {
        verifyThat("#eepromTypeLabel", LabeledMatchers.hasText("I2C AT24 addr=0x50, PAGESIZE_AT24C128"));
        robot.clickOn("#eepromTypeButton");
        Thread.sleep(250);

        verifyThat("#at24Radio", RadioButton::isSelected);
        verifyThat("#i2cAddrField", Predicate.not(Node::isDisabled));
        verifyThat("#i2cAddrField", TextInputControlMatchers.hasText("0x50"));
        verifyThat("#romPageCombo", (ComboBox<?> n) -> n.getSelectionModel().getSelectedIndex() == 2);
        verifyThat("#memOffsetField", Node::isDisabled);
        selectItemInCombo(robot, "#romPageCombo", o -> o.equals(ROM_PAGE_SIZES.get(1)));
        robot.clickOn("#okButton");
        Thread.sleep(250);
        verifyThat("#eepromTypeLabel", LabeledMatchers.hasText("I2C AT24 addr=0x50, PAGESIZE_AT24C64"));

        robot.clickOn("#eepromTypeButton");
        Thread.sleep(250);

        robot.clickOn("#avrRomRadio");
        verifyThat("#i2cAddrField", Node::isDisabled);
        verifyThat("#memOffsetField", Node::isDisabled);
        robot.clickOn("#okButton");
        Thread.sleep(250);
        verifyThat("#eepromTypeLabel", LabeledMatchers.hasText("Direct AVR EEPROM functions"));

        robot.clickOn("#eepromTypeButton");
        Thread.sleep(250);

        verifyThat("#avrRomRadio", RadioButton::isSelected);
        robot.clickOn("#eepromRadio");
        robot.clickOn("#okButton");
        Thread.sleep(250);
        verifyThat("#eepromTypeLabel", LabeledMatchers.hasText("Arduino EEPROM class"));

        robot.clickOn("#eepromTypeButton");
        Thread.sleep(250);

        verifyThat("#eepromRadio", RadioButton::isSelected);
        robot.clickOn("#noRomRadio");
        robot.clickOn("#okButton");
        Thread.sleep(250);
        verifyThat("#eepromTypeLabel", LabeledMatchers.hasText("No / Custom EEPROM"));

        robot.clickOn("#eepromTypeButton");
        Thread.sleep(250);

        verifyThat("#noRomRadio", RadioButton::isSelected);
        robot.clickOn("#bspStRadio");
        writeIntoField(robot, "#memOffsetField", 100, 5);
        robot.clickOn("#okButton");
        Thread.sleep(250);
        verifyThat("#eepromTypeLabel", LabeledMatchers.hasText("STM32 BSP offset=100"));
    }

    void assertExpectedPlugin(FxRobot robot, CodePluginItem item, String id) throws Exception {
        FxAssert.verifyThat("#" + id + "Title", LabeledMatchers.hasText(item.getDescription()));
        FxAssert.verifyThat("#" + id + "Description", LabeledMatchers.hasText(item.getExtendedDescription()));
        String moduleName = item.getConfig().getName() + " - " + item.getConfig().getVersion();
        FxAssert.verifyThat("#" + id + "WhichPlugin", LabeledMatchers.hasText(moduleName));
        FxAssert.verifyThat("#" + id + "Docs", LabeledMatchers.hasText("Click for documentation"));

        for (var prop : item.getProperties()) {
            String nodeName = "#" + id + prop.getName();
            if (prop.getValidationRules() instanceof BooleanPropertyValidationRules) {
                checkBooleanPropertyEditing(robot, prop, nodeName);
            } else if (prop.getValidationRules() instanceof IntegerPropertyValidationRules intVal) {
                checkIntegerPropertyEditing(robot, id, prop, nodeName, intVal);
            } else if (prop.getValidationRules() instanceof StringPropertyValidationRules) {
                checkStringPropertyEditing(robot, id, prop, nodeName);
            } else if (prop.getValidationRules() instanceof PinPropertyValidationRules) {
                checkPinPropertyEditing(robot, id, prop, nodeName);
            } else if (prop.getValidationRules() instanceof ChoicesPropertyValidationRules choiceVal) {
                checkChoicePropertyEditing(robot, prop, nodeName, choiceVal);
            } else if (prop.getValidationRules() instanceof FontPropertyValidationRules) {
                checkFontPropertyEditing(robot, prop, nodeName);
            } else if (prop.getValidationRules() instanceof IoExpanderPropertyValidationRules) {
                checkIoExpanderEditing(robot, prop, nodeName);
            }
        }
    }

    private void checkIoExpanderEditing(FxRobot robot, CreatorProperty prop, String nodeName) throws InterruptedException {
        Thread.sleep(250);
        IoExpanderDefinitionCollection expanders = project.getGeneratorOptions().getExpanderDefinitions();
        String latestValue = expanders.getDefinitionById(prop.getLatestValue()).orElseThrow().getNicePrintableName();
        Thread.sleep(4000);
        FxAssert.verifyThat(nodeName, TextInputControlMatchers.hasText(latestValue));

        robot.clickOn(nodeName + "_btn");

        assertTrue(selectItemInTable(robot, "#mainTable", (IoExpanderDefinition iod) -> iod.getId().equals("custom123")));
        robot.clickOn("#selectButton");
        assertEquals("custom123", prop.getLatestValue());
        latestValue = expanders.getDefinitionById(prop.getLatestValue()).orElseThrow().getNicePrintableName();
        FxAssert.verifyThat(nodeName, TextInputControlMatchers.hasText(latestValue));
    }

    private void checkFontPropertyEditing(FxRobot robot, CreatorProperty prop, String nodeName) throws InterruptedException {
        // try the default font x2
        var dialogPane = compareFontDialogToProperty(robot, nodeName, prop);
        robot.clickOn("#defaultFontSelect");
        writeIntoField(robot, "#fontVarField", "", 10);
        writeIntoField(robot, "#fontNumField", 2, 4);
        clickOnButtonInDialog(robot, dialogPane, "Set Font");

        // try the numbered x2
        dialogPane = compareFontDialogToProperty(robot, nodeName, prop);
        robot.clickOn("#largeNumSelect");
        writeIntoField(robot, "#fontNumField", 9, 4);
        writeIntoField(robot, "#fontVarField", "", 10);
        clickOnButtonInDialog(robot, dialogPane, "Set Font");

        // try ada font x1
        dialogPane = compareFontDialogToProperty(robot, nodeName, prop);
        robot.clickOn("#adafruitFontSel");
        writeIntoField(robot, "#fontNumField", 2, 4);
        writeIntoField(robot, "#fontVarField", "myFont", 10);
        clickOnButtonInDialog(robot, dialogPane, "Set Font");

        robot.clickOn(nodeName + "_btn");
        dialogPane = compareFontDialogToProperty(robot, nodeName, prop);
        clickOnButtonInDialog(robot, dialogPane, "Cancel");
    }

    private Node compareFontDialogToProperty(FxRobot robot, String nodeName, CreatorProperty prop) throws InterruptedException {
        String latestValue = fromString(prop.getLatestValue()).orElseThrow().getNicePrintableName();
        withRetryOnFxThread(new TextFieldPredicate(robot, nodeName, latestValue), "nodeName check for " + latestValue);

        robot.clickOn(nodeName + "_btn");
        var def = fromString(prop.getLatestValue()).orElseThrow();
        var radioToCheck = switch (def.getFontMode()) {
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

    private void checkChoicePropertyEditing(FxRobot robot, CreatorProperty prop, String
            nodeName, ChoicesPropertyValidationRules choiceVal) throws InterruptedException {
        for (var choice : choiceVal.choices()) {
            assertTrue(selectItemInCombo(robot, nodeName, (ChoiceDescription cd) ->
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

    private void checkIntegerPropertyEditing(FxRobot robot, String id, CreatorProperty prop, String
            nodeName, IntegerPropertyValidationRules intVal) {
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
        writeIntoField(robot, nodeName, value, 5);
        robot.clickOn("#" + id + "Title");
        assertEquals(value.toString(), property.getLatestValue());
    }

    private class TextFieldPredicate implements Supplier<Boolean> {
        private final FxRobot robot;
        private final String nodeName;
        private final String latestValue;

        public TextFieldPredicate(FxRobot robot, String nodeName, String latestValue) {
            this.robot = robot;
            this.nodeName = nodeName;
            this.latestValue = latestValue;
        }

        @Override
        public Boolean get() {
            var node = robot.lookup(nodeName).query();
            if (node instanceof Labeled labeled) {
                return labeled.getText().equals(latestValue);
            } else if (node instanceof TextInputControl textControl) {
                return textControl.getText().equals(latestValue);
            }
            return false;
        }
    }
}
