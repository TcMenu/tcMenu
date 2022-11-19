package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.controller.EditMenuInMenuItemController;
import com.thecoderscorner.menu.editorui.controller.SelectUnicodeRangesController;
import com.thecoderscorner.menu.editorui.generator.font.LoadedFont;
import com.thecoderscorner.menu.editorui.generator.font.UnicodeBlockMapping;
import com.thecoderscorner.menu.editorui.generator.parameters.MenuInMenuDefinition;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.Set;

public class SelectUnicodeRangesDialog extends BaseDialogSupport<SelectUnicodeRangesController> {

    private final LoadedFont loadedFont;
    private Set<UnicodeBlockMapping> selectedRegions;

    public SelectUnicodeRangesDialog(Stage stage, LoadedFont loadedFont, Set<UnicodeBlockMapping> selectedRegions) {
        this.loadedFont = loadedFont;
        this.selectedRegions = selectedRegions;
        tryAndCreateDialog(stage, "/ui/selectUnicodeRanges.fxml", "Select Unicode Regions", true);
    }

    @Override
    protected void initialiseController(SelectUnicodeRangesController controller) throws Exception {
        controller.initialise(loadedFont, selectedRegions);
    }

    public Optional<Set<UnicodeBlockMapping>> getBlockMappings() {
        return controller.getBlockMappings();
    }
}
