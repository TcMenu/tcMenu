/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

#include <tcMenu.h>
#include "BaseRenderers.h"

BaseMenuRenderer* BaseMenuRenderer::INSTANCE;

void doRender() {
	RendererCallbackFn callbackFn = BaseMenuRenderer::INSTANCE->getRenderingCallback();
	if(callbackFn) {
		callbackFn(false);
	}
	else {
		BaseMenuRenderer::INSTANCE->render();
	}
}

BaseMenuRenderer::BaseMenuRenderer(int bufferSize) {
	buffer = new char[bufferSize + 1]; // add one to allow for the trailing 0.
	this->bufferSize = bufferSize;
	ticksToReset = 0;
	renderCallback = NULL;
	redrawMode = MENUDRAW_COMPLETE_REDRAW;
	this->currentEditor = NULL;
	this->currentRoot = menuMgr.getRoot();
	this->lastOffset = 0;
}

void BaseMenuRenderer::initialise() {
	INSTANCE = this;
	ticksToReset = 0;
	renderCallback = NULL;
	redrawMode = MENUDRAW_COMPLETE_REDRAW;

	resetToDefault();

	taskManager.scheduleFixedRate(SCREEN_DRAW_INTERVAL, doRender);
}

void BaseMenuRenderer::activeIndexChanged(uint8_t index) {
	MenuItem* currentActive = menuMgr.findCurrentActive();
	currentActive->setActive(false);
	currentActive = getItemAtPosition(index);
	currentActive->setActive(true);
	menuAltered();
}

void BaseMenuRenderer::resetToDefault() {
	prepareNewSubmenu(menuMgr.getRoot());
	setupForEditing(NULL);
	ticksToReset = 255;
}

void BaseMenuRenderer::countdownToDefaulting() {
	if (ticksToReset == 0) {
		resetToDefault();
		ticksToReset = 255;
	}
	else if (ticksToReset != 255) {
		--ticksToReset;
	}
}

void BaseMenuRenderer::menuValueToText(MenuItem* item,	MenuDrawJustification justification) {
	switch (item->getMenuType()) {
	case MENUTYPE_INT_VALUE:
		menuValueAnalog((AnalogMenuItem*)item, justification);
		break;
	case MENUTYPE_ENUM_VALUE:
		menuValueEnum((EnumMenuItem*)item, justification);
		break;
	case MENUTYPE_BOOLEAN_VALUE:
		menuValueBool((BooleanMenuItem*)item, justification);
		break;
	case MENUTYPE_SUB_VALUE:
		menuValueSub((SubMenuItem*)item, justification);
		break;
	case MENUTYPE_BACK_VALUE:
		menuValueBack((BackMenuItem*)item, justification);
		break;
	case MENUTYPE_TEXT_VALUE:
		menuValueText((TextMenuItem*)item, justification);
		break;
	default:
		strcpy(buffer, "???");
		break;
	}

}

void zeropaditoa(int range, int value, char* buff) {
	uint8_t pos = 0;
	do {
		range = range / 10;
		buff[pos] = (value / range) + '0';
		value = value % range;
		++pos;
	} while(range > 1);

	buff[pos]=0;
}

