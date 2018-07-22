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
 * The information block stored in program memory for analog items. DO NOT move the first 3 items!!!
 */
struct AnalogMenuInfo {
	char name[NAME_SIZE_T];
	uint16_t id;
	uint16_t eepromAddr;
	uint16_t maxValue;
	int offset;
	uint16_t divisor;
	char unitName[5];
	MenuCallbackFn callback;
};

/**
 * The information block stored in program memory for enumeration items. DO NOT move the first 3 items!!!
 */
struct EnumMenuInfo {
	char name[NAME_SIZE_T];
	uint16_t id;
	uint16_t eepromAddr;
	const char * const *menuItems;
	uint8_t noOfItems;
	MenuCallbackFn callback;
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
* The information block stored in program memory for boolean items. DO NOT move the first 3 items!!!
*/
struct BooleanMenuInfo {
	char name[NAME_SIZE_T];
	uint16_t id;
	uint16_t eepromAddr;
	BooleanNaming naming;
	MenuCallbackFn callback;
};

/**
 * The information block for a submenu stored in program memory
 */
struct SubMenuInfo {
	char name[NAME_SIZE_T];
	uint16_t id;
};

/**
 * The information block for a text menu component
 */
struct TextMenuInfo {
	char name[NAME_SIZE_T];
	uint16_t id;
	uint16_t eeprom;
	uint8_t length;
	MenuCallbackFn callback;
};

/** 
 * Each menu item can be in the following states.
 */
enum Flags : byte {
	MENUITEM_ACTIVE = 1,       // the menu is currently active but not editing
	MENUITEM_CHANGED = 2,      // the menu has changed and needs drawing
	MENUITEM_READONLY = 3,     // the menu cannot be changed
	MENUITEM_EDITING = 4,      // the menu is being edited
	MENUITEM_REMOTE_SEND0 = 6, // the menu needs to be sent remotely (for remote 0)
	MENUITEM_REMOTE_SEND1 = 7, // the menu needs to be sent remotely (for remote 1)
	MENUITEM_REMOTE_SEND2 = 8  // the menu needs to be sent remotely (for remote 2)
};

#define MENUITEM_ALL_REMOTES (32+64+128)

/**
 * As we don't have RTTI we need a way of identifying each menu item
 */
enum MenuType : byte {
	MENUTYPE_INT_VALUE = 1,  // AnalogMenuItem
	MENUTYPE_ENUM_VALUE,     // EnumMenuItem
	MENUTYPE_BOOLEAN_VALUE,  // BooleanMenuItem
	MENUTYPE_SUB_VALUE,      // SubMenuItem
	MENUTYPE_BACK_VALUE,     // BackMenuItem
	MENUTYPE_TEXT_VALUE      // TextMenuItem
};

/**
 * This is the base class of all menu items, containing functionality to hold the current state of the menu
 * and determine which is the next menu in the chain. It also defines a few functions that all implementations
 * implement.
 */
class MenuItem {
protected:
	uint8_t flags;
	MenuItem* next;
public:
	virtual ~MenuItem() { }
	/** Gets hold of the name pointer - caution this is in PROGMEM */
	virtual const char* getNamePgm() = 0;
	/** Retrieves the ID from the info block */
	virtual int getId() = 0;
	/** Gets hold of the maximum integer value for this entry when being edited */
	virtual int getMaximumValue() = 0;
	/** returns the menu type as one of the above menu type enumeration */
	virtual MenuType getMenuType() = 0;
	/** reads current value from eeprom */
	virtual void load(EepromAbstraction& eeprom) = 0;
	/** stores this items data to eeprom */
	virtual void save(EepromAbstraction& eeprom) = 0;

