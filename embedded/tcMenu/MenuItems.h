/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

#ifndef _MENUITEMS_h
#define _MENUITEMS_h

#include <EepromAbstraction.h>

#define NAME_SIZE_T 20

#define NO_CALLBACK NULL

typedef void (*MenuCallbackFn)(int id);

/**
 * Every single info structure must have these fields in this order. Therefore it is always save to use this structure
 * in place of a specific one.
 */
struct AnyMenuInfo {
	char name[NAME_SIZE_T];
	uint16_t id;
	uint16_t eepromAddr;
	uint16_t maxValue;
	MenuCallbackFn callback;
};

/**
 * The information block stored in program memory for analog items. DO NOT move these items without considering AnyMenuInfo!!!
 */
struct AnalogMenuInfo {
	char name[NAME_SIZE_T];
	uint16_t id;
	uint16_t eepromAddr;
	uint16_t maxValue;
	MenuCallbackFn callback;
	int16_t offset;
	uint16_t divisor;
	char unitName[5];
};

/**
 * The information block stored in program memory for enumeration items. DO NOT move these items without considering AnyMenuInfo!!!
 */
struct EnumMenuInfo {
	char name[NAME_SIZE_T];
	uint16_t id;
	uint16_t eepromAddr;
	uint16_t noOfItems;
	MenuCallbackFn callback;
	const char * const *menuItems;
};


/**
 * These are the names for true / false that can be used in a menu item.
 */
enum BooleanNaming : byte {
	NAMING_TRUE_FALSE = 0,
	NAMING_ON_OFF,
	NAMING_YES_NO
};

/**
* The information block stored in program memory for boolean items. DO NOT move these items without considering AnyMenuInfo!!!
*/
struct BooleanMenuInfo {
	char name[NAME_SIZE_T];
	uint16_t id;
	uint16_t eepromAddr;
	uint16_t maxValue; // always 1, needed for consistency with other structs
	MenuCallbackFn callback;
	BooleanNaming naming;
};

/**
 * The information block for a submenu stored in program memory. DO NOT move these items without considering AnyMenuInfo!!!
 */
struct SubMenuInfo {
	char name[NAME_SIZE_T];
	uint16_t id;
	uint16_t eeprom;
	uint16_t maxValue;// always 0, needed for consistency with other structs
	MenuCallbackFn callback; // not used, for consistency with other structs
};

/**
 * The information block for a text menu component. DO NOT move these items without considering AnyMenuInfo!!!
 */
struct TextMenuInfo {
	char name[NAME_SIZE_T];
	uint16_t id;
	uint16_t eeprom;
	uint16_t maxLength;
	MenuCallbackFn callback;
};

/** 
 * Each menu item can be in the following states.
 */
enum Flags : byte {
	MENUITEM_ACTIVE = 0,       // the menu is currently active but not editing
	MENUITEM_CHANGED = 1,      // the menu has changed and needs drawing
	MENUITEM_READONLY = 2,     // the menu cannot be changed
	MENUITEM_EDITING = 3,      // the menu is being edited
	MENUITEM_REMOTE_SEND0 = 5, // the menu needs to be sent remotely (for remote 0)
	MENUITEM_REMOTE_SEND1 = 6, // the menu needs to be sent remotely (for remote 1)
	MENUITEM_REMOTE_SEND2 = 7  // the menu needs to be sent remotely (for remote 2)
};

#define MENUITEM_ALL_REMOTES (32+64+128)

/**
 * As we don't have RTTI we need a way of identifying each menu item. Any value below 100 is based
 * on ValueMenuItem and can therefore be edited, otherwise it cannot be edited on the device.
 */
enum MenuType : byte {
	MENUTYPE_INT_VALUE = 1,      // AnalogMenuItem
	MENUTYPE_ENUM_VALUE = 2,     // EnumMenuItem
	MENUTYPE_BOOLEAN_VALUE = 3,  // BooleanMenuItem
	MENUTYPE_SUB_VALUE = 100,    // SubMenuItem
	MENUTYPE_BACK_VALUE = 101,   // BackMenuItem
	MENUTYPE_TEXT_VALUE = 102    // TextMenuItem
};

