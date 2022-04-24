package com.thecoderscorner.menu.editorui.generator.parameters;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MenuInMenuCollection {
    private final Set<MenuInMenuDefinition> menuDefinitions = new HashSet<>();

    public void replaceCollection(Collection<MenuInMenuDefinition> collection) {
        synchronized (menuDefinitions) {
            menuDefinitions.clear();
            menuDefinitions.addAll(collection);
        }
    }

    public void addDefinition(MenuInMenuDefinition definition) {
        synchronized (menuDefinitions) {
            menuDefinitions.add(definition);
        }
    }

    public void removeDefinition(MenuInMenuDefinition definition) {
        synchronized (menuDefinitions) {
            menuDefinitions.remove(definition);
        }
    }

    public void replaceDefinition(MenuInMenuDefinition oldDef, MenuInMenuDefinition newDef) {
        synchronized (menuDefinitions) {
            menuDefinitions.remove(oldDef);
            menuDefinitions.add(newDef);
        }
    }

    public Collection<MenuInMenuDefinition> getAllDefinitions() {
        synchronized (menuDefinitions) {
            return Set.copyOf(menuDefinitions);
        }
    }
}
