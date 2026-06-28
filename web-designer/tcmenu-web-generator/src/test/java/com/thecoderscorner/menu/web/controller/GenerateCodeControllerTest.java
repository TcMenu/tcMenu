package com.thecoderscorner.menu.web.controller;

import com.thecoderscorner.menu.domain.util.DomainFixtures;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorSupplier;
import com.thecoderscorner.menu.editorui.generator.EepromSaveMode;
import com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.EepromAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.eeprom.ArduinoClassEepromDefinition;
import com.thecoderscorner.menu.editorui.generator.plugin.*;
import com.thecoderscorner.menu.editorui.generator.plugin.display.GxEPD2SimplePluginImpl;
import com.thecoderscorner.menu.editorui.generator.plugin.display.InbuiltDisplayInputPlugins;
import com.thecoderscorner.menu.editorui.generator.plugin.theme.EinkBlockTheme;
import com.thecoderscorner.menu.editorui.project.FileBasedProjectPersistor;
import com.thecoderscorner.menu.editorui.project.MenuTreeWithCodeOptions;
import com.thecoderscorner.menu.editorui.project.PersistedProject;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.persist.JsonMenuItemSerializer;
import com.thecoderscorner.menu.persist.VersionInfo;
import com.thecoderscorner.menu.web.domain.GenerateCodeRequest;
import com.thecoderscorner.menu.web.domain.GenerationResponse;
import com.thecoderscorner.menu.web.domain.LogEntry;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.thecoderscorner.menu.editorui.generator.plugin.JavaPluginItem.ALWAYS_APPLICABLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GenerateCodeControllerTest {
    private GenerateCodeController controller;
    private FileBasedProjectPersistor persistor;
    private CodeGeneratorSupplier codeGeneratorSupplier;
    private CodePluginManager codePluginManager;
    private JsonMenuItemSerializer serializer;
    private ConfigurationStorage configStorage;

    @BeforeEach
    public void setUp() {
        configStorage = mock(ConfigurationStorage.class);
        codeGeneratorSupplier = new CodeGeneratorSupplier(configStorage);
        persistor = new FileBasedProjectPersistor(new PluginEmbeddedPlatformsImpl());
        codePluginManager = mock(CodePluginManager.class);

        controller = spy(new GenerateCodeController(persistor, codeGeneratorSupplier, codePluginManager));
        controller.init();
    }

    @Test
    public void testGenerateCode() throws Exception {
        // Given
        String inputUuid = "input-uuid";
        String displayUuid = "display-uuid";
        String themeUuid = "theme-uuid";
        
        CodeGeneratorOptions options = new CodeGeneratorOptions(
                EmbeddedPlatform.ARDUINO_AVR, displayUuid, inputUuid, List.of("remote-uuid"), themeUuid,
                List.of(), UUID.randomUUID(), "TestApp",
                new ArduinoClassEepromDefinition(), new EepromAuthenticatorDefinition(256, 2),
                null, null, ProjectSaveLocation.ALL_TO_CURRENT, false,
                false, EepromSaveMode.LEGACY_WRITE_BY_POSITION, true
        );

        MenuTreeWithCodeOptions projectOptions = new MenuTreeWithCodeOptions(DomainFixtures.fullEspAmplifierTestTree(), options, "Test Project");
        PersistedProject project = persistor.asPersistedProject(projectOptions);
        GenerateCodeRequest request = new GenerateCodeRequest(project, null, List.of(), List.of());
        doReturn(request).when(controller).processGenerateCodeRequest(anyString());

        CodePluginItem inputPlugin = mock(CodePluginItem.class);
        when(inputPlugin.getId()).thenReturn(inputUuid);
        when(inputPlugin.getProperties()).thenReturn(List.of());

        JavaPluginGroup group = new InbuiltDisplayInputPlugins(codePluginManager, new VersionInfo("1.0"));
        CodePluginItem displayPlugin = new GxEPD2SimplePluginImpl(group, codePluginManager).getPlugin();
        CodePluginItem themePlugin = new EinkBlockTheme(group, codePluginManager).getPlugin();

        CodePluginItem remotePlugin = mock(CodePluginItem.class);
        when(remotePlugin.getId()).thenReturn("remote-uuid");
        when(remotePlugin.getProperties()).thenReturn(List.of());

        when(codePluginManager.getPluginById(inputUuid)).thenReturn(Optional.of(inputPlugin));
        when(codePluginManager.getPluginById(displayUuid)).thenReturn(Optional.of(displayPlugin));
        when(codePluginManager.getPluginById(themeUuid)).thenReturn(Optional.of(themePlugin));
        when(codePluginManager.getPluginById("remote-uuid")).thenReturn(Optional.of(remotePlugin));

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        // When
        GenerationResponse response = controller.generateCode("{}", servletRequest);

        // Then
        assertNotNull(response);
        assertEquals(6, response.getGeneratedFiles().size());
        assertTrue(response.getFileByName("TestApp.ino").getContent().contains("setupMenu();"));
        assertTrue(response.getFileByName("TestApp.ino").getContent().contains("taskManager.runLoop();"));

        assertTrue(response.getFileByName("TestApp_menu.h").getContent().contains("#ifndef MENU_GENERATED_CODE_H"));
        assertTrue(response.getFileByName("TestApp_menu.h").getContent().contains("extern GraphicsDeviceRenderer renderer;"));
        assertThat(response.getLogLines()).isNotEmpty();
        //assertFileWithContents("");
        assertTrue(response.isSuccessful());
    }

    @Test
    public void testGenerateCodeInvalidRequest() throws Exception {
        // Given - a request with a null UUID in options
        CodeGeneratorOptions options = new CodeGeneratorOptions(
                EmbeddedPlatform.ARDUINO_AVR, "display-uuid", "input-uuid", List.of("remote-uuid"), "theme-uuid",
                List.of(), null, "TestApp",
                null, null, null, null, ProjectSaveLocation.ALL_TO_CURRENT,
                false, false, EepromSaveMode.LEGACY_WRITE_BY_POSITION, true
        );

        MenuTreeWithCodeOptions projectOptions = new MenuTreeWithCodeOptions(DomainFixtures.fullEspAmplifierTestTree(), options, "Test Project");
        PersistedProject project = persistor.asPersistedProject(projectOptions);
        GenerateCodeRequest request = new GenerateCodeRequest(project, "override", List.of(), List.of());
        doReturn(request).when(controller).processGenerateCodeRequest(anyString());

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        // When
        GenerationResponse response = controller.generateCode("{}", servletRequest);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccessful());
        assertThat(response.getLogLines()).anyMatch(log -> log.getLog().contains("Request was not valid"));
    }

    @Test
    public void testGenerateCodeRateLimit() throws Exception {
        // Given
        String inputUuid = "input-uuid";
        String displayUuid = "display-uuid";
        String themeUuid = "theme-uuid";

        CodeGeneratorOptions options = new CodeGeneratorOptions(
                EmbeddedPlatform.ARDUINO_AVR, displayUuid, inputUuid, List.of("remote-uuid"), themeUuid,
                List.of(), UUID.randomUUID(), "TestApp",
                null, null, null, null, ProjectSaveLocation.ALL_TO_CURRENT,
                false, false, EepromSaveMode.LEGACY_WRITE_BY_POSITION, true
        );

        MenuTreeWithCodeOptions projectOptions = new MenuTreeWithCodeOptions(DomainFixtures.fullEspAmplifierTestTree(), options, "Test Project");
        PersistedProject project = persistor.asPersistedProject(projectOptions);
        GenerateCodeRequest request = new GenerateCodeRequest(project, null, List.of(), List.of());
        doReturn(request).when(controller).processGenerateCodeRequest(anyString());

        CodePluginItem inputPlugin = mock(CodePluginItem.class);
        when(inputPlugin.getId()).thenReturn(inputUuid);
        when(inputPlugin.getProperties()).thenReturn(List.of());
        when(codePluginManager.getPluginById(anyString())).thenReturn(Optional.of(inputPlugin));

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getRemoteAddr()).thenReturn("1.2.3.4");

        // When - First call succeeds
        GenerationResponse response1 = controller.generateCode("{}", servletRequest);
        assertTrue(response1.isSuccessful());

        // When - Second call immediately fails due to rate limit
        GenerationResponse response2 = controller.generateCode("{}", servletRequest);

        // Then
        assertFalse(response2.isSuccessful());
        assertThat(response2.getLogLines()).anyMatch(log -> log.getLog().contains("Rate limit exceeded"));
    }
    @Test
    public void testGenerateCodeWithDuplicatePropertyNames() throws Exception {
        // Given
        String inputUuid = "input-uuid";
        String displayUuid = "display-uuid";

        CodeGeneratorOptions options = new CodeGeneratorOptions(
                EmbeddedPlatform.ARDUINO_AVR, displayUuid, inputUuid, List.of(), null,
                List.of(), UUID.randomUUID(), "TestApp",
                null, null, null, null, ProjectSaveLocation.ALL_TO_CURRENT,
                false, false, EepromSaveMode.LEGACY_WRITE_BY_POSITION, true
        );

        MenuTreeWithCodeOptions projectOptions = new MenuTreeWithCodeOptions(DomainFixtures.fullEspAmplifierTestTree(), options, "Test Project");
        PersistedProject project = persistor.asPersistedProject(projectOptions);
        // Add two properties with the same name but different subsystems to the request
        List<CreatorProperty> frontEndProperties = List.of(
                new CreatorProperty("ORIENTATION_SEPARATOR", "val1", SubSystem.DISPLAY),
                new CreatorProperty("ORIENTATION_SEPARATOR", "val2", SubSystem.INPUT)
        );
        GenerateCodeRequest request = new GenerateCodeRequest(project, null, frontEndProperties, List.of());
        doReturn(request).when(controller).processGenerateCodeRequest(anyString());

        CodePluginItem inputPlugin = mock(CodePluginItem.class);
        when(inputPlugin.getId()).thenReturn(inputUuid);
        when(inputPlugin.getProperties()).thenReturn(List.of(
                new CreatorProperty("ORIENTATION_SEPARATOR", "desc", "ext", "def", SubSystem.INPUT, CreatorProperty.PropType.TEXTUAL, null, ALWAYS_APPLICABLE)
        ));

        CodePluginItem displayPlugin = mock(CodePluginItem.class);
        when(displayPlugin.getId()).thenReturn(displayUuid);
        when(displayPlugin.getProperties()).thenReturn(List.of(
                new CreatorProperty("ORIENTATION_SEPARATOR", "desc", "ext", "def", SubSystem.DISPLAY, CreatorProperty.PropType.TEXTUAL, null, ALWAYS_APPLICABLE)
        ));

        when(codePluginManager.getPluginById(inputUuid)).thenReturn(Optional.of(inputPlugin));
        when(codePluginManager.getPluginById(displayUuid)).thenReturn(Optional.of(displayPlugin));

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        // When
        GenerationResponse response = controller.generateCode("{}", servletRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccessful(), "Generation should succeed even with duplicate property names across subsystems");
    }
}
