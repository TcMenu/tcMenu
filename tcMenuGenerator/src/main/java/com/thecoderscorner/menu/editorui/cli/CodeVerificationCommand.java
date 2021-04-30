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

    public void log(boolean verboseLogging, String toLog) {
        if(verboseLogging && !verbose) return;
        System.out.println(toLog);
    }

    @Override
    public Integer call() throws Exception {
        if(projectFile == null || !projectFile.exists()) {
            log(false, "Chosen emf file does not exist");
            return -1;
        }

        log(false, "Starting EEPROM overlap check");

        var persistor = new FileBasedProjectPersistor();
        var project = persistor.open(projectFile.getAbsolutePath());

        log(true, "Project has been loaded " + project.getOptions().getApplicationName());

        var allItems = project.getMenuTree().getAllMenuItems().stream()
                .filter(itm -> itm.getEepromAddress() != -1)
                .sorted(Comparator.comparingInt(MenuItem::getEepromAddress))
                .collect(Collectors.toList());

        log(true, "Evaluating tree, items with EEPROM address: " + allItems.size());


        int currentEepromPosition = 0;
        int currentSize = 2;
        int retCode = 0;
        for(var item : allItems) {
            int currentMin = currentEepromPosition + currentSize;
            if(item.getEepromAddress() < currentMin) {
                log(true, String.format("OVERLAP: Item '%20s' ID(%5d) EEPROM(%5d)", item.getName(),
                        item.getId(), item.getEepromAddress()
                ));
                retCode = -1;
            }
            else {
                log(true, String.format("ITEM OK: Item '%20s' ID(%5d) EEPROM(%5d) %s", item.getName(),
                        item.getId(), item.getEepromAddress(), "=".repeat(Math.max(0, MenuItemHelper.eepromSizeForItem(item)))
                ));
            }
            currentEepromPosition = item.getEepromAddress();
            currentSize = MenuItemHelper.eepromSizeForItem(item);
        }

        log(true, "Returning with code " + retCode);

        return retCode;
    }

}
