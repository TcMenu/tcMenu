/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

#include "tcMenu.h"
#include "MenuItems.h"

bool MenuItem::isSendRemoteNeeded(uint8_t remoteNo) {
	remoteNo += 5;
	return flags & (1 << remoteNo);
}

void MenuItem::setSendRemoteNeeded(uint8_t remoteNo, bool needed) {
	remoteNo += 5;
	bitWrite(flags, remoteNo, needed);
}

void MenuItem::setSendRemoteNeededAll(bool needed) {
	if(needed) {
		flags = flags | MENUITEM_ALL_REMOTES;
	}
	else {
		flags = flags & (~MENUITEM_ALL_REMOTES);
	}
}

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

void SubMenuItem::load(EepromAbstraction& eeprom) {
	MenuItem* chItem = child;
	while (chItem != NULL) {
		chItem->load(eeprom);
		if(chItem->isChanged()) {
			menuMgr.menuItemChanged(chItem);
		}
		chItem = chItem->getNext();
	}
}

void SubMenuItem::save(EepromAbstraction& eeprom) {
	MenuItem* chItem = child;
	while (chItem != NULL) {
		chItem->save(eeprom);
		chItem = chItem->getNext();
	}
}

const char * BackMenuItem::getNamePgm(){
	return pgmName;
}

void BooleanMenuItem::setBoolean(bool newVal) {
	setSendRemoteNeededAll(currentValue != newVal);
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

void TextMenuItem::load(EepromAbstraction& eeprom) {
	uint16_t eepromAddr = pgm_read_word_near(&menuInfo->eeprom);
	if(eepromAddr == 0xffff) return;
	uint8_t len = textLength();
	eeprom.readIntoMemArray((uint8_t*)menuText, eepromAddr, len);
	menuText[len - 1] = 0; // make sure it's properly terminated!!
	setSendRemoteNeededAll(true);
}

void TextMenuItem::save(EepromAbstraction& eeprom) {
	uint16_t eepromAddr = pgm_read_word_near(&menuInfo->eeprom);
	if (eepromAddr == 0xffff) return;
	uint8_t len = textLength();
	eeprom.writeArrayToRom(eepromAddr, (uint8_t*)menuText, len);
}

void TextMenuItem::setTextValue(const char* text) {
	setSendRemoteNeededAll(strncmp(menuText, text, textLength()));
	strncpy(menuText, text, textLength());
	menuText[textLength() - 1] = 0;
	setChanged(true);
}
