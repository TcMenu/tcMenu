package com.thecoderscorner.embedcontrol.customization;

import com.thecoderscorner.embedcontrol.core.controlmgr.*;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.persist.XMLDOMHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.thecoderscorner.menu.domain.util.MenuItemHelper.asSubMenu;

public class ScreenLayoutPersistence {
    public static final int DEFAULT_GRID_SIZE = 4;
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

    public static final String CONTROL_TYPE_ATTRIBUTE = "controlType";
    public static final String RECURSIVE_ATTRIBUTE = "recursive";
    public static final String ROOTID_ATTRIBUTE = "rootId";
    public static final String IS_PRESENT_ATTRIBUTE = "isPresent";
    private static final String ROW_POSITION_ATTRIBUTE = "rowPosition";
    private static final String COL_POSITION_ATTRIBUTE = "colPosition";
    private static final String COL_SPAN_ATTRIBUTE = "colSpan";
    private static final String JUSTIFICATION_ATTRIBUTE = "justification";
    private static final String DRAW_MODE_ATTRIBUTE = "drawingMode";
    private static final String FONT_SIZE_ATTR = "fontSize";

    protected final System.Logger logger = System.getLogger(getClass().getSimpleName());
    protected final Map<Integer, ComponentSettingsForSub> settingsMap = new HashMap<>();
    protected final GlobalSettings globalSettings;
    protected MenuTree tree;
    private Path rootPath;
    private Path layoutPath;
    private int fontSize;
    private int gridSize;
    private boolean globalRecursive;
    private final List<ColumnLayoutHolder> columnPositions = new ArrayList<>();
    protected UUID remoteUuid;

    public ScreenLayoutPersistence(MenuTree tree, GlobalSettings settings, UUID appUuid, Path path, int defFontSize) {
        this.globalSettings = settings;
        this.tree = tree;
        rootPath = path;
        fontSize = defFontSize;
        gridSize = DEFAULT_GRID_SIZE;
        this.layoutPath = uuidToFileName(rootPath, appUuid);
        this.remoteUuid = appUuid;
    }

    public static Path uuidToFileName(Path root, UUID uuid) {
        return root.resolve(uuid.toString().toLowerCase(Locale.ROOT) + "-layout.xml");
    }

