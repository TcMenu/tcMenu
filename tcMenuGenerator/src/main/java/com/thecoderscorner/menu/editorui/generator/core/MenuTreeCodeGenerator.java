package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.domain.state.MenuTree;

import java.util.Collection;
import java.util.List;

public interface MenuTreeCodeGenerator {
    void initialise(MenuTree tree) throws TcMenuConversionException;
    List<HeaderDefinition> headersToGenerate();
    String getRootMenuCode();
    String getHeaderMenuCode();
}
