package com.thecoderscorner.embedcontrol.customization;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.menu.domain.state.MenuTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.thecoderscorner.menu.domain.AnalogMenuItemBuilder.anAnalogMenuItemBuilder;
import static com.thecoderscorner.menu.domain.BooleanMenuItemBuilder.aBooleanMenuItemBuilder;
import static com.thecoderscorner.menu.domain.SubMenuItemBuilder.aSubMenuItemBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MenuItemStoreTest {
    private MenuItemStore store;
    private GlobalSettings unitGlobalSettings;
    private MenuTree tree;
    private final UUID demoUuid = UUID.fromString("5f22995e-8da2-49c4-9ec8-d055901003af");

    @BeforeEach
    void setUp() {
        unitGlobalSettings = new GlobalSettings(new ApplicationThemeManager());
        tree = new MenuTree();
        tree.addMenuItem(MenuTree.ROOT, aBooleanMenuItemBuilder().withName("abc").withId(1).menuItem());
        var sub = aSubMenuItemBuilder().withName("sub").withId(9).menuItem();
        tree.addMenuItem(sub, anAnalogMenuItemBuilder().withName("def").withId(10).menuItem());
        tree.addMenuItem(sub, anAnalogMenuItemBuilder().withName("xyz").withId(11).menuItem());
        store = new MenuItemStore(unitGlobalSettings, tree, "layout1", 1, 2, true);
    }

    @Test
    void testFormStoreBasics() {
        store.setLayoutName("ABC");
        store.setRecursive(false);
        store.setGlobalFontInfo(MenuFormItem.FONT_100_PERCENT);

        assertThat(store.getTopLevelColorSet()).isInstanceOf(GlobalColorCustomizable.class);
        assertEquals("ABC", store.getLayoutName());
        assertEquals(MenuFormItem.FONT_100_PERCENT, store.getGlobalFontInfo());
    }

    @Test
    void testFormLoading() {
        store.loadLayout(EXAMPLE_FORM_XML, demoUuid);
        assertEquals("test", store.getLayoutName());
        assertEquals(3, store.getGridSize());
        assertEquals(16, store.getGlobalFontInfo().fontSize());
        assertEquals(FontInformation.SizeMeasurement.ABS_SIZE, store.getGlobalFontInfo().sizeMeasurement());
        assertThat(store.getAllColorSetNames()).containsExactlyInAnyOrder("Global", "abcdef");
        store.changeSubStore(0);
        var formItem = store.getFormItemAt(0, 0);
        assertThat(formItem).isInstanceOf(MenuItemFormItem.class);
        assertEquals("Edit abc id(1)", formItem.getDescription());
        var staticItem = store.getFormItemAt(0, 1);
        assertThat(staticItem).isInstanceOf(TextFormItem.class);
        assertEquals("Edit Text", staticItem.getDescription());

        store.changeSubStore(9);
        formItem = store.getFormItemAt(0, 0);
        assertThat(formItem).isInstanceOf(MenuItemFormItem.class);
        assertEquals("Edit def id(10)", formItem.getDescription());
        formItem = store.getFormItemAt(0, 1);
        assertThat(formItem).isInstanceOf(MenuItemFormItem.class);
        assertEquals("Edit xyz id(11)", formItem.getDescription());
        staticItem = store.getFormItemAt(1, 0);
        assertThat(staticItem).isInstanceOf(TextFormItem.class);
        assertEquals("Edit Text", staticItem.getDescription());

    }

    private final static String EXAMPLE_FORM_XML = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <EmbedControl boardUuid="5f22995e-8da2-49c4-9ec8-d055901003af" layoutName="test">
              <MenuLayouts>
                <MenuLayout cols="3" fontInfo="16" recursive="false" rootId="0">
                  <MenuElement alignment="LEFT" colorSet="Global" controlType="TEXT_CONTROL" drawMode="SHOW_NAME_VALUE" fontInfo="100%" menuId="1" position="0,0"/>
                  <StaticText alignment="CENTER" colorSet="abcdef" position="0,1">hello world</StaticText>
                </MenuLayout>
                <MenuLayout cols="3" fontInfo="120%" recursive="false" rootId="9">
                  <MenuElement alignment="LEFT" colorSet="Global" controlType="TEXT_CONTROL" drawMode="SHOW_NAME" fontInfo="100%" menuId="10" position="0,0"/>
                  <MenuElement alignment="LEFT" colorSet="Global" controlType="UP_DOWN_CONTROL" drawMode="SHOW_NAME_VALUE" fontInfo="100%" menuId="11" position="0,1"/>
                  <StaticText alignment="LEFT" colorSet="Global" position="1,0">Change me</StaticText>
                </MenuLayout>
              </MenuLayouts>
              <ColorSets>
                <ColorSet name="abcdef">
                  <text bg="#80B3B3FF" fg="#1A4D4DFF" isPresent="true"/>
                  <button bg="#FFFFFFFF" fg="#336666FF" isPresent="true"/>
                  <highlight isPresent="false"/>
                  <error isPresent="false"/>
                  <custom isPresent="false"/>
                  <dialog bg="#FFFFFFFF" fg="#B31A1AFF" isPresent="true"/>
                  <pending isPresent="false"/>
                </ColorSet>
              </ColorSets>
            </EmbedControl>
            """;
}