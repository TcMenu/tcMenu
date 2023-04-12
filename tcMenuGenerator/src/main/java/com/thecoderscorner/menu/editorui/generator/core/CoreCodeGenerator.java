/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.CurrentScrollPosition;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation;
import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.arduino.CallbackRequirement;
import com.thecoderscorner.menu.editorui.generator.arduino.MenuItemToEmbeddedGenerator;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeGeneratorCapable;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeParameter;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.EepromAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.eeprom.NoEepromDefinition;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.plugin.FunctionDefinition;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;
import com.thecoderscorner.menu.persist.PropertiesLocaleEnabledHandler;
import com.thecoderscorner.menu.persist.VersionInfo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.domain.CustomBuilderMenuItem.CustomMenuType.AUTHENTICATION;
import static com.thecoderscorner.menu.domain.CustomBuilderMenuItem.CustomMenuType.REMOTE_IOT_MONITOR;
import static com.thecoderscorner.menu.domain.ScrollChoiceMenuItem.ScrollChoiceMode.ARRAY_IN_EEPROM;
import static com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation.*;
import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType;
import static com.thecoderscorner.menu.editorui.util.StringHelper.isStringEmptyOrNull;
import static java.lang.System.Logger.Level.*;

public abstract class CoreCodeGenerator implements CodeGenerator {
    public static final String GENERATED_LOCAL_HEADER = "// TcMenu Generated Locale File, do not edit this file.";
    protected final System.Logger logger = System.getLogger(getClass().getSimpleName());
    public static final String LINE_BREAK = System.getProperty("line.separator");
    public static final String TWO_LINES = LINE_BREAK + LINE_BREAK;
    public static final String NO_REMOTE_ID = "2c101fec-1f7d-4ff3-8d2b-992ad41e7fcb";

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
    protected BiConsumer<System.Logger.Level, String> uiLogger = null;
    protected MenuTree menuTree;
    protected List<String> previousPluginFiles = List.of();
    protected boolean usesProgMem;
    protected CodeConversionContext context;
    protected VariableNameGenerator namingGenerator;
    private LocaleMappingHandler localeHandler;
    protected boolean hasRemotePlugins;
    protected CodeGeneratorOptions options;
    private final AtomicInteger logEntryNum = new AtomicInteger(0);

    public CoreCodeGenerator(SketchFileAdjuster adjuster, ArduinoLibraryInstaller installer, EmbeddedPlatform embeddedPlatform) {
        this.installer = installer;
        this.sketchAdjuster = adjuster;
        this.embeddedPlatform = embeddedPlatform;
    }

