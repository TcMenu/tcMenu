package com.thecoderscorner.embedcontrol.customization;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentPositioning;
import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.persist.XMLDOMHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.text.html.Option;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

public class ScreenLayoutPersistence {
    public static final String TEXT_COLOR_ELEMENT = "TextColor";
    public static final String BUTTON_COLOR_ELEMENT = "ButtonColor";
    public static final String UPDATE_COLOR_ELEMENT = "UpdateColor";
    private static final String HIGHLIGHT_COLOR_ELEMENT = "HighlightColor";
    public static final String GRID_SIZE_ELEMENT = "GridSize";
    public static final String RECURSIVE_ELEMENT = "Recursive";
    public static final String FONT_SIZE_ELEMENT = "FontSize";
    public static final String SUB_MENU_SETTINGS_ELEMENT = "SubMenuSettings";
    public static final String SUB_MENU_SETTING_ELEMENT = "SubMenuSetting";
    public static final String ITEM_OVERRIDE_SETTING_ELEMENT = "ItemOverride";

    public static final String RECURSIVE_ATTRIBUTE = "recursive";
    public static final String ROOTID_ATTRIBUTE = "rootId";
    public static final String IS_PRESENT_ATTRIBUTE = "isPresent";
    private static final String ROW_POSITION_ATTRIBUTE = "rowPosition";
    private static final String COL_POSITION_ATTRIBUTE = "colPosition";
    private static final String COL_SPAN_ATTRIBUTE = "colSpan";
    private static final String JUSTIFICATION_ATTRIBUTE = "justification";
    private static final String DRAW_MODE_ATTRIBUTE = "drawingMode";
    private static final String FONT_SIZE_ATTR = "fontSize";

    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final Map<Integer, ComponentSettingsForSub> settingsMap = new HashMap<>();
    private final GlobalSettings globalSettings;
    private final MenuTree tree;
    private final Consumer<Element> serializeAdditional;
    private final Path layoutPath;
    private int fontSize;
    private int gridSize;
    private boolean globalRecursive;
    private final List<ColumnLayoutHolder> columnPositions = new ArrayList<>();

