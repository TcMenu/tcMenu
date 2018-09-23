/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

#include "tcMenu.h"
#include "MenuItems.h"

MenuItem::MenuItem(MenuType menuType, const AnyMenuInfo* menuInfo, MenuItem* next) {
	this->flags = 0;
	this->menuType = menuType;
	this->info = menuInfo;
	this->next = next;
}

bool MenuItem::isSendRemoteNeeded(uint8_t remoteNo) {
	remoteNo += 5;
	return (flags & (1 << remoteNo)) != 0;
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

void MenuItem::triggerCallback() {
	MenuCallbackFn fn = pgm_read_ptr_near(&info->callback);
	if(fn != NULL) {
		fn(getId());
	}
}

void TextMenuItem::setTextValue(const char* text) {
	setSendRemoteNeededAll(strncmp(menuText, text, textLength()));
	strncpy(menuText, text, textLength());
	menuText[textLength() - 1] = 0;
	setChanged(true);
	triggerCallback();
}
