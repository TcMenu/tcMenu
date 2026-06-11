/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.domain.ScrollChoiceMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation;
import com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator;
import com.thecoderscorner.menu.editorui.generator.core.SketchFileAdjuster;
import com.thecoderscorner.menu.editorui.generator.core.TcMenuConversionException;
import com.thecoderscorner.menu.editorui.generator.logger.GeneratedFile;
import com.thecoderscorner.menu.editorui.generator.logger.UserFeedbackLogger;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.util.BackupManager;
import com.thecoderscorner.menu.editorui.util.StringHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.thecoderscorner.menu.domain.ScrollChoiceMenuItem.ScrollChoiceMode;
import static com.thecoderscorner.menu.domain.util.MenuItemHelper.isRuntimeStructureNeeded;
import static com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation.*;

public class ArduinoSketchFileAdjuster implements SketchFileAdjuster {
    /** The pattern to look for call back functions */
    public static final Pattern FUNCTION_PATTERN = Pattern.compile("(void|int)\\s+CALLBACK_FUNCTION\\s+([^\\(\\s]+).*");
    /** the pattern to look for set up */
    private static final Pattern SETUP_PATTERN = Pattern.compile("void\\s+setup\\(\\)(.*)");
    /** the pattern to loop for the loop method */
    private static final Pattern LOOP_PATTERN = Pattern.compile("void\\s+loop\\(\\)(.*)");
    protected final CodeGeneratorOptions options;
    private final ConfigurationStorage config;
    private final AtomicReference<String> menuBuilderContent = new AtomicReference<>();

    protected boolean changed = false;
    protected UserFeedbackLogger logger;

    public ArduinoSketchFileAdjuster(CodeGeneratorOptions options, ConfigurationStorage config) {
        this.options = options;
        this.config = config;
    }

    protected String emptyFileContents() {
        return """

                void setup() {

                }

                void loop() {

                }
                """;
    }

    @Override
    public Path createFileIfNeeded(UserFeedbackLogger logger, Path dir, CodeGeneratorOptions projectOptions) throws IOException {
        Path inoFile;
        this.logger = logger;
        var path = isSavingToSrcDir() ? dir.resolve("src") : dir;
        if(options.isUseCppMain()) {
            inoFile = getCppMainPath(path);
        }
        else {
            inoFile = Paths.get(CoreCodeGenerator.toSourceFile(path, ".ino", false));
        }

        if(Files.exists(inoFile)) return inoFile;

        logger.info("File does not exist, creating " + inoFile.getFileName());
        Files.write(inoFile, emptyFileContents().getBytes(), StandardOpenOption.CREATE);

        if(!Files.exists(inoFile)) throw new IOException("Main file not created in " + path);
        return inoFile;
    }

    @Override
    public void menuTreeInSketch(String rootMenuCode) {
        menuBuilderContent.set(rootMenuCode);
    }

    private boolean isSavingToSrcDir() {
        return options.getSaveLocation() == ALL_TO_SRC || options.getSaveLocation() == PROJECT_TO_SRC_WITH_GENERATED;
    }

    protected Path getCppMainPath(Path path) {
        return Paths.get(CoreCodeGenerator.toSourceFile(path, "_main.cpp", false));
    }

    public void makeAdjustments(UserFeedbackLogger logger, Path projectRoot, String inoFile,
                                String projectName, Collection<CallbackRequirement> callbacks, MenuTree tree) throws IOException, TcMenuConversionException {

        this.logger = logger;
        changed = false;

        boolean needsInclude = true;
        boolean needsTaskMgr = true;
        boolean needsSetup = true;

        List<String> callbacksDefined = new ArrayList<>();
        boolean needsMenuBuild = menuBuilderContent.get() != null;
        Path source = Paths.get(inoFile);
        try(var fileLines = Files.lines(source)) {
            for (String line : fileLines.toList()) {
                if (line.contains("#include") && line.contains(projectName + "_menu.h")) {
                    logger.info("found include in INO");
                    needsInclude = false;
                } else if (line.contains("taskManager.runLoop()")) {
                    logger.info("found runLoop in INO");
                    needsTaskMgr = false;
                } else if (line.contains("void buildMenu(TcMenuBuilder& builder)")) {
                    logger.info("found buildMenu in INO, will not replace");
                    needsMenuBuild = false;
                } else if (line.contains("setupMenu(")) {
                    logger.info("found setup in INO");
                    needsSetup = false;
                } else if (line.contains("CALLBACK_FUNCTION")) {
                    Matcher fnMatch = FUNCTION_PATTERN.matcher(line);
                    if (fnMatch.matches()) {
                        logger.info("found callback for " + fnMatch.group(2));
                        callbacksDefined.add(fnMatch.group(2));
                    }
                }
            }
        }

        ArrayList<String> lines = new ArrayList<>(Files.readAllLines(source));
        if(needsInclude) addIncludeToTopOfFile(lines, projectName, options.getSaveLocation());
        if(needsSetup) addSetupCode(lines, SETUP_PATTERN, "    setupMenu();");
        if(needsTaskMgr) taskManagerIsMissing(lines);
        if(needsMenuBuild) appendMenuBuilderCode(lines);
        List<CallbackRequirement> callbacksToMake = new ArrayList<>(callbacks);
        makeNewCallbacks(lines, callbacksToMake, callbacksDefined);
        addScrollVariablesIfNeeded(lines, tree);

        if(changed) {
            logger.info("INO Previously existed, backup existing file");
            var backupMgr = new BackupManager(config);
            backupMgr.backupFile(projectRoot, source);

            logger.info("Writing out changes to INO sketch file");
            chompBlankLines(lines);
            Files.write(source, lines);
            logger.fileModificiation(GeneratedFile.always(source, String.join(CoreCodeGenerator.LINE_BREAK, lines)));
        }
        else {
            logger.info("No changes to the INO file, not writing out");
        }
    }

