/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

#include <tcMenu.h>
#include "BaseRenderers.h"

LiquidCrystalRenderer::LiquidCrystalRenderer(LiquidCrystal& lcd, uint8_t dimX, uint8_t dimY) {

	this->dimX = dimX;
	this->dimY = dimY;
	this->buffer = new char[dimX + 2];
	this->lcd = &lcd;

	currentEditor = NULL;
	currentRoot = menuMgr.getRoot();
	redrawRequirement(MENUDRAW_COMPLETE_REDRAW);
}

LiquidCrystalRenderer* LiquidCrystalRenderer::INSTANCE;

void doRender() {
	LiquidCrystalRenderer::INSTANCE->render();
}

void LiquidCrystalRenderer::initialise() {
	INSTANCE = this;
	resetToDefault();
	taskManager.scheduleFixedRate(250, doRender);
}

void LiquidCrystalRenderer::activeIndexChanged(uint8_t index) {
	MenuItem* currentActive = menuMgr.findCurrentActive();
	currentActive->setActive(false);
	currentActive = getItemAtPosition(index);
	currentActive->setActive(true);
	menuAltered();
}

void LiquidCrystalRenderer::setupForEditing(MenuItem* item) {
	if(currentEditor != NULL) {
		currentEditor->setEditing(false);
		currentEditor->setActive(false);
	}
	MenuType ty = item->getMenuType();
	// these are the only types we can edit with a rotary encoder & LCD.
	if ((ty == MENUTYPE_ENUM_VALUE || ty == MENUTYPE_INT_VALUE || ty == MENUTYPE_BOOLEAN_VALUE) && !item->isReadOnly()) {
		currentEditor = item;
		currentEditor->setEditing(true);
		menuMgr.changePrecisionForType(currentEditor);
	}
}

uint8_t itemCount(MenuItem* item) {
	uint8_t count = 0;
	while (item) {
		++count;
		item = item->getNext();
	}
	return count;
}

void LiquidCrystalRenderer::prepareNewSubmenu(MenuItem* newItems) {
	menuAltered();
	currentRoot = newItems;
	currentRoot->setActive(true);

	menuMgr.setItemsInCurrentMenu(itemCount(newItems) - 1);
	redrawRequirement(MENUDRAW_COMPLETE_REDRAW);
}

