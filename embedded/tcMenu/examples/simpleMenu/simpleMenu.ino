#include <IoAbstraction.h>
#include <tcMenu.h>
#include <Wire.h>
#include <LiquidCrystalIO.h>
#include <BaseRenderers.h>

#define ENCODER_BUTTON_PIN 24
#define BACK_BUTTON_PIN 25
#define ENCODER_PIN_A 2
#define ENCODER_PIN_B 3

#define LCD_PWM_CONTRAST 5
#define LCD_RS 1
#define LCD_EN 2
#define LCD_D4 3
#define LCD_D5 4
#define LCD_D6 5
#define LCD_D7 6

#define SCREEN_WIDTH 20
#define SCREEN_HEIGHT 4

LiquidCrystal lcd(LCD_RS, LCD_EN, LCD_D4, LCD_D5, LCD_D6, LCD_D7, ioFrom8754(0x20));

const PROGMEM BooleanMenuInfo minfBool2 = { "Boolean2", 5, 7, NAMING_ON_OFF };
BooleanMenuItem menuBoolean2(&minfBool2, false);

const PROGMEM BooleanMenuInfo minfBool = { "Boolean", 4, 6, NAMING_YES_NO };
BooleanMenuItem menuBoolean(&minfBool, false, &menuBoolean2);

BackMenuItem menuBackBools(&menuBoolean);


const PROGMEM SubMenuInfo minfParent = { "Sub menu", 100 };
SubMenuItem menuParent(&minfParent, &menuBackBools, NULL);

const char statusWarmUpStr[] PROGMEM = "Warm-up";
const char statusReadyStr[] PROGMEM = "Ready";
const char statusDcOutputStr[] PROGMEM = "DC output";
const char* const minfStatusStr[] PROGMEM = { statusWarmUpStr, statusReadyStr, statusDcOutputStr};
const PROGMEM EnumMenuInfo minfStatus = { "Status", 3, 0,minfStatusStr, 3 };
EnumMenuItem menuStatus(&minfStatus, 0, &menuParent);

const char channelComputerStr[] PROGMEM = "Computer";
const char channelTurntableStr[] PROGMEM = "Turntable";
const char channelAvStr[] PROGMEM = "A/V System";
const char* const minfChannelsStr[] PROGMEM  = { channelComputerStr, channelTurntableStr, channelAvStr };
const PROGMEM EnumMenuInfo minfChannels = { "Input", 2, 2, minfChannelsStr, 3 };
EnumMenuItem menuChannels(&minfChannels, 0, &menuStatus);

const PROGMEM AnalogMenuInfo minfVol = { "Volume", 1, 4, 255, -180, 2, "dB" };
AnalogMenuItem menuVolume(&minfVol, 50, &menuChannels);

void setup() {
	Wire.begin();
	pinMode(LCD_PWM_CONTRAST, OUTPUT);
	analogWrite(LCD_PWM_CONTRAST, 10);

	lcd.begin(SCREEN_WIDTH, SCREEN_HEIGHT);
	lcd.print("Starting up..");

	switches.initialise(ioUsingArduino());
		
	menuStatus.setReadOnly(true);

	menuMgr.initForEncoder(liquidCrystalRenderer(lcd, SCREEN_WIDTH, SCREEN_HEIGHT), &menuVolume, ENCODER_PIN_A, ENCODER_PIN_B, ENCODER_BUTTON_PIN);
}

void loop() {
	taskManager.runLoop();
}