/**
 * Any MenuType with an ID less than 100 is editable on the device. EG: Numeric, Boolean or choices
 */
#define menuTypeIsBasedOnValueType(x) (x < 99)

/**
 * This is the base class of all menu items, containing functionality to hold the current state of the menu
 * and determine which is the next menu in the chain. It also defines a few functions that all implementations
 * implement.
 */
class MenuItem {
protected:
	uint8_t flags;
	MenuType menuType;
	MenuItem* next;
	const AnyMenuInfo *info;
public:

	/** Gets hold of the name pointer - caution this is in PROGMEM */
	const char* getNamePgm() { return info->name; }
	/** Retrieves the ID from the info block */
	int getId() { return pgm_read_word_near(&info->id); }
	/** Retrieves the maximum value for this menu type */
	int getMaximumValue() { return pgm_read_word_near(&info->maxValue); }
	/** Retrieves the eeprom storage position for this menu (or 0xffff if not applicable) */
	int getEepromPosition() { return pgm_read_word_near(&info->eepromAddr); }
	/** returns the menu type as one of the above menu type enumeration */
	MenuType getMenuType() { return menuType; }
	/** returns the event callback associated with this item */
	void triggerCallback();

	/** set the item to be changed, this lets the renderer know it needs painting */
	void setChanged(bool changed) { bitWrite(flags, MENUITEM_CHANGED, changed); }
	/** returns the changed state of the item */
	bool isChanged() { return bitRead(flags, MENUITEM_CHANGED); }
	/** returns if the menu item needs to be sent remotely */
	bool isSendRemoteNeeded(uint8_t remoteNo);
	/** set the flag indicating that a remote refresh is needed for all remotes - default */
	void setSendRemoteNeededAll(bool needed);
	/** set the flag indicating that a remote refresh is needed for a specific remote */
	void setSendRemoteNeeded(uint8_t remoteNo, bool needed);

	/** sets this to be the active item, so that the renderer shows it highlighted */
	void setActive(bool active) { bitWrite(flags, MENUITEM_ACTIVE, active); setChanged(true); }
	/** returns the active status of the item */
	bool isActive() { return bitRead(flags, MENUITEM_ACTIVE); }

	/** sets this item as the currently being edited, so that the renderer shows it as being edited */
	void setEditing(bool active) { bitWrite(flags, MENUITEM_EDITING, active); setChanged(true); }
	/** returns true if the status is currently being edited */
	bool isEditing() { return bitRead(flags, MENUITEM_EDITING); }

	/** sets this item to be read only, so that the manager will not allow it to be edited */
	void setReadOnly(bool active) { bitWrite(flags, MENUITEM_READONLY, active); }
	/** returns true if this item is read only */
	bool isReadOnly() { return bitRead(flags, MENUITEM_READONLY); }

	/** gets the next menu (sibling) at this level */
	MenuItem* getNext() { return next; }

protected:
	/**
	 * Do not directly create menu items, always use the leaf classes, such as AnalogMenuItem etc.
	 */
	MenuItem(MenuType menuType, const AnyMenuInfo* menuInfo, MenuItem* next);
};

/** 
 * Represents an item that has a 16 bit unsigned integer backing it, and an info structure in program memory.
 */
class ValueMenuItem : public MenuItem {
protected:
	uint16_t currentValue;

	/** Use the leaf types, dont construct directly. Initialise an instance of this type with the required data values */
	ValueMenuItem(MenuType menuType, const AnyMenuInfo* info, uint16_t defaultVal, MenuItem* next = NULL) : MenuItem(menuType, info, next) {
		this->currentValue = defaultVal;
	}
public:
	/** Sets the integer current value to a new value, and marks the menu changed */
	void setCurrentValue(uint16_t val) {
		setChanged(true);
		setSendRemoteNeededAll(currentValue != val);
		currentValue = val;
		triggerCallback();
	}

	/** gets the current value */
	uint16_t getCurrentValue() { return currentValue; }
};

/**
 * The implementation of MenuItem for storing numbers. Goes with it's AnalogMenuInfo
 * counterpart.
 */