    public boolean startConversion(Path directory, List<CodePluginItem> codeGenerators, MenuTree menuTree,
                                   List<String> previousPluginFiles, CodeGeneratorOptions options,
                                   LocaleMappingHandler handler) {
        this.menuTree = menuTree;
        this.options = options;
        this.localeHandler = handler;
        namingGenerator = new VariableNameGenerator(menuTree, options.isNamingRecursive());
        this.previousPluginFiles = previousPluginFiles;
        logLine(INFO, "Starting " + embeddedPlatform.getBoardId() + " generate into : " + directory);

        hasRemotePlugins = codeGenerators.stream()
                .anyMatch(p -> p.getSubsystem() == SubSystem.REMOTE && !p.getId().equals(NO_REMOTE_ID));

        usesProgMem = embeddedPlatform.isUsesProgmem();

        try {
            Path srcDir = directory;
            ProjectSaveLocation psl = options.getSaveLocation();
            if (psl == ALL_TO_SRC || psl == PROJECT_TO_SRC_WITH_GENERATED) {
                srcDir = directory.resolve("src");
            }

            boolean genDir = isWritingToGeneratedDir(psl);
            createTheSourceDirectoriesIfNeeded(srcDir, genDir);

            String cppFile = toSourceFile(srcDir, "_menu.cpp", genDir);
            String headerFile = toSourceFile(srcDir, "_menu.h", genDir);
            String projectName = directory.getFileName().toString();

            // Prepare the generator by initialising all the structures ready for conversion.
            String root = getFirstMenuVariable(menuTree);
            var allProps = codeGenerators.stream().flatMap(gen -> gen.getProperties().stream()).collect(Collectors.toList());
            context = new CodeConversionContext(embeddedPlatform, root, options, allProps);
            var extractor = new CodeVariableCppExtractor(context, usesProgMem);

            Collection<BuildStructInitializer> menuStructure = generateMenusInOrder(menuTree);

            // generate the source by first generating the CPP and H for the menu definition and then
            // update the sketch. Also, if any plugins have changed, then update them.
            Map<MenuItem, CallbackRequirement> callbackFunctions = callBackFunctions(menuTree);
            generateHeaders(codeGenerators, headerFile, menuStructure, extractor, projectName, callbackFunctions);
            generateSource(codeGenerators, cppFile, menuStructure, projectName, extractor, callbackFunctions);
            var fileProcessor = new PluginRequiredFileProcessor(context, this::logLine);
            fileProcessor.dealWithRequiredPlugins(codeGenerators, srcDir, directory, psl,previousPluginFiles);

            if(localeHandler.isLocalSupportEnabled()) {
                processLocale(srcDir, genDir);
            }

            internalConversion(directory, srcDir, callbackFunctions, projectName);

            doSanityChecks();

            logLine(INFO, "Process has completed, make sure the code in your IDE is up-to-date.");
            logLine(INFO, "You may need to close the project and then re-open it to pick up changes..");
        } catch (Exception e) {
            logLine(ERROR, "ERROR during conversion---------------------------------------------");
            logLine(ERROR, "The conversion process has failed with an error: " + e.getMessage());
            logLine(ERROR, "A more complete error can be found in the log file in <Home>/.tcMenu");
            logger.log(ERROR, "Exception caught while converting code: ", e);
        }

        return true;
    }

    private static boolean isWritingToGeneratedDir(ProjectSaveLocation psl) {
        boolean genDir = psl == PROJECT_TO_CURRENT_WITH_GENERATED || psl == PROJECT_TO_SRC_WITH_GENERATED;
        return genDir;
    }

