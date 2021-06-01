package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import com.thecoderscorner.menu.editorui.project.MenuIdChooserImpl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import static com.thecoderscorner.menu.editorui.cli.CodeGeneratorCommand.persistProject;
import static com.thecoderscorner.menu.editorui.cli.CodeGeneratorCommand.projectFileOrNull;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

@SuppressWarnings("rawtypes")
@Command(name="create-item")
public class CreateItemCommand implements Callable<Integer> {
    @Option(names = {"-f", "--emf-file"}, description = "emf file name")
    private File projectFile;

    @Option(names = {"-p", "--parent"}, required = true, description = "parent id or name")
    String parent;

    @Option(names = {"-e", "--eeprom"}, description = "An EEPROM value, NONE, or AUTO", defaultValue = "NONE")
    String eeprom;

    @Option(names = {"-t", "--type"}, description = "One of analog, enum, boolean, submenu, float, action, list, largenum, text, choice, rgb", required = true)
    String menuType;

    @Option(names = {"-n", "--name"}, description = "Name of the item (19 chars max)", required = true)
    String itemName;

    @Option(names = {"-c", "--callback"}, description = "The callback function or NONE, default NONE")
    String callback;

    @Option(names = {"-v", "--variable"}, description = "The variable name to be appended after menu")
    String varName;

    @Option(names = {"-r", "--readonly"}, description = "Mark the item as readonly")
    boolean readonly;

    @Option(names = {"-l", "--localonly"}, description = "Mark the item as local only")
    boolean localOnly;

    @Option(names = {"-x", "--hide"}, description = "Hide the item from view")
    boolean hide;

    @Override
    public Integer call() {
        try {
            var project = projectFileOrNull(projectFile);
            var chooser = new MenuIdChooserImpl(project.getMenuTree());
            var variableGen = new VariableNameGenerator(project.getMenuTree(), project.getOptions().isNamingRecursive());
            SubMenuItem parentItem = findParent(project.getMenuTree());

            MenuItem item;
            switch (menuType) {
                case "submenu" -> item = doDefaulting(chooser, variableGen, new SubMenuItemBuilder(), false);
                case "float" -> item = doDefaulting(chooser, variableGen, new FloatMenuItemBuilder(), false);
                case "action" -> item = doDefaulting(chooser, variableGen, new ActionMenuItemBuilder(), false);
                case "list" -> item = doDefaulting(chooser, variableGen, new RuntimeListMenuItemBuilder(), false);
                case "analog" -> item = doDefaulting(chooser, variableGen, new AnalogMenuItemBuilder(), true);
                case "enum" -> {
                    var enumBuilder = new EnumMenuItemBuilder();
                    doDefaulting(chooser, variableGen, enumBuilder, true);
                    enumBuilder.withEnumList(List.of("Enum 1", "Enum 2"));
                    item = enumBuilder.menuItem();
                }
                case "boolean" -> item = doDefaulting(chooser, variableGen, new BooleanMenuItemBuilder(), true);
                case "largenum" -> item = doDefaulting(chooser, variableGen, new EditableLargeNumberMenuItemBuilder(), true);
                case "text" -> item = doDefaulting(chooser, variableGen, new EditableTextMenuItemBuilder(), true);
                case "choice" -> item = doDefaulting(chooser, variableGen, new ScrollChoiceMenuItemBuilder(), true);
                case "rgb" -> item = doDefaulting(chooser, variableGen, new Rgb32MenuItemBuilder(), true);
                default -> throw new IllegalArgumentException("Invalid type specified");
            }


            project.getMenuTree().addMenuItem(parentItem, item);

            persistProject(project.getMenuTree(), project.getOptions());

            return 0;
        }
        catch(Exception ex) {
            System.out.println("Error during create item " + ex.getClass().getSimpleName() + " " + ex.getMessage());
            return -1;
        }
    }

    private <T extends MenuItemBuilder, M extends MenuItem> M doDefaulting(
            MenuIdChooser chooser, VariableNameGenerator gen, MenuItemBuilder<T, M> theBuilder, boolean allowRom) {
        int romPos = getEepromPos(chooser);
        int nextId = chooser.nextHighestId();
        String cbFunction = (callback == null || callback.equals("NONE")) ? null : callback;

        theBuilder.withId(nextId)
                .withName(itemName)
                .withVariableName(varName)
                .withFunctionName(cbFunction)
                .withLocalOnly(localOnly)
                .withReadOnly(readonly)
                .withVisible(!hide);
        if(allowRom) theBuilder.withEepromAddr(romPos);


        if(varName == null) {
            var tempItem = theBuilder.menuItem();
            theBuilder.withVariableName(gen.makeNameToVar(tempItem)).menuItem();
        }

        return theBuilder.menuItem();
    }


    private int getEepromPos(MenuIdChooser chooser) {
        if(eeprom == null || eeprom.equals("NONE")) return -1;
        if(eeprom.equals("AUTO")) return chooser.nextHighestEeprom();
        return Integer.parseInt(eeprom);
    }

    private SubMenuItem findParent(MenuTree tree) throws IOException {
        if(parent == null) throw new IOException("parent cannot be null");
        Optional<MenuItem> maybeItem;
        try {
            int id = Integer.parseInt(parent);
            maybeItem = tree.getMenuById(id);
        }
        catch(Exception ex) {
            maybeItem = tree.getAllMenuItems().stream()
                    .filter(it -> it.getName().equals(parent))
                    .findFirst();

        }

        if(maybeItem.isEmpty() || !(maybeItem.get() instanceof SubMenuItem)) {
            throw new IllegalArgumentException("Parent not a sub menu");
        }

        return (SubMenuItem) maybeItem.get();
    }
}
