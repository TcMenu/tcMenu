/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

#ifndef _BASE_RENDERERS_H_
#define _BASE_RENDERERS_H_

#include "tcMenu.h"

/** the frequency at which the screen is redrawn (only if needed). */
#define SCREEN_DRAW_INTERVAL 250
/** the number of ticks the menu should reset to defaults after not being used */
#define TICKS_BEFORE_DEFAULTING 120

/** Should you wish to take over the rendering, function of this type is needed */
typedef void (*RendererCallbackFn)(bool userClicked);

/**
 * Title widgets allow for drawing small graphics in the title area, for example connectivity status
 * of the wifi network, if a remote connection to the menu is active. They are in a linked list so
 * as to make storage as efficient as possible. Chain them together using the constructor or setNext().
 * Image icons should be declared in PROGMEM.
 * 
 * Thread / interrupt safety: get/setCurrentState & isChanged can be called from threads / interrupts
 * 
 * Currently, only graphical renderers can use title widgets.
 */
class TitleWidget {
private:
	const uint8_t **iconData;
	volatile uint8_t currentState;
	volatile bool changed;
	uint8_t width;
	uint8_t height;
	TitleWidget* next;
public:
	/** Construct a widget with its icons and size */
	TitleWidget(const uint8_t** icons, uint8_t width, uint8_t height, TitleWidget* next = NULL);
	/** Get the current state that the widget represents */
	uint8_t getCurrentState() {return currentState;}
	/** gets the current icon data */
	const uint8_t* getCurrentIcon() {return iconData[currentState]; changed = false;}
	/** sets the current state of the widget, there must be an icon for this value */
	void setCurrentState(uint8_t state) {this->currentState = state;this->changed = true;}
	/** checks if the widget has changed since last drawn. */
	bool isChanged() {return this->changed;}
	/** gets the width of all the images */
	uint8_t getWidth() {return width;}
	/** gets the height of all the images */
	uint8_t getHeight() {return height;}
	/** gets the next widget in the chain or null */
	TitleWidget* getNext() {return next;}
	/** sets the next widget in the chain */
	void setNext(TitleWidget* next) {this->next = next;}
};

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
	char* buffer;
	uint8_t bufferSize;
	TitleWidget* firstWidget;
	uint8_t ticksToReset;
	MenuRedrawState redrawMode;
	uint8_t lastOffset;
	RendererCallbackFn renderCallback;
	MenuItem* currentRoot;
	MenuItem* currentEditor;

public:
	BaseMenuRenderer(int bufferSize);
	virtual ~BaseMenuRenderer() {delete buffer;}
	virtual void initialise();
	virtual void render() = 0;
	virtual MenuItem* getCurrentEditor() { return currentEditor; }
	virtual MenuItem* getCurrentSubMenu() { return currentRoot; }
	virtual void activeIndexChanged(uint8_t index);
	virtual void setCurrentEditor(MenuItem* editor);

	void setFirstWidget(TitleWidget* widget);

	/**
	 * Called when the menu has been altered, to reset the countdown to
	 * reset behaviour
	 */
	void menuAltered() { ticksToReset = TICKS_BEFORE_DEFAULTING; }

	/**
	 * In order to take over the display, provide a callback function that will receive
	 * the regular render call backs instead of this renderer.
	 * @param displayFn the callback to render the display
	 */
	void takeOverDisplay(RendererCallbackFn displayFn);

	/**
	 * Call this method to clear custom display rendering after a call to takeOverDisplay.
	 * It will cause a complete repaint of the display.
	 */
	void giveBackDisplay();

	/**
	 * Returns a pointer to the rendering callback
	 */
	RendererCallbackFn getRenderingCallback() { return renderCallback; }

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
	void findFirstVisible();

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
