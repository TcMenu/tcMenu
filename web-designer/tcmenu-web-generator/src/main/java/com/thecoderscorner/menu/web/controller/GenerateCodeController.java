package com.thecoderscorner.menu.web.controller;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptionsBuilder;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorSupplier;
import com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.generator.logger.GeneratedFile;
import com.thecoderscorner.menu.editorui.generator.logger.UserFeedbackLogger;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.plugin.ThemeDescription;
import com.thecoderscorner.menu.editorui.project.FileBasedProjectPersistor;
import com.thecoderscorner.menu.editorui.project.MenuTreeWithCodeOptions;
import com.thecoderscorner.menu.persist.JsonMenuItemSerializer;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;
import com.thecoderscorner.menu.persist.PropertiesLocaleEnabledHandler;
import com.thecoderscorner.menu.persist.SafeBundleLoader;
import com.thecoderscorner.menu.web.domain.CodeBuildInfo;
import com.thecoderscorner.menu.web.domain.GenerateCodeRequest;
import com.thecoderscorner.menu.web.domain.GenerationResponse;
import com.thecoderscorner.menu.web.domain.LogEntry;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation.ALL_TO_SRC;
import static com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation.PROJECT_TO_SRC_WITH_GENERATED;
import static com.thecoderscorner.menu.editorui.project.CurrentEditorProject.MENU_PROJECT_LANG_FILENAME;

