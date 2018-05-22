/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.CodeGenerator;
import com.thecoderscorner.menu.editorui.generator.CppAndHeader;
import com.thecoderscorner.menu.editorui.generator.EmbeddedCodeCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
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

import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoItemGenerator.LINE_BREAK;
import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoItemGenerator.makeNameToVar;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ArduinoGenerator implements CodeGenerator {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault());
    private final Consumer<String> uiLogger;
    private final Path directory;
    private final MenuTree menuTree;
    private List<EmbeddedCodeCreator> generators;

    public ArduinoGenerator(Consumer<String> logger, Path directory, List<EmbeddedCodeCreator> generators,
                            MenuTree menuTree) {
        this.uiLogger = logger;
        this.directory = directory;
        this.menuTree = menuTree;
        this.generators = generators;
    }

    @Override
    public boolean startConversion() {
        logLine("Starting Arduino generate: " + directory);

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setTemplateMode(TemplateMode.TEXT);
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);

        String inoFile = toSourceFile(directory, ".ino");
        String cppFile = toSourceFile(directory, ".cpp");
        String headerFile = toSourceFile(directory, ".h");
        String projectName = directory.getFileName().toString();

        Path source = Paths.get(inoFile);
        if (Files.exists(source)) {
            try {
                logLine("INO Previously existed, backup existing file");
                Files.copy(source, Paths.get(source.toString() + ".backup"), REPLACE_EXISTING);
            } catch (IOException e) {
                logLine("Failed to backup file:" + e.getMessage());
                logger.error("Backup failed - WILL NOT CONTINUE", e);
                return false;
            }
        }

        String root = getFirstMenuVariable(menuTree);

        try (Writer writer = new FileWriter(headerFile)) {
            logLine("Writing out header file: " + headerFile);
            Context context = new Context(Locale.getDefault());
            context.setVariable("allGeneratorIncludes", generators.stream()
                    .flatMap(g -> g.getIncludes().stream())
                    .collect(Collectors.toList()));
            context.setVariable("allGeneratorExports", generators.stream()
                    .map(EmbeddedCodeCreator::getExportDefinitions)
                    .collect(Collectors.joining(LINE_BREAK)));
            context.setVariable("menuItems", generateMenusInOrder(menuTree));
            context.setVariable("callbacks", callBackFunctions());
            engine.process("/generator/template.h", context, writer);
            logLine("Finished processing header file.");
        } catch (Exception e) {
            logLine("Failed to generate header file: " + e.getMessage());
            logger.error("Header Code Generation failed", e);
            return false;
        }

        try (Writer writer = new FileWriter(cppFile)) {
            logLine("Writing out source CPP file: " + cppFile);
            Context context = new Context(Locale.getDefault());
            context.setVariable("projectName", projectName);
            context.setVariable("allGlobals", generators.stream()
                    .map(EmbeddedCodeCreator::getGlobalVariables)
                    .collect(Collectors.joining(LINE_BREAK)));
            context.setVariable("allSetups", generators.stream()
                    .map(ecc -> ecc.getSetupCode(root))
                    .collect(Collectors.joining(LINE_BREAK)));
            context.setVariable("menuItems", generateMenusInOrder(menuTree));
            context.setVariable("callbacks", callBackFunctions());
            engine.process("/generator/template.cpp", context, writer);
            logLine("Finished processing source file.");

            logLine("Making adjustments to " + inoFile);
        } catch (Exception e) {
            logLine("Failed to generate CPP: " + e.getMessage());
            logger.error("CPP Code Generation failed", e);
            return false;
        }

        try {
            ArduinoSketchFileAdjuster adjuster = new ArduinoSketchFileAdjuster(
                    this::logLine,
                    inoFile,
                    projectName,
                    callBackFunctions());
            adjuster.makeAdjustments();
        } catch (IOException e) {
            logLine("Failed to make changes to sketch" +  e.getMessage());
            logger.error("Sketch modification failed", e);
        }

        logLine("Process has completed, make sure the code in your IDE is up-to-date.");
        logLine("You may need to close the project and then re-open it to pick up changes..");

        return true;

    }

    private String getFirstMenuVariable(MenuTree menuTree) {
        return menuTree.getMenuItems(MenuTree.ROOT).stream().findFirst()
                .map(menuItem -> "menu" + makeNameToVar(menuItem.getName()))
                .orElse("");
    }

    private Collection<CppAndHeader> generateMenusInOrder(MenuTree menuTree) {
        ImmutableList<MenuItem> root = menuTree.getMenuItems(MenuTree.ROOT);
        List<CppAndHeader> itemsInOrder = renderMenu(root);
        Collections.reverse(itemsInOrder);
        return itemsInOrder;
    }

    private List<String> callBackFunctions() {
        return menuTree.getAllSubMenus().stream()
                .flatMap(menuItem -> menuTree.getMenuItems(menuItem).stream())
                .filter(menuItem -> !Strings.isNullOrEmpty(menuItem.getFunctionName()))
                .map(MenuItem::getFunctionName)
                .collect(Collectors.toList());

    }

    private List<CppAndHeader> renderMenu(Collection<MenuItem> itemsColl) {
        ArrayList<MenuItem> items = new ArrayList<>(itemsColl);
        List<CppAndHeader> itemsInOrder = new ArrayList<>(100);
        for (int i = 0; i < items.size(); i++) {

            if (items.get(i).hasChildren()) {
                int nextIdx = i + 1;
                String nextSub = (nextIdx < items.size()) ? items.get(nextIdx).getName() : null;

                ImmutableList<MenuItem> childItems = menuTree.getMenuItems(items.get(i));
                String nextChild = (!childItems.isEmpty()) ? childItems.get(0).getName() : null;
                itemsInOrder.add(MenuItemHelper.visitWithResult(items.get(i),
                        new ArduinoItemGenerator(nextSub, nextChild)).orElse(new CppAndHeader("", "")));
                itemsInOrder.addAll(renderMenu(childItems));
            } else {
                int nextIdx = i + 1;
                String next = (nextIdx < items.size()) ? items.get(nextIdx).getName() : null;
                itemsInOrder.add(MenuItemHelper.visitWithResult(items.get(i),
                        new ArduinoItemGenerator(next)).orElse(new CppAndHeader("", "")));
            }
        }
        return itemsInOrder;
    }

    private String toSourceFile(Path directory, String ext) {
        Path file = directory.getFileName();
        return Paths.get(directory.toString(), file.toString() + ext).toString();
    }

    private void logLine(String s) {
        uiLogger.accept(DATE_TIME_FORMATTER.format(Instant.now()) + " - " + s);
        logger.info(s);
    }
}
