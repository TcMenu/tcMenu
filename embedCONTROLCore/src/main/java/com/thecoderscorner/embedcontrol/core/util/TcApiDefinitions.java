package com.thecoderscorner.embedcontrol.core.util;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.persist.JsonMenuItemSerializer;

/**
 * Used by the Java embedded device support, classes that extend this interface can provide a menu tree and a serializer
 * for menu items. It also provides any menu in menu arrangements
 */
public interface TcApiDefinitions {
    MenuTree getMenuTree();
    void configureMenuInMenuComponents(BaseMenuConfig config);
    JsonMenuItemSerializer getJsonSerializer();
}