    public void loadApplicationData() {
        try {
            if(Files.exists(layoutPath)) {
                var doc = XMLDOMHelper.loadDocumentFromPath(layoutPath);
                globalRecursive = XMLDOMHelper.textOfElementByName(doc.getDocumentElement(), RECURSIVE_ELEMENT).equals("true");
                gridSize = XMLDOMHelper.integerOfElementByName(doc.getDocumentElement(), GRID_SIZE_ELEMENT, DEFAULT_GRID_SIZE);
                fontSize = XMLDOMHelper.integerOfElementByName(doc.getDocumentElement(), FONT_SIZE_ELEMENT, fontSize);
                var layouts = XMLDOMHelper.transformElements(doc.getDocumentElement(), SUB_MENU_SETTINGS_ELEMENT, SUB_MENU_SETTING_ELEMENT, this::transformSubMenu);
                for(var layout : layouts) {
                    settingsMap.put(layout.rootItemId(), layout);
                }
                loadApplicationSpecific(doc.getDocumentElement());
                return;
            }
        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR, "Layout restore failed for " + layoutPath, e);
        }
        gridSize = DEFAULT_GRID_SIZE;
        globalRecursive = false;
    }

    public void remoteApplicationDidLoad(UUID appUuid, MenuTree tree) {
        this.remoteUuid = appUuid;
        this.layoutPath = uuidToFileName(rootPath, appUuid);
        this.tree = tree;
        resetAutoLayout();
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
            return autoScreenLayout(parent, Optional.empty(), itemId, forceNewRow);
        }
        else {
            return autoScreenLayout(parent, Optional.empty(), itemId, forceNewRow);
        }

    }

    public ComponentSettings getSettingsForMenuItem(MenuItem item, boolean forceNewRow) {
        var parent = tree.findParent(item);
        var parentOverrides = settingsMap.get(parent.getId());
        if(parentOverrides != null) {
            var itemOverride = parentOverrides.overrideFor(item.getId());
            if(itemOverride.isPresent()) {
                var ovr = itemOverride.get();
                putIntoRowColLayout(item.getId(), ovr);
                return ovr;
            } else {
                return autoScreenLayout(parent, Optional.of(item), item.getId(), forceNewRow);
            }
        } else {
            return autoScreenLayout(parent, Optional.of(item), item.getId(), forceNewRow);
        }
    }

    private void putIntoRowColLayout(int id, ComponentSettings ovr) {
        ColumnLayoutHolder holder;
        if(columnPositions.size() > ovr.getPosition().getRow()) {
            holder = columnPositions.get(ovr.getPosition().getRow());
            if(holder.isAuto()) {
                holder = new ColumnLayoutHolder(gridSize, false);
                columnPositions.add(ovr.getPosition().getRow(), holder);
            }
        } else {
            while(columnPositions.size() < ovr.getPosition().getRow()) {
                columnPositions.add(new ColumnLayoutHolder(gridSize, true));
            }
            holder = new ColumnLayoutHolder(gridSize, false);
            columnPositions.add(ovr.getPosition().getRow(), holder);
        }
        holder.configuredColumn(id, ovr.getPosition().getCol(), ovr.getPosition().getColSpan());

    }

    private ComponentSettings autoScreenLayout(MenuItem parent, Optional<MenuItem> maybeItem, int id, boolean forceNewRow) {
        Optional<MenuIdWithSpace> spaceRequirement = Optional.empty();
        int row = 0;
        if(!forceNewRow) {
            for (int i = 0; i < columnPositions.size(); i++) {
                spaceRequirement = columnPositions.get(i).nextColumn(id, defaultSpacesForItem(maybeItem));
                if (spaceRequirement.isPresent()) {
                    row = i;
                    break;
                }
            }
        }

        if(spaceRequirement.isEmpty()) {
            var newCol = new ColumnLayoutHolder(gridSize, true);
            columnPositions.add(newCol);
            spaceRequirement = newCol.nextColumn(id, forceNewRow ? gridSize : defaultSpacesForItem(maybeItem));
            row = columnPositions.size() - 1;
        }

        if(spaceRequirement.isEmpty()) throw new IllegalStateException("Unable to allocate space for " + maybeItem);

        ComponentPositioning position = new ComponentPositioning(row, spaceRequirement.get().start(), 1, spaceRequirement.get().colsTaken());
        ConditionalColoring condColoring = new ScreenLayoutBasedConditionalColor(globalSettings, this, id, parent);
        return new ComponentSettings(
                condColoring, defaultFontSizeForItem(maybeItem), defaultJustificationForItem(maybeItem),
                position, defaultRedrawModeForItem(maybeItem), getDefaultControlType(maybeItem), false);
    }

    protected RedrawingMode defaultRedrawModeForItem(Optional<MenuItem> item) {
        return RedrawingMode.SHOW_NAME_VALUE;
    }

    protected EditorComponent.PortableAlignment defaultJustificationForItem(Optional<MenuItem> item) {
        return EditorComponent.PortableAlignment.CENTER;
    }

    protected int defaultFontSizeForItem(Optional<MenuItem> maybeItem) {
        if(maybeItem.isEmpty()) return fontSize;
        var item = maybeItem.get();
        var parent = tree.findParent(item);
        var parentOverrides = settingsMap.get(parent.getId());
        if (parentOverrides != null) {
            var itemOverride = parentOverrides.overrideFor(item.getId());
            return (itemOverride.isPresent()) ? itemOverride.get().getFontSize() : parentOverrides.fontSize();
        }
        return fontSize;
    }

    protected int defaultSpacesForItem(Optional<MenuItem> item) {
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
        var menuId = Integer.parseInt(element.getAttribute(ROOTID_ATTRIBUTE));
        var item = tree.getMenuById(menuId).orElseThrow();
        var parent = tree.findParent(item);

        ComponentPositioning componentPositioning = new ComponentPositioning(
                Integer.parseInt(XMLDOMHelper.getAttributeOrDefault(element, ROW_POSITION_ATTRIBUTE, 0)),
                Integer.parseInt(XMLDOMHelper.getAttributeOrDefault(element, COL_POSITION_ATTRIBUTE, 0)),
                1,
                Integer.parseInt(XMLDOMHelper.getAttributeOrDefault(element, COL_SPAN_ATTRIBUTE, 1))
        );
        ComponentSettings settings = new ComponentSettings(
                new ScreenLayoutBasedConditionalColor(globalSettings, this, menuId, parent),
                Integer.parseInt(XMLDOMHelper.getAttributeOrDefault(element, FONT_SIZE_ATTR, fontSize)),
                EditorComponent.PortableAlignment.valueOf(XMLDOMHelper.getAttributeOrDefault(element, JUSTIFICATION_ATTRIBUTE, EditorComponent.PortableAlignment.LEFT)),
                componentPositioning,
                RedrawingMode.valueOf(XMLDOMHelper.getAttributeOrDefault(element, DRAW_MODE_ATTRIBUTE, RedrawingMode.SHOW_NAME_VALUE)),
                ControlType.valueOf(XMLDOMHelper.getAttributeOrDefault(element, CONTROL_TYPE_ATTRIBUTE, ControlType.TEXT_CONTROL)),
                true
        );
        return new ComponentSettingsWithMenuId(
                menuId,
                settings,
                parseColors(element, TEXT_COLOR_ELEMENT),
                parseColors(element, BUTTON_COLOR_ELEMENT),
                parseColors(element, UPDATE_COLOR_ELEMENT),
                parseColors(element, HIGHLIGHT_COLOR_ELEMENT)
        );
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
                subElement.setAttribute(ROOTID_ATTRIBUTE, Integer.toString(subSetting.getKey()));
                subElement.setAttribute(FONT_SIZE_ATTR, Integer.toString(subSetting.getValue().fontSize()));
                subElement.setAttribute(RECURSIVE_ATTRIBUTE, Boolean.toString(subSetting.getValue().recursive()));
                subSettings.appendChild(subElement);
                saveControlColor(subElement, TEXT_COLOR_ELEMENT, subSetting.getValue().textColor());
                saveControlColor(subElement, BUTTON_COLOR_ELEMENT, subSetting.getValue().buttonColor());
                saveControlColor(subElement, UPDATE_COLOR_ELEMENT, subSetting.getValue().updateColor());
                saveControlColor(subElement, HIGHLIGHT_COLOR_ELEMENT, subSetting.getValue().highlightColor());
                for(var override : subSetting.getValue().menuIdLevelOverrides()) {
                    serializeSetting(subElement, tree.getMenuById(override.menuId()).orElseThrow(), override);
                }
            }
            saveApplicationSpecific(doc.getDocumentElement());
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
            itemEle.setAttribute(ROOTID_ATTRIBUTE, String.valueOf(item.getId()));
            itemEle.setAttribute(JUSTIFICATION_ATTRIBUTE, settings.settings().getJustification().toString());
            itemEle.setAttribute(DRAW_MODE_ATTRIBUTE, settings.settings().getDrawMode().toString());
            itemEle.setAttribute(CONTROL_TYPE_ATTRIBUTE, settings.settings().getControlType().toString());
            itemEle.setAttribute(FONT_SIZE_ATTR, String.valueOf(settings.settings().getFontSize()));
            itemEle.setAttribute(COL_POSITION_ATTRIBUTE, String.valueOf(settings.settings().getPosition().getCol()));
            itemEle.setAttribute(COL_SPAN_ATTRIBUTE, String.valueOf(settings.settings().getPosition().getColSpan()));
            itemEle.setAttribute(ROW_POSITION_ATTRIBUTE, String.valueOf(settings.settings().getPosition().getRow()));
        } else {
            logger.log(System.Logger.Level.DEBUG, "Not persisting a non-custom setting for " + item.getId());
        }

    }

    public ColorCustomizable getColorCustomizerFor(MenuItem item) {
        if(item instanceof SubMenuItem sub) {
            mustContainKeyForSub(sub);
            return new LayoutBasedSubColorCustomizable("SubMenu - " + sub.getName(), this, settingsMap.get(sub.getId()));
        } else {
            var par = tree.findParent(item);
            mustContainKeyForSub(par);

            // at this point the parent must have an entry, even if just the default, to store the overrides
            var itemLevelLayouts = settingsMap.get(par.getId()).menuIdLevelOverrides();
            var override = itemLevelLayouts.stream()
                    .filter(compAndId -> compAndId.menuId() == item.getId())
                    .findFirst().orElseGet(() -> new ComponentSettingsWithMenuId(item.getId(), ComponentSettings.NO_COMPONENT,
                            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty() ));
            String itemName = "Item - " + item.getName() + " in " + par.getName();
            return new LayoutBasedItemColorCustomizable(itemName, par, override, this);
        }
    }

    ComponentSettingsForSub getSettingsForSubIfAvailable(MenuItem item) {
        return settingsMap.get(item.getId());
    }

    protected void mustContainKeyForSub(SubMenuItem par) {
        if(!settingsMap.containsKey(par.getId())) {
            settingsMap.put(par.getId(), new ComponentSettingsForSub(par.getId(), isRecursive(MenuTree.ROOT),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), fontSize, List.of()));
        }
    }

    void replaceItemOverride(MenuItem parent, int id, ComponentSettingsWithMenuId newSetting) {
        mustContainKeyForSub(asSubMenu(parent));
        ComponentSettingsForSub subSettings = settingsMap.get(parent.getId());
        settingsMap.put(parent.getId(), ComponentSettingsForSub.copyWithNewItem(subSettings, newSetting));
    }

    public void removeItemOverride(SubMenuItem parent, int menuId) throws InvalidItemChangeException {
        ComponentSettingsForSub subSettings = settingsMap.get(parent.getId());
        if(subSettings == null) throw new InvalidItemChangeException("The parent item did not exist: " + parent);
        settingsMap.put(parent.getId(), ComponentSettingsForSub.removeItemFromList(subSettings, menuId));
    }


    void replaceSubLevelOverride(ComponentSettingsForSub subSettings, ItemOverrideMode overrideMode) {
        if(overrideMode == ItemOverrideMode.TOP_LEVEL_PROPERTIES_ONLY && settingsMap.containsKey(subSettings.rootItemId())) {
            var existingList = settingsMap.get(subSettings.rootItemId()).menuIdLevelOverrides();
            settingsMap.put(subSettings.rootItemId(), new ComponentSettingsForSub(
                    subSettings.rootItemId(), subSettings.recursive(), subSettings.textColor(), subSettings.buttonColor(),
                    subSettings.updateColor(), subSettings.highlightColor(), subSettings.fontSize(), existingList
            ));
        } else {
            settingsMap.put(subSettings.rootItemId(), subSettings);
        }
    }

    public ControlType getDefaultControlType(Optional<MenuItem> maybeItem) {
        if(maybeItem.isEmpty()) return ControlType.TEXT_CONTROL;
        var item = maybeItem.get();
        if(item instanceof SubMenuItem || item instanceof BooleanMenuItem || item instanceof ActionMenuItem) {
            return ControlType.BUTTON_CONTROL;
        } else if (item instanceof AnalogMenuItem) {
            return ControlType.HORIZONTAL_SLIDER;
        } else if (item instanceof Rgb32MenuItem) {
            return ControlType.RGB_CONTROL;
        } else if (item instanceof EnumMenuItem || item instanceof ScrollChoiceMenuItem) {
            return ControlType.UP_DOWN_CONTROL;
        } else if (item instanceof RuntimeListMenuItem) {
            return ControlType.LIST_CONTROL;
        } else if (item instanceof EditableTextMenuItem textItem) {
            if (textItem.getItemType() == EditItemType.GREGORIAN_DATE) {
                return ControlType.DATE_CONTROL;
            } else if (textItem.getItemType() == EditItemType.TIME_24_HUNDREDS ||
                    textItem.getItemType() == EditItemType.TIME_12H ||
                    textItem.getItemType() == EditItemType.TIME_24H) {
                return ControlType.TIME_CONTROL;
            } else {
                return ControlType.TEXT_CONTROL;
            }
        } else {
            return ControlType.TEXT_CONTROL;
        }
    }

    public ConditionalColoring provideConditionalColorComponent(MenuItem parent, int id) {
        return new ScreenLayoutBasedConditionalColor(globalSettings, this, id, parent);
    }

    record MenuIdWithSpace(int menuId, int start, int colsTaken) {
    }

    record ScreenLayoutBasedConditionalColor(GlobalSettings globalSettings, ScreenLayoutPersistence layoutPersistence, int id, MenuItem parent) implements ConditionalColoring {
        @Override
        public PortableColor foregroundFor(EditorComponent.RenderingStatus status, ColorComponentType compType) {
            return getControlColor(status, compType).getFg();
        }

        @Override
        public PortableColor backgroundFor(EditorComponent.RenderingStatus status, ColorComponentType compType) {
            return getControlColor(status, compType).getBg();
        }

        private ControlColor getControlColor(EditorComponent.RenderingStatus status, ColorComponentType compType) {
            if(status == EditorComponent.RenderingStatus.RECENT_UPDATE) compType = ColorComponentType.CUSTOM;
            else if(status == EditorComponent.RenderingStatus.EDIT_IN_PROGRESS) compType = ColorComponentType.PENDING;
            else if(status == EditorComponent.RenderingStatus.CORRELATION_ERROR) compType = ColorComponentType.ERROR;

            var finalCompType = compType;

            var subOrNull = layoutPersistence.getSettingsForSubIfAvailable(parent);
            if(subOrNull != null) {
                var itemOverride = subOrNull.menuIdLevelOverrides().stream().filter(item -> item.menuId() == id).findFirst();
                if(itemOverride.isPresent()) {
                    var override = itemOverride.get();
                    return override.getColor(finalCompType).orElseGet(() ->
                            subOrNull.getColor(finalCompType).orElseGet(() ->
                                    colorFromGlobalSettings(finalCompType)));
                } else {
                    return subOrNull.getColor(finalCompType).orElseGet(() -> colorFromGlobalSettings(finalCompType));
                }
            } else {
                return colorFromGlobalSettings(finalCompType);
            }
        }

        private ControlColor colorFromGlobalSettings(ColorComponentType compType) {
            return switch (compType) {
                case TEXT_FIELD -> globalSettings.getTextColor();
                case BUTTON -> globalSettings.getButtonColor();
                case HIGHLIGHT -> globalSettings.getHighlightColor();
                case CUSTOM -> globalSettings.getUpdateColor();
                case DIALOG -> globalSettings.getDialogColor();
                case ERROR -> globalSettings.getErrorColor();
                case PENDING -> globalSettings.getPendingColor();
            };
        }
    }

    public void loadApplicationSpecific(Element rootElement) {}
    public void saveApplicationSpecific(Element rootElement) {}
}
