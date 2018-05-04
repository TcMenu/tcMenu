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
import com.thecoderscorner.menu.editorui.generator.display.DisplayType;
import com.thecoderscorner.menu.editorui.generator.input.InputType;
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
    private final DisplayType displayType;
    private final InputType inputType;

    public ArduinoGenerator(Consumer<String> logger, Path directory, DisplayType displayType, InputType inputType, MenuTree menuTree) {
        this.uiLogger = logger;
        this.directory = directory;
        this.menuTree = menuTree;
        this.inputType = inputType;
        this.displayType = displayType;
    }

    @Override
    public boolean startConversion() {
        logLine("Starting Arduino 8bit generate: " + directory);
        logLine("Display type is " + displayType);

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setTemplateMode(TemplateMode.TEXT);
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);


        String fileName = toArduinoIno(directory);
        Path source = Paths.get(fileName);
        if(Files.exists(source)) {
            try {
                logLine("Backup existing file");
                Files.copy(source, Paths.get(source.toString() + ".backup"), REPLACE_EXISTING);
            } catch (IOException e) {
                logLine("Failed to backup file:" + e.getMessage() );
                logger.error("Backup failed", e);
                return false;
            }
        }
        try(Writer writer = new FileWriter(fileName)) {
            logLine("File created, writing out .ino file");
            Context context = new Context(Locale.getDefault());
            context.setVariable("display", displayType.getCreator());
            context.setVariable("menuItems", generateMenusInOrder(menuTree));
            context.setVariable("firstItem", getFirstMenuVariable(menuTree));
            context.setVariable("callbacks", callBackFunctions());
            context.setVariable("input", inputType.getCreator());
            engine.process("/generator/template.ino", context, writer);
            logLine("Process finished, check output directory for file.");
        }
        catch (Exception e) {
            logLine("Failed to generate: " + e.getMessage());
            logger.error("Code Generation failed", e);
            return false;
        }
        return true;
    }

    private Object getFirstMenuVariable(MenuTree menuTree) {
        return menuTree.getMenuItems(MenuTree.ROOT).stream().findFirst()
                .map(menuItem -> "menu" + makeNameToVar(menuItem.getName()))
                .orElse("");
    }

    private Collection<String> generateMenusInOrder(MenuTree menuTree) {
        ImmutableList<MenuItem> root = menuTree.getMenuItems(MenuTree.ROOT);
        List<String> itemsInOrder = renderMenu(root);
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

    private List<String> renderMenu(Collection<MenuItem> itemsColl) {
        ArrayList<MenuItem> items = new ArrayList<>(itemsColl);
        List<String> itemsInOrder = new ArrayList<>(100);
        for(int i = 0; i < items.size();i++) {

            if(items.get(i).hasChildren()) {
                int nextIdx = i+1;
                String nextSub = (nextIdx < items.size()) ? items.get(nextIdx).getName() : null;

                ImmutableList<MenuItem> childItems = menuTree.getMenuItems(items.get(i));
                String nextChild = (!childItems.isEmpty()) ? childItems.get(0).getName() : null;
                itemsInOrder.add(MenuItemHelper.visitWithResult(items.get(i),
                        new ArduinoItemGenerator(nextSub, nextChild)).orElse("// missed " + items.get(i)));
                itemsInOrder.addAll(renderMenu(childItems));
            }
            else {
                int nextIdx = i+1;
                String next = (nextIdx < items.size()) ? items.get(nextIdx).getName() : null;
                itemsInOrder.add(MenuItemHelper.visitWithResult(items.get(i),
                        new ArduinoItemGenerator(next)).orElse("// missed " + items.get(i)));
            }
        }
        return itemsInOrder;
    }

    private String toArduinoIno(Path directory) {
        Path file = directory.getFileName();
        return Paths.get(directory.toString(), file.toString() + ".ino").toString();
    }

    private void logLine(String s) {
        uiLogger.accept(DATE_TIME_FORMATTER.format(Instant.now()) + " - " + s);
        logger.info(s);
    }
}
