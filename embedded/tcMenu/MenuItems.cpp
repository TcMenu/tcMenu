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
		if(chItem->isChanged()) {
			menuMgr.menuItemChanged(chItem);
		}
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
	setSendRemoteNeeded(currentValue != newVal);
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
	char* eepromAddr = pgm_read_ptr_near(&menuInfo->eeprom);
	if(eepromAddr == (char*)0xffff) return;
	uint8_t len = textLength();
	eeprom_read_block(menuText, eepromAddr, len);
	menuText[len - 1] = 0; // make sure it's properly terminated!!
	setSendRemoteNeeded(true);
}

void TextMenuItem::save() {
	uint8_t* eepromAddr = pgm_read_ptr_near(&menuInfo->eeprom);
	if (eepromAddr == (uint8_t*)0xffff) return;
	uint8_t len = textLength();
	eeprom_write_block(menuText, eepromAddr, len);
}

void TextMenuItem::setTextValue(const char* text) {
	setSendRemoteNeeded(strncmp(menuText, text, textLength()));
	strncpy(menuText, text, textLength());
	menuText[textLength() - 1] = 0;
	setChanged(true);
}
