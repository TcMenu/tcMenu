package com.thecoderscorner.embedcontrol.customization.formbuilder;

import com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class EnumWithValueList<T> {
    List<EnumWithValue<T>> allValues = new ArrayList<>();

    public EnumWithValue<T> fromValue(T v) {
        return allValues.stream().filter(sp -> sp.value() == v).findFirst().orElseThrow();
    }

    public EnumWithValueList<T> add(String txt, T val) {
        allValues.add(new EnumWithValue<>(txt, val));
        return this;
    }

    public List<EnumWithValue<T>> getAll() {
        return allValues;
    }

    public record EnumWithValue<T>(String name, T value) {
        @Override
        public String toString() {
            return name;
        }
    }
}
