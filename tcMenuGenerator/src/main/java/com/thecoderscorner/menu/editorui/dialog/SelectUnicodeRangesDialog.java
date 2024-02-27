package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.controller.SelectUnicodeRangesController;
import com.thecoderscorner.menu.editorui.gfxui.font.UnicodeBlockMapping;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.Set;

public class SelectUnicodeRangesDialog extends BaseDialogSupport<SelectUnicodeRangesController> {

    private final Set<UnicodeBlockMapping> selectedRegions;

    public SelectUnicodeRangesDialog(Stage stage, Set<UnicodeBlockMapping> selectedRegions) {
        this.selectedRegions = selectedRegions;
        tryAndCreateDialog(stage, "/ui/selectUnicodeRanges.fxml", bundle.getString("unicode.select.ranges"), true);
    }

    @Override
    protected void initialiseController(SelectUnicodeRangesController controller) throws Exception {
        controller.initialise(selectedRegions);
    }

    public Optional<Set<UnicodeBlockMapping>> getBlockMappings() {
        return controller.getBlockMappings();
    }
}
