package com.thecoderscorner.menu.domain.util;

import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.BooleanMenuItem;
import com.thecoderscorner.menu.domain.EnumMenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;

public interface MenuItemVisitor {
    void visit(AnalogMenuItem item);
    void visit(BooleanMenuItem item);
    void visit(EnumMenuItem item);
    void visit(SubMenuItem item);
}
