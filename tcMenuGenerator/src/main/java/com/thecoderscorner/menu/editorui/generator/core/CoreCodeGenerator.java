/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.arduino.CallbackRequirement;
import com.thecoderscorner.menu.editorui.generator.arduino.MenuItemToEmbeddedGenerator;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeParameter;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.plugin.FunctionDefinition;
import com.thecoderscorner.menu.editorui.generator.plugin.RequiredSourceFile;
import com.thecoderscorner.menu.editorui.util.StringHelper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.util.StringHelper.isStringEmptyOrNull;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public abstract class CoreCodeGenerator implements CodeGenerator {
    protected final System.Logger logger = System.getLogger(getClass().getSimpleName());
    public static final String LINE_BREAK = System.getProperty("line.separator");
    public static final String TWO_LINES = LINE_BREAK + LINE_BREAK;

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault());

    private static final String COMMENT_HEADER = "/*\n" +
            "    The code in this file uses open source libraries provided by thecoderscorner" + LINE_BREAK + LINE_BREAK +
            "    DO NOT EDIT THIS FILE, IT WILL BE GENERATED EVERY TIME YOU USE THE UI DESIGNER" + LINE_BREAK +
            "    INSTEAD EITHER PUT CODE IN YOUR SKETCH OR CREATE ANOTHER SOURCE FILE." + LINE_BREAK + LINE_BREAK +
            "    All the variables you may need access to are marked extern in this file for easy" + LINE_BREAK +
            "    use elsewhere." + LINE_BREAK +
            " */" + LINE_BREAK + LINE_BREAK;

    protected final ArduinoLibraryInstaller installer;
    protected final SketchFileAdjuster sketchAdjuster;
    protected final EmbeddedPlatform embeddedPlatform;
    protected final CodeGeneratorOptions options;
    protected Consumer<String> uiLogger = null;
    protected MenuTree menuTree;
    protected List<String> previousPluginFiles = List.of();
    protected boolean usesProgMem;
    protected CodeConversionContext context;
    protected VariableNameGenerator namingGenerator;
    protected NameAndKey nameAndKey;

    public CoreCodeGenerator(SketchFileAdjuster adjuster, ArduinoLibraryInstaller installer, EmbeddedPlatform embeddedPlatform,
                             CodeGeneratorOptions options) {
        this.installer = installer;
        this.sketchAdjuster = adjuster;
        this.embeddedPlatform = embeddedPlatform;
        this.options = options;
    }

    public boolean startConversion(Path directory, List<CodePluginItem> codeGenerators, MenuTree menuTree,
                                   NameAndKey nameKey, List<String> previousPluginFiles, boolean saveToSrc) {
        this.menuTree = menuTree;
        this.nameAndKey = nameKey;
        namingGenerator = new VariableNameGenerator(menuTree, options.isNamingRecursive());
        this.previousPluginFiles = previousPluginFiles;
        logLine("Starting " + embeddedPlatform.getBoardId() + " generate into : " + directory);

        usesProgMem = embeddedPlatform.isUsesProgmem();

        try {
            Path srcDir = directory;
            if (saveToSrc) {
                srcDir = directory.resolve("src");
            }
            if (!Files.exists(srcDir)) Files.createDirectories(srcDir);
            String cppFile = toSourceFile(srcDir, "_menu.cpp");
            String headerFile = toSourceFile(srcDir, "_menu.h");
            String projectName = directory.getFileName().toString();

            // Prepare the generator by initialising all the structures ready for conversion.
            String root = getFirstMenuVariable(menuTree);
            var allProps = codeGenerators.stream().flatMap(gen -> gen.getProperties().stream()).collect(Collectors.toList());
            context = new CodeConversionContext(embeddedPlatform, root, allProps);
            CodeVariableExtractor extractor = new CodeVariableCppExtractor(
                    context, usesProgMem
            );

            Collection<BuildStructInitializer> menuStructure = generateMenusInOrder(menuTree);

            // generate the source by first generating the CPP and H for the menu definition and then
            // update the sketch. Also, if any plugins have changed, then update them.
            Map<MenuItem, CallbackRequirement> callbackFunctions = callBackFunctions(menuTree);
            generateHeaders(codeGenerators, headerFile, menuStructure, extractor, callbackFunctions);
            generateSource(codeGenerators, cppFile, menuStructure, projectName, extractor, callbackFunctions);
            dealWithRequiredPlugins(codeGenerators, srcDir);

            internalConversion(directory, srcDir, callbackFunctions, projectName);

            logLine("Process has completed, make sure the code in your IDE is up-to-date.");
            logLine("You may need to close the project and then re-open it to pick up changes..");
        } catch (Exception e) {
            logLine("ERROR during conversion---------------------------------------------");
            logLine("The conversion process has failed with an error: " + e.getMessage());
            logLine("A more complete error can be found in the log file in <Home>/.tcMenu");
            logger.log(ERROR, "Exception caught while converting code: ", e);
        }

        return true;
    }

    protected abstract void internalConversion(Path directory, Path srcDir, Map<MenuItem, CallbackRequirement> callbackFunctions, String projectName) throws TcMenuConversionException;


    private List<FunctionDefinition> generateReadOnlyLocal() {
        var allFunctions = new ArrayList<FunctionDefinition>();

        allFunctions.addAll(menuTree.getAllMenuItems().stream().filter(MenuItem::isReadOnly)
                .map(item -> {
                    var params = List.of(new CodeParameter(null, true, "true"));
                    return new FunctionDefinition("setReadOnly", "menu" + menuNameFor(item), false, params, new AlwaysApplicable());
                })
                .collect(Collectors.toList())
        );

        allFunctions.addAll(menuTree.getAllMenuItems().stream().filter(MenuItem::isLocalOnly)
                .map(item -> {
                    var params = List.of(new CodeParameter(null, true, "true"));
                    return new FunctionDefinition("setLocalOnly", "menu" + menuNameFor(item), false, params, new AlwaysApplicable());
                })
                .collect(Collectors.toList())
        );

        allFunctions.addAll(menuTree.getAllMenuItems().stream().filter(this::isSecureSubMenu)
                .map(item -> {
                    var params = List.of(new CodeParameter(null, true, "true"));
                    return new FunctionDefinition("setSecured", "menu" + menuNameFor(item), false, params, new AlwaysApplicable());
                })
                .collect(Collectors.toList())
        );

        // lastly we deal with any INVISIBLE items, visible is the default.
        allFunctions.addAll(menuTree.getAllMenuItems().stream().filter((item) -> !item.isVisible())
                .map(item -> {
                    var params = List.of(new CodeParameter(null, true, "false"));
                    return new FunctionDefinition("setVisible", "menu" + menuNameFor(item), false, params, new AlwaysApplicable());
                })
                .collect(Collectors.toList())
        );

        return allFunctions;
    }

    private String menuNameFor(MenuItem item) {
        if(StringHelper.isStringEmptyOrNull(item.getVariableName())) {
            return namingGenerator.makeNameToVar(item);
        }
        else return item.getVariableName();
    }

    private boolean isSecureSubMenu(MenuItem toCheck) {
        SubMenuItem item = MenuItemHelper.asSubMenu(toCheck);
        return item != null && item.isSecured();
    }

    protected List<BuildStructInitializer> addNameAndKeyToStructure(Collection<BuildStructInitializer> menuStructure,
                                                                  NameAndKey nameKey) {
        var bsi = new BuildStructInitializer(MenuTree.ROOT, "applicationInfo", "ConnectorLocalInfo");
        var list = new ArrayList<>(menuStructure);
        list.add(bsi.addQuoted(nameKey.getName())
                .addQuoted(nameKey.getUuid())
                .progMemStruct().requiresExtern());
        return list;
    }

    protected void dealWithRequiredPlugins(List<CodePluginItem> generators, Path directory) throws TcMenuConversionException {
        logLine("Checking if any plugins have been removed from the project and need removal");

        var props = generators.stream().flatMap(gen ->  gen.getProperties().stream()).collect(Collectors.toList());

        var newPluginFileSet = generators.stream()
                .flatMap(gen -> gen.getRequiredSourceFiles().stream())
                .filter(sf -> sf.getApplicability().isApplicable(props))
                .map(RequiredSourceFile::getFileName)
                .collect(Collectors.toSet());

        for (var plugin : previousPluginFiles) {
            if (!newPluginFileSet.contains(plugin)) {
                var fileNamePart = Paths.get(plugin).getFileName().toString();
                var actualFile = directory.resolve(fileNamePart);
                try {
                    if (Files.exists(actualFile)) {
                        logLine("Removing unused plugin: " + actualFile);
                        Files.delete(actualFile);
                    }
                } catch (IOException e) {
                    logLine("Could not delete plugin: " + actualFile + " error " + e.getMessage());
                }
            }
        }

        logLine("Finding any required rendering / remote plugins to add to project");

        for (var gen : generators) {
            generatePluginsForCreator(gen, directory);
        }
    }

    protected void generatePluginsForCreator(CodePluginItem item, Path directory) throws TcMenuConversionException {
        var expando = new CodeParameter(null, true, "");
        var filteredSourceFiles = item.getRequiredSourceFiles().stream()
                .filter(sf-> sf.getApplicability().isApplicable(context.getProperties()))
                .collect(Collectors.toList());

        for (var srcFile : filteredSourceFiles) {
            try {
                var fileName = expando.expandExpression(context, srcFile.getFileName());
                // get the source (either from the plugin or from the tcMenu library)
                String fileNamePart;
                String fileData;
                Path location = item.getConfig().getPath().resolve(fileName);
                try (var sourceInputStream = new FileInputStream(location.toFile())) {
                    fileData = new String(sourceInputStream.readAllBytes());
                    fileNamePart = Paths.get(fileName).getFileName().toString();
                } catch (Exception e) {
                    throw new TcMenuConversionException("Unable to locate file in plugin: " + srcFile, e);
                }

                for (var cr : srcFile.getReplacementList()) {
                    if (cr.getApplicability().isApplicable(context.getProperties())) {
                        var replacement = StringHelper.escapeRex(expando.expandExpression(context, cr.getReplace()));
                        fileData = fileData.replaceAll(cr.getFind(), replacement);
                    }
                }

                // and copy into the destination
                Files.write(directory.resolve(fileNamePart), fileData.getBytes(), TRUNCATE_EXISTING, CREATE);
                logLine("Copied with replacement " + srcFile);
            } catch (Exception e) {
                throw new TcMenuConversionException("Unexpected exception processing " + srcFile, e);
            }
        }
    }

    @Override
    public void setLoggerFunction(Consumer<String> uiLogger) {
        this.uiLogger = uiLogger;
    }

    protected String getFirstMenuVariable(MenuTree menuTree) {
        return menuTree.getMenuItems(MenuTree.ROOT).stream().findFirst()
                .map(menuItem -> "menu" + menuNameFor(menuItem))
                .orElse("");
    }

    protected Collection<BuildStructInitializer> generateMenusInOrder(MenuTree menuTree) {
        List<MenuItem> root = menuTree.getMenuItems(MenuTree.ROOT);
        List<List<BuildStructInitializer>> itemsInOrder = renderMenu(menuTree, root);
        Collections.reverse(itemsInOrder);
        return itemsInOrder.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    protected List<List<BuildStructInitializer>> renderMenu(MenuTree menuTree, Collection<MenuItem> itemsColl) {
        ArrayList<MenuItem> items = new ArrayList<>(itemsColl);
        List<List<BuildStructInitializer>> itemsInOrder = new ArrayList<>(100);
        for (int i = 0; i < items.size(); i++) {

            if (items.get(i).hasChildren()) {
                int nextIdx = i + 1;
                String nextSub = (nextIdx < items.size()) ? menuNameFor(items.get(nextIdx)) : "NULL";

                List<MenuItem> childItems = menuTree.getMenuItems(items.get(i));
                String nextChild = (!childItems.isEmpty()) ? menuNameFor(childItems.get(0)) : "NULL";
                itemsInOrder.add(MenuItemHelper.visitWithResult(items.get(i),
                        new MenuItemToEmbeddedGenerator(menuNameFor(items.get(i)), nextSub, nextChild))
                        .orElse(Collections.emptyList()));
                itemsInOrder.addAll(renderMenu(menuTree, childItems));
            } else {
                int nextIdx = i + 1;
                String next = (nextIdx < items.size()) ? menuNameFor(items.get(nextIdx)) : "NULL";
                itemsInOrder.add(MenuItemHelper.visitWithResult(items.get(i),
                        new MenuItemToEmbeddedGenerator(menuNameFor(items.get(i)), next))
                        .orElse(Collections.emptyList()));
            }
        }
        return itemsInOrder;
    }

    protected Map<MenuItem, CallbackRequirement> callBackFunctions(MenuTree menuTree) {
        return menuTree.getAllSubMenus().stream()
                .flatMap(menuItem -> menuTree.getMenuItems(menuItem).stream())
                .filter(mi -> (!isStringEmptyOrNull(mi.getFunctionName())) || MenuItemHelper.isRuntimeStructureNeeded(mi))
                .map(i -> new CallbackRequirement(namingGenerator, i.getFunctionName(), i))
                .collect(Collectors.toMap(CallbackRequirement::getCallbackItem, cr -> cr));
    }

    protected String toSourceFile(Path directory, String ext) {
        Path file = directory.getFileName();
        if (file.toString().equals("src")) {
            // special case, go back one more level
            file = directory.getParent().getFileName();
        }
        return Paths.get(directory.toString(), file.toString() + ext).toString();
    }

    protected void logLine(String s) {
        if (uiLogger != null) uiLogger.accept(DATE_TIME_FORMATTER.format(Instant.now()) + " - " + s);
        logger.log(INFO, s);
    }

    protected void generateSource(List<CodePluginItem> generators, String cppFile,
                                  Collection<BuildStructInitializer> menuStructure,
                                  String projectName, CodeVariableExtractor extractor,
                                  Map<MenuItem, CallbackRequirement> callbackRequirements) throws TcMenuConversionException {

        try (Writer writer = new BufferedWriter(new FileWriter(cppFile))) {
            logLine("Writing out source CPP file: " + cppFile);

            writer.write(COMMENT_HEADER);

            writer.write("#include <tcMenu.h>");
            writer.write(LINE_BREAK);
            writer.write("#include \"" + projectName + "_menu.h\"");
            writer.write(LINE_BREAK);

            List<HeaderDefinition> includeList = getHeaderDefinitions(generators, menuStructure);

            // and write out the CPP includes, these are needed for things like adafruit fonts that must only be included once ever
            var includeDefs = extractor.mapCppIncludes(includeList);
            writer.write(includeDefs);
            writer.write(StringHelper.isStringEmptyOrNull(includeDefs) ? LINE_BREAK : TWO_LINES);

            writer.write("// Global variable declarations" + TWO_LINES);
            writer.write("const " + (usesProgMem ? "PROGMEM " : "") + " ConnectorLocalInfo applicationInfo = { \"" +
                    nameAndKey.getName() + "\", \"" + nameAndKey.getUuid() + "\" };");
            writer.write(TWO_LINES);
            writer.write(extractor.mapVariables(
                    generators.stream().flatMap(ecc -> ecc.getVariables().stream()).collect(Collectors.toList())
            ));

            var localCbReq = new HashMap<>(callbackRequirements);

            writer.write(TWO_LINES + "// Global Menu Item declarations" + TWO_LINES);
            StringBuilder toWrite = new StringBuilder(255);
            menuStructure.forEach(struct -> {
                var callback = localCbReq.remove(struct.getMenuItem());
                if (callback != null) {
                    var srcList = callback.generateSource();
                    if (!srcList.isEmpty()) {
                        toWrite.append(String.join(LINE_BREAK, srcList));
                        toWrite.append(LINE_BREAK);
                    }
                }
                toWrite.append(extractor.mapStructSource(struct));
                toWrite.append(LINE_BREAK);
            });
            writer.write(toWrite.toString());

            writer.write(LINE_BREAK + "// Set up code" + TWO_LINES);
            writer.write("void setupMenu() {" + LINE_BREAK);
            List<FunctionDefinition> readOnlyLocal = generateReadOnlyLocal();
            if (!readOnlyLocal.isEmpty()) {
                writer.write("    // Read only and local only function calls" + LINE_BREAK);
                writer.write(extractor.mapFunctions(readOnlyLocal));
                writer.write(LINE_BREAK + LINE_BREAK);
            }

            writer.write(extractor.mapFunctions(
                    generators.stream().flatMap(ecc -> ecc.getFunctions().stream()).collect(Collectors.toList())
            ));

            writer.write(LINE_BREAK + "}" + LINE_BREAK);
            writer.write(LINE_BREAK);

            logLine("Finished processing source file.");

        } catch (Exception e) {
            logLine("Failed to generate CPP: " + e.getMessage());
            throw new TcMenuConversionException("Header Generation failed", e);
        }

    }

    protected void generateHeaders(List<CodePluginItem> embeddedCreators,
                                   String headerFile, Collection<BuildStructInitializer> menuStructure,
                                   CodeVariableExtractor extractor,
                                   Map<MenuItem, CallbackRequirement> allCallbacks) throws TcMenuConversionException {
        try (Writer writer = new BufferedWriter(new FileWriter(headerFile))) {

            logLine("Writing out header file: " + headerFile);

            writer.write(COMMENT_HEADER);
            writer.write(platformIncludes());

            List<HeaderDefinition> includeList = getHeaderDefinitions(embeddedCreators, menuStructure);

            // and write out the includes
            writer.write(extractor.mapIncludes(includeList));

            writer.write(LINE_BREAK + LINE_BREAK + "// all define statements needed" + LINE_BREAK);

            // now get all the #defines that we need to add.
            writer.write(extractor.mapDefines());

            writer.write(LINE_BREAK + LINE_BREAK + "// all variables that need exporting" + LINE_BREAK);

            // and put the exports in the file too
            writer.write(extractor.mapExports(embeddedCreators.stream()
                    .flatMap(ecc -> ecc.getVariables().stream())
                    .filter(var -> var.getApplicability().isApplicable(context.getProperties()))
                    .collect(Collectors.toList())
            ));
            writer.write(LINE_BREAK + LINE_BREAK + "// all menu item forward references." + LINE_BREAK);

            writer.write(menuStructure.stream()
                    .map(extractor::mapStructHeader)
                    .filter(hdr -> !hdr.isEmpty())
                    .collect(Collectors.joining(LINE_BREAK))
            );
            writer.write(LINE_BREAK);
            writer.write("extern const ConnectorLocalInfo applicationInfo;");

            writer.write(TWO_LINES);

            writer.write("// Callback functions must always include CALLBACK_FUNCTION after the return type"
                    + LINE_BREAK + "#define CALLBACK_FUNCTION" + LINE_BREAK + LINE_BREAK);

            List<CallbackRequirement> callbackRequirements = new ArrayList<>(allCallbacks.values());
            callbackRequirements.sort((CallbackRequirement o1, CallbackRequirement o2) -> {
                if (o1.getCallbackName() == null && o2.getCallbackName() == null) return 0;
                if (o1.getCallbackName() == null) return -1;
                if (o2.getCallbackName() == null) return 1;
                return o1.getCallbackName().compareTo(o2.getCallbackName());
            });

            var callbacksDeclared = new HashSet<String>();
            for (CallbackRequirement callback : callbackRequirements) {
                var header = callback.generateHeader();
                if (!StringHelper.isStringEmptyOrNull(header) && !callbacksDeclared.contains(callback.getCallbackName())) {
                    writer.write(header + LINE_BREAK);
                    callbacksDeclared.add(callback.getCallbackName());
                }
            }

            writer.write(LINE_BREAK + "void setupMenu();" + LINE_BREAK);
            writer.write(LINE_BREAK + "#endif // MENU_GENERATED_CODE_H" + LINE_BREAK);

            logLine("Finished processing header file.");
        } catch (Exception e) {
            logLine("Failed to generate header file: " + e.getMessage());
            throw new TcMenuConversionException("Header Generation failed", e);
        }
    }

    private List<HeaderDefinition> getHeaderDefinitions(List<CodePluginItem> embeddedCreators, Collection<BuildStructInitializer> menuStructure) {
        // first get a list of includes to add to the header file from the creators
        var includeList = embeddedCreators.stream().flatMap(g -> g.getIncludeFiles().stream()).collect(Collectors.toList());

        // now add any extra headers needed for the menu structure items.
        includeList.addAll(menuStructure.stream()
                .flatMap(s -> s.getHeaderRequirements().stream())
                .collect(Collectors.toList()));
        return includeList;
    }

    protected abstract String platformIncludes();

}
