package com.thecoderscorner.menu.editorui.generator.ejava;

import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator.LINE_BREAK;

public class ModuleFilePatcher {
    public static final String MODULE_FILE_NAME = "module-info.java";
    private static final Pattern REQUIRES_PATTERN = Pattern.compile("\\s*requires\\s*(.*);.*");
    private static final Pattern EXPORTS_PATTERN = Pattern.compile("\\s*exports\\s*(.*);.*");
    private static final Pattern OPENS_PATTERN = Pattern.compile("\\s*opens\\s*(.*);.*");

    private final Path path;
    private final List<String> requiresList = new ArrayList<>();
    private final List<String> opensList = new ArrayList<>();
    private final List<String> exportsList = new ArrayList<>();

    public ModuleFilePatcher(Path path) {
        this.path = path.resolve(MODULE_FILE_NAME);
    }

    public void addRequires(String what) {
        requiresList.add(what);
    }

    public void addExports(String what) {
        exportsList.add(what);
    }

    public void addOpens(String what) {
        opensList.add(what);
    }

    public boolean startConversion(CodeGeneratorOptions options, EmbeddedJavaProject project) throws IOException {
        List<String> existingLines = new ArrayList<>();
        if(Files.exists(path)) {
            existingLines.addAll(Files.readAllLines(path));
        } else {
            existingLines.add(String.format("module %s.%s {", options.getPackageNamespace(), project.getAppClassName("").toLowerCase()));
            existingLines.add("}");
        }

        boolean reqUpdated = appendAsNeeded("requires", REQUIRES_PATTERN, requiresList, existingLines);
        boolean expUpdated = appendAsNeeded("exports", EXPORTS_PATTERN, exportsList, existingLines);
        boolean openUpdated = appendAsNeeded("opens", OPENS_PATTERN, opensList, existingLines);

        if(reqUpdated || expUpdated || openUpdated) {
            Files.writeString(path, String.join(LINE_BREAK, existingLines) + LINE_BREAK);
            return true;
        } else {
            return false;
        }
    }

    private boolean appendAsNeeded(String op, Pattern findPattern, List<String> neededList, List<String> existingLines) {
        var listToActuallyAdd = new ArrayList<>(neededList.stream().distinct().toList());
        int nextEntryPosition = -1;
        int i=0;
        for(var line : existingLines) {
            Matcher matcher = findPattern.matcher(line);
            if(matcher.matches()) {
                var moduleName = matcher.group(1);
                if(neededList.contains(moduleName)) {
                    listToActuallyAdd.remove(moduleName);
                }
            }
            if(line.trim().equals("}")) nextEntryPosition = i;
            i++;
        }

        boolean anythingUpdated = false;
        for(var item : listToActuallyAdd) {
            anythingUpdated = true;
            existingLines.add(nextEntryPosition, "    " + op + " " + item + ";");
            nextEntryPosition++;
        }
        return anythingUpdated;
    }
}
