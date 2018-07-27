/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

#include <Arduino.h>
#include "tcMenu.h"
#include <IoAbstraction.h>

MenuManager menuMgr;

void MenuManager::initForUpDownOk(MenuRenderer* renderer, MenuItem* root, uint8_t pinUp, uint8_t pinDown, uint8_t pinOk) {
	this->renderer = renderer;
	this->rootMenu = root;

	switches.addSwitch(pinOk, [](__attribute__((unused)) uint8_t key, bool held) {menuMgr.onMenuSelect(held); });
	setupUpDownButtonEncoder(pinUp, pinDown, [](int value) {menuMgr.valueChanged(value); });

	renderer->initialise();

}

void MenuManager::initForEncoder(MenuRenderer* renderer,  MenuItem* root, uint8_t encoderPinA, uint8_t encoderPinB, uint8_t encoderButton) {
	this->renderer = renderer;
	this->rootMenu = root;

	switches.addSwitch(encoderButton, [](__attribute__((unused)) uint8_t key, bool held) {menuMgr.onMenuSelect(held); });
	setupRotaryEncoderWithInterrupt(encoderPinA, encoderPinB, [](int value) {menuMgr.valueChanged(value); });

	renderer->initialise();
}

bool isMenuBoolean(MenuType ty) {
	return ty == MENUTYPE_BOOLEAN_VALUE;
}

bool isMenuEditable(MenuItem* item) {
	MenuType ty = item->getMenuType();
	return (ty == MENUTYPE_ENUM_VALUE || ty == MENUTYPE_INT_VALUE || ty == MENUTYPE_BOOLEAN_VALUE) && !item->isReadOnly();
}

void MenuManager::valueChanged(int value) {
	MenuItem* currentEditor = renderer->getCurrentEditor();
	if (currentEditor) {
		if (isMenuBoolean(currentEditor->getMenuType())) {
			((BooleanMenuItem*)currentEditor)->setBoolean(value != 0);
		}
		else if (isMenuEditable(currentEditor)) {
			((ValueMenuItem<AnalogMenuInfo*>*)currentEditor)->setCurrentValue(value);
		}
		menuItemChanged(currentEditor);
	}
	else {
		renderer->activeIndexChanged(value / switches.getMenuDivisor());
	}
}

void MenuManager::menuItemChanged(MenuItem* item) { 
	MenuCallbackFn fn = NULL;

	switch (item->getMenuType()) {
	case MENUTYPE_ENUM_VALUE:
		fn = (MenuCallbackFn)pgm_read_ptr_near(&((EnumMenuItem*)item)->getMenuInfo()->callback);
		break;
	case MENUTYPE_INT_VALUE:
		fn = (MenuCallbackFn)pgm_read_ptr_near(&((AnalogMenuItem*)item)->getMenuInfo()->callback);
		break;
	case MENUTYPE_BOOLEAN_VALUE:
		fn = (MenuCallbackFn)pgm_read_ptr_near(&((BooleanMenuItem*)item)->getBooleanMenuInfo()->callback);
		break;
	case MENUTYPE_TEXT_VALUE:
		fn = (MenuCallbackFn)pgm_read_ptr_near(&((TextMenuItem*)item)->getTextMenuInfo()->callback);
		break;
	default:
		fn = NULL;
		break;
	}

	if (fn) {
		fn(item->getId());
	}
}

void MenuManager::onMenuSelect(__attribute__((unused)) bool held) {
	if (renderer->getCurrentEditor() != NULL) {
		renderer->setCurrentEditor(NULL);
	}
	else {
		renderer->setCurrentEditor(findCurrentActive());
	}
}

MenuItem* MenuManager::findCurrentActive() {
	MenuItem* itm = renderer->getCurrentSubMenu();
	while (itm != NULL) {
		if (itm->isActive()) {
			return itm;
		}
		itm = itm->getNext();
	}

	return renderer->getCurrentSubMenu();
}

void MenuManager::changePrecisionForType(MenuItem* item) {
	if (isMenuBoolean(item->getMenuType())) {
		switches.changeEncoderPrecision(item->getMaximumValue(), 1);
	}
	else {
		switches.changeEncoderPrecision(item->getMaximumValue(), ((ValueMenuItem<void*>*)item)->getCurrentValue());
	}
}

void MenuManager::load(EepromAbstraction& eeprom, uint16_t magicKey) {
	if(eeprom.read16(0) == magicKey) {
		MenuItem* nextMenuItem = rootMenu;
		while(nextMenuItem) {
			nextMenuItem->load(eeprom);
			if(nextMenuItem->isChanged()) {
				menuItemChanged(nextMenuItem);
			}
			nextMenuItem = nextMenuItem->getNext();
		}
	}
}

void MenuManager::save(EepromAbstraction& eeprom, uint16_t magicKey) {
	eeprom.write16(0, magicKey);
	MenuItem* nextMenuItem = rootMenu;
	while(nextMenuItem) {
		nextMenuItem->save(eeprom);
		nextMenuItem = nextMenuItem->getNext();
	}

}