    private void processLocale(Path srcDir, boolean generated) throws IOException {
        logLine(DEBUG, "Starting locale processing");
        StringBuilder langSelectText = new StringBuilder(8192);
        langSelectText.append(GENERATED_LOCAL_HEADER).append(LINE_BREAK);
        langSelectText.append("// This is the header to include. Set TC_LOCAL_?? to a locale").append(LINE_BREAK);
        langSelectText.append("// or omit for the default language").append(TWO_LINES);

        var previousLocale = localeHandler.getCurrentLocale();

        try {
            String defaultLocaleFile = toSourceFile(srcDir, "_lang" + ".h", generated);
            localeHandler.changeLocale(PropertiesLocaleEnabledHandler.DEFAULT_LOCALE);
            var defaultLocaleMap = localeHandler.getUnderlyingMap();
            localeToCpp(defaultLocaleFile, PropertiesLocaleEnabledHandler.DEFAULT_LOCALE, defaultLocaleMap);
            boolean useElIf = false;

            for (var locale : localeHandler.getEnabledLocales().stream().filter(l -> !l.getLanguage().equals("")).toList()) {
                String localeFile = toSourceFile(srcDir, "_lang_" + locale.toString() + ".h", generated);
                localeToCpp(localeFile, locale, defaultLocaleMap);

                if (useElIf) {
                    langSelectText.append("#elif");
                } else {
                    langSelectText.append("#if");
                    useElIf = true;
                }
                langSelectText.append(" defined(TC_LOCALE_").append(locale.toString().toUpperCase()).append(')')
                        .append(LINE_BREAK);
                langSelectText.append("# include \"").append(Paths.get(localeFile).getFileName()).append("\"").append(LINE_BREAK);
            }

            Path defPath = Paths.get(defaultLocaleFile);
            if (useElIf) {
                langSelectText.append("#else").append(LINE_BREAK).append("#include \"")
                        .append(defPath.getFileName()).append("\"").append(LINE_BREAK)
                        .append("#endif").append(LINE_BREAK);
            } else {
                langSelectText.append("#include \"").append(defPath.getFileName()).append("\"").append(LINE_BREAK);
            }

            var selFile = Paths.get(toSourceFile(srcDir, "_langSelect" + ".h", generated));
            Files.writeString(selFile, langSelectText.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            logLine(INFO, "Wrote out the language selector - " + selFile);
            logLine(DEBUG, "Finished locale processing");
        }
        finally {
            // put back the same locale as was selected before.
            localeHandler.changeLocale(previousLocale);
        }
    }

    private void localeToCpp(String localeOutputFile, Locale locale, Map<String, String> defaultLocaleMap) throws IOException {
        StringBuilder sb = new StringBuilder(8192);
        sb.append(GENERATED_LOCAL_HEADER).append(" Locale ").append(locale).append(LINE_BREAK);
        sb.append("// Never include directly, always include the langSelect header").append(TWO_LINES);

        localeHandler.changeLocale(locale);
        var allEntries = localeHandler.getUnderlyingMap();
        for(var entry : defaultLocaleMap.entrySet()) {
            var value = (allEntries.containsKey(entry.getKey())) ? allEntries.get(entry.getKey()) : entry.getValue();
            sb.append("#define ").append(asDefine(entry.getKey())).append(" \"").append(value).append("\"");
            sb.append(LINE_BREAK);
        }
        Files.writeString(Path.of(localeOutputFile), sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        logLine(INFO, "Created locale file " + localeOutputFile + " for " + locale);
    }

    public static String asDefine(String key) {
        var words = key.split("[^A-Za-z0-9]+");
        return "TC_I18N_" + Arrays.stream(words).map(String::toUpperCase).collect(Collectors.joining("_"));
    }

    private void createTheSourceDirectoriesIfNeeded(Path srcDir, boolean genDir) throws IOException {
        if(genDir) {
            Path genPath = srcDir.resolve("generated");
            if(!Files.exists(genPath)) {
                Files.createDirectories(genPath);
            }
        } else {
            if (!Files.exists(srcDir)) {
                Files.createDirectories(srcDir);
            }
        }
    }


    private List<FunctionDefinition> generateReadOnlyLocal() {
        var allFunctions = new ArrayList<FunctionDefinition>();

        allFunctions.addAll(menuTree.getAllMenuItems().stream().filter(MenuItem::isReadOnly)
                .map(item -> {
                    var params = List.of(new CodeParameter(CodeParameter.NO_TYPE, null, true, "true"));
                    return new FunctionDefinition("setReadOnly", "menu" + menuNameFor(item), false, false, params, new AlwaysApplicable());
                }).toList()
        );

        allFunctions.addAll(menuTree.getAllMenuItems().stream().filter(MenuItem::isLocalOnly)
                .map(item -> {
                    var params = List.of(new CodeParameter(CodeParameter.NO_TYPE, null, true, "true"));
                    return new FunctionDefinition("setLocalOnly", "menu" + menuNameFor(item), false, false, params, new AlwaysApplicable());
                }).toList()
        );

        allFunctions.addAll(menuTree.getAllMenuItems().stream().filter(this::isSecureSubMenu)
                .map(item -> {
                    var params = List.of(new CodeParameter(CodeParameter.NO_TYPE, null, true, "true"));
                    return new FunctionDefinition("setSecured", "menu" + menuNameFor(item), false, false, params, new AlwaysApplicable());
                }).toList()
        );

        // lastly we deal with any INVISIBLE items, visible is the default.
        allFunctions.addAll(menuTree.getAllMenuItems().stream().filter((item) -> !item.isVisible())
                .map(item -> {
                    var params = List.of(new CodeParameter(CodeParameter.NO_TYPE, null, true, "false"));
                    return new FunctionDefinition("setVisible", "menu" + menuNameFor(item), false, false, params, new AlwaysApplicable());
                }).toList()
        );

        allFunctions.addAll(menuTree.getAllMenuItems().stream().filter(item -> item instanceof AnalogMenuItem an && an.getStep() > 1)
                .map(item -> {
                    var analogMenuItem = (AnalogMenuItem) item;
                    var params = List.of(new CodeParameter(CodeParameter.NO_TYPE, null, true, Integer.toString(analogMenuItem.getStep())));
                    return new FunctionDefinition("setStep", "menu" + menuNameFor(item), false, false, params, new AlwaysApplicable());
                }).toList()
        );

        return allFunctions;
    }

    private String menuNameFor(MenuItem item) {
        if (StringHelper.isStringEmptyOrNull(item.getVariableName())) {
            return namingGenerator.makeNameToVar(item);
        } else return item.getVariableName();
    }

    private boolean isSecureSubMenu(MenuItem toCheck) {
        SubMenuItem item = MenuItemHelper.asSubMenu(toCheck);
        return item != null && item.isSecured();
    }


    @Override
    public void setLoggerFunction(BiConsumer<System.Logger.Level, String> uiLogger) {
        this.uiLogger = uiLogger;
    }

    @Override
    public SketchFileAdjuster getSketchFileAdjuster() {
        return sketchAdjuster;
    }

    protected String getFirstMenuVariable(MenuTree menuTree) {
        return menuTree.getMenuItems(MenuTree.ROOT).stream().findFirst()
                .map(menuItem -> "menu" + menuNameFor(menuItem))
                .orElse("");
    }

    protected Collection<BuildStructInitializer> generateMenusInOrder(MenuTree menuTree) throws TcMenuConversionException {
        List<MenuItem> root = menuTree.getMenuItems(MenuTree.ROOT);
        List<List<BuildStructInitializer>> itemsInOrder = renderMenu(menuTree, root);
        Collections.reverse(itemsInOrder);
        return itemsInOrder.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    protected List<List<BuildStructInitializer>> renderMenu(MenuTree menuTree, Collection<MenuItem> itemsColl) throws TcMenuConversionException {
        ArrayList<MenuItem> items = new ArrayList<>(itemsColl);
        List<List<BuildStructInitializer>> itemsInOrder = new ArrayList<>(100);
        for (int i = 0; i < items.size(); i++) {

            MenuItem item = items.get(i);
            if (item.hasChildren()) {
                int nextIdx = i + 1;
                String nextSub = (nextIdx < items.size()) ? menuNameFor(items.get(nextIdx)) : "NULL";

                List<MenuItem> childItems = menuTree.getMenuItems(item);
                String nextChild = (!childItems.isEmpty()) ? menuNameFor(childItems.get(0)) : "NULL";
                itemsInOrder.add(MenuItemHelper.visitWithResult(item,
                                new MenuItemToEmbeddedGenerator(menuNameFor(item), nextSub, nextChild,
                                        false, localeHandler))
                        .orElse(Collections.emptyList()));
                itemsInOrder.addAll(renderMenu(menuTree, childItems));
            } else {
                int nextIdx = i + 1;
                Object defVal = MenuItemHelper.getValueFor(item, menuTree, MenuItemHelper.getDefaultFor(item));
                String next = (nextIdx < items.size()) ? menuNameFor(items.get(nextIdx)) : "NULL";
                itemsInOrder.add(MenuItemHelper.visitWithResult(item,
                                new MenuItemToEmbeddedGenerator(menuNameFor(item), next, null,
                                        toEmbeddedCppValue(item, defVal), localeHandler))
                        .orElse(Collections.emptyList()));
            }
        }
        return itemsInOrder;
    }

    private String toEmbeddedCppValue(MenuItem item, Object defaultValue) throws TcMenuConversionException {
        if (defaultValue instanceof BigDecimal bd && item instanceof EditableLargeNumberMenuItem lge) {
            boolean neg = bd.doubleValue() < 0.0;
            long whole = Math.abs(bd.longValue());
            long fraction = (long) (((Math.abs(bd.doubleValue()) - (double) whole) + 0.0000001) * (Math.pow(10, lge.getDecimalPlaces())));
            return String.format("LargeFixedNumber(%d, %d, %dU, %dU, %s)", lge.getDigitsAllowed(), lge.getDecimalPlaces(),
                    whole, fraction, neg);
        } else if (defaultValue instanceof String s && item instanceof EditableTextMenuItem tmi) {
            return toEmbeddedCppTextValue(tmi, s);
        } else if (defaultValue instanceof String) {
            return "\"" + defaultValue + "\"";
        } else if (defaultValue instanceof PortableColor c && item instanceof Rgb32MenuItem rgbItem) {
            if (rgbItem.isIncludeAlphaChannel()) {
                return String.format("RgbColor32(%d, %d, %d, %d)", c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
            } else {
                return String.format("RgbColor32(%d, %d, %d)", c.getRed(), c.getGreen(), c.getBlue());
            }
        } else if (defaultValue instanceof CurrentScrollPosition sc && item instanceof ScrollChoiceMenuItem) {
            return Integer.toString(sc.getPosition());
        } else {
            return Objects.toString(defaultValue);
        }
    }

    private String toEmbeddedCppTextValue(EditableTextMenuItem tmi, String s) throws TcMenuConversionException {
        switch (tmi.getItemType()) {
            case PLAIN_TEXT -> {
                return '\"' + s + '\"';
            }
            case IP_ADDRESS -> {
                var pattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)");
                var matcher = pattern.matcher(s);
                if (matcher.matches() && matcher.groupCount() == 4) {
                    return String.format("IpAddressStorage(%s, %s, %s, %s)", matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4));
                } else {
                    return "IpAddressStorage(127, 0, 0, 1)";
                }
            }
            case TIME_24H, TIME_12H, TIME_24_HUNDREDS, TIME_DURATION_SECONDS, TIME_DURATION_HUNDREDS, TIME_24H_HHMM, TIME_12H_HHMM -> {
                var pattern = Pattern.compile("(\\d+):(\\d+):(\\d+)(:.\\d+)*");
                var matcher = pattern.matcher(s);
                if (matcher.matches()) {
                    var hundreds = (matcher.groupCount() == 4 && matcher.group(4) != null) ? matcher.group(4).substring(1) : "0";
                    return String.format("TimeStorage(%s, %s, %s, %s)", matcher.group(1), matcher.group(2), matcher.group(3), hundreds);
                } else if (matcher.matches() && matcher.groupCount() == 3) {
                    return String.format("TimeStorage(%s, %s, %s, 0)", matcher.group(1), matcher.group(2), matcher.group(3));
                } else {
                    return "TimeStorage(0, 0, 0, 0)";
                }
            }
            case GREGORIAN_DATE -> {
                var pattern = Pattern.compile("(\\d+)/(\\d+)/(\\d+)");
                var matcher = pattern.matcher(s);
                if (matcher.matches() && matcher.groupCount() == 3) {
                    return String.format("DateStorage(%s, %s, %s)", matcher.group(3), matcher.group(2), matcher.group(1));
                } else {
                    return "DateStorage(1, 1, 2020)";
                }
            }
        }
        throw new TcMenuConversionException("Unexpected and unhandled edit type on " + tmi);
    }

    protected Map<MenuItem, CallbackRequirement> callBackFunctions(MenuTree menuTree) {
        return menuTree.getAllSubMenus().stream()
                .flatMap(menuItem -> menuTree.getMenuItems(menuItem).stream())
                .filter(mi -> (!isStringEmptyOrNull(mi.getFunctionName())) || MenuItemHelper.isRuntimeStructureNeeded(mi))
                .map(i -> new CallbackRequirement(namingGenerator, i.getFunctionName(), i, localeHandler))
                .collect(Collectors.toMap(CallbackRequirement::getCallbackItem, cr -> cr));
    }

    public static String toSourceFile(Path directory, String ext, boolean generated) {
        Path file = directory.getFileName();
        if (file.toString().equals("src")) {
            // special case, go back one more level
            file = directory.getParent().getFileName();
        }
        if(generated) {
            directory = directory.resolve("generated");
        }
        return Paths.get(directory.toString(), file.toString() + ext).toString();
    }

    protected void logLine(System.Logger.Level level, String s) {
        var ent = logEntryNum.incrementAndGet();
        if (uiLogger != null) uiLogger.accept(level, ent + " - " + s);
        logger.log(INFO, s);
    }

    protected void generateSource(List<CodePluginItem> generators, String cppFile,
                                  Collection<BuildStructInitializer> menuStructure,
                                  String projectName, CodeVariableExtractor extractor,
                                  Map<MenuItem, CallbackRequirement> callbackRequirements) throws TcMenuConversionException {

        try (Writer writer = new BufferedWriter(new FileWriter(cppFile))) {
            logLine(INFO, "Writing out source CPP file: " + cppFile);

            writer.write(COMMENT_HEADER);

            writer.write("#include <tcMenu.h>" + LINE_BREAK);
            writer.write("#include \"" + projectName + "_menu.h\"" + LINE_BREAK);

            List<HeaderDefinition> includeList = getHeaderDefinitions(generators, menuStructure);

            // and write out the CPP includes, these are needed for things like adafruit fonts that must only be included once ever
            var includeDefs = extractor.mapCppIncludes(includeList);
            writer.write(includeDefs);
            writer.write(StringHelper.isStringEmptyOrNull(includeDefs) ? LINE_BREAK : TWO_LINES);

            writer.write("// Global variable declarations");
            writer.write(LINE_BREAK);
            writer.write("const " + (usesProgMem ? "PROGMEM " : "") + " ConnectorLocalInfo applicationInfo = { " +
                    getApplicationName() + ", \"" + options.getApplicationUUID().toString() + "\" };");
            writer.write(LINE_BREAK);
            if (hasRemotePlugins && requiresGlobalServerDefinition()) {
                writer.write("TcMenuRemoteServer remoteServer(applicationInfo);");
                writer.write(LINE_BREAK);
            }

            writer.write(extraCodeDefinitions().stream()
                    .map(CodeGeneratorCapable::generateGlobal)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.joining(LINE_BREAK))
            );
            writer.write(LINE_BREAK);

            writer.write(extractor.mapVariables(
                    generators.stream().flatMap(ecc -> ecc.getVariables().stream()).collect(Collectors.toList())
            ));

            var localCbReq = new HashMap<>(callbackRequirements);

            writer.write(TWO_LINES + "// Global Menu Item declarations");
            writer.write(LINE_BREAK);
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

            writer.write(LINE_BREAK);
            writer.write("void setupMenu() {");
            writer.write(LINE_BREAK);

            writer.write("    // First we set up eeprom and authentication (if needed)." + LINE_BREAK);
            writer.write(extraCodeDefinitions().stream()
                    .map(CodeGeneratorCapable::generateCode)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.joining(LINE_BREAK))
            );
            writer.write(LINE_BREAK);


            List<FunctionDefinition> readOnlyLocal = generateReadOnlyLocal();
            if (!readOnlyLocal.isEmpty()) {
                writer.write("    // Now add any readonly, non-remote and visible flags." + LINE_BREAK);
                writer.write(extractor.mapFunctions(readOnlyLocal));
                writer.write(LINE_BREAK + LINE_BREAK);
            }

            writer.write("    // Code generated by plugins." + LINE_BREAK);
            writer.write(extractor.mapFunctions(
                    generators.stream().flatMap(ecc -> ecc.getFunctions().stream()).collect(Collectors.toList())
            ));

            if (hasRemotePlugins && requiresGlobalServerDefinition()) {
                menuTree.getAllMenuItems().stream()
                        .filter(menuItem -> menuItem instanceof CustomBuilderMenuItem custom && custom.getMenuType() == REMOTE_IOT_MONITOR)
                        .findFirst()
                        .ifPresent(item -> {
                            try {
                                writer.write(TWO_LINES + "    // We have an IoT monitor, register the server" + LINE_BREAK);
                                writer.write("    menu" + namingGenerator.makeNameToVar(item) + ".setRemoteServer(remoteServer);");
                            } catch (IOException e) {
                                logLine(ERROR, "Exception writing cpp file: remote server on IoT monitor");
                                logger.log(ERROR, "Exception during write of remote server block", e);
                            }
                        });

                menuTree.getAllMenuItems().stream()
                        .filter(menuItem -> menuItem instanceof CustomBuilderMenuItem custom && custom.getMenuType() == AUTHENTICATION)
                        .findFirst()
                        .ifPresent(item -> {
                            try {
                                writer.write(TWO_LINES + "    // We have an EEPROM authenticator, it needs initialising" + LINE_BREAK);
                                writer.write("    menu" + namingGenerator.makeNameToVar(item) + ".init();");
                            } catch (IOException e) {
                                logLine(ERROR, "Exception writing cpp file: remote server on EEPROM Authenticator init");
                                logger.log(ERROR, "Exception during write of authenticator init block", e);
                            }
                        });
            }


            writer.write(LINE_BREAK + "}" + LINE_BREAK);
            writer.write(LINE_BREAK);

            logLine(INFO, "Finished processing source file.");

        } catch (Exception e) {
            logLine(ERROR, "Failed to generate CPP: " + e.getMessage());
            throw new TcMenuConversionException("Header Generation failed", e);
        }

    }

