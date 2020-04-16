// This sketch is for testing of the adafruit renderer only, it is not intended as an example
// This just assembles enough components to get rendering working.

#include <tcMenu.h>
#include <Adafruit_ILI9341.h>
//#include <Adafruit_PCD8544.h>
#include <Fonts/FreeSans18pt7b.h>
#include <Fonts/FreeSans9pt7b.h>
#include "tcMenuAdaFruitGfx.h"
#include <tcUtil.h>
#include <IoAbstractionWire.h>

#include "tcm_test/testFixtures.h"

Adafruit_ILI9341 gfx(6, 7);
//Adafruit_PCD8544 gfx = Adafruit_PCD8544(35, 34, 38, 37, 36);
AdaColorGfxMenuConfig config;

AdaFruitGfxMenuRenderer renderer;
const ConnectorLocalInfo applicationInfo PROGMEM = {"Graphics Test", "b3371783-d35a-4fcd-9189-64192117e0c1"};

const uint8_t iconWifiNotConnected[] PROGMEM = {
	0b00000001, 0b10000000,
	0b00000110, 0b01100000,
	0b00111000, 0b00011100,
	0b11000000, 0b00000011,
	0b01000000, 0b00000010,
	0b00100000, 0b00000100,
	0b00010000, 0b00001000,
	0b00001000, 0b00010000,
	0b00000100, 0b00100000,
	0b00000011, 0b11000000,
};

const uint8_t iconWifiOneBar[] PROGMEM = {
	0b00000000, 0b00000000,
	0b00000000, 0b00000000,
	0b00000000, 0b00000000,
	0b00000000, 0b00000000,
	0b00000000, 0b00000000,
	0b00000000, 0b00000000,
	0b00000000, 0b00000000,
	0b00000000, 0b00000000,
	0b00000001, 0b10000000,
	0b00000001, 0b10000000
};

const uint8_t iconWifiTwoBar[] PROGMEM = {
	0b00000000, 0b00000000,
	0b00000000, 0b00000000,
	0b00000000, 0b00000000,
	0b00000000, 0b00000000,
	0b00000000, 0b00000000,
	0b00000000, 0b00000000,
	0b00000111, 0b11100000,
	0b00001100, 0b00110000,
	0b00000001, 0b10000000,
	0b00000001, 0b10000000
};

const uint8_t iconWifiThreeBar[] PROGMEM = {
	0b00000000, 0b00000000,
	0b00000000, 0b00000000,
	0b00000000, 0b00000000,
	0b00000111, 0b11100000,
	0b00011100, 0b00111000,
	0b00110000, 0b00001100,
	0b00000111, 0b11100000,
	0b00001100, 0b00110000,
	0b00000001, 0b10000000,
	0b00000001, 0b10000000
};

const uint8_t iconWifiFourBar[] PROGMEM = {
	0b00000011, 0b11000000,
	0b00001111, 0b11110000,
	0b01110000, 0b00001110,
	0b11000111, 0b11100011,
	0b00011100, 0b00111000,
	0b00110000, 0b00001100,
	0b00000111, 0b11100000,
	0b00001100, 0b00110000,
	0b00000001, 0b10000000,
	0b00000001, 0b10000000
};

const uint8_t iconConnectionNone[] PROGMEM = {
	0b01111111, 0b11111110,
	0b10110000, 0b00000001,
	0b10001100, 0b00000001,
	0b10000110, 0b00000001,
	0b10000011, 0b00000001,
	0b10000000, 0b11000001,
	0b10000001, 0b00110001,
	0b10000000, 0b00001101,
	0b10000001, 0b10000011,
	0b01111111, 0b11111110,
};

const uint8_t iconConnected[] PROGMEM = {
	0b01111111, 0b11111110,
	0b11000000, 0b00000011,
	0b11011101, 0b11101111,
	0b11000000, 0b00000011,
	0b11011011, 0b00110011,
	0b11000000, 0b00000011,
	0b11000000, 0b00000011,
	0b11000000, 0b00000011,
	0b11000001, 0b10000011,
	0b01111111, 0b11111110,
};

