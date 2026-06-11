package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.DomainFixtures;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.arduino.CallbackRequirement;
import com.thecoderscorner.menu.editorui.generator.logger.UserFeedbackLogger;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class MenuBuilderTreeCodeGeneratorTest {
    @Test
    public void testGenerator() throws TcMenuConversionException {
        MenuTree tree = DomainFixtures.fullEspAmplifierTestTree();
        CodeVariableCppExtractor extractor = new CodeVariableCppExtractor(mock(CodeConversionContext.class));
        VariableNameGenerator namingGenerator = new VariableNameGenerator(tree, true);
        MenuBuilderTreeCodeGeneratorImpl generator = new MenuBuilderTreeCodeGeneratorImpl("builder", true, namingGenerator, mock(UserFeedbackLogger.class));
        generator.initialise(tree);

        Map<MenuItem, CallbackRequirement> callbackRequirements = Map.of();
        String cpp = generator.getRootMenuCode(callbackRequirements, extractor);

        assertThat(cpp).isEqualToNormalizingNewlines("""
                
                
                // Declaring any arrays used by enum/list items
                const char* strStatusAmpStatusEnumEntries[] = { "Warm up", "Warm Valves", "Ready", "DC Protection", "Overloaded", "Overheated" };
                
                void buildMenu(TcMenuBuilder& builder) {
                    builder.usingDynamicEEPROMStorage()
                        .analogBuilder(MENU_VOLUME_ID, "Volume", ROM_SAVE, NoMenuFlags, 0, onVolumeChanged)
                            .offset(-180).divisor(2).step(1).maxValue(255).unit("dB").endItem()
                        .scrollChoiceBuilder(MENU_CHANNELS_ID, "Channel", ROM_SAVE, NoMenuFlags, 0, onChannelChanged).fromRomChoices(150, 3, 16).endItem()
                        .boolItem(MENU_DIRECT_ID, "Direct", ROM_SAVE, NAMING_TRUE_FALSE, NoMenuFlags, false, onAudioDirect)
                        .boolItem(MENU_MUTE_ID, "Mute", DONT_SAVE, NAMING_ON_OFF, NoMenuFlags, false, onMuteSound)
                        .subMenu(MENU_SETTINGS_ID, "Settings", NoMenuFlags, nullptr)
                            .subMenu(MENU_SETTINGS_CHANNEL_SETTINGS_ID, "Channel Settings", NoMenuFlags, nullptr)
                                .scrollChoiceBuilder(MENU_CHANNEL_SETTINGS_CHANNEL_ID, "Channel Num", DONT_SAVE, NoMenuFlags, 0, nullptr).ofCustomRtFunction(fnChannelSettingsChannelRtCall, 3).endItem()
                                .analogBuilder(MENU_CHANNEL_SETTINGS_LEVEL_TRIM_ID, "Level Trim", ROM_SAVE, NoMenuFlags, 0, nullptr)
                                    .offset(-10).divisor(2).step(1).maxValue(20).unit("dB").endItem()
                                .textItem(MENU_CHANNEL_SETTINGS_NAME_ID, "Name", DONT_SAVE, 15, NoMenuFlags, "", nullptr)
                                .actionItem(MENU_CHANNEL_SETTINGS_UPDATE_SETTINGS_ID, "Update Settings", NoMenuFlags, onChannelSetttingsUpdate)
                                .endSub()
                            .analogBuilder(MENU_SETTINGS_WARM_UP_TIME_ID, "Warm up time", ROM_SAVE, NoMenuFlags, 0, warmUpChanged)
                                .offset(0).divisor(10).step(1).maxValue(300).unit("s").endItem()
                            .analogBuilder(MENU_SETTINGS_VALVE_HEATING_ID, "Valve Heating", ROM_SAVE, NoMenuFlags, 0, valveHeatingChanged)
                                .offset(0).divisor(10).step(1).maxValue(600).unit("s").endItem()
                            .actionItem(MENU_SETTINGS_SAVE_SETTINGS_ID, "Save settings", NoMenuFlags, onSaveSettings)
                            .endSub()
                        .subMenu(MENU_STATUS_ID, "Status", NoMenuFlags, nullptr)
                            .enumItem(MENU_STATUS_AMP_STATUS_ID, "Amp Status", DONT_SAVE, strStatusAmpStatusEnumEntries, 6, MenuFlags().readOnly(), 0, nullptr)
                            .analogBuilder(MENU_STATUS_LEFT_VU_ID, "Left VU", DONT_SAVE, MenuFlags().readOnly(), 0, nullptr)
                                .offset(-20000).divisor(1000).step(1).maxValue(30000).unit("dB").endItem()
                            .analogBuilder(MENU_STATUS_RIGHT_VU_ID, "Right VU", DONT_SAVE, MenuFlags().readOnly(), 0, nullptr)
                                .offset(-20000).divisor(1000).step(1).maxValue(30000).unit("dB").endItem()
                            .actionItem(MENU_STATUS_SHOW_DIALOGS_ID, "Show Dialogs", NoMenuFlags, onShowDialogs)
                            .listItemRtCustom(MENU_STATUS_DATA_LIST_ID, "Data List", 0, fnStatusDataListRtCall, NoMenuFlags, nullptr)
                            .analogBuilder(MENU_STATUS_TEST_ID, "Test", DONT_SAVE, NoMenuFlags, 0, nullptr)
                                .offset(-5000).divisor(10).step(1).maxValue(65535).unit("U").endItem()
                            .endSub()
                        .subMenu(MENU_CONNECTIVITY_ID, "Connectivity", NoMenuFlags, nullptr)
                            .ipAddressItem(MENU_CONNECTIVITY_IPADDRESS_ID, "IP address", DONT_SAVE, MenuFlags().readOnly(), IpAddressStorage(127, 0, 0, 1), nullptr)
                            .textItem(MENU_CONNECTIVITY_SSID_ID, "SSID", ROM_SAVE, 20, NoMenuFlags, "", nullptr)
                            .textItem(MENU_CONNECTIVITY_PASSCODE_ID, "Passcode", ROM_SAVE, 20, NoMenuFlags, "", nullptr)
                            .timeItem(MENU_CONNECTIVITY_TIME24_ID, "Time 24", DONT_SAVE, NoMenuFlags, EDITMODE_TIME_HUNDREDS_24H, TimeStorage(0, 0, 0, 0), nullptr)
                            .dateItem(MENU_CONNECTIVITY_DATE_FIELD_ID, "Date field", DONT_SAVE, NoMenuFlags, DateStorage(1, 1, 2020), nullptr)
                            .remoteConnectivityMonitor(MENU_CONNECTIVITY_IO_TMONITOR_ID, "IoT Monitor", NoMenuFlags)
                            .eepromAuthenticationItem(MENU_CONNECTIVITY_AUTHENTICATOR_ID, "Authenticator", NoMenuFlags, nullptr)
                            .rgb32Item(MENU_CONNECTIVITY_RGB_ID, "RGB", ROM_SAVE, false, NoMenuFlags, RgbColor32(0, 0, 0), onRgbChanged)
                            .endSub();
                }
                """);

        assertThat(generator.getHeaderMenuCode(extractor)).isEqualToNormalizingNewlines("""
                // Forward define the menu builder function
                void buildMenu(TcMenuBuilder& builder);
                
                // The following defines all menu item IDs.
                #define MENU_VOLUME_ID  1
                #define MENU_CHANNELS_ID  2
                #define MENU_DIRECT_ID  3
                #define MENU_MUTE_ID  4
                #define MENU_SETTINGS_ID  5
                #define MENU_SETTINGS_CHANNEL_SETTINGS_ID  7
                #define MENU_CHANNEL_SETTINGS_CHANNEL_ID  23
                #define MENU_CHANNEL_SETTINGS_LEVEL_TRIM_ID  8
                #define MENU_CHANNEL_SETTINGS_NAME_ID  22
                #define MENU_CHANNEL_SETTINGS_UPDATE_SETTINGS_ID  24
                #define MENU_SETTINGS_WARM_UP_TIME_ID  11
                #define MENU_SETTINGS_VALVE_HEATING_ID  17
                #define MENU_SETTINGS_SAVE_SETTINGS_ID  25
                #define MENU_STATUS_ID  6
                #define MENU_STATUS_AMP_STATUS_ID  14
                #define MENU_STATUS_LEFT_VU_ID  15
                #define MENU_STATUS_RIGHT_VU_ID  16
                #define MENU_STATUS_SHOW_DIALOGS_ID  20
                #define MENU_STATUS_DATA_LIST_ID  21
                #define MENU_STATUS_TEST_ID  28
                #define MENU_CONNECTIVITY_ID  12
                #define MENU_CONNECTIVITY_IPADDRESS_ID  13
                #define MENU_CONNECTIVITY_SSID_ID  18
                #define MENU_CONNECTIVITY_PASSCODE_ID  19
                #define MENU_CONNECTIVITY_TIME24_ID  91
                #define MENU_CONNECTIVITY_DATE_FIELD_ID  92
                #define MENU_CONNECTIVITY_IO_TMONITOR_ID  26
                #define MENU_CONNECTIVITY_AUTHENTICATOR_ID  27
                #define MENU_CONNECTIVITY_RGB_ID  90
                
                // Inline helper methods to access menu items
                inline AnalogMenuItem& getMenuVolume() { return getAnalogItemById(MENU_VOLUME_ID); }
                inline ScrollChoiceMenuItem& getMenuChannels() { return getScrollChoiceItemById(MENU_CHANNELS_ID); }
                inline BooleanMenuItem& getMenuDirect() { return getBooleanItemById(MENU_DIRECT_ID); }
                inline BooleanMenuItem& getMenuMute() { return getBooleanItemById(MENU_MUTE_ID); }
                inline SubMenuItem& getMenuSettings() { return getSubMenuById(MENU_SETTINGS_ID); }
                inline SubMenuItem& getMenuSettingsChannelSettings() { return getSubMenuById(MENU_SETTINGS_CHANNEL_SETTINGS_ID); }
                inline ScrollChoiceMenuItem& getMenuChannelSettingsChannel() { return getScrollChoiceItemById(MENU_CHANNEL_SETTINGS_CHANNEL_ID); }
                inline AnalogMenuItem& getMenuChannelSettingsLevelTrim() { return getAnalogItemById(MENU_CHANNEL_SETTINGS_LEVEL_TRIM_ID); }
                inline TextMenuItem& getMenuChannelSettingsName() { return getTextItemById(MENU_CHANNEL_SETTINGS_NAME_ID); }
                inline ActionMenuItem& getMenuChannelSettingsUpdateSettings() { return getActionItemById(MENU_CHANNEL_SETTINGS_UPDATE_SETTINGS_ID); }
                inline AnalogMenuItem& getMenuSettingsWarmUpTime() { return getAnalogItemById(MENU_SETTINGS_WARM_UP_TIME_ID); }
                inline AnalogMenuItem& getMenuSettingsValveHeating() { return getAnalogItemById(MENU_SETTINGS_VALVE_HEATING_ID); }
                inline ActionMenuItem& getMenuSettingsSaveSettings() { return getActionItemById(MENU_SETTINGS_SAVE_SETTINGS_ID); }
                inline SubMenuItem& getMenuStatus() { return getSubMenuById(MENU_STATUS_ID); }
                inline EnumMenuItem& getMenuStatusAmpStatus() { return getEnumItemById(MENU_STATUS_AMP_STATUS_ID); }
                inline AnalogMenuItem& getMenuStatusLeftVU() { return getAnalogItemById(MENU_STATUS_LEFT_VU_ID); }
                inline AnalogMenuItem& getMenuStatusRightVU() { return getAnalogItemById(MENU_STATUS_RIGHT_VU_ID); }
                inline ActionMenuItem& getMenuStatusShowDialogs() { return getActionItemById(MENU_STATUS_SHOW_DIALOGS_ID); }
                inline ListRuntimeMenuItem& getMenuStatusDataList() { return getListItemById(MENU_STATUS_DATA_LIST_ID); }
                inline AnalogMenuItem& getMenuStatusTest() { return getAnalogItemById(MENU_STATUS_TEST_ID); }
                inline SubMenuItem& getMenuConnectivity() { return getSubMenuById(MENU_CONNECTIVITY_ID); }
                inline IpAddressMenuItem& getMenuConnectivityIPAddress() { return getIpAddressItemById(MENU_CONNECTIVITY_IPADDRESS_ID); }
                inline TextMenuItem& getMenuConnectivitySSID() { return getTextItemById(MENU_CONNECTIVITY_SSID_ID); }
                inline TextMenuItem& getMenuConnectivityPasscode() { return getTextItemById(MENU_CONNECTIVITY_PASSCODE_ID); }
                inline DateFormattedMenuItem& getMenuConnectivityTime24() { return getDateItemById(MENU_CONNECTIVITY_TIME24_ID); }
                inline DateFormattedMenuItem& getMenuConnectivityDateField() { return getDateItemById(MENU_CONNECTIVITY_DATE_FIELD_ID); }
                inline RemoteMenuItem& getMenuConnectivityIoTMonitor() { return getIoTRemoteMenuById(MENU_CONNECTIVITY_IO_TMONITOR_ID); }
                inline EepromAuthenticationInfoMenuItem& getMenuConnectivityAuthenticator() { return getAuthenticationMenuById(MENU_CONNECTIVITY_AUTHENTICATOR_ID); }
                inline Rgb32MenuItem& getMenuConnectivityRGB() { return getRgb32ItemById(MENU_CONNECTIVITY_RGB_ID); }
                """);

        assertThat(generator.getFirstMenuVariable()).isEqualToNormalizingNewlines("getMenuVolume()");

        assertThat(generator.headersToGenerate().stream().map(HeaderDefinition::getHeaderName)).containsExactlyInAnyOrder(
                "ScrollChoiceMenuItem.h", "RemoteMenuItem.h");
    }

    @Test
    public void testGeneratorWithRtCall() throws TcMenuConversionException {
        MenuTree tree = new MenuTree();
        tree.addMenuItem(MenuTree.ROOT, new EditableTextMenuItemBuilder().withName("HexText").withId(1).withFunctionName("hexTextRtCall").menuItem());
        tree.addMenuItem(MenuTree.ROOT, new EditableTextMenuItemBuilder().withName("IpCust").withEditItemType(EditItemType.IP_ADDRESS).withId(2).withFunctionName("hexTextRtCall").menuItem());
        tree.addMenuItem(MenuTree.ROOT, new EditableTextMenuItemBuilder().withName("DateCust").withEditItemType(EditItemType.GREGORIAN_DATE).withId(3).withFunctionName("hexTextRtCall").menuItem());
        tree.addMenuItem(MenuTree.ROOT, new EditableTextMenuItemBuilder().withName("TimeCust").withEditItemType(EditItemType.TIME_12H).withId(4).withFunctionName("hexTextRtCall").menuItem());
        tree.addMenuItem(MenuTree.ROOT, new Rgb32MenuItemBuilder().withName("ExtraRgb").withId(6).withFunctionName("extraRgbRtCall").menuItem());
        tree.addMenuItem(MenuTree.ROOT, new EditableLargeNumberMenuItemBuilder().withName("ExtraLgeNum").withId(7).withFunctionName("extraLgeRtCall").menuItem());
        tree.addMenuItem(MenuTree.ROOT, new EditableLargeNumberMenuItemBuilder().withName("LgeNum").withId(8).withFunctionName("onCallback").menuItem());
        tree.addMenuItem(MenuTree.ROOT, new RuntimeListMenuItemBuilder().withName("Ram List !").withId(9).withFunctionName("onCallback")
                .withCreationMode(RuntimeListMenuItem.ListCreationMode.RAM_ARRAY).menuItem());
        tree.addMenuItem(MenuTree.ROOT, new RuntimeListMenuItemBuilder().withName("Flash List!").withId(10).withFunctionName("onCallbackII")
                .withCreationMode(RuntimeListMenuItem.ListCreationMode.FLASH_ARRAY).withLocalOnly(true).menuItem());

        MenuItemHelper.setMenuState(tree.getMenuById(9).orElseThrow(), List.of("abc1234", "def4567"), tree);
        MenuItemHelper.setMenuState(tree.getMenuById(10).orElseThrow(), List.of("xyz", "abc"), tree);

        CodeVariableCppExtractor extractor = new CodeVariableCppExtractor(mock(CodeConversionContext.class));
        VariableNameGenerator namingGenerator = new VariableNameGenerator(tree, false);
        MenuBuilderTreeCodeGeneratorImpl generator = new MenuBuilderTreeCodeGeneratorImpl("builder", true, namingGenerator, mock(UserFeedbackLogger.class));
        generator.initialise(tree);

        Map<MenuItem, CallbackRequirement> callbackRequirements = Map.of();
        String cpp = generator.getRootMenuCode(callbackRequirements, extractor);

        assertThat(cpp).isEqualToNormalizingNewlines("""
                
                
                // Declaring any arrays used by enum/list items
                char* strRamListListItems[] = { "abc1234", "def4567" };
                const char* strFlashListListItems[] = { "xyz", "abc" };
                
                void buildMenu(TcMenuBuilder& builder) {
                    builder.usingDynamicEEPROMStorage()
                        .textCustomRt(MENU_HEX_TEXT_ID, "HexText", ROM_SAVE, 0, hexTextRtCall, NoMenuFlags, "")
                        .ipAddressCustomRt(MENU_IP_CUST_ID, "IpCust", ROM_SAVE, NoMenuFlags, hexTextRtCall, IpAddressStorage(127, 0, 0, 1))
                        .dateItemCustomRt(MENU_DATE_CUST_ID, "DateCust", ROM_SAVE, NoMenuFlags, DateStorage(1, 1, 2020), hexTextRtCall)
                        .timeItemCustomRt(MENU_TIME_CUST_ID, "TimeCust", ROM_SAVE, TimeStorage(0, 0, 0, 0), hexTextRtCall, NoMenuFlags, EDITMODE_TIME_12H)
                        .rgb32CustomRt(MENU_EXTRA_RGB_ID, "ExtraRgb", ROM_SAVE, false, extraRgbRtCall, NoMenuFlags, RgbColor32(0, 0, 0))
                        .largeNumberRtCustom(MENU_EXTRA_LGE_NUM_ID, "ExtraLgeNum", ROM_SAVE, LargeFixedNumber(0, 0, 0U, 0U, false), true, extraLgeRtCall, NoMenuFlags, nullptr)
                        .largeNumberItem(MENU_LGE_NUM_ID, "LgeNum", ROM_SAVE, LargeFixedNumber(0, 0, 0U, 0U, false), true, NoMenuFlags, onCallback)
                        .listItemRam(MENU_RAM_LIST_ID, "Ram List !", 0, strRamListListItems, NoMenuFlags, onCallback)
                        .listItemFlash(MENU_FLASH_LIST_ID, "Flash List!", 0, strFlashListListItems, MenuFlags().localOnly(), onCallbackII);
                }
                """);

        assertThat(generator.getFirstMenuVariable()).isEqualToNormalizingNewlines("getMenuHexText()");

        assertThat(generator.headersToGenerate().stream().map(HeaderDefinition::getHeaderName)).containsExactlyInAnyOrder(
                "ScrollChoiceMenuItem.h", "RemoteMenuItem.h", "EditableLargeNumberMenuItem.h");
    }
}
