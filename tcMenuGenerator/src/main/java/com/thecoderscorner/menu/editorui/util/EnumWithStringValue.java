package com.thecoderscorner.menu.editorui.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Arrays;

/**
 * Enum with string value provides support for a simple object that has user-friendly versions of an enum, to be
 * printed in a choice or combo box for example. There are two helper methods that can turn a set of enum values into
 * an FXCollection that can be used with a combo, and to get a friendly name object from an enum entry that can be
 * used for equality and selection.
 * @param stringValue the string representation
 * @param enumVal the actual enum value
 * @param <E> the enum type
 */
public record EnumWithStringValue<E extends Enum<?>>(String stringValue, E enumVal) {
    @Override
    public String toString() {
        return stringValue;
    }

    public static <T extends Enum<?>> EnumWithStringValue<T> asFriendlyEnum(T v) {
        return new EnumWithStringValue<T>(StringHelper.capitaliseWords(v), v);
    }

    public static <T extends Enum<?>> ObservableList<EnumWithStringValue<T>> createFriendlyEnum(T[] values) {
        var rawList = Arrays.stream(values)
                .map(val -> new EnumWithStringValue<T>(StringHelper.capitaliseWords(val), val))
                .toList();
        return FXCollections.observableList(rawList);
    }
}