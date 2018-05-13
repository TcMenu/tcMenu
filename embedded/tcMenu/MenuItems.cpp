/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

#include "tcMenu.h"
#include "MenuItems.h"

AnalogMenuItem::AnalogMenuItem(const AnalogMenuInfo* info, uint16_t currentValue, MenuItem* next) {
	this->init(info, currentValue, next);
}

EnumMenuItem::EnumMenuItem(const EnumMenuInfo *info, uint8_t defaultVal, MenuItem* next) {
	this->init(info, defaultVal, next);
}

BooleanMenuItem::BooleanMenuItem(const BooleanMenuInfo* info, bool defaultVal, MenuItem* next) {
	this->next = next;
	this->info = info;
	this->currentValue = defaultVal;
	this->flags = 0;
}

SubMenuItem::SubMenuItem(const SubMenuInfo* info, MenuItem* child, MenuItem* next) {
	this->info = info;
	this->child = child;
	this->next = next;
}

void SubMenuItem::load() {
	MenuItem* chItem = child;
	while (chItem != NULL) {
		chItem->load();
		chItem = chItem->getNext();
	}
}

void SubMenuItem::save() {
	MenuItem* chItem = child;
	while (chItem != NULL) {
		chItem->save();
		chItem = chItem->getNext();
	}
}

const char * BackMenuItem::getNamePgm(){
	return pgmName;
}

void BooleanMenuItem::setBoolean(bool newVal) {
	currentValue = newVal; 
	setChanged(true);
}

TextMenuItem::TextMenuItem(const TextMenuInfo* textInfo, MenuItem* next) {
	this->menuInfo = textInfo;
	menuText = new char[textLength()];
	menuText[0] = 0;
	this->next = next;
	flags=0;
}

void TextMenuItem::load() {
	const uint8_t* eepromStart = pgm_read_ptr_near(menuInfo->eeprom);
	if(eepromStart == (const uint8_t*)0xffff) return;
	uint8_t len = textLength();
	for(int i=0;i<len;i++) {
		menuText[i] = (char) eeprom_read_byte(eepromStart + i);
	}
}

void TextMenuItem::save() {
	uint8_t* eepromAddr = pgm_read_word_near(&menuInfo->eeprom);
	if (eepromAddr == (uint8_t*)0xffff) return;
	uint8_t len = textLength();

	for(int i=0;i<len;i++) {
		if (((char)eeprom_read_byte(eepromAddr + i)) != menuText[i]) {
			eeprom_update_byte(eepromAddr + i, (uint8_t) menuText[i]);
		}
	}
}
