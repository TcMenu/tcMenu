package com.thecoderscorner.menu.editorui.generator.parameters;

import com.thecoderscorner.menu.editorui.generator.parameters.expander.InternalDeviceExpander;

import java.util.*;

public class IoExpanderDefinitionCollection {
    private final Set<IoExpanderDefinition> definitions;

    public IoExpanderDefinitionCollection() {
        definitions = Set.of(new InternalDeviceExpander());
    }

    public IoExpanderDefinitionCollection(Collection<IoExpanderDefinition> definitions) {
        // there must always be an internal device definition, add if need be.
        // don't care about duplicates, they are handled before code generation starts.
        if(definitions.stream().noneMatch(def -> def instanceof InternalDeviceExpander)) {
            var list = new ArrayList<>(definitions);
            list.add(new InternalDeviceExpander());
            this.definitions = Set.copyOf(list);
        }
        else {
            this.definitions = Set.copyOf(definitions);
        }
    }

    public Optional<IoExpanderDefinition> getDefinitionById(String id) {
        return definitions.stream().filter(def -> def.getId().equals(id)).findFirst();
    }

    public Collection<IoExpanderDefinition> getAllExpanders() {
        return definitions;
    }

    public IoExpanderDefinition getInternalExpander() {
        return getDefinitionById(InternalDeviceExpander.DEVICE_ID).orElseThrow();
    }
}