MenuItem* LiquidCrystalRenderer::getItemAtPosition(uint8_t pos) {
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

void LiquidCrystalRenderer::resetToDefault() {
	prepareNewSubmenu(menuMgr.getRoot());
	setupForEditing(getItemAtPosition(0));
	ticksToReset = 255;
}

void LiquidCrystalRenderer::setCurrentEditor(MenuItem* toEdit) {
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

int LiquidCrystalRenderer::offsetOfCurrentActive() {
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

void LiquidCrystalRenderer::render() {
	uint8_t locRedrawMode = redrawMode;
	redrawMode = MENUDRAW_NO_CHANGE;
	if (locRedrawMode == MENUDRAW_COMPLETE_REDRAW) {
		lcd->clear();
	}

	if (ticksToReset == 0) {
		resetToDefault();
		ticksToReset = 255;
	}
	else if (ticksToReset != 255) {
		--ticksToReset;
	}

	MenuItem* item = currentRoot;
	uint8_t cnt = 0;
	if (offsetOfCurrentActive() >= dimY) {
		uint8_t toOffsetBy = (offsetOfCurrentActive() - dimY) + 1;
		while (item != NULL && toOffsetBy--) {
			item = item->getNext();
		}
	}

	while (item && cnt < dimY) {
		if (locRedrawMode != MENUDRAW_NO_CHANGE || item->isChanged()) {
			renderMenuItem(cnt, item);
		}
		++cnt;
		item = item->getNext();
	}
}

void LiquidCrystalRenderer::renderMenuItem(uint8_t row, MenuItem* item) {
	if (item == NULL || row > dimY) return;

	lcd->setCursor(0, row);

	memset(buffer, 32, dimX);
	buffer[dimX] = 0;

	switch (item->getMenuType()) {
	case MENUTYPE_INT_VALUE:
		renderAnalogItem((AnalogMenuItem*)item);
		break;
	case MENUTYPE_ENUM_VALUE:
		renderEnumItem((EnumMenuItem*)item);
		break;
	case MENUTYPE_BOOLEAN_VALUE:
		renderBooleanItem((BooleanMenuItem*)item);
		break;
	case MENUTYPE_SUB_VALUE:
		renderSubItem((SubMenuItem*)item);
		break;
	case MENUTYPE_BACK_VALUE:
		renderBackItem((BackMenuItem*)item);
		break;
	case MENUTYPE_TEXT_VALUE:
		renderTextItem((TextMenuItem*)item);
		break;
	default:
		strcpy(buffer, "unknown type..");
		break;
	}
	lcd->print(buffer);
}

inline void renderName(char * buffer, MenuItem* itm) {
	buffer[0] = itm->isEditing() ? '=' : (itm->isActive() ? '>' : ' ');
	const char * name = itm->getNamePgm();
	while (char nm = pgm_read_byte_near(name)) {
		*(++buffer) = nm;
		++name;
	}
}

void LiquidCrystalRenderer::renderTextItem(TextMenuItem* item) {
	renderName(buffer, item);

	uint8_t count = strlen(item->getTextValue());
	int cpy = dimX - count;
	strcpy(buffer + cpy, item->getTextValue());
}

void LiquidCrystalRenderer::renderAnalogItem(AnalogMenuItem* item) {
	renderName(buffer, item);

	char itoaBuf[10];

	int calcVal = item->getCurrentValue() + ((int)pgm_read_word_near(&item->getMenuInfo()->offset));
	uint8_t divisor = ((int)pgm_read_word_near(&item->getMenuInfo()->divisor));

	if (divisor < 2) {
		itoa(calcVal, itoaBuf, 10);
	}
	else if (divisor > 10) {
		int whole = calcVal / divisor;
		uint8_t fraction = abs((calcVal % divisor) * divisor);

		itoa(whole, itoaBuf, 10);
		uint8_t decPart = strlen(itoaBuf);
		itoaBuf[decPart] = '.';
		itoa(fraction, &itoaBuf[decPart + 1], 10);
	}
	else {
		int whole = calcVal / divisor;
		uint8_t fraction = abs((calcVal % divisor) * (10 / divisor));

		itoa(whole, itoaBuf, 10);
		uint8_t decPart = strlen(itoaBuf);
		itoaBuf[decPart] = '.';
		itoaBuf[decPart + 1] = fraction + '0';
		itoaBuf[decPart + 2] = 0;
	}
	uint8_t numLen = strlen(itoaBuf);
	uint8_t unitLen = strlen_P(item->getMenuInfo()->unitName);
	uint8_t startPlace = dimX - (numLen + unitLen);
	strcpy(buffer + startPlace, itoaBuf);
	strcpy_P(buffer + (dimX - unitLen), item->getMenuInfo()->unitName);

}

void LiquidCrystalRenderer::renderEnumItem(EnumMenuItem* item) {
	renderName(buffer, item);
	char** itemPtr = ((char**)pgm_read_ptr_near(&item->getMenuInfo()->menuItems)) + item->getCurrentValue();
	char* itemLoc = (char *)pgm_read_ptr_near(itemPtr);
	uint8_t count = strlen_P(itemLoc);
	int cpy = dimX - count;
	strcpy_P(buffer + cpy, itemLoc);
}

const char ON_STR[] PROGMEM   = " ON";
const char OFF_STR[] PROGMEM  = "OFF";
const char YES_STR[] PROGMEM  = "YES";
const char NO_STR[] PROGMEM   = " NO";
const char TRUE_STR[] PROGMEM = " TRUE";
const char FALSE_STR[] PROGMEM= "FALSE";
const char SUB_STR[] PROGMEM  = "->>>";

void LiquidCrystalRenderer::renderBooleanItem(BooleanMenuItem* item) {
	renderName(buffer, item);

	BooleanNaming naming = (BooleanNaming)pgm_read_byte_near(&item->getBooleanMenuInfo()->naming);
	if(naming == NAMING_ON_OFF) {
		strcpy_P(buffer + (dimX - 3), item->getBoolean() ? ON_STR : OFF_STR);
	}
	else if (naming == NAMING_YES_NO) {
		strcpy_P(buffer + (dimX - 3), item->getBoolean() ? YES_STR : NO_STR);
	}
	else {
		strcpy_P(buffer + (dimX - 5), item->getBoolean() ? TRUE_STR : FALSE_STR);
	}
}

void LiquidCrystalRenderer::renderSubItem(SubMenuItem* item) {
	renderName(buffer, item);

	strcpy_P(buffer + (dimX - 4), SUB_STR);
}

const char BACK_MENU_NAME[] PROGMEM  = "[Back]";

void LiquidCrystalRenderer::renderBackItem(BackMenuItem* item) {
	renderName(buffer, item);
	strcpy_P(buffer + (dimX-6), BACK_MENU_NAME);
}
