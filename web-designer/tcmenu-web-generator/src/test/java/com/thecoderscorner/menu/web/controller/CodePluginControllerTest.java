package com.thecoderscorner.menu.web.controller;

import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatforms;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CodePluginControllerTest {
    private MockMvc mockMvc;
    private CodePluginManager pluginManager;
    private EmbeddedPlatforms embeddedPlatforms;

    @BeforeEach
    public void setUp() {
        pluginManager = mock(CodePluginManager.class);
        embeddedPlatforms = mock(EmbeddedPlatforms.class);
        CodePluginController controller = new CodePluginController(pluginManager, embeddedPlatforms);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void testGetById_ValidRequest_ReturnsPlugins() throws Exception {
        // Arrange
        var plugin = Mockito.mock(com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem.class);
        var config = Mockito.mock(com.thecoderscorner.menu.editorui.generator.plugin.CodePluginConfig.class);
        when(plugin.getConfig()).thenReturn(config);
        when(config.getName()).thenReturn("PluginName");
        when(config.getLicense()).thenReturn("Apache-2.0");
        when(config.getVendor()).thenReturn("TheCodersCorner");
        when(plugin.getId()).thenReturn("plugin1");
        
        when(pluginManager.getPluginById("plugin1")).thenReturn(Optional.of(plugin));
        when(pluginManager.getPluginById("plugin2")).thenReturn(Optional.of(plugin));

        // Act & Assert
        mockMvc.perform(post("/api/v1/generator/plugins/byIdList")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"plugin1\", \"plugin2\"]"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(pluginManager, times(1)).getPluginById("plugin1");
        verify(pluginManager, times(1)).getPluginById("plugin2");
    }

    @Test
    public void testGetById_InvalidRequest_EmptyIds_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/generator/plugins/byIdList")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void testGetById_InvalidRequest_TooManyIds_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/generator/plugins/byIdList")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"id1\", \"id2\", \"id3\", \"id4\", \"id5\", \"id6\", \"id7\", \"id8\", \"id9\", \"id10\", \"id11\"]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void testGetById_ValidRequest_NoMatchingPlugin_ReturnsEmptyResponse() throws Exception {
        // Arrange
        when(pluginManager.getPluginById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/v1/generator/plugins/byIdList")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"plugin1\"]"))
                .andExpect(status().isOk());

        verify(pluginManager, times(1)).getPluginById("plugin1");
    }

    @Test
    public void testGetImageById_ReturnsImageWithCacheHeaders() throws Exception {
        // Arrange
        var plugin = Mockito.mock(com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem.class);
        when(pluginManager.getPluginById("plugin1")).thenReturn(Optional.of(plugin));
        var bufferedImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        when(pluginManager.getImageForName(plugin)).thenReturn(Optional.of(bufferedImage));

        // Act & Assert
        mockMvc.perform(get("/api/v1/generator/plugins/imgById/plugin1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=2592000"))
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));

        // Test caching (server side)
        mockMvc.perform(get("/api/v1/generator/plugins/imgById/plugin1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=2592000"));

        // verify pluginManager was only called once for the image due to caching
        verify(pluginManager, times(1)).getImageForName(plugin);
    }

    @Test
    public void testGetImageById_ReturnsNotFoundForUnknownPluginId() throws Exception {
        // Arrange
        when(pluginManager.getPluginById("unknown")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/generator/plugins/imgById/unknown"))
                .andExpect(status().isNotFound());

        verify(pluginManager, times(1)).getPluginById("unknown");
        verifyNoMoreInteractions(pluginManager);
    }

    @Test
    public void testGetImageById_ReturnsAnImage() throws Exception {
        when(pluginManager.getImageForName(any())).thenReturn(
                Optional.of(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB)));
        when(pluginManager.getPluginById(any())).thenReturn(Optional.of(Mockito.mock(CodePluginItem.class)));
        var mvc = mockMvc.perform(get("/api/v1/generator/plugins/imgById/plugin1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andReturn();

        byte[] content = mvc.getResponse().getContentAsByteArray();
        assertTrue(content.length > 0);
        BufferedImage read = ImageIO.read(new ByteArrayInputStream(content));
        assertNotNull(read);
        assertEquals(10, read.getWidth());
        assertEquals(10, read.getHeight());
    }





}