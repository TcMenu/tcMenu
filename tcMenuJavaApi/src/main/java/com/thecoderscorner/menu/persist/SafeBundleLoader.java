package com.thecoderscorner.menu.persist;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class SafeBundleLoader {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    enum PropertiesLineType { BLANK, COMMENT, HAS_KEY_AND_VALUE, UNKNOWN }

    private final Path location;
    private final String baseName;
    private final URLClassLoader classLoader;

    public SafeBundleLoader(Path location, String baseName) {
        this.location = location;
        this.baseName = baseName;
        boolean unsuitableDirectory;
        try {
            unsuitableDirectory = Files.walk(location, 2, FileVisitOption.FOLLOW_LINKS)
                    .anyMatch(p -> Files.isRegularFile(p) && !p.toString().endsWith(".properties"));
        } catch (IOException e) {
            logger.log(System.Logger.Level.ERROR, "Unable to determine if properties dir is safe", e);
            unsuitableDirectory = true;
        }
        if(unsuitableDirectory) throw new IllegalArgumentException("Directory is not safe for properties classpath");

        try {
            classLoader = new URLClassLoader(new URL[]{location.toUri().toURL()});
        } catch(Exception e) {
            logger.log(System.Logger.Level.ERROR, "Class loader for resource bundle was not created", e);
            throw new IllegalArgumentException("Class loader on properties failed", e);
        }
    }

    public ResourceBundle getBundleForLocale(Locale locale) {
        return ResourceBundle.getBundle(baseName, locale, classLoader);
    }

    public Path getLocation() {
        return location;
    }

    public String getBaseName() {
        return baseName;
    }

    public Map<String, String> loadResourceBundleAsMap(Locale locale) throws IOException {
        Path resolvedPath = getPathForLocale(locale);

        var lines = Files.readAllLines(resolvedPath)
                .stream().map(PropertiesFileLine::new)
                .filter(line -> line.getLineType() == PropertiesLineType.HAS_KEY_AND_VALUE)
                .collect(Collectors.toList());

        var mapToReturn = new HashMap<String, String>(lines.size() + 20);
        for(var line : lines) {
            mapToReturn.put(line.getKey(), line.getValue());
        }
        return mapToReturn;
    }

    public void saveChangesKeepingFormatting(Locale locale, Map<String, String> allValues) {
        Path resolvedPath = getPathForLocale(locale);
        try {
            List<PropertiesFileLine> lines;
            if(Files.exists(resolvedPath)) {
                lines = Files.readAllLines(resolvedPath)
                        .stream().map(PropertiesFileLine::new)
                        .collect(Collectors.toCollection(ArrayList::new));
            } else {
                lines = new ArrayList();
                lines.add(new PropertiesFileLine("# Created by TcMenu to hold menu translations", true));
            }

            for(var value : allValues.entrySet()) {
                var line = findLine(lines, value.getKey());
                if(line != null) {
                    line.setValue(value.getValue());
                } else {
                    lines.add(new PropertiesFileLine(value.getKey(), value.getValue()));
                }
            }

            String lineSeparator = System.getProperty("line.separator");

            if(lines.stream().noneMatch(PropertiesFileLine::isChanged)) return; // no need to save.

            var toWrite = lines.stream().map(PropertiesFileLine::outputLine).collect(Collectors.joining(lineSeparator)) + lineSeparator;
            Files.write(resolvedPath, toWrite.getBytes(StandardCharsets.UTF_8));

        } catch (IOException e) {
            logger.log(System.Logger.Level.ERROR, "Unable to save " + resolvedPath, e);
        }
    }

    public Path getPathForLocale(Locale locale) {
        if(locale.getLanguage() == null || locale.getLanguage().length() == 0) {
            return location.resolve(baseName + ".properties");
        } else {
            return location.resolve(baseName + "_" + locale.toString() + ".properties");
        }
    }

    private PropertiesFileLine findLine(List<PropertiesFileLine> lines, String key) {
        for(var line : lines) {
            if(line.getLineType() == PropertiesLineType.HAS_KEY_AND_VALUE && line.getKey().equals(key)) return line;
        }
        return null;
    }

    static class PropertiesFileLine {
        private final String line;
        private final PropertiesLineType lineType;
        private String key = null;
        private String value = null;
        private boolean needsSave = false;

        public PropertiesFileLine(String key, String value) {
            line = key + "=" + value;
            lineType = PropertiesLineType.HAS_KEY_AND_VALUE;
            this.key = key;
            this.value = value;
            this.needsSave = true;
        }

        public PropertiesFileLine(String line, boolean needsSave) {
            this(line);
            this.needsSave = needsSave;
        }

        public PropertiesFileLine(String line) {
            this.line = line.trim();
            if(line.isEmpty()) lineType = PropertiesLineType.BLANK;
            else if(line.startsWith("#")||line.startsWith("!")) lineType = PropertiesLineType.COMMENT;
            else {
                var parts = line.split("\\s*=\\s*");
                if(parts.length > 1) {
                    key = parts[0].trim();
                    lineType = PropertiesLineType.HAS_KEY_AND_VALUE;
                    value = parts[1];
                } else {
                    lineType = PropertiesLineType.UNKNOWN;
                }
            }
        }

        PropertiesLineType getLineType() {
            return lineType;
        }

        String getKey() {
            return key;
        }

        String getValue() {
            return value;
        }

        void setValue(String replacementValue) {
            if(replacementValue.equals(value)) return;
            this.value = replacementValue;
            needsSave = true;
        }

        boolean isChanged() { return needsSave; }

        String outputLine() {
            if(!needsSave) return line; // save exactly as it was loaded if unchanged

            if(lineType == PropertiesLineType.HAS_KEY_AND_VALUE) {
                return key + "=" + value;
            } else {
                return line;
            }
        }
    }
}
