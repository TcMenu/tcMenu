package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.AbstractMenuItemVisitor;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.dialog.AboutDialog;
import com.thecoderscorner.menu.editorui.dialog.NewItemDialog;
import com.thecoderscorner.menu.editorui.dialog.RomLayoutDialog;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.ui.CodeGeneratorDialog;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import com.thecoderscorner.menu.editorui.project.MenuIdChooserImpl;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;
import java.util.function.BiConsumer;

public class CurrentProjectEditorUIImpl implements CurrentProjectEditorUI {

    private Stage mainStage;

    public CurrentProjectEditorUIImpl(Stage mainStage) {
        this.mainStage = mainStage;
    }

    @Override
    public Optional<String> findFileNameFromUser(boolean open) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a Menu File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Embedded menu", "*.emf"));
        File f;
        if (open) {
            f = fileChooser.showOpenDialog(mainStage);
        } else {
            f = fileChooser.showSaveDialog(mainStage);
        }

        if (f != null) {
            return Optional.of(f.getPath());
        }
        return Optional.empty();
    }

    @Override
    public void alertOnError(String heading, String description) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(heading);
        alert.setHeaderText(heading);
        alert.setContentText(description);
        alert.showAndWait();
    }

    @Override
    public boolean questionYesNo(String title, String header) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    @Override
    public void setTitle(String s) {
        mainStage.setTitle(s);
    }

    @Override
    public Optional<MenuItem> showNewItemDialog(MenuTree tree) {
        NewItemDialog dlg = new NewItemDialog(mainStage, tree, this);
        return dlg.showAndWait();
    }

    @Override
    public void showRomLayoutDialog(MenuTree tree) {
        RomLayoutDialog rld = new RomLayoutDialog(mainStage, tree);
        rld.showAndWait();
    }

    @Override
    public void showAboutDialog(ArduinoLibraryInstaller installer) {
        AboutDialog ad = new AboutDialog(mainStage, installer);
        ad.showAndWait();
    }

    @Override
    public void showCodeGeneratorDialog(CurrentEditorProject project, ArduinoLibraryInstaller installer) {
        CodeGeneratorDialog dialog = new CodeGeneratorDialog();
        dialog.showCodeGenerator(mainStage, project, installer);
    }


    public Optional<UIMenuItem> createPanelForMenuItem(MenuItem menuItem, MenuTree tree, BiConsumer<MenuItem, MenuItem> changeConsumer) {
        RenderingChooserVisitor renderingChooserVisitor = new RenderingChooserVisitor(changeConsumer, tree);
        return MenuItemHelper.visitWithResult(menuItem, renderingChooserVisitor);
    }

    class RenderingChooserVisitor extends AbstractMenuItemVisitor<UIMenuItem> {

        private final BiConsumer<MenuItem,MenuItem> changeConsumer;
        private final MenuIdChooser menuIdChooser;

        RenderingChooserVisitor(BiConsumer<MenuItem, MenuItem> changeConsumer, MenuTree tree) {
            this.changeConsumer = changeConsumer;
            this.menuIdChooser = new MenuIdChooserImpl(tree);
        }

        @Override
        public void visit(AnalogMenuItem item) {
            setResult(new UIAnalogMenuItem(item, menuIdChooser, changeConsumer));
        }

        @Override
        public void visit(TextMenuItem item) {
            setResult(new UITextMenuItem(item, menuIdChooser, changeConsumer));
        }

        @Override
        public void visit(EnumMenuItem item) {
            setResult(new UIEnumMenuItem(item, menuIdChooser, changeConsumer));
        }

        @Override
        public void visit(BooleanMenuItem item) {
            setResult(new UIBooleanMenuItem(item, menuIdChooser, changeConsumer));
        }

        @Override
        public void visit(RemoteMenuItem item) {
            setResult(new UIRemoteMenuItem(item, menuIdChooser, changeConsumer));
        }

        @Override
        public void visit(FloatMenuItem item) {
            setResult(new UIFloatMenuItem(item, menuIdChooser, changeConsumer));
        }

        @Override
        public void visit(ActionMenuItem item) {
            setResult(new UIActionMenuItem(item, menuIdChooser, changeConsumer));
        }

        @Override
        public void visit(SubMenuItem item) {
            if(!MenuTree.ROOT.equals(item)) {
                setResult(new UISubMenuItem(item, menuIdChooser, changeConsumer));
            }
        }
    }
}
