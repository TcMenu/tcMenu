package com.thecoderscorner.menu.editorui.generator.ui;

import java.util.Comparator;

public enum TcSortOrder {
    NAME_ASC("Sort Name asc", Comparator.comparing((UICodePluginItem p) -> p.getItem().getDescription())),
    NAME_DESC("Sort Name desc", Comparator.comparing((UICodePluginItem p) -> p.getItem().getDescription()).reversed()),
    LICENSE_ASC("Sort Licence asc", Comparator.comparing((UICodePluginItem p) -> p.getItem().getConfig().getLicense())),
    LICENSE_DESC("Sort Licence desc", Comparator.comparing((UICodePluginItem p) -> p.getItem().getConfig().getLicense()).reversed());

    private final String desc;
    private final Comparator<? super UICodePluginItem> comparitor;

    TcSortOrder(String desc, Comparator<? super UICodePluginItem> comparitor) {
        this.desc = desc;
        this.comparitor = comparitor;
    }

    @Override
    public String toString() {
        return desc;
    }

    public Comparator<? super UICodePluginItem> getComparitor() {
        return comparitor;
    }
}