    private String getApplicationName() {
        if(localeHandler.isLocalSupportEnabled() && options.getApplicationName().startsWith("%")) {
            return asDefine(options.getApplicationName().substring(1));
        } else {
            return "\"" + options.getApplicationName() + "\"";
        }
    }

    private boolean requiresGlobalServerDefinition() {
        try {
            var pluginVer = installer.getVersionOfLibrary("core-remote", InstallationType.CURRENT_PLUGIN);
            var twoPointTwo = VersionInfo.fromString("2.2.0");
            return pluginVer.isSameOrNewerThan(twoPointTwo);
        } catch (IOException e) {
            logger.log(ERROR, "Cannot determine tcMenu library version, assume > 2.2.0");
            return true;
        }
    }

    protected void generateHeaders(List<CodePluginItem> embeddedCreators,
                                   String headerFile, Collection<BuildStructInitializer> menuStructure,
                                   CodeVariableExtractor extractor, String projectName,
                                   Map<MenuItem, CallbackRequirement> allCallbacks) throws TcMenuConversionException {
        try (Writer writer = new BufferedWriter(new FileWriter(headerFile))) {

            logLine(INFO, "Writing out header file: " + headerFile);

            writer.write(COMMENT_HEADER);
            writer.write(platformIncludes());

            List<HeaderDefinition> includeList = getHeaderDefinitions(embeddedCreators, menuStructure);

            // and write out the includes
            writer.write(extractor.mapIncludes(includeList));
            if(localeHandler.isLocalSupportEnabled()) {
                writer.write(LINE_BREAK + "#include \"" + projectName + "_langSelect.h\"");
            }

            writer.write(TWO_LINES);
            writer.write("// variables we declare that you may need to access" + LINE_BREAK);
            writer.write("extern const PROGMEM ConnectorLocalInfo applicationInfo;");
            writer.write(LINE_BREAK);

            if (hasRemotePlugins && requiresGlobalServerDefinition()) {
                writer.write("extern TcMenuRemoteServer remoteServer;" + LINE_BREAK);
            }
            // and put the exports in the file too
            writer.write(extractor.mapExports(embeddedCreators.stream()
                    .flatMap(ecc -> ecc.getVariables().stream())
                    .filter(var -> var.getApplicability().isApplicable(context.getProperties()))
                    .collect(Collectors.toList())
            ));
            writer.write(TWO_LINES);
            writer.write("// Any externals needed by IO expanders, EEPROMs etc");
            writer.write(LINE_BREAK);
            writer.write(extraCodeDefinitions().stream()
                    .map(CodeGeneratorCapable::generateExport)
                    .filter(Optional::isPresent).map(Optional::get)
                    .collect(Collectors.joining(LINE_BREAK)));
            writer.write(TWO_LINES);

            writer.write("// Global Menu Item exports" + LINE_BREAK);
            writer.write(menuStructure.stream()
                    .map(extractor::mapStructHeader)
                    .filter(hdr -> !hdr.isEmpty())
                    .collect(Collectors.joining(LINE_BREAK))
            );

            writer.write(TWO_LINES);

            writer.write("// Provide a wrapper to get hold of the root menu item and export setupMenu");
            writer.write(LINE_BREAK);
            writer.write("inline MenuItem& rootMenuItem() { return " + context.getRootObject() + "; }");
            writer.write(LINE_BREAK);
            writer.write("void setupMenu();");
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

            writer.write(LINE_BREAK + "#endif // MENU_GENERATED_CODE_H" + LINE_BREAK);

            logLine(INFO, "Finished processing header file.");
        } catch (Exception e) {
            logLine(ERROR, "Failed to generate header file: " + e.getMessage());
            throw new TcMenuConversionException("Header Generation failed", e);
        }
    }

