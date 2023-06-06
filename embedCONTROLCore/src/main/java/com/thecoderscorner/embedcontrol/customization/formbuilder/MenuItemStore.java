package com.thecoderscorner.embedcontrol.customization.formbuilder;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentPositioning;
import com.thecoderscorner.embedcontrol.core.controlmgr.ControlType;
import com.thecoderscorner.embedcontrol.core.controlmgr.RedrawingMode;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.core.util.StringHelper;
import com.thecoderscorner.embedcontrol.customization.ColorCustomizable;
import com.thecoderscorner.embedcontrol.customization.FontInformation;
import com.thecoderscorner.embedcontrol.customization.GlobalColorCustomizable;
import com.thecoderscorner.embedcontrol.customization.NamedColorCustomizable;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.persist.XMLDOMHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.*;
import java.util.*;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.PortableAlignment;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.ColorComponentType;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.ColorComponentType.*;
import static com.thecoderscorner.embedcontrol.customization.ScreenLayoutPersistence.IS_PRESENT_ATTRIBUTE;

public class MenuItemStore {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final Map<Integer, RowEntry> rowEntries;
    private final MenuTree tree;
    private int rootItemId;
    private final Map<String, ColorCustomizable> colorSets = new HashMap<>();
    private final GlobalSettings settings;
    private ColorCustomizable topLevelColorSet;
    private boolean recursive;
    private int gridSize;
    private FontInformation globalFontInfo = MenuFormItem.FONT_100_PERCENT;

    /**
     * create a new item store of a given dimension and fill with empty slots
     * @param rows the number of rows
     * @param columns the number of columns
     */
    public MenuItemStore(GlobalSettings settings, MenuTree tree, int rootMenuId, int rows, int columns, boolean recursive) {
        this.settings = settings;
        this.gridSize = columns;
        this.rootItemId = rootMenuId;
        this.recursive = recursive;
        this.tree = tree;
        rowEntries = new HashMap<>(128);

        for(int r=0; r<rows; r++) {
            rowEntries.put(r, new RowEntry());
        }

        colorSets.put(GlobalColorCustomizable.KEY_NAME, new GlobalColorCustomizable(settings));
        topLevelColorSet = getColorSet(null);
    }

    public List<String> getAllColorSetNames() {
        return colorSets.keySet().stream().toList();
    }

    public void addColorSet(ColorCustomizable colors) {
        colorSets.put(colors.getColorSchemeName(), colors);
    }

    public ColorCustomizable getColorSet(String name) {
        if(StringHelper.isStringEmptyOrNull(name)) {
            return colorSets.get(GlobalColorCustomizable.KEY_NAME);
        }
        return colorSets.get(name);
    }

    public ColorCustomizable getTopLevelColorSet() {
        return topLevelColorSet;
    }

    public void setTopLevelColorSet(ColorCustomizable topLevelColorSet) {
        this.topLevelColorSet = topLevelColorSet;
    }

    public int getRootItemId() {
        return rootItemId;
    }

    public Map<Integer, RowEntry> getRowEntries() {
        return rowEntries;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
        for(var r : rowEntries.values()) {
            r.resizeTo(gridSize);
        }
    }

    public boolean hasItemAtPosition(int row, int col) {
        return row < rowEntries.size() && rowEntries.get(row).getAtPosition(col) != null;
    }

    public int getMaximumRow() {
        return rowEntries.keySet().stream().max(Integer::compareTo).orElse(0) + 1;
    }

    public void setFormItemAt(int row, int col, MenuFormItem item) {
        if(col >= gridSize) throw new IllegalArgumentException("Out of bounds " + col + " >= " + gridSize);
        if(!rowEntries.containsKey(row)) {
            rowEntries.put(row, new RowEntry());
        }
        rowEntries.get(row).setAtPosition(col, item);
    }

    public MenuFormItem getFormItemAt(int row, int col) {
        if(col >= gridSize) throw new IllegalArgumentException("Out of bounds " + col + " >= " + gridSize);
        if(!rowEntries.containsKey(row)) {
            setFormItemAt(row, col, MenuFormItem.NO_FORM_ITEM);
        }
        return rowEntries.get(row).getAtPosition(col);
    }

