#include <U8g2lib.h>
#include <Wire.h>
#include <IoAbstraction.h>
#include "tcm_test/testFixtures.h"

U8G2_SSD1306_128X64_NONAME_F_SW_I2C u8g2(U8G2_R0, 15, 4, 16);

U8g2MenuRenderer renderer;
U8g2GfxMenuConfig config;

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

const uint8_t* iconsConnection[] PROGMEM = { iconConnectionNone, iconConnected };
TitleWidget connectedWidget(iconsConnection, 2, 16, 10);

const char applicationName[] = "Graphics Test";

void setup() {
	Serial.begin(115200);
	Serial.print("Testing u8g2 driver");
	Serial.println(applicationName);

    u8g2.begin();

	// either one as needed for testing..
	//prepareAdaColorDefaultGfxConfig(&config);
	prepareBasicU8x8Config(config);

	renderer.setGraphicsDevice(&u8g2, &config);
  
	menuMgr.initWithoutInput(&renderer, &menuVolume);
	menuVolume.setActive(true);
	renderer.setFirstWidget(&connectedWidget);

	taskManager.scheduleFixedRate(5, [] {
		renderer.onSelectPressed(&menuVolume);
		menuVolume.setCurrentValue(menuVolume.getCurrentValue() + 1);

		connectedWidget.setCurrentState(!connectedWidget.getCurrentState());
	}, TIME_SECONDS);
}

void loop() {
    taskManager.runLoop();
}