    private List<HeaderDefinition> getHeaderDefinitions(List<CodePluginItem> embeddedCreators, Collection<BuildStructInitializer> menuStructure) {
        // first get a list of includes to add to the header file from the creators
        var includeList = embeddedCreators.stream().flatMap(g -> g.getIncludeFiles().stream()).collect(Collectors.toList());

        // now add any extra headers needed for the menu structure items.
        includeList.addAll(menuStructure.stream()
                .flatMap(s -> s.getHeaderRequirements().stream()).toList());

        includeList.addAll(extraCodeDefinitions().stream()
                .map(CodeGeneratorCapable::generateHeader)
                .filter(Optional::isPresent)
                .map(Optional::get).toList());

        return includeList;
    }

    private Collection<CodeGeneratorCapable> extraCodeDefinitions() {
        var extraDefs = new ArrayList<CodeGeneratorCapable>(options.getExpanderDefinitions().getAllExpanders());
        extraDefs.add(new SizeBasedEEPROMCodeCapable(options.isUsingSizedEEPROMStorage()));
        extraDefs.add(options.getEepromDefinition());
        extraDefs.add(options.getAuthenticatorDefinition());
        return extraDefs;
    }

    protected void doSanityChecks() {
        boolean eepromAuthenticator = options.getAuthenticatorDefinition() instanceof EepromAuthenticatorDefinition;
        boolean noEeprom = options.getEepromDefinition() instanceof NoEepromDefinition;
        boolean errorFound = false;

        if (!options.getMenuInMenuCollection().getAllDefinitions().isEmpty() &&
                !options.getEmbeddedPlatform().equals(EmbeddedPlatform.RASPBERRY_PIJ.getBoardId())) {
            logLine(ERROR, "Menu In Menu is only supported with embedded Java (Raspberry PI / Linux)");
            errorFound = true;
        }

        if (noEeprom && eepromAuthenticator) {
            logLine(ERROR, "You have selected No EEPROM but then used an EEPROM based authenticator.");
            errorFound = true;
        }

        Collection<MenuItem> allItems = menuTree.getAllMenuItems();

        if (allItems.isEmpty()) {
            logLine(ERROR, "The menu tree is empty, this is not supported, please add at least one item.");
            errorFound = true;
        }

        if (noEeprom && allItems.stream().anyMatch(mt -> mt instanceof ScrollChoiceMenuItem sc && sc.getChoiceMode() == ARRAY_IN_EEPROM)) {
            logLine(ERROR, "You have included a scroll choice EEPROM item but have not configured an EEPROM.");
            errorFound = true;
        }

        if (!eepromAuthenticator && allItems.stream().anyMatch(mt -> mt instanceof CustomBuilderMenuItem ci && ci.getMenuType() == AUTHENTICATION)) {
            logLine(ERROR, "You have included an EEPROM authentication menu item without using EEPROM authentication.");
            errorFound = true;
        }

        if (errorFound) {
            logLine(ERROR, "It is highly likely that your menu will not work as expected, please fix any errors before deploying.");
        }
    }

    public void internalConversion(Path directory, Path srcDir, Map<MenuItem, CallbackRequirement> callbackFunctions,
                                   String projectName) throws TcMenuConversionException {

        try {
            var inoFile = sketchAdjuster.createFileIfNeeded(this::logLine, directory, options);
            logLine(INFO, "Making adjustments to " + inoFile);
            sketchAdjuster.makeAdjustments(this::logLine, directory, inoFile.toString(), projectName, callbackFunctions.values(), menuTree);
        } catch (IOException e) {
            logger.log(ERROR, "Sketch modification failed", e);
            throw new TcMenuConversionException("Could not modify sketch", e);
        }

        // do a couple of final checks and put out warnings if need be
    }

    protected abstract String platformIncludes();

}