void BaseMenuRenderer::menuValueAnalog(AnalogMenuItem* item, MenuDrawJustification justification) {
	char itoaBuf[12];

	int32_t calcVal = ((int32_t)item->getCurrentValue()) + ((int32_t)item->getOffset());
	int divisor = item->getDivisor();

	if (divisor < 2) {
		// in this case divisor was 0 or 1, this means treat as integer.
		itoa(calcVal, itoaBuf, 10);
	}
	else if (divisor > 10) {
		// so we can display as decimal, work out the nearest highest unit for 2dp, 3dp and 4dp.
		int fractMax = (divisor > 1000) ? divisor = 10000 : (divisor >= 100) ? 1000 : 100;

		// when divisor is greater than 10 we need to deal with both parts using itoa
		int whole = calcVal / divisor;
		uint8_t fraction = abs((calcVal % divisor)) * (fractMax / divisor);

		itoa(whole, itoaBuf, 10);
		uint8_t decPart = strlen(itoaBuf);
		itoaBuf[decPart] = '.';
		zeropaditoa(fractMax, fraction, &itoaBuf[decPart + 1]);
	}
	else {
		// an efficient optimisation for fractions < 10.
		int whole = calcVal / divisor;
		uint8_t fraction = abs((calcVal % divisor)) * (10 / divisor);

		itoa(whole, itoaBuf, 10);
		uint8_t decPart = strlen(itoaBuf);
		itoaBuf[decPart] = '.';
		itoaBuf[decPart + 1] = fraction + '0';
		itoaBuf[decPart + 2] = 0;
	}
	uint8_t numLen = strlen(itoaBuf);

	if(justification == JUSTIFY_TEXT_LEFT) {
		strcpy(buffer, itoaBuf);
		item->copyUnitToBuffer(buffer + numLen);
	}
	else {
		uint8_t unitLen = item->unitNameLength();
		uint8_t startPlace = bufferSize - (numLen + unitLen);
		strcpy(buffer + startPlace, itoaBuf);
		item->copyUnitToBuffer(buffer + (bufferSize - unitLen));
	}

}

void BaseMenuRenderer::menuValueEnum(EnumMenuItem* item, MenuDrawJustification justification) {
	if(justification == JUSTIFY_TEXT_LEFT) {
		item->copyEnumStrToBuffer(buffer, item->getCurrentValue());
	}
	else {
		uint8_t count = item->getLengthOfEnumStr(item->getCurrentValue());
		item->copyEnumStrToBuffer(buffer + (bufferSize - count), item->getCurrentValue());
	}
}

const char ON_STR[] PROGMEM   = "ON";
const char OFF_STR[] PROGMEM  = "OFF";
const char YES_STR[] PROGMEM  = "YES";
const char NO_STR[] PROGMEM   = " NO";
const char TRUE_STR[] PROGMEM = " TRUE";
const char FALSE_STR[] PROGMEM= "FALSE";
const char SUB_STR[] PROGMEM  = "->>>";
const char BACK_MENU_NAME[] PROGMEM  = "[Back]";

void BaseMenuRenderer::menuValueBool(BooleanMenuItem* item, MenuDrawJustification justification) {
	BooleanNaming naming = item->getBooleanNaming();
	const char* val;
	switch(naming) {
	case NAMING_ON_OFF:
		val = item->getBoolean() ? ON_STR : OFF_STR;
		break;
	case NAMING_YES_NO:
		val = item->getBoolean() ? YES_STR : NO_STR;
		break;
	default:
		val = item->getBoolean() ? TRUE_STR : FALSE_STR;
		break;
	}

	if(justification == JUSTIFY_TEXT_LEFT) {
		strcpy_P(buffer, val);
	}
	else {
		uint8_t len = strlen_P(val);
		strcpy_P(buffer + (bufferSize - len), val);
	}
}

void BaseMenuRenderer::menuValueSub(__attribute((unused)) SubMenuItem* item, MenuDrawJustification justification) {
	if(justification == JUSTIFY_TEXT_LEFT) {
		strcpy_P(buffer, SUB_STR);
	}
	else {
		strcpy_P(buffer + (bufferSize - 4), SUB_STR);
	}
}

void BaseMenuRenderer::menuValueBack(__attribute((unused)) BackMenuItem* item, MenuDrawJustification justification) {
	if(justification == JUSTIFY_TEXT_LEFT) {
		strcpy_P(buffer, BACK_MENU_NAME);
	}
	else {
		strcpy_P(buffer + (bufferSize - 6), BACK_MENU_NAME);
	}
}

