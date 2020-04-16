#include <Wire.h>
#include <IoAbstractionWire.h>
#include <LiquidCrystalIO.h>
#include "tcMenuLiquidCrystal.h"
#include <tcMenu.h>

void onPressMe(int);
#define PRESSMECALLBACK onPressMe
#include <tcm_test/testFixtures.h>
#include <stockIcons/wifiAndConnectionIconsLCD.h>

const ConnectorLocalInfo applicationInfo = {"Test App", "12342345534533453" };

IoAbstractionRef io23017 = ioFrom23017(0x20, ACTIVE_LOW_OPEN, 2);
#define LCD_RS 8
#define LCD_EN 9
#define LCD_D4 10
#define LCD_D5 11
#define LCD_D6 12
#define LCD_D7 13
LiquidCrystal lcd(LCD_RS, LCD_EN, LCD_D4, LCD_D5, LCD_D6, LCD_D7);
LiquidCrystalRenderer renderer(lcd, 20, 4);

int testRenderFn(RuntimeMenuItem* item, uint8_t row, RenderFnMode mode, char* buffer, int bufferSize) {
	switch (mode) {
	case RENDERFN_NAME:
		strcpy(buffer, "TestNm");
		fastltoa(buffer, row, 3, NOT_PADDED, bufferSize);
		break;
	case RENDERFN_VALUE:
		buffer[0] = 0;
		fastltoa(buffer, row, 3, NOT_PADDED, bufferSize);
		break;
    default:
        break;
	}
	return true;
}

RENDERING_CALLBACK_NAME_INVOKE(textRenderingFunction, textItemRenderFn, "Text", 0xffff, NULL)
TextMenuItem textItem(textRenderingFunction, 10003, 10, NULL);

RENDERING_CALLBACK_NAME_INVOKE(ipRenderingFunction, ipAddressRenderFn, "IpAddr", 0xffff, NULL)
IpAddressMenuItem ipItem(ipRenderingFunction, 10002, &textItem);

RuntimeMenuItem runtimeItem(MENUTYPE_RUNTIME_VALUE, 10001, testRenderFn, 99, 1, &ipItem);

ListRuntimeMenuItem listItem(10000, 10, testRenderFn, &runtimeItem);

TitleWidget wifiWidget(iconsWifi, 5, 5, 8, NULL);
TitleWidget titleWidget(iconsConnection, 2, 5, 8, &wifiWidget);

void setup() {
    Wire.begin();
    Serial.begin(115200);
	Serial.println("Testing LiquidCrystal driver");

    lcd.setIoAbstraction(io23017);
    lcd.begin(20, 4);
	renderer.setEditorChars(0x7f, 0x7e, 0xf6);
    renderer.setFirstWidget(&titleWidget);
  
    switches.initialise(io23017, true);
	menuMgr.initForEncoder(&renderer, &menuVolume, 6, 7, 5);
	menuCaseTemp.setNext(&listItem);
	textItem.setTextValue("hello");

    taskManager.scheduleFixedRate(1500, [] {
        titleWidget.setCurrentState(titleWidget.getCurrentState() ? 0 : 1);
        int wifiState = (wifiWidget.getCurrentState() + 1) % 5;
        wifiWidget.setCurrentState(wifiState);
    });
}

void loop() {
    taskManager.runLoop();
}

const char helloWorld[] PROGMEM = "Pairing mode..";
const char secondMsg[] PROGMEM = "Pairing done..";

void onPressMe(int /*id*/) {
    BaseDialog* dialog = renderer.getDialog();
    if(dialog == NULL) return; // no dialog available.

    dialog->show(helloWorld, false, [] (ButtonType type, void *data) {
        if(type == BTNTYPE_ACCEPT) {
            BaseDialog* dialog = renderer.getDialog();
            dialog->show(secondMsg, false);
            dialog->setButtons(BTNTYPE_NONE, BTNTYPE_OK);
            dialog->copyIntoBuffer((char*)data);
        }
    });
    dialog->setButtons(BTNTYPE_NONE, BTNTYPE_CANCEL);
    dialog->copyIntoBuffer("Nothing found");
    dialog->setUserData((void*)"Something else");
    taskManager.scheduleOnce(2000, [] {
        BaseDialog* dialog = renderer.getDialog();
        dialog->copyIntoBuffer("Daves Phone");
        dialog->setButtons(BTNTYPE_ACCEPT, BTNTYPE_CANCEL, 1);
    });
}