/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.embed;

/**
 * This is the window controller, responsible for all actions to do the main window. It listens to change
 * from the tcMenu remote control and renders them to the display, and sends commands to the Arduino when
 * changes are made locally.
 * <p>
 * I've tried to keep this class as simple as possible, avoiding functional style and optimisation.
 * <p>
 * I've also avoided doing any more styling than absolutely required so the UI looks quite basic, get
 * out your crayons and make your own look pretty!
 * <p>
 * You'll notice that tcMenu JavaAPI does make use of both immutability and the 'visitor pattern', this
 * allows you to use most of the API objects on any thread.
 */
public class MainWindowController {
   /* private final System.Logger logger = System.getLogger(MainWindowController.class.getSimpleName());
    //
    // Begin JavaFX field bindings.
    //
    public Label statusLabel;
    public BorderPane mainBorderPane;
    public Label versionField;
    public ListView<PanelPresentable<Node>> connectionList;
    public BorderPane detailPane;
    private GlobalSettings settings;
    private PanelPresentable currentlyDisplayed;

    public void initialise(GlobalSettings settings, ObservableList<PanelPresentable<Node>> initialPanels, VersionHelper versionHelper) {
        this.settings = settings;

        versionField.setText("Version " + versionHelper.getVersion());

        connectionList.setCellFactory(list -> new PanelPresentableListCell());
        connectionList.setItems(initialPanels);
        connectionList.getSelectionModel().selectedItemProperty().addListener((observableValue, oldVal, newVal) -> {
            if(newVal != null) {
                try {
                    logger.log(INFO, "Change panel to ", newVal.getPanelName());
                    if(currentlyDisplayed != null) {
                        currentlyDisplayed.closePanel();
                    }
                    clearBorderPanel();
                    logger.log(INFO, "Present new panel ", newVal.getPanelName());
                    detailPane.setCenter(newVal.getPanelToPresent(detailPane.getWidth()));
                    currentlyDisplayed = newVal;
                } catch (Exception e) {
                    logger.log(ERROR, "Failed to open the new panel", e);
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Form did not load", ButtonType.CLOSE);
                    alert.showAndWait();
                }
            }
        });
        connectionList.getSelectionModel().select(0);
    }

    private void clearBorderPanel() {
        detailPane.setTop(null);
        detailPane.setCenter(null);
        detailPane.setBottom(null);
        detailPane.setRight(null);
    }

    public void panelsChanged(ObservableList<PanelPresentable<Node>> panels, Optional<PanelPresentable<Node>> maybePanelForSelect) {
        connectionList.setItems(panels);
        if(maybePanelForSelect.isPresent()) {
            connectionList.getSelectionModel().select(maybePanelForSelect.get());
        } else {
            connectionList.getSelectionModel().select(0);
        }
    }

    public void refreshAllPanels() {
        connectionList.refresh();
    }

    private static class PanelPresentableListCell extends ListCell<PanelPresentable<Node>> {
        @Override
        public void updateItem(PanelPresentable item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null) {
                setText("");
            }
            else {
                setText(item.getPanelName());
            }
        }
    }*/
}

