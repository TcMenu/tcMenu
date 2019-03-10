/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.pluginapi.SubSystem;
import javafx.scene.image.Image;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

import static com.thecoderscorner.menu.pluginapi.EmbeddedPlatform.ARDUINO_AVR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DirectoryCodePluginManagerTest {

    private Path dir;

    @Test
    void testLoadingAPlugin() throws Exception {
        dir = Files.createTempDirectory("tcmenuplugin");
        Files.copy(getClass().getResourceAsStream("/unitTestPlugin.jar"), dir.resolve("unitTestPlugin.jar"));

        EmbeddedPlatforms platforms = mock(EmbeddedPlatforms.class);
        when(platforms.getEmbeddedPlatformFromId(ARDUINO_AVR.getBoardId())).thenReturn(ARDUINO_AVR);
        TestDirectoryCodePluginManager manager = new TestDirectoryCodePluginManager(platforms);

        manager.loadPlugins(dir.toString());

        assertEquals(dir.toString(), manager.getModuleDirLoaded());
        assertThat(manager.getModuleNames()).containsExactlyInAnyOrder("com.thecoderscorner.tcmenu.unittest");

        assertThat(manager.getLoadedPlugins()).hasSize(1);
        CodePluginConfig config = manager.getLoadedPlugins().get(0);
        assertEquals("com.thecoderscorner.tcmenu.unittest", config.getModuleName());
        assertEquals("Apache 2.0", config.getLicense());

        assertThat(config.getPlugins()).hasSize(1);
        CodePluginItem item = config.getPlugins().get(0);

        Optional<Image> maybeImg = manager.getImageForName("rotary-encoder.jpg");
        assertTrue(maybeImg.isPresent());
        assertEquals(180, maybeImg.get().getWidth());

        var maybeItem = manager.getPluginConfigForItem(item);
        assertTrue(maybeItem.isPresent());
        assertEquals(config, maybeItem.get());
        assertThat(manager.getPluginsThatMatch(ARDUINO_AVR, SubSystem.INPUT)).hasSize(1);
        assertThat(manager.getPluginsThatMatch(ARDUINO_AVR, SubSystem.REMOTE)).isEmpty();
    }

    @AfterEach
    public void tearDown() throws Exception {
        Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    class TestDirectoryCodePluginManager extends DirectoryCodePluginManager {
        private String moduleDirLoaded;
        private Collection<String> moduleNames;

        public TestDirectoryCodePluginManager(EmbeddedPlatforms platforms) {
            super(platforms);
        }

        public String getModuleDirLoaded() {
            return moduleDirLoaded;
        }

        public Collection<String> getModuleNames() {
            return moduleNames;
        }

        @Override
        protected void loadModules(String sourceDir, Collection<String> moduleNames) {
            this.moduleDirLoaded = sourceDir;
            this.moduleNames = moduleNames;
            layer = ModuleLayer.boot();
        }
    }
}