@Slf4j
@RestController
@RequestMapping("/api/v1/generator/generate")
public class GenerateCodeController {
    private final Cache<String, CodeBuildInfo> codeBuildCache = Caffeine.newBuilder()
            .maximumSize(128)
            .expireAfterWrite(Duration.ofMinutes(15))
            .build();
    private final Cache<String, LocalDateTime> userRateLimitCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(2))
            .build();
    private final FileBasedProjectPersistor persistor;
    private final JsonMenuItemSerializer serializer;
    private final CodeGeneratorSupplier codeGeneratorSupplier;
    private final CodePluginManager codePluginManager;

    @Value("${tcmenu.web.generator.concurrency:2}")
    private int buildConcurrency = 2;
    @Value("${tcmenu.web.generator.maxFiles:50}")
    private int maxFiles = 50;
    @Value("${tcmenu.web.generator.maxFileSize:1048576}")
    private int maxFileSize = 1048576;
    @Value("${tcmenu.web.generator.maxTotalSize:5242880}")
    private int maxTotalSize = 5242880;
    @Value("${tcmenu.web.generator.rateLimitMinutes:1}")
    private int rateLimitMinutes = 1;

    private Semaphore buildSemaphore;

    @PostConstruct
    public void init() {
        buildSemaphore = new Semaphore(buildConcurrency);
    }

    public GenerateCodeController(FileBasedProjectPersistor persistor, CodeGeneratorSupplier codeGeneratorSupplier,
                                  CodePluginManager codePluginManager) {
        this.persistor = persistor;
        this.codeGeneratorSupplier = codeGeneratorSupplier;
        this.codePluginManager = codePluginManager;
        this.serializer = persistor.getSerializer();
    }

    public GenerateCodeRequest processGenerateCodeRequest(String requestData) {
        return serializer.getGson().fromJson(requestData, GenerateCodeRequest.class);
    }

    private void validateRequest(GenerateCodeRequest request) {
        if (request == null) throw new IllegalArgumentException("Request is missing");
        if (request.getProject() == null) throw new IllegalArgumentException("Project is missing");
        var project = request.getProject();
        if (ObjectUtils.isEmpty(project.getProjectName())) throw new IllegalArgumentException("Project name is missing");
        if (project.getProjectName().length() > 100) throw new IllegalArgumentException("Project name too long");
        if (project.getCodeOptions() == null) throw new IllegalArgumentException("Code options are missing");
        var options = project.getCodeOptions();
        if (options.getApplicationUUID() == null) throw new IllegalArgumentException("Application UUID is missing");
        if (ObjectUtils.isEmpty(options.getApplicationName())) throw new IllegalArgumentException("Application name is missing");
        if (options.getApplicationName().length() > 100) throw new IllegalArgumentException("Application name too long");
        if (options.getEmbeddedPlatform() == null) throw new IllegalArgumentException("Embedded platform is missing");

        if (request.getRequiredFiles() != null) {
            for (var file : request.getRequiredFiles()) {
                if (ObjectUtils.isEmpty(file.getFileName())) throw new IllegalArgumentException("File name is missing");
                if (file.getFileName().length() > 255) throw new IllegalArgumentException("File name too long");
            }
        }
    }

    @PostMapping
    @ResponseBody
    public GenerationResponse generateCode(@RequestBody String requestData, HttpServletRequest servletRequest) {
        long millisStart = System.currentTimeMillis();
        String userIp = servletRequest.getRemoteAddr();
        var lastBuildTime = userRateLimitCache.getIfPresent(userIp);
        if(lastBuildTime != null && Duration.between(lastBuildTime, LocalDateTime.now()).toMinutes() < rateLimitMinutes) {
            log.warn("Request rejected: Rate limit exceeded for IP {}", userIp);
            return GenerationResponse.badResponse(List.of(new LogEntry("Rate limit exceeded, please wait " + rateLimitMinutes + " minute(s) between builds", Level.ERROR)));
        }

        GenerateCodeRequest request;
        try {
            request = processGenerateCodeRequest(requestData);
            validateRequest(request);
        } catch (IllegalArgumentException ex) {
            log.warn("Request validation failed: {}", ex.getMessage());
            return GenerationResponse.badResponse(List.of(new LogEntry("Request was not valid", Level.ERROR)));
        } catch (Exception ex) {
            log.error("Failed to parse request data", ex);
            return GenerationResponse.badResponse(List.of(new LogEntry("Invalid request format", Level.ERROR)));
        }

        if(request.getRequiredFiles().size() > maxFiles) {
            log.warn("Request rejected: too many files {}", request.getRequiredFiles().size());
            return GenerationResponse.badResponse(List.of(new LogEntry("Too many files in request", Level.ERROR)));
        }

        long totalSize = 0;
        for(var file : request.getRequiredFiles()) {
            if(file.getContent() != null) {
                if(file.getContent().length() > maxFileSize) {
                    log.warn("Request rejected: file {} too large", file.getFileName());
                    return GenerationResponse.badResponse(List.of(new LogEntry("File " + file.getFileName() + " is too large", Level.ERROR)));
                }
                totalSize += file.getContent().length();
            }
        }

        if(totalSize > maxTotalSize) {
            log.warn("Request rejected: total content size {} too large", totalSize);
            return GenerationResponse.badResponse(List.of(new LogEntry("Total request size is too large", Level.ERROR)));
        }

        try {
            if(!buildSemaphore.tryAcquire(10, TimeUnit.SECONDS)) {
                log.warn("Request rejected: timeout waiting for build semaphore");
                return GenerationResponse.badResponse(List.of(new LogEntry("Server busy, please try again later", Level.ERROR)));
            }

            var menuWithOptions = persistor.getMenuTreeWithCodeOptions(request.getProject());
            var frontEndProperties = request.getExistingProperties();
            var projectFiles = request.getRequiredFiles();
            log.info("Received generate code request for project {} and parsed successfully", request.getProject().getProjectName());

            return doCodeGeneration(request, menuWithOptions, frontEndProperties, projectFiles, millisStart, servletRequest);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return GenerationResponse.badResponse(List.of(new LogEntry("Internal error during build concurrency wait", Level.ERROR)));
        } finally {
            buildSemaphore.release();
        }
    }

    private @NonNull GenerationResponse doCodeGeneration(GenerateCodeRequest request, MenuTreeWithCodeOptions menuWithOptions,
                                                         List<CreatorProperty> frontEndProperties, List<GeneratedFile> projectFiles,
                                                         long millisStart, HttpServletRequest servletRequest) {
        var uuid = request.getProject().getCodeOptions().getApplicationUUID();
        var existingOpt = codeBuildCache.asMap().values().stream()
                .filter(c -> c.getProjectUuid().equals(uuid))
                .findFirst();
        if(existingOpt.isPresent()) {
            log.warn("Project {} already existed in cache, replacing ", uuid);
            codeBuildCache.invalidate(existingOpt.get().getBuildId());
        }

        var buildUuid = UUID.randomUUID();
        log.info("Generating code for project {} with build UUID of {}", uuid, buildUuid);

        var logger = new ControllerFeedbackLogger();
        EmbeddedPlatform platform = menuWithOptions.getOptions().getEmbeddedPlatform();
        var generator = codeGeneratorSupplier.getCodeGeneratorFor(platform, menuWithOptions.getOptions(), logger);
        log.info("Code generator for {} is an instance of {}", platform, generator.getClass().getSimpleName());
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("tcmenu-gen");
            var fileName = VariableNameGenerator.makeNameFromVariable(menuWithOptions.getOptions().getApplicationName());
            if(fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                throw new IllegalArgumentException("Invalid filename - directory traversal not allowed");
            }

            var projectPath = tempDir.resolve(fileName).normalize();
            if(!projectPath.startsWith(tempDir)) {
                throw new IllegalArgumentException("Invalid filename - resolved path outside temp directory");
            }
            Files.createDirectories(projectPath);
            var srcPath = projectPath;
            ProjectSaveLocation saveLocation = menuWithOptions.getOptions().getSaveLocation();
            if(saveLocation == ALL_TO_SRC || saveLocation == PROJECT_TO_SRC_WITH_GENERATED) {
                srcPath = projectPath.resolve("src");
                Files.createDirectories(srcPath);
                log.info("Project needs src directory");
            } else {
                log.info("Project created in root dir");
            }
            var filePath = projectPath.resolve(fileName + ".emf");
            var plugins = allPlugins(menuWithOptions.getOptions());
            var combinedSetProperties = deepCopyAndPrepareProps(plugins, frontEndProperties);

            var localeHandler = prepareLocaleHandler(projectFiles, srcPath, logger);
            copyInoFileIfPresent(projectFiles, srcPath, menuWithOptions.getOptions().isUseCppMain());

            var codeOptions = new CodeGeneratorOptionsBuilder().withExisting(menuWithOptions.getOptions())
                    .withProperties(frontEndProperties).codeOptions();
            persistor.save(filePath.toString(), menuWithOptions.getDescription(), menuWithOptions.getMenuTree(), codeOptions, localeHandler);
            generator.startConversion(projectPath, plugins, menuWithOptions.getMenuTree(), List.of(),
                    menuWithOptions.getOptions(), localeHandler, combinedSetProperties);

            userRateLimitCache.put(servletRequest.getRemoteAddr(), LocalDateTime.now());

            var howLong = Duration.ofMillis(System.currentTimeMillis() - millisStart);
            logger.info("Thank you for choosing TcMenu. Task completed in %02d.%03d seconds".formatted(howLong.toSecondsPart(), howLong.toMillisPart()));
            var response = GenerationResponse.okResponse(logger.filesInOutput, logger.logEntries, buildUuid);
            codeBuildCache.put(buildUuid.toString(), new CodeBuildInfo(buildUuid.toString(), uuid, LocalDateTime.now(), response, tempDir));
            return response;
        } catch(Exception ex) {
            log.error("Error generating code", ex);
            return GenerationResponse.badResponse(List.of(new LogEntry("Internal Error generating code", Level.ERROR)));
        } finally {
            getRidOfTheLot(tempDir);
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public GenerationResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        var logs = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new LogEntry(fe.getField() + ": " + fe.getDefaultMessage(), Level.ERROR))
                .toList();
        return GenerationResponse.badResponse(logs);
    }

    @GetMapping("/zip/{buildUuid}")
    @ResponseBody
    public ResponseEntity<byte[]> getZipFile(@PathVariable String buildUuid) {
        if(buildUuid == null || buildUuid.isBlank()) throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "No build UUID provided");
        if(buildUuid.endsWith(".zip")) buildUuid = buildUuid.substring(0, buildUuid.length() - 4);

        var buildInfo = codeBuildCache.asMap().get(buildUuid);
        if(buildInfo == null) throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "No build found with UUID " + buildUuid);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tcmenu-build-" + buildUuid + ".zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(buildInfo.getZipFile());
    }


    private void getRidOfTheLot(Path tempDir) {
        try {
            log.info("Cleaning up temp directory immediately {}", tempDir);
            if(tempDir != null) {
                if(!FileSystemUtils.deleteRecursively(tempDir)) {
                    log.error("Failed to clean up temp directory (returned false) {}", tempDir);
                }
            }
        } catch (IOException e) {
            log.error("The following file was not cleaned down {}", tempDir, e);
        }
    }

    private void copyInoFileIfPresent(List<GeneratedFile> allFiles, Path projectPath, boolean cppProject) {
        var inoFileOpt = allFiles.stream().filter(f -> f.getFileName().endsWith(".ino") || f.getFileName().endsWith("_main.cpp"))
                .findFirst();
        if(inoFileOpt.isPresent()) {
            var inoFile = inoFileOpt.get();
            try {
                var fileName = projectPath.getFileName().toString() + (cppProject ? "_menu.cpp" : ".ino");
                var filePath = projectPath.resolve(fileName).normalize();
                if(!filePath.startsWith(projectPath)) {
                    throw new IllegalArgumentException("Invalid filename - resolved path outside project directory");
                }
                Files.writeString(filePath, inoFile.getContent());
                log.info("INO/main file was provided, cpp mode is {}, file copied as {}", cppProject, filePath.getFileName());
            } catch (IOException e) {
                log.error("Failed to copy menu.ino file", e);
            }
        } else {
            log.info("No INO/main file was provided, nothing to copy");
        }
    }

    private static LocaleMappingHandler prepareLocaleHandler(List<GeneratedFile> allFiles, Path rootDir,ControllerFeedbackLogger logger) throws IOException {
        var localeHandler = LocaleMappingHandler.NOOP_IMPLEMENTATION;
        var localeFiles = allFiles.stream().filter(f -> f.getFileName().endsWith(".properties")).toList();
        if (!ObjectUtils.isEmpty(localeFiles)) {
            log.info("Found {} locale files to copy, copying into structure", localeFiles.size());
            var i18nDir = rootDir.resolve("i18n");
            Files.createDirectories(i18nDir);
            for (var file : localeFiles) {
                var fileName = file.getFileName();
                // select files that start with i18n/ and normalise
                if (!fileName.startsWith("i18n/")) continue;
                fileName = fileName.substring(5);

                // ensure nothing untoward in the path. Only latin characters allowed in path at the moment
                if (fileName.contains("/") || fileName.contains("\\") || fileName.contains("..")) {
                    throw new IllegalArgumentException("Invalid filename - directory traversal not allowed");
                }
                var sanitizedName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
                log.info("Locale file {} copied into i18n", sanitizedName);
                var filePath = i18nDir.resolve(sanitizedName).normalize();
                if (!filePath.startsWith(i18nDir)) {
                    throw new IllegalArgumentException("Invalid filename - resolved path outside i18n directory");
                }
                Files.writeString(filePath, file.getContent());
                logger.fileModificiation(GeneratedFile.always(filePath, file.getContent()));
            }
            localeHandler = new PropertiesLocaleEnabledHandler(new SafeBundleLoader(i18nDir, MENU_PROJECT_LANG_FILENAME));
        }
        return localeHandler;
    }

    private List<CreatorProperty> deepCopyAndPrepareProps(List<CodePluginItem> plugins, List<CreatorProperty> frontEndProperties) {
        var uiPropMap = frontEndProperties.stream().collect(Collectors.toMap(CreatorProperty::getName, Function.identity(), (existing, replacement) -> existing));
        return plugins.stream().flatMap(p -> p.getProperties().stream())
                .map(cp -> CreatorProperty.deepCopy(cp, uiPropMap.get(cp.getName())))
                .toList();
    }

    private List<CodePluginItem> allPlugins(CodeGeneratorOptions options) {

        // IMPORTANT
        // The order of plugins returned absolutely must ALWAYS be as below or firmware board crashes result:
        // 1. Display
        // 2. Input
        // 3. Remote
        // 4. Theme

        var list = new ArrayList<CodePluginItem>();
        AtomicReference<CodePluginItem> theme = new AtomicReference<>(null);
        codePluginManager.getPluginById(options.getLastDisplayUuid()).ifPresent(d -> {
            list.add(d);
            if(d.getThemeDescription() != null && d.getThemeDescription().getThemeMode() != ThemeDescription.ThemeMode.NONE) {
                theme.set(codePluginManager.getPluginById(options.getLastThemeUuid()).orElse(null));
            }
        });

        codePluginManager.getPluginById(options.getLastInputUuid()).ifPresent(list::add);

        if(ObjectUtils.isEmpty(options.getLastRemoteCapabilitiesUuids())) {
            codePluginManager.getPluginById(options.getLastRemoteUuid()).ifPresent(list::add);
        } else {
            for (var remote : options.getLastRemoteCapabilitiesUuids()) {
                codePluginManager.getPluginById(remote).ifPresent(list::add);
            }
        }

        if(theme.get() != null) {
            list.add(theme.get());
        }

        return list;
    }

    static class ControllerFeedbackLogger implements UserFeedbackLogger {
        private final List<LogEntry> logEntries = new java.util.ArrayList<>(200);
        private final List<GeneratedFile> filesInOutput = new ArrayList<>();
        @Override
        public void debug(String data) {
            logEntries.add(new LogEntry(data, Level.DEBUG));
        }

        @Override
        public void info(String data) {
            logEntries.add(new LogEntry(data, Level.INFO));
        }

        @Override
        public void warn(String data) {
            logEntries.add(new LogEntry(data, Level.WARN));
        }

        @Override
        public void error(String data) {
            logEntries.add(new LogEntry(data, Level.ERROR));
        }

        @Override
        public void error(String data, Exception ex) {
            logEntries.add(new LogEntry(data + " - " + ex.getMessage(), Level.ERROR));
        }

        @Override
        public void fileModificiation(GeneratedFile generatedFile) {
            filesInOutput.add(generatedFile);
        }
    }
}
