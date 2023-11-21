package com.thecoderscorner.menu.editorui.project;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.thecoderscorner.menu.editorui.project.TccProjectWatcherImplTest.TestProjectWatchListener.TestProjectWatchType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(ApplicationExtension.class)
class TccProjectWatcherImplTest {
    private TccProjectWatcherImpl fileWatcher;
    private TestProjectWatchListener listener;
    private Path rootDir;
    private Path emfFile;
    private Path propFile1, propFile2;

    @BeforeEach
    void setUp() throws IOException {
        rootDir = Files.createTempDirectory("tcmenutest");
        Files.createDirectories(rootDir);
        Path i18nDir = rootDir.resolve("i18n");
        Files.createDirectories(i18nDir);
        emfFile = rootDir.resolve("project.emf");
        propFile1 = i18nDir.resolve("project-lang.properties");
        propFile2 = i18nDir.resolve("project-lang_fr.properties");
        Files.writeString(emfFile, "Hello world");
        Files.writeString(rootDir.resolve(propFile1), "property.1=hello");
        Files.writeString(rootDir.resolve(propFile2), "property.1=bonjour");

        fileWatcher = new TccProjectWatcherImpl();
        listener = new TestProjectWatchListener();
        fileWatcher.registerWatchListener(listener);

        fileWatcher.setProjectName(emfFile);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(rootDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        fileWatcher.close();
    }

    @Test
    void testEmfFileWatching() throws Exception {
        listener.reset();
        Files.writeString(emfFile, "File has changed");
        assertEquals(EMF_FILE_NOTIFIED, listener.waitForEvent());
    }

    @Test
    void testI18nFileWatching() throws Exception {
        listener.reset();
        Files.writeString(propFile1, "propery.1=abcdefghi");
        assertEquals(I18N_NOTIFIED, listener.waitForEvent());
    }

    @Test
    void testWhereThereIsNoChange() throws Exception {
        fileWatcher.fileWasSaved(propFile2.getFileName(), "property.1=bonjour");
        listener.reset();
        Files.writeString(propFile2, "property.1=bonjour");
        assertEquals(NONE, listener.waitForEvent());
    }

    static class TestProjectWatchListener implements TccProjectWatcher.ProjectWatchListener {
        enum TestProjectWatchType { NONE, EMF_FILE_NOTIFIED, PROJECT_REFRESH_NOTIFIED, I18N_NOTIFIED}
        private CountDownLatch latch = new CountDownLatch(1);
        private TestProjectWatchType notificationType = NONE;

        void reset() {
            latch = new CountDownLatch(1);
            notificationType = NONE;
        }

        TestProjectWatchType waitForEvent() throws InterruptedException {
            latch.await(1000, TimeUnit.MILLISECONDS);
            return notificationType;
        }

        @Override
        public void externalChangeToProject() {
            notificationType = EMF_FILE_NOTIFIED;
            latch.countDown();
        }

        @Override
        public void projectRefreshRequired() {
            notificationType = PROJECT_REFRESH_NOTIFIED;
            latch.countDown();

        }

        @Override
        public void i18nFileUpdated(String context) {
            notificationType = I18N_NOTIFIED;
            latch.countDown();
        }
    }
}