package com.thecoderscorner.menu.persist;

import com.thecoderscorner.menu.domain.state.AnyMenuState;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class PropertiesMenuStateSerialiser implements MenuStateSerialiser{
    System.Logger logger = System.getLogger(PropertiesMenuStateSerialiser.class.getSimpleName());
    private final MenuTree tree;
    private final Path propertiesFile;

    public PropertiesMenuStateSerialiser(MenuTree tree, Path propertiesFile) {
        this.tree = tree;
        this.propertiesFile = propertiesFile;
    }

    @Override
    public void loadMenuStatesAndApply() {
        var allStates = loadMenuStates();
        for(var state : allStates) {
            tree.changeItem(state.getItem(), state);
        }
    }

    @Override
    public List<AnyMenuState> loadMenuStates() {
        logger.log(INFO, "Load menu state");
        Properties properties = new Properties();
        var states = new ArrayList<AnyMenuState>();
        try(var propFile = new FileInputStream(propertiesFile.toFile())) {
            properties.load(propFile);
            properties.forEach((key, value) -> {
                var possibleId = tryParseIntKey(key);
                if(possibleId.isPresent() && tree.getMenuById(possibleId.get()).isPresent()) {
                    var item = tree.getMenuById(possibleId.get()).get();
                    states.add(MenuItemHelper.stateForMenuItem(item, value.toString().trim(), false, false));
                }
            });
            return states;
        }
        catch (Exception ex) {
            logger.log(ERROR, "Error loading menu state", ex);
            return Collections.emptyList();
        }
    }

    private Optional<Integer> tryParseIntKey(Object key) {
        try {
            return Optional.of(Integer.parseInt(key.toString()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Override
    public void saveMenuStates() {
        logger.log(INFO, "Save menu state");
        Properties properties = new Properties();
        try(var propFile = new FileWriter(propertiesFile.toFile())) {
            tree.getAllMenuItems().stream()
                    .filter(item -> item.getEepromAddress() != -1 && MenuItemHelper.eepromSizeForItem(item) != 0)
                    .filter(item -> tree.getMenuState(item) != null)
                    .forEach(item -> {
                        var state = tree.getMenuState(item);
                        if(state.getValue() != null) {
                            properties.setProperty(Integer.toString(item.getId()), state.getValue().toString());
                        }
                    });
            properties.store(propFile, "TcMenu Menu States saved on " + LocalDateTime.now());
        }
        catch(Exception ex) {
            logger.log(ERROR, "Error saving property state", ex);
        }
    }
}
