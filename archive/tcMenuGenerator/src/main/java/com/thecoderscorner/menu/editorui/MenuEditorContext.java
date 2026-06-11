package com.thecoderscorner.menu.editorui;

import com.thecoderscorner.embedcontrol.core.service.TcMenuPersistedConnection;
import com.thecoderscorner.menu.editorui.controller.MenuEditorController;
import com.thecoderscorner.menu.editorui.embed.RemoteConnectionPanel;
import com.thecoderscorner.menu.editorui.storage.MenuEditorConfig;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * This context wraps the application's configuration in such a way that it can be easily mocked as needed. For regular
 * use it is implemented by the App itself, for testing, it is generally mocked and set onto the App. Nothing should
 * use the app functions directly to make testing easier.
 */
public interface MenuEditorContext {


    /**
     * force a refresh of the embed control system, so any new panels are added.
     */
    void embedControlRefresh();

    /**
     * Gets hold of the general application configuration, this is for legacy components that do not use the start up
     * based wiring within the menu configuration itself..
     * @return the context.
     */
    MenuEditorConfig getAppContext();

    /**
     * Creates a new primary tcMenu Designer window that can be used to open and edit menu projects independently.
     * @param stage the stage to display on but normally should be null.
     * @return the editor controller of the new window.
     */
    MenuEditorController createPrimaryWindow(Stage stage) throws IOException;

    /**
     * Sets the theme to either dark or light mode
     * @param mode the theme mode either "lightMode" or "darkMode";
     */
    void setCurrentTheme(String mode);

    /**
     * Handles the creating of a new connection by presenting a suitable dialog for creation.
     * @param stage the stage on which to show.
     */
    void handleCreatingConnection(Stage stage);

    /**
     * @return a list of all menu editor windows that are active.
     */
    Collection<MenuEditorController> getAllMenuEditors();

    /**
     * @return a list of all active remote connection panels, IE ones where there is an active connection.
     */
    Collection<RemoteConnectionPanel> getAllActiveConnections();

    /**
     * @return a list of connections, all connections active or not.
     */
    Collection<TcMenuPersistedConnection> getAvailableConnections();

    /**
     * From an existing connection, this creates an active connection and adds it to the list.
     * @param con the connection detail
     */
    void createEmbedControlPanel(TcMenuPersistedConnection con);

    /**
     * Creates a preview window based on embed control that shows a simulated embedded UI
     * @param path the project to path to be simulated.
     */
    void previewOnProject(Path path);
}
