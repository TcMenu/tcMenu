package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.project.FileBasedProjectPersistor;

import java.io.File;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.cli.CodeGeneratorCommand.projectFileOrNull;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

@Command(name="verify")
public class CodeVerificationCommand implements Callable<Integer> {
    @Option(names = {"-f", "--emf-file"}, description = "emf file name")
    private File projectFile;

    @Option(names = {"-v", "--verbose"}, description = "verbose logging")
    private boolean verbose;

    public void log(boolean verboseLogging, String toLog) {
        if(verboseLogging && !verbose) return;
        System.out.println(toLog);
    }

    @Override
    public Integer call()  {
        try {
            log(false, "Starting EEPROM overlap check");

            var project = projectFileOrNull(projectFile);

            var allItems = project.getMenuTree().getAllMenuItems().stream()
                    .filter(itm -> itm.getEepromAddress() != -1)
                    .sorted(Comparator.comparingInt(MenuItem::getEepromAddress))
                    .collect(Collectors.toList());

            log(true, "Evaluating tree, items with EEPROM address: " + allItems.size());


            int currentEepromPosition = 0;
            int currentSize = 2;
            int retCode = 0;
            log(true, "MAGICNO: Item '           Magic Key'           EEPROM(0000) ==");
            for (var item : allItems) {
                int currentMin = currentEepromPosition + currentSize;
                if (item.getEepromAddress() < currentMin) {
                    log(true, String.format("OVERLAP: Item '%20s' ID(%05d) EEPROM(%04d)", item.getName(),
                            item.getId(), item.getEepromAddress()
                    ));
                    retCode = -1;
                } else {
                    log(true, String.format("ITEM OK: Item '%20s' ID(%05d) EEPROM(%04d) %s", item.getName(),
                            item.getId(), item.getEepromAddress(), "=".repeat(Math.max(0, MenuItemHelper.eepromSizeForItem(item)))
                    ));
                }
                currentEepromPosition = item.getEepromAddress();
                currentSize = MenuItemHelper.eepromSizeForItem(item);
            }

            log(false, "EEPROM structure " + (retCode == 0 ? "is OK" : "has overlapping items"));
            return 0;
        }
        catch (Exception ex) {
            System.out.println("Error during verification " + ex.getClass().getSimpleName() + " " + ex.getMessage());
            if(verbose) {
                ex.printStackTrace();
            }

            return -1;
        }
    }

}