    private void appendMenuBuilderCode(ArrayList<String> lines) throws TcMenuConversionException {
        logger.info("Appending menu builder code to your sketch file");

        int setupLineIndex = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (SETUP_PATTERN.matcher(lines.get(i)).matches()) {
                setupLineIndex = i;
                break;
            }
        }

        if (setupLineIndex == -1) {
            logger.error("No setup() function found, cannot append menu builder code");
            throw new TcMenuConversionException("Could not process INO file to add menu structure because no setup was found");
        }

        String menuContent = menuBuilderContent.get();
        if (menuContent != null && !menuContent.isEmpty()) {
            lines.add(setupLineIndex, CoreCodeGenerator.LINE_BREAK);
            lines.add(setupLineIndex, menuContent);
            lines.add(setupLineIndex, CoreCodeGenerator.LINE_BREAK);
            changed = true;
            logger.info("Menu builder code appended before setup()");
        }
    }

    private void addScrollVariablesIfNeeded(ArrayList<String> lines, MenuTree tree) {
        var scrollItems = tree.getAllMenuItems().stream()
                .filter(it -> it instanceof ScrollChoiceMenuItem scr && scr.getChoiceMode() == ScrollChoiceMode.ARRAY_IN_RAM)
                .map(mi -> (ScrollChoiceMenuItem)mi)
                .filter(sc -> !isDefinitionOnlyVariable(sc.getVariable()))
                .toList();

        for(var sc : scrollItems) {
            if(lines.stream().anyMatch(line -> line.contains(sc.getVariable()))) {
                logger.info("Scroll choice variable exists - " + sc.getVariable());
            } else {
                logger.info("Adding variable for scroll choice - " + sc.getVariable());

                int l;
                for(l=0; l<lines.size(); l++) {
                    if(lines.get(l).contains("setup()")) {
                        break;
                    }
                }

                StringBuilder sb = new StringBuilder(100);
                for(int i=0; i<sc.getNumEntries(); i++) {
                    var strNum = Integer.toString(i + 1);
                    sb.append(strNum).append("\\0");
                    sb.append(StringHelper.repeat(" ", (sc.getItemWidth() - (strNum.length() + 1))));
                }
                sb.append('~');

                lines.add(l++, "// This variable is the RAM data for scroll choice item " + sc.getName());
                lines.add(l++, "char " + sc.getVariable() + "[] = \"" + sb + "\";");
                lines.add(l, CoreCodeGenerator.LINE_BREAK);
                changed = true;
            }
        }
    }

    public static boolean isDefinitionOnlyVariable(String variableName) {
        return variableName.startsWith("@");
    }

    private void chompBlankLines(ArrayList<String> lines) {
        //  remove blank lines at the end, we don't want loads of trailing empty lines at the end
        while(lines.get(lines.size()-1).isBlank()) {
            lines.remove(lines.size()-1);
        }
    }

    protected void taskManagerIsMissing(ArrayList<String> lines) {
        addSetupCode(lines, LOOP_PATTERN, "    taskManager.runLoop();");
    }

    private void makeNewCallbacks(ArrayList<String> lines, List<CallbackRequirement> callbacksToMake,
                                  List<String> alreadyDefined) {

        var filteredCb = callbacksToMake.stream()
                .filter(cb -> !StringHelper.isStringEmptyOrNull(cb.getCallbackName()) ||
                        isRuntimeStructureNeeded(cb.getCallbackItem())).toList();

        var definedList = new HashSet<>(alreadyDefined);

        for (var cb : filteredCb) {
            if(cb.isHeaderOnlyCallback()) {
                logger.info("Callback function void " + cb.getCallbackName() + "(int id) must be implemented by you");
            }
            else if(cb.isCallbackGenerationNeeded(definedList)) {
                logger.info("Adding new callback to sketch for: '" + cb.getCallbackItem() + "' with name '" + cb.getCallbackName() + "'");
                lines.add("");
                lines.addAll(cb.generateSketchCallback());
                definedList.add(cb.getCallbackName());
                changed = true;
            }
        }
    }

    private void addSetupCode(ArrayList<String> lines, Pattern codePattern, String extraLine) {
        logger.info("Running sketch setup adjustments: " + extraLine);
        for(int i=0;i<lines.size();i++) {
            Matcher matcher = codePattern.matcher(lines.get(i));
            if(matcher.matches()) {
                if(StringHelper.isStringEmptyOrNull(matcher.group(1)) || !matcher.group(1).contains("{")) {
                    if(!lines.get(++i).contains("{")) {
                        return; // non standard - cant add
                    }
                }
                lines.add(++i, extraLine);
                logger.debug( "-> line added to sketch: " + extraLine);
                changed = true;
                return; // no need to continue
            }
        }
    }

    protected void addIncludeToTopOfFile(ArrayList<String> lines, String projectName, ProjectSaveLocation location) {
        String loc = "";
        if(location == PROJECT_TO_SRC_WITH_GENERATED || location == PROJECT_TO_CURRENT_WITH_GENERATED) {
            loc = "generated/";
        }
        lines.addFirst("#include \"" + loc + projectName + "_menu.h\"");
        changed = true;
    }
}
