#include <Wire.h>
#include <SSD1306Ascii.h>
#include <SSD1306AsciiWire.h>
#include "ssd1306asciiRenderer.h"
#include <tcm_test/testFixtures.h>
#include <tcMenu.h>
#include <IoLogging.h>

void onPressMe(int);
#define PRESSMECALLBACK onPressMe

#define I2C_ADDRESS 0x3C

/*
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
*/
SSD1306AsciiWire oled;
SSD1306AsciiRenderer renderer(20, 8);

void setup() {
	Serial.println("Testing LiquidCrystal driver");

    Wire.begin();
    Serial.begin(115200);
	Serial.println("Serial and wire started");

    oled.begin(&SH1106_128x64, I2C_ADDRESS);
	Serial.println("Device initialised");
    oled.setFont(System5x7);
    oled.clear();

	Serial.println("Setting render device");
    //renderer.setEditorChars('<', '>', '=');
    renderer.setGraphicsDevice(&oled);

	Serial.println("initialising..");
	menuMgr.initWithoutInput(&renderer, &menuVolume);
//	menuCaseTemp.setNext(&listItem);
    menuVolume.setEditing(true);
    menuChannel.setActive(true);
	taskManager.scheduleFixedRate(1000, [] {
        menuVolume.setCurrentValue(random(0, 255));
    });
}

void loop() {
    taskManager.runLoop();    
}

const char helloWorld[] PROGMEM = "Pairing mode..";
const char secondMsg[] PROGMEM = "Pairing done..";

void onPressMe(int /*id*/) {
    // BaseDialog* dialog = renderer.getDialog();
    // if(dialog == NULL) return; // no dialog available.

    // dialog->show(helloWorld, false, [] (ButtonType type, void *data) {
    //     if(type == BTNTYPE_ACCEPT) {
    //         BaseDialog* dialog = renderer.getDialog();
    //         dialog->show(secondMsg, false);
    //         dialog->setButtons(BTNTYPE_NONE, BTNTYPE_OK);
    //         dialog->copyIntoBuffer((char*)data);
    //     }
    // });
    // dialog->setButtons(BTNTYPE_NONE, BTNTYPE_CANCEL);
    // dialog->copyIntoBuffer("Nothing found");
    // dialog->setUserData((void*)"Something else");
    // taskManager.scheduleOnce(2000, [] {
    //     BaseDialog* dialog = renderer.getDialog();
    //     dialog->copyIntoBuffer("Daves Phone");
    //     dialog->setButtons(BTNTYPE_ACCEPT, BTNTYPE_CANCEL, 1);
    // });
}