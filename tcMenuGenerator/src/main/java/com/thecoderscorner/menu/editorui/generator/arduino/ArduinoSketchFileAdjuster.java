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
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.storage.PrefsConfigurationStorage;
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
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.thecoderscorner.menu.domain.ScrollChoiceMenuItem.ScrollChoiceMode;
import static com.thecoderscorner.menu.domain.util.MenuItemHelper.isRuntimeStructureNeeded;
import static com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation.*;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ArduinoSketchFileAdjuster implements SketchFileAdjuster {
    /** The pattern to look for call back functions */
    public static final Pattern FUNCTION_PATTERN = Pattern.compile("(void|int)\\s+CALLBACK_FUNCTION\\s+([^\\(\\s]+).*");
    /** the pattern to look for set up */
    private static final Pattern SETUP_PATTERN = Pattern.compile("void\\s+setup\\(\\)(.*)");
    /** the pattern to loop for the loop method */
    private static final Pattern LOOP_PATTERN = Pattern.compile("void\\s+loop\\(\\)(.*)");
    protected final CodeGeneratorOptions options;
    private final ConfigurationStorage config;

    protected boolean changed = false;
    protected BiConsumer<System.Logger.Level, String> logger;

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
    public Path createFileIfNeeded(BiConsumer<System.Logger.Level, String> logger, Path dir, CodeGeneratorOptions projectOptions) throws IOException {
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

        logger.accept(INFO, "File did not exist, creating " + inoFile);
        Files.write(inoFile, emptyFileContents().getBytes(), StandardOpenOption.CREATE);

        if(!Files.exists(inoFile)) throw new IOException("Main file not created in " + path);
        return inoFile;
    }

    private boolean isSavingToSrcDir() {
        return options.getSaveLocation() == ALL_TO_SRC || options.getSaveLocation() == PROJECT_TO_SRC_WITH_GENERATED;
    }

    protected Path getCppMainPath(Path path) {
        return Paths.get(CoreCodeGenerator.toSourceFile(path, "_main.cpp", false));
    }

    public void makeAdjustments(BiConsumer<System.Logger.Level, String> logger, Path projectRoot, String inoFile,
                                String projectName, Collection<CallbackRequirement> callbacks, MenuTree tree) throws IOException {

        this.logger = logger;
        changed = false;

        boolean needsInclude = true;
        boolean needsTaskMgr = true;
        boolean needsSetup = true;

        List<String> callbacksDefined = new ArrayList<>();

        Path source = Paths.get(inoFile);
        try(var fileLines = Files.lines(source)) {
            for (String line : fileLines.toList()) {
                if (line.contains("#include") && line.contains(projectName + "_menu.h")) {
                    logger.accept(INFO, "found include in INO");
                    needsInclude = false;
                } else if (line.contains("taskManager.runLoop()")) {
                    logger.accept(INFO, "found runLoop in INO");
                    needsTaskMgr = false;
                } else if (line.contains("setupMenu(")) {
                    logger.accept(INFO, "found setup in INO");
                    needsSetup = false;
                } else if (line.contains("CALLBACK_FUNCTION")) {
                    Matcher fnMatch = FUNCTION_PATTERN.matcher(line);
                    if (fnMatch.matches()) {
                        logger.accept(INFO, "found callback for " + fnMatch.group(2));
                        callbacksDefined.add(fnMatch.group(2));
                    }
                }
            }
        }

        ArrayList<String> lines = new ArrayList<>(Files.readAllLines(source));
        if(needsInclude) addIncludeToTopOfFile(lines, projectName, options.getSaveLocation());
        if(needsSetup) addSetupCode(lines, SETUP_PATTERN, "    setupMenu();");
        if(needsTaskMgr) taskManagerIsMissing(lines);
        List<CallbackRequirement> callbacksToMake = new ArrayList<>(callbacks);
        makeNewCallbacks(lines, callbacksToMake, callbacksDefined);
        addScrollVariablesIfNeeded(lines, tree);

        if(changed) {
            logger.accept(INFO, "INO Previously existed, backup existing file");
            var backupMgr = new BackupManager(config);
            backupMgr.backupFile(projectRoot, source);

            logger.accept(INFO, "Writing out changes to INO sketch file");
            chompBlankLines(lines);
            Files.write(source, lines);
        }
        else {
            logger.accept(INFO, "No changes to the INO file, not writing out");
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
                logger.accept(INFO, "Scroll choice variable exists - " + sc.getVariable());
            } else {
                logger.accept(INFO, "Adding variable for scroll choice - " + sc.getVariable());

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
                lines.add(l++, "char* " + sc.getVariable() + " = \"" + sb + "\";");
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
                logger.accept(INFO, "Callback function void " + cb.getCallbackName() + "(int id) must be implemented by you");
            }
            else if(cb.isCallbackGenerationNeeded(definedList)) {
                logger.accept(INFO, "Adding new callback to sketch for: " + cb.getCallbackItem());
                lines.add("");
                lines.addAll(cb.generateSketchCallback());
                definedList.add(cb.getCallbackName());
                changed = true;
            }
            else {
                logger.accept(DEBUG, "Callback already generated " + cb.getCallbackName());
            }
        }
    }

    private void addSetupCode(ArrayList<String> lines, Pattern codePattern, String extraLine) {
        logger.accept(INFO, "Running sketch setup adjustments: " + extraLine);
        for(int i=0;i<lines.size();i++) {
            Matcher matcher = codePattern.matcher(lines.get(i));
            if(matcher.matches()) {
                if(StringHelper.isStringEmptyOrNull(matcher.group(1)) || !matcher.group(1).contains("{")) {
                    if(!lines.get(++i).contains("{")) {
                        return; // non standard - cant add
                    }
                }
                lines.add(++i, extraLine);
                logger.accept(DEBUG, "-> line added to sketch: " + extraLine);
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
        lines.add(0,  "#include \"" + loc + projectName + "_menu.h\"");
        changed = true;
    }
}
