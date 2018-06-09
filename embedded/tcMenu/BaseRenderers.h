/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

#ifndef _BASE_RENDERERS_H_
#define _BASE_RENDERERS_H_

#include "tcMenu.h"
#include <LiquidCrystalIO.h>

/** the frequency at which the screen is redrawn (only if needed). */
#define SCREEN_DRAW_INTERVAL 250
/** the number of ticks the menu should reset to defaults after not being used */
#define TICKS_BEFORE_DEFAULTING 120

/** Should you wish to take over the rendering, function of this type is needed */
typedef void (*RendererCallbackFn)();

/** 
 * Each display must have a renderer, even if it is the NoRenderer, the NoRenderer is for situations
 * where the control is performed exclusively by a remote device.
 */
class MenuRenderer {
public:
	/**
	 * This is called when the menu manager is created, to let the display perform one off tasks
	 * to prepare the display for use
	 */
	virtual void initialise() = 0;

	/**
	 * Tell the renderer that a new item has become active
	 * @index the new active index
	 */
	virtual void activeIndexChanged(uint8_t index) = 0;

	/**
	 * Renders keep track of which item is currently being edited, only one item can be edited at
	 * once
	 * @returns the current item being edited
	 */
	virtual MenuItem* getCurrentEditor() = 0;

	/**
	 * Sets the current editor, only one item can be edited at once.
	 */
	virtual void setCurrentEditor(MenuItem* editItem) = 0;

	/**
	 * Renderers work out which submenu is current.
	 * @returns the current sub menu
	 */
	virtual MenuItem* getCurrentSubMenu() = 0;

	/**
	 * Use this function to tell the menu renderer to stop rendering and give control of the display
	 * to renderingFunction. After this call renderingFunction will be called every tick to redraw
	 * the display. See the limitations of each specific renderer for more information.
	 */
	virtual void takeOverDisplay(RendererCallbackFn renederingFunction) = 0;

	/**
	 * Tell menu library to take back control of the display. The renderingFunction will no longer
	 * be called.
	 */
	virtual void giveBackDisplay() = 0;

	/** virtual destructor is required by the language */
	virtual ~MenuRenderer() { }
};

/**
 * Used by renderers to determine how significant a redraw is needed at the next redraw interval.
 * They are prioritised in ascending order, so a more complete redraw has a higher number.
 */
enum MenuRedrawState : byte {
	MENUDRAW_NO_CHANGE = 0, MENUDRAW_EDITOR_CHANGE, MENUDRAW_COMPLETE_REDRAW
};

/**
 * Used by the base renderer, to indicate if you want text formatted left or right justified in
 * the buffer.
 */
enum MenuDrawJustification: byte {
	JUSTIFY_TEXT_LEFT, JUSTIFY_TEXT_RIGHT
};

/**
 * A renderer that does nothing, for cases where there's no display
 */
class NoRenderer : public MenuRenderer {
public:
	virtual void activeIndexChanged(__attribute__((unused)) uint8_t ignored) {  }
	virtual MenuItem* getCurrentSubMenu() { return NULL; }
	virtual MenuItem* getCurrentEditor() { return NULL; }
	virtual void setCurrentEditor(__attribute__((unused)) MenuItem* ignored) { }
	virtual void initialise() { }
};

/**
 * This class provides the base functionality that will be required by most implementations
 * of renderer, you can extend this class to provide the core functionality.
 */
class BaseMenuRenderer : public MenuRenderer {
protected:
	uint8_t ticksToReset;
	char* buffer;
	uint8_t bufferSize;
	MenuRedrawState redrawMode;
	RendererCallbackFn renderCallback;
	MenuItem* currentRoot;
	MenuItem* currentEditor;

public:
	BaseMenuRenderer(int bufferSize);
	virtual ~BaseMenuRenderer() {delete buffer;}
	virtual void initialise();

	virtual void render() = 0;
	virtual void takeOverDisplay(RendererCallbackFn displayFn);
	virtual void giveBackDisplay();
	virtual MenuItem* getCurrentEditor() { return currentEditor; }
	virtual MenuItem* getCurrentSubMenu() { return currentRoot; }
	virtual void activeIndexChanged(uint8_t index);
	virtual void setCurrentEditor(MenuItem* editor);
	RendererCallbackFn getRenderingCallback() { return renderCallback; }

	void menuAltered() { ticksToReset = TICKS_BEFORE_DEFAULTING; }

	static BaseMenuRenderer* INSTANCE;

protected:
	void menuValueToText(MenuItem* item, MenuDrawJustification justification);
	void handleTicks();
	void redrawRequirement(MenuRedrawState state) { if (state > redrawMode) redrawMode = state; }
	void resetToDefault();
	void prepareNewSubmenu(MenuItem* newItems);
	MenuItem* getItemAtPosition(uint8_t pos);
	void setupForEditing(MenuItem* toEdit);
	int offsetOfCurrentActive();
	void countdownToDefaulting();

private:
	void menuValueAnalog(AnalogMenuItem* item, MenuDrawJustification justification);
	void menuValueEnum(EnumMenuItem* item, MenuDrawJustification justification);
	void menuValueBool(BooleanMenuItem* item, MenuDrawJustification justification);
	void menuValueSub(SubMenuItem* item, MenuDrawJustification justification);
	void menuValueBack(BackMenuItem* item, MenuDrawJustification justification);
	void menuValueText(TextMenuItem* item, MenuDrawJustification justification);
};

/** Counts the number of items from this menu item to the end of the list */
uint8_t itemCount(MenuItem* item);

#endif