	/** set the item to be changed, this lets the renderer know it needs painting */
	void setChanged(bool changed) { bitWrite(flags, MENUITEM_CHANGED, changed); }
	/** returns the changed state of the item */
	bool isChanged() { return bitRead(flags, MENUITEM_CHANGED); }
	/** returns if the menu item needs to be sent remotely */
	bool isSendRemoteNeeded(uint8_t remoteNo);
	/** set the flag indicating that a remote refresh is needed */
	void setSendRemoteNeededAll(bool needed);
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
};

/** 
 * Represents an item that has a 16 bit unsigned integer backing it, and an info structure in program memory.
 */
template <typename M> class ValueMenuItem : public MenuItem {
protected:
	uint16_t currentValue;
	M info;
public:
	/** initialise an instance of this type with the required data values */
	void init(M info, uint16_t defaultVal, MenuItem* next = NULL) {
		this->info = info;
		this->currentValue = defaultVal;
		this->next = next;
		flags = 0;
	}

	/** Sets the integer current value to a new value, and marks the menu changed */
	void setCurrentValue(uint16_t val) {
		setChanged(true);
		setSendRemoteNeededAll(currentValue != val);
		currentValue = val;
	}

	/** gets the current value */
	uint16_t getCurrentValue() { return currentValue; }

	virtual const char* getNamePgm() { return info->name; }
	int getId() { return pgm_read_word_near(&info->id); }

	virtual void load(EepromAbstraction& eeprom) {
		uint16_t eeVal = pgm_read_word_near(&info->eepromAddr);
		if (eeVal != 0xffff) {
			setCurrentValue((int)eeprom.read16(eeVal));
		}
	}

	/** saves the current value word into eeprom, if the eeprom address is not -1 */
	virtual void save(EepromAbstraction& eeprom) {
		uint16_t eepromAddr = pgm_read_word_near(&info->eepromAddr);
		if (eepromAddr == 0xffff) return;

		eeprom.write16(eepromAddr, (uint16_t)currentValue);
	}

	/** Gets hold of the menu info struct, careful this is in PROGMEM */
	M getMenuInfo() { return info; }
};

/**
 * The implementation of MenuItem for storing numbers. Goes with it's AnalogMenuInfo
 * counterpart.
 */
class AnalogMenuItem : public ValueMenuItem<const AnalogMenuInfo*> {
public:
	AnalogMenuItem(const AnalogMenuInfo* info, uint16_t defaultVal, MenuItem* next = NULL);
	virtual ~AnalogMenuItem() { }
	virtual int getMaximumValue() { return (int)pgm_read_word_near(&info->maxValue); }
	virtual MenuType getMenuType() { return MENUTYPE_INT_VALUE; }
	virtual const char* getUnitNamePgm() { return info->unitName; }
};

/**
 * The implmentation of MenuItem for a list of choices, where only one can be chosen.
 * Goes with its EnumMenuInfo counterpart.
 */
class EnumMenuItem : public ValueMenuItem<const EnumMenuInfo*> {
public:
	EnumMenuItem(const EnumMenuInfo *info, uint8_t defaultVal, MenuItem* next = NULL);
	virtual ~EnumMenuItem() { }
	int getMaximumValue() { return pgm_read_byte_near(&info->noOfItems) - 1; }
	virtual MenuType getMenuType() { return MENUTYPE_ENUM_VALUE; }
	virtual const char* getNamePgm() { return info->name; }
	int getId() { return pgm_read_word_near(&info->id); }
};

/**
 * The implemenation of MenuItem for boolean, where it is true or false.
 */
class BooleanMenuItem : public MenuItem {
private:
	bool currentValue;
	const BooleanMenuInfo* info;
public:
	BooleanMenuItem(const BooleanMenuInfo* info, bool defaultVal, MenuItem* next = NULL);
	virtual ~BooleanMenuItem() { }
	virtual int getMaximumValue() { return 1; }
	virtual MenuType getMenuType() { return MENUTYPE_BOOLEAN_VALUE; }
	virtual const char* getNamePgm() { return info->name; }
	int getId() { return pgm_read_word_near(&info->id); }

	void setBoolean(bool newVal);
	bool getBoolean() { return currentValue; }

	virtual void load(EepromAbstraction& eeprom) {
		uint16_t eepromAddr = pgm_read_word_near(&info->eepromAddr);
		if (eepromAddr != 0xffff) {
			setBoolean(eeprom.read8(eepromAddr));
		}
	}

	virtual void save(EepromAbstraction& eeprom) {
		uint16_t eepromAddr = pgm_read_word_near(&info->eepromAddr);
		if (eepromAddr == 0xffff) return;
		eeprom.write8(eepromAddr, getBoolean());
	}
	const BooleanMenuInfo* getBooleanMenuInfo() { return info; }
};

class SubMenuItem : public MenuItem {
private:
	const SubMenuInfo* info;
	MenuItem* child;
public:
	SubMenuItem(const SubMenuInfo* info, MenuItem* child, MenuItem* next);
	virtual ~SubMenuItem() { }
	virtual int getMaximumValue() { return 1; }
	virtual MenuType getMenuType() { return MENUTYPE_SUB_VALUE; }
	virtual const char* getNamePgm() { return info->name; }
	int getId() { return pgm_read_word_near(&info->id); }
	virtual void load(EepromAbstraction& eeprom);
	virtual void save(EepromAbstraction& eeprom);
	MenuItem* getChild() { return child; }
};

class BackMenuItem : public MenuItem {
	const char* pgmName;
public:
	BackMenuItem(MenuItem* next, const char* subMenuNamePgm) {
		this->pgmName = subMenuNamePgm;
		this->next = next;
		flags=0;
	}
	virtual ~BackMenuItem() { }
	virtual int getMaximumValue() { return 1; }
	virtual MenuType getMenuType() { return MENUTYPE_BACK_VALUE; }
	virtual const char* getNamePgm();
	int getId() { return -1; }
	virtual void load(__attribute__((unused)) EepromAbstraction& eeprom) { }
	virtual void save(__attribute__((unused)) EepromAbstraction& eeprom) { }
};

/**
 * TextMenuItem is for situations where text modified at runtime must be shown, for showing
 * a series of text values from PROGMEM storage use EnumMenuItem instead.
 */
class TextMenuItem : public MenuItem {
private:
	const TextMenuInfo* menuInfo;
	char *menuText;

public:
	TextMenuItem(const TextMenuInfo* textInfo, MenuItem* next);
	uint8_t textLength() { return pgm_read_byte_near(&menuInfo->length); }

	void setTextValue(const char* text);

	const char* getTextValue() { return menuText; }

	virtual ~TextMenuItem() { delete menuText; }
	virtual int getMaximumValue() { return 1; }
	virtual MenuType getMenuType() { return MENUTYPE_TEXT_VALUE; }
	virtual const char* getNamePgm() { return menuInfo->name; }
	int getId() { return pgm_read_word_near(&menuInfo->id); }
	const TextMenuInfo* getTextMenuInfo() {return menuInfo;}

	virtual void load(EepromAbstraction& eeprom);
	virtual void save(EepromAbstraction& eeprom);
};
#endif

