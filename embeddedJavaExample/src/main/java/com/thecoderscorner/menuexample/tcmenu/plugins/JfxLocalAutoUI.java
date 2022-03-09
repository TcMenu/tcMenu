package com.thecoderscorner.menuexample.tcmenu.plugins;

import com.thecoderscorner.embedcontrol.core.controlmgr.*;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxScreenManager;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.mgr.MenuManagerServer;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.commands.MenuButtonType;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

public class JfxLocalAutoUI extends Application {
    private static final AtomicReference<ApplicationContext> GLOBAL_CONTEXT = new AtomicReference<>(null);
    private MenuManagerServer mgr;
    private LocalTreeComponentManager localTree;
    private JfxScreenManager screenManager;

    public static void setAppContext(ApplicationContext context) {
        GLOBAL_CONTEXT.set(context);
    }

    @Override
    public void start(Stage stage) throws Exception {
        var ctx = GLOBAL_CONTEXT.get();
        mgr = ctx.getBean(MenuManagerServer.class);
        var executor = ctx.getBean(ScheduledExecutorService.class);

        stage.setTitle(mgr.getServerName());
        var border = new BorderPane();
        border.setOpaqueInsets(new Insets(5));
        var scroller = new ScrollPane();
        var settings = new GlobalSettings();
        LocalMenuController localControl = new LocalMenuController();
        screenManager = new JfxScreenManager(localControl, scroller, Platform::runLater, 2);
        localTree = new LocalTreeComponentManager(screenManager, settings, new LocalDialogViewer(), executor,
                Platform::runLater, localControl, mgr);
        border.setCenter(scroller);

        stage.setScene(new Scene(border, 800, 500));
        stage.show();
    }

    class LocalMenuController implements MenuComponentControl {

        @Override
        public CorrelationId editorUpdatedItem(MenuItem menuItem, Object val) {
            MenuItemHelper.setMenuState(menuItem, val, mgr.getManagedMenu());
            mgr.menuItemDidUpdate(menuItem);
            return CorrelationId.EMPTY_CORRELATION;
        }

        @Override
        public CorrelationId editorUpdatedItemDelta(MenuItem menuItem, int delta) {
            MenuItemHelper.applyIncrementalValueChange(menuItem, delta, mgr.getManagedMenu());
            mgr.menuItemDidUpdate(menuItem);
            return CorrelationId.EMPTY_CORRELATION;
        }

        @Override
        public MenuTree getMenuTree() {
            return mgr.getManagedMenu();
        }

        @Override
        public String getConnectionName() {
            return mgr.getServerName();
        }
    }

    private class LocalDialogViewer implements DialogViewer {
        @Override
        public void setButton1(MenuButtonType menuButtonType) {

        }

        @Override
        public void setButton2(MenuButtonType menuButtonType) {

        }

        @Override
        public void show(boolean b) {

        }

        @Override
        public void setText(String s, String s1) {

        }

        @Override
        public void statusHasChanged(AuthStatus authStatus) {

        }
    }
}
