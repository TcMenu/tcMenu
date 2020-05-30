/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.editorui.generator.core.SketchFileAdjuster;
import com.thecoderscorner.menu.editorui.util.StringHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.domain.util.MenuItemHelper.isRuntimeStructureNeeded;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ArduinoSketchFileAdjuster implements SketchFileAdjuster {
    /** The pattern to look for call back functions */
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("(void|int)\\s+CALLBACK_FUNCTION\\s+([^\\(\\s]+).*");
    /** the pattern to look for set up */
    private static final Pattern SETUP_PATTERN = Pattern.compile("void\\s+setup\\(\\)(.*)");
    /** the pattern to loop for the loop method */
    private static final Pattern LOOP_PATTERN = Pattern.compile("void\\s+loop\\(\\)(.*)");

    protected boolean changed = false;
    protected Consumer<String> logger;

    public ArduinoSketchFileAdjuster() {
    }

    protected String emptyFileContents() {
        return "\nvoid setup() {\n\n}\n\n" + "void loop() {\n\n}\n";
    }

    /**
     * Is able to update and round trip an ino file for the items that tcMenu needs.
     *
     * Not thread safe, should be created for each run.
     * @param logger a consumer that handles UI logging
     * @param inoFile the file to be modified
     * @param projectName the project name
     * @param callbacks the list of callbacks.
     * @throws IOException in the event of an error
     */
    public void makeAdjustments(Consumer<String> logger, String inoFile, String projectName,
                                Collection<CallbackRequirement> callbacks) throws IOException {

        this.logger = logger;
        changed = false;

        Path source = Paths.get(inoFile);
        if (!Files.exists(source)) {
            logger.accept("No existing sketch, generating an empty one");
            Files.write(source, emptyFileContents().getBytes());
        }

        boolean needsInclude = true;
        boolean needsTaskMgr = true;
        boolean needsSetup = true;

        List<String> callbacksDefined = new ArrayList<>();

        try(var fileLines = Files.lines(source)) {
            for (String line : fileLines.collect(Collectors.toList())) {
                if (line.contains("#include") && line.contains(projectName + "_menu.h")) {
                    logger.accept("found include in INO");
                    needsInclude = false;
                } else if (line.contains("taskManager.runLoop()")) {
                    logger.accept("found runLoop in INO");
                    needsTaskMgr = false;
                } else if (line.contains("setupMenu(")) {
                    logger.accept("found setup in INO");
                    needsSetup = false;
                } else if (line.contains("CALLBACK_FUNCTION")) {
                    Matcher fnMatch = FUNCTION_PATTERN.matcher(line);
                    if (fnMatch.matches()) {
                        logger.accept("found callback for " + fnMatch.group(2));
                        callbacksDefined.add(fnMatch.group(2));
                    }
                }
            }
        }

        ArrayList<String> lines = new ArrayList<>(Files.readAllLines(source));
        if(needsInclude) addIncludeToTopOfFile(lines, projectName);
        if(needsSetup) addSetupCode(lines, SETUP_PATTERN, "    setupMenu();");
        if(needsTaskMgr) taskManagerIsMissing(lines);
        List<CallbackRequirement> callbacksToMake = new ArrayList<>(callbacks);
        makeNewCallbacks(lines, callbacksToMake, callbacksDefined);

        if(changed) {
            logger.accept("INO Previously existed, backup existing file");
            Files.copy(source, Paths.get(source.toString() + ".backup"), REPLACE_EXISTING);

            logger.accept("Writing out changes to INO sketch file");
            Files.write(Paths.get(inoFile), lines);
        }
        else {
            logger.accept("No changes to the INO file, not writing out");
        }
    }

    private void taskManagerIsMissing(ArrayList<String> lines) {
        addSetupCode(lines, LOOP_PATTERN, "    taskManager.runLoop();");
    }

    private void makeNewCallbacks(ArrayList<String> lines, List<CallbackRequirement> callbacksToMake,
                                  List<String> alreadyDefined) {

        var filteredCb = callbacksToMake.stream()
                .filter(cb -> !StringHelper.isStringEmptyOrNull(cb.getCallbackName()) || isRuntimeStructureNeeded(cb.getCallbackItem()))
                .collect(Collectors.toList());

        for (CallbackRequirement cb : filteredCb) {
            if(!alreadyDefined.contains(cb.getCallbackName())) {
                logger.accept("Adding new callback to sketch: " + cb.getCallbackName());
                lines.add("");
                lines.addAll(cb.generateSketchCallback());
                changed = true;
            }
            else {
                logger.accept("Skip callback generation for " + cb.getCallbackName());
            }
        }
    }

    private void addSetupCode(ArrayList<String> lines, Pattern codePattern, String extraLine) {
        logger.accept("Running sketch setup adjustments: " + extraLine);
        for(int i=0;i<lines.size();i++) {
            Matcher matcher = codePattern.matcher(lines.get(i));
            if(matcher.matches()) {
                if(StringHelper.isStringEmptyOrNull(matcher.group(1)) || !matcher.group(1).contains("{")) {
                    if(!lines.get(++i).contains("{")) {
                        return; // non standard - cant add
                    }
                }
                lines.add(++i, extraLine);
                logger.accept("-> line added to sketch");
                changed = true;
                return; // no need to continue
            }
        }
    }

    protected void addIncludeToTopOfFile(ArrayList<String> lines, String projectName) {
        lines.add(0, "#include \"" + projectName + "_menu.h\"");
        changed = true;
    }
}
