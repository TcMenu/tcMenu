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
			((ValueMenuItem*)currentEditor)->setCurrentValue(value);
		}
	}
	else {
		renderer->activeIndexChanged(value / switches.getMenuDivisor());
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
		switches.changeEncoderPrecision(item->getMaximumValue(), ((ValueMenuItem*)item)->getCurrentValue());
	}
}

void loadRecursively(EepromAbstraction& eeprom, MenuItem* nextMenuItem) {
	while(nextMenuItem) {
		if(nextMenuItem->getMenuType() == MENUTYPE_SUB_VALUE) {
			loadRecursively(eeprom, ((SubMenuItem*)nextMenuItem)->getChild());
		}
		else if(nextMenuItem->getMenuType() == MENUTYPE_TEXT_VALUE) {
			TextMenuItem* textItem = (TextMenuItem*) nextMenuItem;
			eeprom.readIntoMemArray((uint8_t*) textItem->getTextValue(), textItem->getEepromPosition(), textItem->getMaximumValue());
			textItem->setSendRemoteNeededAll(true);
			textItem->setChanged(true);
		}
		else if(nextMenuItem->getMenuType() == MENUTYPE_INT_VALUE) {
			AnalogMenuItem* intItem = (AnalogMenuItem*)nextMenuItem;
			intItem->setCurrentValue(eeprom.read16(intItem->getEepromPosition()));
		}
		else if(nextMenuItem->getMenuType() == MENUTYPE_ENUM_VALUE) {
			EnumMenuItem* valItem = (EnumMenuItem*)nextMenuItem;
			valItem->setCurrentValue(eeprom.read16(valItem->getEepromPosition()));
		}
		else if(nextMenuItem->getMenuType() == MENUTYPE_BOOLEAN_VALUE) {
			BooleanMenuItem* valItem = (BooleanMenuItem*)nextMenuItem;
			valItem->setCurrentValue(eeprom.read8(valItem->getEepromPosition()));
		}
		nextMenuItem = nextMenuItem->getNext();
	}
}

void MenuManager::load(EepromAbstraction& eeprom, uint16_t magicKey) {
	if(eeprom.read16(0) == magicKey) {
		MenuItem* nextMenuItem = rootMenu;
		loadRecursively(eeprom, nextMenuItem);
	}
}

void saveRecursively(EepromAbstraction& eeprom, MenuItem* nextMenuItem) {
	while(nextMenuItem) {
		if(nextMenuItem->getMenuType() == MENUTYPE_SUB_VALUE) {
			saveRecursively(eeprom, ((SubMenuItem*)nextMenuItem)->getChild());
		}
		else if(nextMenuItem->getMenuType() == MENUTYPE_TEXT_VALUE) {
			TextMenuItem* textItem = (TextMenuItem*) nextMenuItem;
			eeprom.writeArrayToRom(textItem->getEepromPosition(), (const uint8_t*) textItem->getTextValue(), textItem->getMaximumValue());
		}
		else if(nextMenuItem->getMenuType() == MENUTYPE_INT_VALUE) {
			AnalogMenuItem* intItem = (AnalogMenuItem*)nextMenuItem;
			eeprom.write16(intItem->getEepromPosition(), intItem->getCurrentValue());
		}
		else if(nextMenuItem->getMenuType() == MENUTYPE_ENUM_VALUE) {
			EnumMenuItem* valItem = (EnumMenuItem*)nextMenuItem;
			eeprom.write16(valItem->getEepromPosition(), valItem->getCurrentValue());
		}
		else if(nextMenuItem->getMenuType() == MENUTYPE_BOOLEAN_VALUE) {
			BooleanMenuItem* valItem = (BooleanMenuItem*)nextMenuItem;
			eeprom.write16(valItem->getEepromPosition(), valItem->getCurrentValue());
		}
		nextMenuItem = nextMenuItem->getNext();
	}
}

void MenuManager::save(EepromAbstraction& eeprom, uint16_t magicKey) {
	eeprom.write16(0, magicKey);
	saveRecursively(eeprom, rootMenu);
}