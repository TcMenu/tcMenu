/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

#ifndef _BASE_RENDERERS_H_
#define _BASE_RENDERERS_H_

#include "tcMenu.h"
#include <LiquidCrystalIO.h>

/** 
 * Each display must have a renderer, even if it is the NoRenderer, the NoRenderer is for situations
 * where the control is performed by a remote device such as netowrk or rs232.
 */
class MenuRenderer {
public:
	virtual ~MenuRenderer() { }
	virtual void activeIndexChanged(uint8_t index) = 0;
	virtual MenuItem* getCurrentEditor() = 0;
	virtual void setCurrentEditor(MenuItem* editItem) = 0;
	virtual MenuItem* getCurrentSubMenu() = 0;
	virtual void initialise() = 0;
	virtual void render() = 0;

};

/**
 * Used by renderers to determine how significant a redraw is needed at the next redraw interval.
 * They are prioritised in ascending order, so a more complete redraw has a higher number.
 */
enum MenuRedrawState : byte {
	MENUDRAW_NO_CHANGE = 0, MENUDRAW_EDITOR_CHANGE, MENUDRAW_COMPLETE_REDRAW
};



/**
 * A renderer that does nothing, for cases where there's no display
 */
class NoRenderer : public MenuRenderer {
private:
public:
	virtual void activeIndexChanged(__attribute__((unused)) uint8_t ignored) {  }
	virtual MenuItem* getCurrentSubMenu() { return NULL; }
	virtual MenuItem* getCurrentEditor() { return NULL; }
	virtual void setCurrentEditor(__attribute__((unused)) MenuItem* ignored) { }
	virtual void initialise() { }
	virtual void render() {  }
};

/**
 * A renderer that can renderer onto a LiquidCrystal display and supports the concept of single level
 * sub menus, active items and editing.
 */
class LiquidCrystalRenderer : public MenuRenderer {
private:
	LiquidCrystal* lcd;
	uint8_t dimX, dimY;
	char* buffer;
	uint8_t ticksToReset;
	MenuRedrawState redrawMode;
	MenuItem* currentRoot;
	MenuItem* currentEditor;
public:
	static LiquidCrystalRenderer* INSTANCE;
	LiquidCrystalRenderer(LiquidCrystal& lcd, uint8_t dimX, uint8_t dimY);
	virtual void initialise();
	virtual ~LiquidCrystalRenderer() { delete this->buffer; }
	virtual MenuItem* getCurrentEditor() { return currentEditor; }
	virtual MenuItem* getCurrentSubMenu() { return currentRoot; }
	virtual void setCurrentEditor(MenuItem* editor);
	virtual void activeIndexChanged(uint8_t index);
	virtual void render();

private:
	void redrawRequirement(MenuRedrawState state) { if (state > redrawMode) redrawMode = state; }
	void menuAltered() { ticksToReset = 120; }
	MenuItem* getItemAtPosition(uint8_t pos);
	void setupForEditing(MenuItem* toEdit);
	void prepareNewSubmenu(MenuItem* newItems);
	void resetToDefault();
	int offsetOfCurrentActive();

	virtual void renderMenuItem(uint8_t row, MenuItem* item);
	void renderAnalogItem(AnalogMenuItem* item);
	void renderEnumItem(EnumMenuItem* item);
	void renderBooleanItem(BooleanMenuItem* item);
	void renderSubItem(SubMenuItem* item);
	void renderBackItem(BackMenuItem* item);
};

inline MenuRenderer* liquidCrystalRenderer(LiquidCrystal& lcd, uint8_t dimX, uint8_t dimY) {
	return new LiquidCrystalRenderer(lcd, dimX, dimY);
}

#endif