    public void removeColorSet(ColorCustomizable removal) {
        if(checkAllEntriesFor(removal)) throw new IllegalArgumentException("Entry has references");
        colorSets.remove(removal.getColorSchemeName());
    }

    private boolean checkAllEntriesFor(ColorCustomizable removal) {
        // global cannot be deleted
        if(removal.isRepresentingGlobal() || removal.getColorSchemeName().equals(GlobalColorCustomizable.KEY_NAME)) {
            return true;
        }

        // cannot delete while item references.
        for(var ent : rowEntries.values()) {
            for(var col : ent.items) {
                if(col.getSettings() != null && col.getSettings().equals(removal)) return true;
            }
        }

        // no references
        return false;
    }

    public void loadLayout(String file, UUID uuid) {
        try(var input = new BufferedInputStream((new FileInputStream(file)))) {
            var doc = XMLDOMHelper.loadDocumentStream(input);
            if(!doc.getDocumentElement().getNodeName().equals("EmbedControl")) throw new IOException("Not EmbedControl document");
            if(!XMLDOMHelper.getAttributeOrDefault(doc.getDocumentElement(), "boardUuid", "").equalsIgnoreCase(uuid.toString())) throw new IOException("UUID does not match");


            var csEle = XMLDOMHelper.elementWithName(doc.getDocumentElement(), "ColorSets");
            var allSets = XMLDOMHelper.getChildElementsWithName(csEle, "ColorSet");

            colorSets.clear();
            colorSets.put(GlobalColorCustomizable.KEY_NAME, new GlobalColorCustomizable(settings));

            for(var set : allSets) {
                ColorCustomizable cc = new NamedColorCustomizable(set.getAttribute("name"));
                loadSingleColorValue(set, cc, "text", TEXT_FIELD);
                loadSingleColorValue(set, cc, "button", BUTTON);
                loadSingleColorValue(set, cc, "custom", CUSTOM);
                loadSingleColorValue(set, cc, "dialog", DIALOG);
                loadSingleColorValue(set, cc, "error", ERROR);
                loadSingleColorValue(set, cc, "highlight", HIGHLIGHT);
                loadSingleColorValue(set, cc, "pending", PENDING);
                colorSets.put(cc.getColorSchemeName(), cc);
            }

            var mlEle = XMLDOMHelper.elementWithName(doc.getDocumentElement(), "MenuLayouts");
            var allMenuLayouts = XMLDOMHelper.getChildElementsWithName(mlEle, "MenuLayout");

            rowEntries.clear();
            for(var layout : allMenuLayouts) {
                gridSize = Integer.parseInt(XMLDOMHelper.getAttributeOrDefault(layout, "cols", "0"));
                globalFontInfo = FontInformation.fromWire(XMLDOMHelper.getAttributeOrDefault(layout, "cols", "100%"));
                recursive = Boolean.parseBoolean(XMLDOMHelper.getAttributeOrDefault(layout, "recursive", "false"));
                rootItemId = Integer.parseInt(XMLDOMHelper.getAttributeOrDefault(layout, "rootId", "0"));
                topLevelColorSet = colorSets.get(XMLDOMHelper.getAttributeOrDefault(layout, "colorSet", GlobalColorCustomizable.KEY_NAME));
                readTextElements(layout);
                readSpaceElements(layout);
                readMenuElements(layout);
            }

            //gridSize = doc.getDocumentElement().getAttribute("cols")

        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR, "Could not load layout from " + file, e);
        }
    }

    private void readTextElements(Element layout) {
        for(var text : XMLDOMHelper.getChildElementsWithName(layout, "StaticText")) {
            var textForm = new TextFormItem(
                    text.getTextContent().trim(),
                    colorSets.get(XMLDOMHelper.getAttributeOrDefault(text, "colorSet", GlobalColorCustomizable.KEY_NAME)),
                    ComponentPositioning.fromWire(XMLDOMHelper.getAttributeOrDefault(text, "position", "0,0")),
                    PortableAlignment.valueOf(XMLDOMHelper.getAttributeOrDefault(text, "alignment", "LEFT"))
            );
            textForm.setFontInfo(FontInformation.fromWire(XMLDOMHelper.getAttributeOrDefault(text, "fontInfo", "100%")));
            setFormItemAt(textForm.getPositioning().getRow(), textForm.getPositioning().getCol(), textForm);
        }
    }

    private void readSpaceElements(Element layout) {
        for(var text : XMLDOMHelper.getChildElementsWithName(layout, "VertSpace")) {
            var spaceForm = new SpaceFormItem(
                    colorSets.get(XMLDOMHelper.getAttributeOrDefault(text, "colorSet", GlobalColorCustomizable.KEY_NAME)),
                    ComponentPositioning.fromWire(XMLDOMHelper.getAttributeOrDefault(text, "position", "0,0")),
                    Integer.parseInt(XMLDOMHelper.getAttributeOrDefault(text, "height", "10"))
            );

            setFormItemAt(spaceForm.getPositioning().getRow(), spaceForm.getPositioning().getCol(), spaceForm);
        }
    }

    private void readMenuElements(Element layout) {
        for(var text : XMLDOMHelper.getChildElementsWithName(layout, "MenuElement")) {
            var menuForm = new MenuItemFormItem(
                    tree.getMenuById(Integer.parseInt(XMLDOMHelper.getAttributeOrDefault(text, "menuId", "0"))).orElseThrow(),
                    colorSets.get(XMLDOMHelper.getAttributeOrDefault(text, "colorSet", GlobalColorCustomizable.KEY_NAME)),
                    ComponentPositioning.fromWire(XMLDOMHelper.getAttributeOrDefault(text, "position", "0,0")),
                    ControlType.valueOf(XMLDOMHelper.getAttributeOrDefault(text,"controlType", "TEXT_CONTROL")),
                    PortableAlignment.valueOf(XMLDOMHelper.getAttributeOrDefault(text,"alignment", "LEFT")),
                    RedrawingMode.valueOf(XMLDOMHelper.getAttributeOrDefault(text, "drawMode", "SHOW_NAME_VALUE"))
            );

            menuForm.setFontInfo(FontInformation.fromWire(XMLDOMHelper.getAttributeOrDefault(text, "fontInfo", "100%")));
            setFormItemAt(menuForm.getPositioning().getRow(), menuForm.getPositioning().getCol(), menuForm);
        }
    }

    private static void loadSingleColorValue(Element eleParent, ColorCustomizable cc, String elementName, ColorComponentType compTy) {
        var ce = XMLDOMHelper.elementWithName(eleParent, elementName);
        if(ce != null && Boolean.valueOf(ce.getAttribute("isPresent"))) {
            cc.setColorFor(compTy, new ControlColor(
                    new PortableColor(ce.getAttribute("fg")),
                    new PortableColor(ce.getAttribute("bg"))
            ));
        }
    }

    public void saveLayout(String file, UUID uuid) {
        try(var output = new BufferedOutputStream(new FileOutputStream(file)) ) {
            Document doc = XMLDOMHelper.newDocumentRoot("EmbedControl");
            doc.getDocumentElement().setAttribute("boardUuid", String.valueOf(uuid));

            var subsSettings = XMLDOMHelper.appendElementWithNameValue(doc.getDocumentElement(), "MenuLayouts", null);
            var subSetting = XMLDOMHelper.appendElementWithNameValue(subsSettings, "MenuLayout", null);
            subSetting.setAttribute("rootId", String.valueOf(rootItemId));
            subSetting.setAttribute("recursive", String.valueOf(recursive));
            subSetting.setAttribute("cols", String.valueOf(gridSize));
            subSetting.setAttribute("fontInfo", globalFontInfo.toWire());
            for(var mfi : allFormEntries()) {
                if(mfi instanceof MenuItemFormItem menuFormItem) {
                    serializeMenuItem(subSetting, menuFormItem);
                } else if(mfi instanceof TextFormItem textFormItem) {
                    serializeTextItem(subSetting, textFormItem);
                } else if(mfi instanceof SpaceFormItem spaceItem) {
                    var itemEle = XMLDOMHelper.appendElementWithNameValue(subSetting, "VertSpace", null);
                    itemEle.setAttribute("height", Integer.toString(spaceItem.getVerticalSpace()));
                    itemEle.setAttribute("position", spaceItem.getPositioning().toWire());
                }
            }

            var colEles = XMLDOMHelper.appendElementWithNameValue(doc.getDocumentElement(), "ColorSets", null);
            for(var colorSet : colorSets.values()) {
                // skip the global color scheme.
                if(colorSet.getColorSchemeName().equals(GlobalColorCustomizable.KEY_NAME)) continue;

                var colEle = XMLDOMHelper.appendElementWithNameValue(colEles, "ColorSet", null);
                colEle.setAttribute("name", colorSet.getColorSchemeName());
                saveControlColor(colEle, "text", colorSet, TEXT_FIELD);
                saveControlColor(colEle, "button", colorSet, BUTTON);
                saveControlColor(colEle, "highlight", colorSet, HIGHLIGHT);
                saveControlColor(colEle, "error", colorSet, ERROR);
                saveControlColor(colEle, "custom", colorSet, CUSTOM);
                saveControlColor(colEle, "dialog", colorSet, DIALOG);
                saveControlColor(colEle, "pending", colorSet, PENDING);
            }
            XMLDOMHelper.writeXml(doc, output, true);
        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR, "Unable to save state for " + file, e);
        }

    }

    private void serializeTextItem(Element subSetting, TextFormItem textFormItem) {
        var itemEle = XMLDOMHelper.appendElementWithNameValue(subSetting, "StaticText", textFormItem.getText());
        itemEle.setAttribute("position", String.valueOf(textFormItem.getPositioning().toWire()));
        itemEle.setAttribute("alignment", String.valueOf(textFormItem.getAlignment()));
        itemEle.setAttribute("colorSet", String.valueOf(textFormItem.getSettings().getColorSchemeName()));
    }

    private void saveControlColor(Element ele, String colorName, ColorCustomizable colorSet, ColorComponentType colType) {
        var colorElement = XMLDOMHelper.appendElementWithNameValue(ele, colorName, null);
        if(colorSet.getColorStatus(colType) == ColorCustomizable.ColorStatus.AVAILABLE) {
            colorElement.setAttribute(IS_PRESENT_ATTRIBUTE, "true");
            colorElement.setAttribute("fg", colorSet.getColorFor(colType).getFg().toString());
            colorElement.setAttribute("bg", colorSet.getColorFor(colType).getBg().toString());
        } else {
            colorElement.setAttribute(IS_PRESENT_ATTRIBUTE, "false");
        }
    }

    void serializeMenuItem(Element element, MenuItemFormItem menuFormItem) {
        logger.log(System.Logger.Level.INFO, "Persisting settings for " + menuFormItem.getDescription());
        var itemEle = XMLDOMHelper.appendElementWithNameValue(element, "MenuElement", null);
        itemEle.setAttribute("menuId", String.valueOf(menuFormItem.getItem().getId()));
        itemEle.setAttribute("position", String.valueOf(menuFormItem.getPositioning().toWire()));
        itemEle.setAttribute("alignment", menuFormItem.getAlignment().toString());
        itemEle.setAttribute("drawMode", menuFormItem.getRedrawingMode().toString());
        itemEle.setAttribute("controlType", menuFormItem.getControlType().toString());
        itemEle.setAttribute("fontInfo", menuFormItem.getFontInfo().toWire());
        itemEle.setAttribute("colorSet", String.valueOf(menuFormItem.getSettings().getColorSchemeName()));
    }

    List<MenuFormItem> allFormEntries() {
        var l = new ArrayList<MenuFormItem>(128);
        for(var ent : rowEntries.values()) {
            for(var it : ent.items) {
                if(it != null && !(it instanceof MenuFormItem.NoFormItem)) {
                    l.add(it);
                }
            }
        }
        return l.stream().sorted(Comparator.comparingInt(menuFormItem -> menuFormItem.getPositioning().getRow())).toList();
    }


    protected class RowEntry {
        private MenuFormItem[] items = new MenuFormItem[gridSize];
        public RowEntry() {
            Arrays.fill(items, MenuFormItem.NO_FORM_ITEM);
        }

        void resizeTo(int cols) {
            items = new MenuFormItem[cols];
            Arrays.fill(items, MenuFormItem.NO_FORM_ITEM);
        }

        void setAtPosition(int pos, MenuFormItem item) {
            items[pos] = item;
        }

        MenuFormItem getAtPosition(int i) {
            return items[i];
        }
    }
}
