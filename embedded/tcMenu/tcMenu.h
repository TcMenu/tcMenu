/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

#ifndef __MENU_MANAGER_H
#define __MENU_MANAGER_H

#include <LiquidCrystalIO.h>
#include <IoAbstraction.h>
#include <tcMenu.h>
#include "MenuItems.h"
#include "BaseRenderers.h"

class MenuManager {
private:
	MenuItem* rootMenu;
	MenuRenderer* renderer;

public:
	// TODO: next release, move into support class and create
	// TODO:   1/ encoder input
	// TODO:   2/ switch (up / down) input
	// TODO:   3/ Touch based input
	// TODO:   4/ No input device

	/**
	 * Handle the various types of input
	 */
	void initForEncoder(MenuRenderer* renderer, MenuItem* root, uint8_t encoderPinA, uint8_t encoderPinB, uint8_t encoderButton);
	void initForUpDownOk(MenuRenderer* renderer, MenuItem* root, uint8_t upPin, uint8_t downPin, uint8_t okPin);
	void valueChanged(int value);
	void onMenuSelect(bool held);
	void setItemsInCurrentMenu(int size) { switches.changeEncoderPrecision(size * switches.getMenuDivisor(), 0); }
	void changePrecisionForType(MenuItem* item);

	/**
	 * Used during initialisation to load the previously stored state. Only if the magic key matches at location 0.
	 */
	void load(uint16_t magicKey = 0xfade);

	/**
	 * Call to save all item values into eeprom. The magic key is saved at location 0 if not already set. This is a
	 * lazy save that reads the eeprom values first, and only saves to eeprom when there are changes.
	 */
	void save(uint16_t magicKey);

	/**
	 * Use this to record external changes made to a menu item. This will render and run any callbacks.
	 */
	void menuItemChanged(MenuItem* item);

	/**
	 * Find the menu item that is currently active.
	 */
	MenuItem* findCurrentActive();

	/**
	 * Get the root of all menus, the first menu item basically
	 */
	MenuItem* getRoot() { return rootMenu; }
};

extern MenuManager menuMgr;

#endif // defined header file