const uint8_t* const iconsWifi[]  PROGMEM = { iconWifiNotConnected, iconWifiOneBar, iconWifiTwoBar, iconWifiThreeBar, iconWifiFourBar };
const uint8_t* const iconsConnection[] PROGMEM = { iconConnectionNone, iconConnected };

TitleWidget connectedWidget(iconsConnection, 2, 16, 10);
TitleWidget wifiWidget(iconsWifi, 5, 16, 10, &connectedWidget);

void prepareGfxConfig() {
	makePadding(config.titlePadding, 5, 5, 20, 5);
	makePadding(config.itemPadding, 5, 5, 3, 5);
	makePadding(config.widgetPadding, 5, 10, 0, 5);

	config.bgTitleColor = RGB(255, 255, 0);
	config.fgTitleColor = RGB(0, 0, 0);
	config.titleFont = &FreeSans18pt7b;
	config.titleBottomMargin = 10;

	config.bgItemColor = RGB(0, 0, 0);
	config.fgItemColor = RGB(222, 222, 222);
	config.itemFont = &FreeSans9pt7b;

	config.bgSelectColor = RGB(0, 0, 200);
	config.fgSelectColor = RGB(255, 255, 255);
	config.widgetColor = RGB(30, 30, 30);

	config.titleFontMagnification = 1;
	config.itemFontMagnification = 1;

	config.activeIcon = NULL;
	config.editIcon = NULL;
}

const char myHeaderPgm[] PROGMEM = "Really test menu";

// all define statements needed
#define ENCODER_PIN_A 7
#define ENCODER_PIN_B 6
#define ENCODER_PIN_OK 5

IoAbstractionRef io8574 = ioFrom8574(0x20, 0);

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
	}
	return true;
}

RENDERING_CALLBACK_NAME_INVOKE(textRenderingFunction, textItemRenderFn, "Text", 0xffff, NULL)
TextMenuItem textItem(textRenderingFunction, 10003, 10, NULL);

RENDERING_CALLBACK_NAME_INVOKE(ipRenderingFunction, ipAddressRenderFn, "IpAddr", 0xffff, NULL)
IpAddressMenuItem ipItem(ipRenderingFunction, 10002, &textItem);

RuntimeMenuItem runtimeItem(MENUTYPE_RUNTIME_VALUE, 10001, testRenderFn, 99, 1, &ipItem);

ListRuntimeMenuItem listItem(10000, 10, testRenderFn, &runtimeItem);

void setup() {
	while (!Serial);
	Serial.begin(115200);
    Wire.begin();
	Serial.print("Testing adafruit driver");

	gfx.begin();
	gfx.setRotation(3);
    //gfx.setContrast(50);
	//gfx.clearDisplay();
    //gfx.display();

	// either one as needed for testing..
	//prepareAdaColorDefaultGfxConfig(&config);
	prepareGfxConfig();
    //prepareAdaMonoGfxConfigLoRes(&config);

    menuStatus.setNext(&listItem);

	renderer.setGraphicsDevice(&gfx, &config);
    switches.initialise(io8574, true);
    menuMgr.initForEncoder(&renderer, &menuVolume, ENCODER_PIN_A, ENCODER_PIN_B, ENCODER_PIN_OK);

	menuVolume.setActive(true);
	renderer.setFirstWidget(&wifiWidget);

    BaseDialog* dlg = renderer.getDialog();
    if(dlg != NULL) {
        serdebugF("Show dialog");
        dlg->setButtons(BTNTYPE_ACCEPT, BTNTYPE_CANCEL);
        Serial.println("Buttons set");
        dlg->show(myHeaderPgm, true);
        Serial.println("Show done");
        dlg->copyIntoBuffer("Testing proceeding");
        Serial.println("Show dialog complete");
    }

	taskManager.scheduleFixedRate(5, [] {
	 	int wifiState = wifiWidget.getCurrentState() + 1;
	 	if (wifiState == 5) wifiState = 0;
	 	wifiWidget.setCurrentState(wifiState);

	 	connectedWidget.setCurrentState(!connectedWidget.getCurrentState());
	}, TIME_SECONDS);
}

void loop() {
	taskManager.runLoop();
}
