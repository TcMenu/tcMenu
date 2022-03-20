package com.thecoderscorner.embedcontrol.customization;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.persist.XMLDOMHelper;
import org.w3c.dom.Element;

import java.nio.file.Path;
import java.util.*;

public class ScreenLayoutPersistence {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final Map<Integer, ComponentSettingsForSub> settingsMap = new HashMap<>();
    private int gridSize;
    private boolean globalRecursive;
    private boolean changed = false;

    public ScreenLayoutPersistence(UUID appUuid, Path path) {
        try {
            var doc = XMLDOMHelper.loadDocumentFromPath(path.resolve(appUuid.toString().toLowerCase(Locale.ROOT) + ".layout.xml"));
            globalRecursive = XMLDOMHelper.textOfElementByName(doc.getDocumentElement(), "Recursive").equals("true");
            gridSize = XMLDOMHelper.integerOfElementByName(doc.getDocumentElement(), "Grid", 2);
            var layouts = XMLDOMHelper.transformElements(doc.getDocumentElement(), "Layouts", "Layout", this::transformSubMenu);
            for(var layout : layouts) {
                settingsMap.put(layout.getRootItemId(), layout);
            }
        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR, "Layout restore failed for " + appUuid, e);
        }
    }

    private ComponentSettingsForSub transformSubMenu(Element element) {
        boolean isRecursive = XMLDOMHelper.getAttributeOrDefault(element, "recursive", "false").equals("true");
        int rootId = Integer.parseInt(XMLDOMHelper.getAttributeOrDefault(element, "rootId", "0"));

        var settings = XMLDOMHelper.transformElements(element, "itemSettings", this::settingsTransformer);

        return new ComponentSettingsForSub(rootId, isRecursive, parseColors(element, "textColor"),
                parseColors(element, "buttonColor"), parseColors(element,"updateColor"));

    }

    private ComponentSettings settingsTransformer(Element element) {
        return ComponentSettings.NO_COMPONENT;
    }

    private Optional<ControlColor> parseColors(Element element, String clrName) {
        var colorData = XMLDOMHelper.elementWithName(element, clrName);
        if(colorData == null) return Optional.empty();
        var portableFg = new PortableColor(colorData.getAttribute("fg"));
        var portableBg = new PortableColor(colorData.getAttribute("bg"));
        return Optional.of(new ControlColor(portableFg, portableBg));
    }

    public int getGridSize() {
        return gridSize;
    }

    public boolean isRecursive(MenuItem item) {
        if(settingsMap.containsKey(item.getId())) return settingsMap.get(item.getId()).isRecursive();
        return globalRecursive;
    }

    void serializeSetting(MenuItem item, ComponentSettings settings) {
        if(!settings.isCustomised()) {
            logger.log(System.Logger.Level.DEBUG, "Not persisting a non-custom value for " + item.getId());
            return;
        }
    }

    public class ComponentSettingsForSub {
        private final int rootItemId;
        private final boolean recursive;
        private final Optional<ControlColor> textColor;
        private final Optional<ControlColor> buttonColor;
        private final Optional<ControlColor> updateColor;

        ComponentSettingsForSub(Integer rootItem, boolean recursive, Optional<ControlColor> textColor, Optional<ControlColor> buttonColor,
                                Optional<ControlColor> updateColor) {
            this.rootItemId = rootItem;
            this.recursive = recursive;
            this.buttonColor = buttonColor;
            this.textColor = textColor;
            this.updateColor = updateColor;
        }

        public int getRootItemId() {
            return rootItemId;
        }

        public boolean isRecursive() {
            return recursive;
        }
    }
}
