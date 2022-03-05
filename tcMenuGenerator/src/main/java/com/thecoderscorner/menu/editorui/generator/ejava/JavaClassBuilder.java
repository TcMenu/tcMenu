package com.thecoderscorner.menu.editorui.generator.ejava;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator.LINE_BREAK;
import static com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator.TWO_LINES;
import static com.thecoderscorner.menu.editorui.generator.ejava.GeneratedJavaMethod.GenerationMode.*;
import static java.lang.System.Logger.Level;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class JavaClassBuilder {
    private static final Pattern IMPORT_PATTERN = Pattern.compile("^import\\s*.+");
    private final EmbeddedJavaProject project;
    private final String pkgName;
    private final String clazzName;
    private final List<String> importStatements = new ArrayList<>();
    private final List<GeneratedStatement> statements = new ArrayList<>();
    private final BiConsumer<Level, String> uiLogger;
    private Optional<String> possibleInterface = Optional.empty();
    private Path location;
    private int indentation = 1;
    private Optional<String> statementBeforeClass = Optional.empty();

    public JavaClassBuilder(EmbeddedJavaProject project, String pkgName, String clazzName, BiConsumer<Level, String> uiLogger) {
        this.project = project;
        this.uiLogger = uiLogger;
        this.location = project.getMainJava();
        this.pkgName = pkgName;
        this.clazzName = clazzName;
    }

    public JavaClassBuilder supportsInterface(String impl) {
        this.possibleInterface = Optional.ofNullable(impl);
        return this;
    }

    public JavaClassBuilder addPackageImport(String imp) {
        this.importStatements.add(imp);
        return this;
    }

    public JavaClassBuilder useTestLocation() {
        this.location = project.getTestJava();
        return this;
    }

    public JavaClassBuilder setIndentation(int level) {
        this.indentation = level;
        return this;
    }

    public int getIndentation() { return indentation; }

    public JavaClassBuilder addStatement(String statement) {
        this.statements.add(new RegularStringStatement(statement));
        return this;
    }

    public String indentIt() {
        return "    ".repeat(Math.max(0, indentation));
    }

    public JavaClassBuilder blankLine() {
        addStatement("");
        return this;
    }

    public void persistClassByPatching() throws IOException {
        uiLogger.accept(Level.INFO, "Patching class " + clazzName);
        boolean fileChanged = false;
        Path myLocation = getPathToPackage();
        Path javaClassFileLoc = myLocation.resolve(clazzName + ".java");
        if(Files.exists(javaClassFileLoc)) {
            var linesFromOriginalFile = trimEmptyLinesTopBottom(Files.readAllLines(javaClassFileLoc));

            for (var imp : importStatements) {
                if (noReferencesInFile(Pattern.compile("import.*" + imp + ";"), linesFromOriginalFile)) {
                    int idx = findLastStatementOfType(IMPORT_PATTERN, linesFromOriginalFile);
                    linesFromOriginalFile.add(idx, "import " + imp + ";");
                    fileChanged = true;
                }
            }

            for(var stmt : statements.stream().filter(GeneratedStatement::isPatchable).toList()) {
                if(stmt instanceof GeneratedJavaMethod m && !m.isIfMissingOnly()) {
                    attemptToRemoveMethodFromFile(m, linesFromOriginalFile);
                }
                if(noReferencesInFile(stmt.getMatchingPattern(this), linesFromOriginalFile)) {
                    var sb = new StringBuilder(100);
                    stmt.generate(this, sb);
                    var fieldDef = sb.toString();

                    int idx;
                    if(stmt instanceof GeneratedJavaMethod m && m.getMethodType() == METHOD_REPLACE) {
                        // replace mode stuff, always put at end of file.
                        idx = linesFromOriginalFile.size() - 1;
                    } else {
                        // in other case, we use the comment to locate the end of the block
                        idx = findLastStatementOfType(stmt.getEndOfBlockPattern(), linesFromOriginalFile);
                    }
                    linesFromOriginalFile.add(idx, fieldDef);
                    fileChanged = true;
                }
            }

            if(fileChanged) {
                Files.copy(javaClassFileLoc, myLocation.resolve(clazzName + ".java.back"), StandardCopyOption.REPLACE_EXISTING);
                Files.writeString(javaClassFileLoc, String.join(LINE_BREAK, linesFromOriginalFile) + LINE_BREAK, TRUNCATE_EXISTING, CREATE);
            }
        } else {
            persistClass(); // persist using the general method for the first time
        }
    }

    private ArrayList<String> trimEmptyLinesTopBottom(List<String> lines) {
        var readAllLines = new ArrayList<>(lines);
        while(readAllLines.get(0).isEmpty()) readAllLines.remove(0);
        while(readAllLines.get(readAllLines.size() - 1).isEmpty()) readAllLines.remove(readAllLines.size() -1);
        return readAllLines;
    }

    private void attemptToRemoveMethodFromFile(GeneratedJavaMethod method, List<String> allLines) {
        uiLogger.accept(Level.INFO, "Attempting to remove method " + method.getName() + " from " + clazzName);
        var search = method.getMatchingPattern(this);
        for(int i=0; i<allLines.size(); i++) {
            if(search.matcher(allLines.get(i)).matches()) {
                removeMethodFrom(method, i, allLines);
                break;
            }
        }
    }

    private void removeMethodFrom(GeneratedJavaMethod method, int start, List<String> allLines) {
        if(!allLines.get(start).trim().endsWith("{") && !allLines.get(start+1).contains("{")) {
            throw new IllegalStateException(method.getName() + " does not have a starting '{'");
        }

        // try and work out the end of the method by matching up starting and ending braces.
        int startingBraces = 1, endingBraces = 0;
        int current = (allLines.get(start+1).trim().equals("{")) ? (start + 2) : (start + 1);
        while(current < allLines.size() && startingBraces != endingBraces) {
            if(allLines.get(current).contains("{")) startingBraces++;
            if(allLines.get(current).contains("}")) endingBraces++;
            current++;
        }

        // do not remove anything if the braces cannot be balanced!
        if(startingBraces != endingBraces) throw new IllegalStateException(method.getName() + " has unbalanced braces");

        if (current >= start + 1) {
            allLines.subList(start, current + 1).clear();
            uiLogger.accept(Level.INFO, "Successfully removed " + method.getName() + " from " + clazzName);
        }
        else {
            uiLogger.accept(Level.WARNING, "Could not find " + method.getName() + " in " + clazzName);
        }
    }

    private int findLastStatementOfType(Pattern search, ArrayList<String> linesFromOriginalFile) {
        int lastMatchingLine = -1;
        for(int i=0; i<linesFromOriginalFile.size(); i++) {
            if(search.matcher(linesFromOriginalFile.get(i)).matches()) lastMatchingLine = i;
        }
        if(lastMatchingLine == -1) throw new IllegalStateException("Could not locate the last matching line, unexpected error");
        return lastMatchingLine;
    }

    private boolean noReferencesInFile(Pattern searchText, List<String> allLines) {
        for(var line : allLines) {
            if(searchText.matcher(line).matches()) return false;
        }
        return true;
    }

    public void persistClass() throws IOException {
        uiLogger.accept(Level.INFO, "Full class generation for " + clazzName);
        var sb = new StringBuilder(4096);
        sb.append("package ").append(pkgName).append(';').append(TWO_LINES);

        boolean hadImports = false;
        for (var imp : importStatements) {
            sb.append("import ").append(imp).append(';').append(LINE_BREAK);
            hadImports = true;
        }
        if (hadImports) sb.append(LINE_BREAK);

        statementBeforeClass.ifPresent(sb::append);

        sb.append("public class ").append(clazzName);
        possibleInterface.ifPresent(s -> sb.append(" implements ").append(s));
        sb.append(" {").append(LINE_BREAK);

        for (var stmt : statements) {
            stmt.generate(this, sb);
            sb.append(LINE_BREAK);
        }

        sb.append('}').append(LINE_BREAK);

        Path myLocation = getPathToPackage();
        Files.createDirectories(myLocation);
        Files.writeString(myLocation.resolve(clazzName + ".java"), sb.toString(), TRUNCATE_EXISTING, CREATE);
    }

    private Path getPathToPackage() {
        var myLocation = location;
        for (var dirPart : pkgName.split("\\.")) {
            myLocation = myLocation.resolve(dirPart);
        }
        return myLocation;
    }

    public JavaClassBuilder addStatement(GeneratedStatement method) {
        statements.add(method);
        return this;
    }

    public JavaClassBuilder setStatementBeforeClass(String s) {
        statementBeforeClass = Optional.of(s);
        return this;
    }

    public String getClazzName() {
        return clazzName;
    }

}