void BaseMenuRenderer::menuValueText(TextMenuItem* item, MenuDrawJustification justification) {
	if(justification == JUSTIFY_TEXT_LEFT) {
		strcpy(buffer, item->getTextValue());
	}
	else {
		uint8_t count = strlen(item->getTextValue());
		int cpy = bufferSize - count;
		strcpy(buffer + cpy, item->getTextValue());
	}
}

void BaseMenuRenderer::takeOverDisplay(RendererCallbackFn displayFn) {
	renderCallback = displayFn;
}

void BaseMenuRenderer::giveBackDisplay() {
	renderCallback = NULL;
	redrawMode = MENUDRAW_COMPLETE_REDRAW;
}

void BaseMenuRenderer::prepareNewSubmenu(MenuItem* newItems) {
	menuAltered();
	currentRoot = newItems;
	currentRoot->setActive(true);

	menuMgr.setItemsInCurrentMenu(itemCount(newItems) - 1);
	redrawRequirement(MENUDRAW_COMPLETE_REDRAW);
}

void BaseMenuRenderer::setupForEditing(MenuItem* item) {
	if(currentEditor != NULL) {
		currentEditor->setEditing(false);
		currentEditor->setActive(false);
	}

	// basically clear down editor state
	if(item == NULL) return;

	MenuType ty = item->getMenuType();
	// these are the only types we can edit with a rotary encoder & LCD.
	if ((ty == MENUTYPE_ENUM_VALUE || ty == MENUTYPE_INT_VALUE || ty == MENUTYPE_BOOLEAN_VALUE) && !item->isReadOnly()) {
		currentEditor = item;
		currentEditor->setEditing(true);
		menuMgr.changePrecisionForType(currentEditor);
	}
}


MenuItem* BaseMenuRenderer::getItemAtPosition(uint8_t pos) {
	uint8_t i = 0;
	MenuItem* itm = currentRoot;

	while (itm != NULL) {
		if (i == pos) {
			return itm;
		}
		i++;
		itm = itm->getNext();
	}

	return currentRoot;
}

int BaseMenuRenderer::offsetOfCurrentActive() {
	uint8_t i = 0;
	MenuItem* itm = currentRoot;
	while (itm != NULL) {
		if (itm->isActive()) {
			return i;
		}
		i++;
		itm = itm->getNext();
	}

	return 0;
}

void BaseMenuRenderer::setCurrentEditor(MenuItem* toEdit) {
	if(renderCallback) {
		// we dont handle click events when the display is taken over
		// instead we tell the custom renderer that we've had a click
		renderCallback(true);
	}

	if (currentEditor != NULL) {
		currentEditor->setEditing(false);
		currentEditor = NULL;
		menuMgr.setItemsInCurrentMenu(itemCount(currentRoot) - 1);
		redrawRequirement(MENUDRAW_EDITOR_CHANGE);
	}

	if(toEdit != NULL) {
		if (toEdit->getMenuType() == MENUTYPE_SUB_VALUE) {
			toEdit->setActive(false);
			prepareNewSubmenu(((SubMenuItem*)toEdit)->getChild());
		}
		else if (toEdit->getMenuType() == MENUTYPE_BACK_VALUE) {
			toEdit->setActive(false);
			prepareNewSubmenu(menuMgr.getRoot());
		}
		else {
			setupForEditing(toEdit);
			redrawRequirement(MENUDRAW_EDITOR_CHANGE);
		}
	}
	menuAltered();
}

void BaseMenuRenderer::setFirstWidget(TitleWidget* widget) {
	this->firstWidget = widget;
	this->redrawMode = MENUDRAW_COMPLETE_REDRAW;
}

uint8_t itemCount(MenuItem* item) {
	uint8_t count = 0;
	while (item) {
		++count;
		item = item->getNext();
	}
	return count;
}

TitleWidget::TitleWidget(const uint8_t ** icons, uint8_t width, uint8_t height, TitleWidget* next) {
	this->iconData = icons;
	this->width = width;
	this->height = height;
	this->currentState = 0;
	this->next = next;
	this->changed = true;
}