    public ScreenLayoutPersistence(MenuTree tree, GlobalSettings settings, UUID appUuid, Path path, int defFontSize,
                                   Consumer<Element> serializeAdditional) {
        this.globalSettings = settings;
        this.tree = tree;
        this.serializeAdditional = serializeAdditional;
        this.layoutPath = path.resolve(appUuid.toString().toLowerCase(Locale.ROOT) + "-layout.xml");
        try {
            if(Files.exists(layoutPath)) {
                var doc = XMLDOMHelper.loadDocumentFromPath(layoutPath);
                globalRecursive = XMLDOMHelper.textOfElementByName(doc.getDocumentElement(), RECURSIVE_ELEMENT).equals("true");
                gridSize = XMLDOMHelper.integerOfElementByName(doc.getDocumentElement(), GRID_SIZE_ELEMENT, 2);
                fontSize = XMLDOMHelper.integerOfElementByName(doc.getDocumentElement(), FONT_SIZE_ELEMENT, defFontSize);
                var layouts = XMLDOMHelper.transformElements(doc.getDocumentElement(), SUB_MENU_SETTINGS_ELEMENT, SUB_MENU_SETTING_ELEMENT, this::transformSubMenu);
                for(var layout : layouts) {
                    settingsMap.put(layout.rootItemId(), layout);
                }
                return;
            }
        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR, "Layout restore failed for " + appUuid, e);
        }
        gridSize = 2;
        fontSize = defFontSize;
        globalRecursive = false;
    }

    public int getGridSize() {
        return gridSize;
    }

    public boolean isRecursive(MenuItem item) {
        if(settingsMap.containsKey(item.getId())) return settingsMap.get(item.getId()).recursive();
        return globalRecursive;
    }

    public void resetAutoLayout() {
        columnPositions.clear();
    }

    public ComponentSettings getSettingsForStaticItem(MenuItem parent, int itemId, boolean forceNewRow) {
        var parentOverrides = settingsMap.get(parent.getId());
        if(parentOverrides != null) {
            return autoScreenLayout(
                    parent, itemId,
                    parentOverrides.textColor().orElse(globalSettings.getTextColor()),
                    parentOverrides.buttonColor().orElse(globalSettings.getButtonColor()),
                    parentOverrides.updateColor().orElse(globalSettings.getUpdateColor()),
                    parentOverrides.highlightColor().orElse(globalSettings.getHighlightColor()),
                    forceNewRow
            );
        }
        else {
            return autoScreenLayout(parent, itemId, globalSettings.getTextColor(), globalSettings.getButtonColor(),
                    globalSettings.getUpdateColor(), globalSettings.getHighlightColor(), forceNewRow);
        }

    }

    public ComponentSettings getSettingsForMenuItem(MenuItem item, boolean forceNewRow) {
        var parent = tree.findParent(item);
        var parentOverrides = settingsMap.get(parent.getId());
        if(parentOverrides != null) {
            var itemOverride = parentOverrides.overrideFor(item);
            if(itemOverride.isPresent()) {
                return itemOverride.get();
            } else {
                return autoScreenLayout(
                        item, item.getId(),
                        parentOverrides.textColor().orElse(globalSettings.getTextColor()),
                        parentOverrides.buttonColor().orElse(globalSettings.getButtonColor()),
                        parentOverrides.updateColor().orElse(globalSettings.getUpdateColor()),
                        parentOverrides.highlightColor.orElse(globalSettings.getHighlightColor()),
                        forceNewRow
                );
            }
        } else {
            return autoScreenLayout(item, item.getId(), globalSettings.getTextColor(), globalSettings.getButtonColor(),
                    globalSettings.getUpdateColor(), globalSettings.getHighlightColor(), forceNewRow);
        }
    }

    private ComponentSettings autoScreenLayout(MenuItem item, int id, ControlColor textColor, ControlColor buttonColor,
                                               ControlColor updateColor, ControlColor highlightColor, boolean forceNewRow) {
        Optional<MenuIdWithSpace> spaceRequirement = Optional.empty();
        int row = 0;
        if(!forceNewRow) {
            for (int i = 0; i < columnPositions.size(); i++) {
                spaceRequirement = columnPositions.get(i).nextColumn(id, defaultSpacesForItem(item));
                if (spaceRequirement.isPresent()) {
                    row = i;
                    break;
                }
            }
        }

        if(spaceRequirement.isEmpty()) {
            var newCol = new ColumnLayoutHolder(gridSize);
            columnPositions.add(newCol);
            spaceRequirement = newCol.nextColumn(id, forceNewRow ? (gridSize - 1) : defaultSpacesForItem(item));
            row = columnPositions.size() - 1;
        }

        if(spaceRequirement.isEmpty()) throw new IllegalStateException("Unable to allocate space for " + item);

        ComponentPositioning position = new ComponentPositioning(row, spaceRequirement.get().start(), 1, spaceRequirement.get().colsTaken());
        ConditionalColoring condColoring = new ConfigurableConditionalColoring(globalSettings, textColor, buttonColor, updateColor, highlightColor);
        return new ComponentSettings(
                condColoring, defaultFontSizeForItem(item), defaultJustificationForItem(item),
                position, defaultRedrawModeForItem(item), false);
    }

    protected EditorComponent.RedrawingMode defaultRedrawModeForItem(MenuItem item) {
        return EditorComponent.RedrawingMode.SHOW_NAME_VALUE;
    }

    protected EditorComponent.PortableAlignment defaultJustificationForItem(MenuItem item) {
        return EditorComponent.PortableAlignment.CENTER;
    }

    protected int defaultFontSizeForItem(MenuItem item) {
        var parent = tree.findParent(item);
        var parentOverrides = settingsMap.get(parent.getId());
        if (parentOverrides != null) {
            var itemOverride = parentOverrides.overrideFor(item);
            return (itemOverride.isPresent()) ? itemOverride.get().getFontSize() : parentOverrides.fontSize();
        }
        return fontSize;
    }

    protected int defaultSpacesForItem(MenuItem item) {
        return gridSize;
    }

    private ComponentSettingsForSub transformSubMenu(Element element) {
        boolean isRecursive = XMLDOMHelper.getAttributeOrDefault(element, RECURSIVE_ATTRIBUTE, "false").equals("true");
        int rootId = Integer.parseInt(XMLDOMHelper.getAttributeOrDefault(element, ROOTID_ATTRIBUTE, "0"));

        var settings = XMLDOMHelper.transformElements(element, ITEM_OVERRIDE_SETTING_ELEMENT, this::settingsTransformer);

        return new ComponentSettingsForSub(
                rootId, isRecursive,
                parseColors(element, TEXT_COLOR_ELEMENT),
                parseColors(element, BUTTON_COLOR_ELEMENT),
                parseColors(element, UPDATE_COLOR_ELEMENT),
                parseColors(element, HIGHLIGHT_COLOR_ELEMENT),
                Integer.parseInt(XMLDOMHelper.getAttributeOrDefault(element, FONT_SIZE_ATTR, fontSize)),
                settings
        );
    }

    private ComponentSettingsWithMenuId settingsTransformer(Element element) {
        var menuId = Integer.parseInt(element.getAttribute("menuId"));
        return new ComponentSettingsWithMenuId(menuId, ComponentSettings.NO_COMPONENT, Optional.empty(),
                Optional.empty(),Optional.empty(),Optional.empty());
    }

    private Optional<ControlColor> parseColors(Element element, String clrName) {
        var colorData = XMLDOMHelper.elementWithName(element, clrName);
        if(colorData == null) return Optional.empty();
        if(Boolean.parseBoolean(XMLDOMHelper.getAttributeOrDefault(colorData, IS_PRESENT_ATTRIBUTE, "false"))) {
            var portableFg = new PortableColor(colorData.getAttribute("fg"));
            var portableBg = new PortableColor(colorData.getAttribute("bg"));
            return Optional.of(new ControlColor(portableFg, portableBg));
        }
        else {
            return Optional.empty();
        }
    }

    public void serialiseAll() {
        try(var output = new BufferedOutputStream(new FileOutputStream(layoutPath.toFile())) ) {
            Document doc = XMLDOMHelper.newDocumentRoot("LayoutPersistence");
            XMLDOMHelper.appendElementWithNameValue(doc.getDocumentElement(), GRID_SIZE_ELEMENT, gridSize);
            XMLDOMHelper.appendElementWithNameValue(doc.getDocumentElement(), RECURSIVE_ELEMENT, globalRecursive);
            XMLDOMHelper.appendElementWithNameValue(doc.getDocumentElement(), FONT_SIZE_ELEMENT, fontSize);

            var subSettings = doc.createElement(SUB_MENU_SETTINGS_ELEMENT);
            doc.getDocumentElement().appendChild(subSettings);
            for(var subSetting : settingsMap.entrySet()) {
                var subElement = doc.createElement(SUB_MENU_SETTING_ELEMENT);
                subSettings.setAttribute(ROOTID_ATTRIBUTE, Integer.toString(subSetting.getKey()));
                subSettings.setAttribute(FONT_SIZE_ATTR, Integer.toString(subSetting.getValue().fontSize()));
                subSettings.setAttribute(RECURSIVE_ATTRIBUTE, Boolean.toString(subSetting.getValue().recursive()));
                subSettings.appendChild(subElement);
                saveControlColor(subElement, TEXT_COLOR_ELEMENT, subSetting.getValue().textColor());
                saveControlColor(subElement, BUTTON_COLOR_ELEMENT, subSetting.getValue().buttonColor());
                saveControlColor(subElement, UPDATE_COLOR_ELEMENT, subSetting.getValue().updateColor());
                saveControlColor(subElement, HIGHLIGHT_COLOR_ELEMENT, subSetting.getValue().highlightColor());
                for(var override : subSetting.getValue().menuIdLevelOverrides()) {
                    serializeSetting(subElement, tree.getMenuById(override.menuId()).orElseThrow(), override);
                }
            }
            serializeAdditional.accept(doc.getDocumentElement());
            XMLDOMHelper.writeXml(doc, output);
        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR, "Unable to save state for " + layoutPath, e);
        }

    }

    private void saveControlColor(Element ele, String colorName, Optional<ControlColor> maybeColor) {
        var colorElement = XMLDOMHelper.appendElementWithNameValue(ele, colorName, null);
        if(maybeColor.isPresent()) {
            colorElement.setAttribute(IS_PRESENT_ATTRIBUTE, "true");
            colorElement.setAttribute("fg", maybeColor.get().getFg().toString());
            colorElement.setAttribute("bg", maybeColor.get().getBg().toString());

        } else {
            colorElement.setAttribute(IS_PRESENT_ATTRIBUTE, "false");
        }
    }

    void serializeSetting(Element element, MenuItem item, ComponentSettingsWithMenuId settings) {
        if(settings.settings().isCustomised()) {
            logger.log(System.Logger.Level.INFO, "Persisting settings for " + item.getId());
            var itemEle = XMLDOMHelper.appendElementWithNameValue(element, ITEM_OVERRIDE_SETTING_ELEMENT, null);
            saveControlColor(itemEle, TEXT_COLOR_ELEMENT, settings.textColor());
            saveControlColor(itemEle, BUTTON_COLOR_ELEMENT, settings.buttonColor());
            saveControlColor(itemEle, UPDATE_COLOR_ELEMENT, settings.updateColor());
            saveControlColor(itemEle, HIGHLIGHT_COLOR_ELEMENT, settings.highlightColor());
            itemEle.setAttribute(JUSTIFICATION_ATTRIBUTE, settings.settings().getJustification().toString());
            itemEle.setAttribute(DRAW_MODE_ATTRIBUTE, settings.settings().getDrawMode().toString());
            itemEle.setAttribute(FONT_SIZE_ELEMENT, String.valueOf(settings.settings().getFontSize()));
            itemEle.setAttribute(COL_POSITION_ATTRIBUTE, String.valueOf(settings.settings().getPosition().getCol()));
            itemEle.setAttribute(COL_SPAN_ATTRIBUTE, String.valueOf(settings.settings().getPosition().getColSpan()));
            itemEle.setAttribute(ROW_POSITION_ATTRIBUTE, String.valueOf(settings.settings().getPosition().getRow()));
        } else {
            logger.log(System.Logger.Level.DEBUG, "Not persisting a non-custom setting for " + item.getId());
        }

    }

    public record ComponentSettingsWithMenuId(int menuId, ComponentSettings settings, Optional<ControlColor> textColor,
                                              Optional<ControlColor> buttonColor, Optional<ControlColor> updateColor,
                                              Optional<ControlColor> highlightColor) {
    }

    public record ComponentSettingsForSub(int rootItemId, boolean recursive, Optional<ControlColor> textColor,
                                          Optional<ControlColor> buttonColor, Optional<ControlColor> updateColor,
                                          Optional<ControlColor> highlightColor, int fontSize,
                                          List<ComponentSettingsWithMenuId> menuIdLevelOverrides) {

        public Optional<ComponentSettings> overrideFor(MenuItem item) {
             return menuIdLevelOverrides.stream()
                     .filter(it -> it.menuId() == item.getId())
                     .map(ComponentSettingsWithMenuId::settings)
                     .findFirst();
        }
    }

    public class ColumnLayoutHolder {
        private final int gridSize;
        private final boolean rowIsAuto;
        private final List<MenuIdWithSpace> positionsInRow;

        public ColumnLayoutHolder(int gridSize) {
            this.gridSize = gridSize;
            this.rowIsAuto = false;
            this.positionsInRow = new ArrayList<>();
        }

        public ColumnLayoutHolder(List<MenuIdWithSpace> autoTakenSpaces, int gridSize) {
            this.rowIsAuto = true;
            this.gridSize = gridSize;
            positionsInRow = List.copyOf(autoTakenSpaces);
        }

        public Optional<MenuIdWithSpace> nextColumn(int id, int spaces) {
            if(rowIsAuto) return Optional.empty();

            int start = 0;
            for(var pos : positionsInRow) {
                start += pos.colsTaken();
            }
            if((start + spaces) > gridSize) return Optional.empty();

            var newItem = new MenuIdWithSpace(id, start, spaces);
            positionsInRow.add(newItem);
            return Optional.of(newItem);
        }

        List<MenuIdWithSpace> menuItemsOnLine() {
            return Collections.unmodifiableList(positionsInRow);
        }
    }

    record MenuIdWithSpace(int menuId, int start, int colsTaken) {
    }
}
