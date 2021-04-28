package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.project.FileBasedProjectPersistor;
import picocli.CommandLine;

import java.io.File;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@CommandLine.Command(name="verify")
public class CodeVerificationCommand implements Callable<Integer> {
    @CommandLine.Option(names = {"-f", "--emf-file"}, description = "emf file name", required = true)
    private File projectFile;

    @CommandLine.Option(names = {"-v", "--verbose"}, description = "verbose logging")
    private boolean verbose;

    @Override
    public Integer call() throws Exception {
        if(projectFile == null || !projectFile.exists()) {
            System.out.println("Chosen emf file does not exist");
            return -1;
        }
        var persistor = new FileBasedProjectPersistor();
        var project = persistor.open(projectFile.getAbsolutePath());

        var allItems = project.getMenuTree().getAllMenuItems().stream()
                .filter(itm -> itm.getEepromAddress() != -1)
                .sorted(Comparator.comparingInt(MenuItem::getEepromAddress))
                .collect(Collectors.toList());

        int currentEepromPosition = 0;
        int currentSize = 2;
        int retCode = 0;
        for(var item : allItems) {
            int currentMin = currentEepromPosition + currentSize;
            if(item.getEepromAddress() < currentMin) {
                System.out.format("Item %s overlaps, location %d", item.getName(), item.getEepromAddress());
                retCode = -1;
            }
            currentEepromPosition = item.getEepromAddress();
            currentSize = MenuItemHelper.eepromSizeForItem(item);
        }
        return retCode;
    }
}