class AnalogMenuItem : public ValueMenuItem {
public:
	AnalogMenuItem(const AnalogMenuInfo* info, uint16_t defaultVal, MenuItem* next = NULL) : ValueMenuItem(MENUTYPE_INT_VALUE, (const AnyMenuInfo*)info, defaultVal, next) {;}

	int getOffset() { return (int) pgm_read_word_near(&((AnalogMenuInfo*)info)->offset);}
	int getDivisor() { return (int) pgm_read_word_near(&((AnalogMenuInfo*)info)->divisor);}
	int unitNameLength() {return (int) strlen_P(((AnalogMenuInfo*)info)->unitName);}
	void copyUnitToBuffer(char* unitBuff) { strcpy_P(unitBuff, ((AnalogMenuInfo*)info)->unitName);}
	const char* getUnitNamePgm() { return ((AnalogMenuInfo*)info)->unitName; }
};

/**
 * The implmentation of MenuItem for a list of choices, where only one can be chosen.
 * Goes with its EnumMenuInfo counterpart.
 */
class EnumMenuItem : public ValueMenuItem {
public:
	EnumMenuItem(const EnumMenuInfo *info, uint8_t defaultVal, MenuItem* next = NULL) : ValueMenuItem(MENUTYPE_ENUM_VALUE, (const AnyMenuInfo*)info, defaultVal, next) {;}

	void copyEnumStrToBuffer(char* buffer, int idx) {
		char** itemPtr = ((char**)pgm_read_ptr_near(&((EnumMenuInfo*)info)->menuItems) + idx);
		char* itemLoc = (char *)pgm_read_ptr_near(itemPtr);
		strcpy_P(buffer, itemLoc);
	}

	int getLengthOfEnumStr(int idx) {
		char** itemPtr = ((char**)pgm_read_ptr_near(&((EnumMenuInfo*)info)->menuItems) + idx);
		char* itemLoc = (char *)pgm_read_ptr_near(itemPtr);
		return strlen_P(itemLoc);
	}

	const char* getEntryPgm(int idx) { return ((char*)pgm_read_ptr_near((char**)pgm_read_ptr_near(&((EnumMenuInfo*)info)->menuItems) + idx)); }
};

/**
 * The implemenation of MenuItem for boolean, where it is true or false.
 */
class BooleanMenuItem : public ValueMenuItem {
public:
	BooleanMenuItem(const BooleanMenuInfo* info, bool defaultVal, MenuItem* next = NULL) : ValueMenuItem(MENUTYPE_BOOLEAN_VALUE, (const AnyMenuInfo*)info, defaultVal, next) {;}
	BooleanNaming getBooleanNaming() { return (BooleanNaming)pgm_read_byte_near(&((BooleanMenuInfo*)info)->naming); }

	bool getBoolean() {return currentValue != 0;}
	void setBoolean(bool b) {setCurrentValue(b);}
};

/**
 * The implementation of a Menuitem that can contain more menu items as children.
 */
class SubMenuItem : public MenuItem {
private:
	MenuItem* child;
public:
	SubMenuItem(const SubMenuInfo* info, MenuItem* child, MenuItem* next) : MenuItem(MENUTYPE_SUB_VALUE, (const AnyMenuInfo*)info, next) {this->child = child;}
	MenuItem* getChild() { return child; }
};

class BackMenuItem : public MenuItem {
public:
	BackMenuItem(MenuItem* nextChild, const AnyMenuInfo* parentInfo) : MenuItem(MENUTYPE_BACK_VALUE, parentInfo, nextChild) {;}
};

/**
 * TextMenuItem is for situations where text modified at runtime must be shown, for showing
 * a series of text values from PROGMEM storage use EnumMenuItem instead.
 */
class TextMenuItem : public MenuItem {
private:
	char *menuText;
public:
	TextMenuItem(const TextMenuInfo* textInfo, MenuItem* next) : MenuItem(MENUTYPE_TEXT_VALUE, (const AnyMenuInfo*)textInfo, next) { menuText = new char[textLength()]; menuText[0] = 0; }
	uint8_t textLength() { return getMaximumValue(); }

	void setTextValue(const char* text);
	const char* getTextValue() { return menuText; }
};
#endif

