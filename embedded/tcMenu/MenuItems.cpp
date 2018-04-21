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
