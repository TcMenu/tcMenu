/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.google.common.base.Strings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ArduinoSketchFileAdjuster {
    /** In case the directory has never previous had a sketch, this is the simplest sketch.. */
    public static final String EMPTY_SKETCH = "\nvoid setup() {\n\n}\n\n" + "void loop() {\n\n}\n";

    /** The pattern to look for call back functions */
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("void\\s+CALLBACK_FUNCTION\\s+([^\\(\\s]+).*");
    /** the pattern to look for set up */
    private static final Pattern SETUP_PATTERN = Pattern.compile("void\\s+setup\\(\\)(.*)");
    /** the pattern to loop for the loop method */
    private static final Pattern LOOP_PATTERN = Pattern.compile("void\\s+loop\\(\\)(.*)");

    private final List<String> callbacks;
    private final String inoFile;
    private final String projectName;
    private final Consumer<String> logger;
    private boolean changed = false;

    public ArduinoSketchFileAdjuster(Consumer<String> logger, String inoFile, String projectName, List<String> callbacks) {
        this.callbacks = callbacks;
        this.projectName = projectName;
        this.inoFile = inoFile;
        this.logger = logger;
    }

    public void makeAdjustments() throws IOException {
        boolean needsInclude = true;
        boolean needsTaskMgr = true;
        boolean needsSetup = true;
        List<String> callbacksDefined = new ArrayList<>();

        for(String line : Files.lines(Paths.get(inoFile)).collect(Collectors.toList())) {
            if(line.contains("#include") && line.contains(projectName + ".h")) {
                logger.accept("found include in INO");
                needsInclude = false;
            }
            else if(line.contains("taskManager.runLoop()")) {
                logger.accept("found runLoop in INO");
                needsTaskMgr = false;
            }
            else if(line.contains("setupMenu(")) {
                logger.accept("found setup in INO");
                needsSetup = false;
            }
            else if(line.contains("CALLBACK_FUNCTION")) {
                Matcher fnMatch = FUNCTION_PATTERN.matcher(line);
                if(fnMatch.matches()) {
                    logger.accept("found callback for " + fnMatch.group(1));
                    callbacksDefined.add(fnMatch.group(1));
                }
            }
        }

        ArrayList<String> lines = new ArrayList<>(Files.readAllLines(Paths.get(inoFile)));
        if(needsInclude) addIncludeToTopOfFile(lines);
        if(needsSetup) addSetupCode(lines, SETUP_PATTERN, "    setupMenu(NULL); // NULL (no remote listener)");
        if(needsTaskMgr) addSetupCode(lines, LOOP_PATTERN, "    taskManager.runLoop();");
        List<String> callbacksToMake = new ArrayList<>(callbacks);
        callbacksToMake.removeAll(callbacksDefined);

        makeNewCallbacks(lines, callbacksToMake);

        if(changed) {
            logger.accept("Writing out changes to INO sketch file");
            Files.write(Paths.get(inoFile), lines);
        }
        else {
            logger.accept("No changes to the INO file, not writing out");
        }
    }

    private void makeNewCallbacks(ArrayList<String> lines, List<String> callbacksToMake) {
        for (String callbackName : callbacksToMake) {
            logger.accept("Adding new callback to sketch: " + callbackName);
            lines.add("");
            lines.add("void CALLBACK_FUNCTION " + callbackName + "(int id) {");
            lines.add("    // TODO - your menu change code");
            lines.add("}");
            changed = true;
        }
    }

    private void addSetupCode(ArrayList<String> lines, Pattern codePattern, String extraLine) throws IOException {
        logger.accept("Running sketch setup adjustments: " + extraLine);
        for(int i=0;i<lines.size();i++) {
            Matcher matcher = codePattern.matcher(lines.get(i));
            if(matcher.matches()) {
                if(Strings.isNullOrEmpty(matcher.group(1)) || !matcher.group(1).contains("{")) {
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

    private void addIncludeToTopOfFile(ArrayList<String> lines) {
        lines.add(0, "#include \"" + projectName + ".h\"");
        changed = true;
    }
}
