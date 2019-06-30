/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

/**
 * Adafruit_GFX renderer that renders menus onto this type of display. This file is a plugin file and should not
 * be directly edited, it will be replaced each time the project is built. If you want to edit this file in place,
 * make sure to rename it first.
 * 
 * LIBRARY REQUIREMENT
 * This library requires the AdaGfx library along with a suitable driver.
 */

#include "tcMenuAdaFruitGfx.h"

extern const ConnectorLocalInfo applicationInfo;

int drawingCount = 0;

#if DISPLAY_HAS_MEMBUFFER == true
    #define refreshDisplayIfNeeded(gr, needUpd) {if(needUpd) reinterpret_cast<Adafruit_ILI9341*>(gr)->display();}
#else
    #define refreshDisplayIfNeeded(g, n)
#endif

const char MENU_BACK_TEXT[] PROGMEM = "[..]";

Coord textExtents(Adafruit_GFX* graphics, const char* text, int16_t x, int16_t y) {
	int16_t x1, y1;
	uint16_t w, h;
	graphics->getTextBounds((char*)text, x, y, &x1, &y1, &w, &h);

    serdebugF4("Textbounds (y1, w, h): ", y1, w, h);
	return Coord(w, h);
}

void AdaFruitGfxMenuRenderer::setGraphicsDevice(Adafruit_GFX* graphics, AdaColorGfxMenuConfig *gfxConfig) {

	if (gfxConfig->editIcon == NULL || gfxConfig->activeIcon == NULL) {
		gfxConfig->editIcon = defEditingIcon;
		gfxConfig->activeIcon = defActiveIcon;
		gfxConfig->editIconWidth = 16;
		gfxConfig->editIconHeight = 12;
	}

	this->graphics = graphics;
	this->gfxConfig = gfxConfig;
}

AdaFruitGfxMenuRenderer::~AdaFruitGfxMenuRenderer() {
}

void AdaFruitGfxMenuRenderer::renderTitleArea() {
	if(currentRoot == menuMgr.getRoot()) {
		safeProgCpy(buffer, applicationInfo.name, bufferSize);
	}
	else {
		currentRoot->copyNameToBuffer(buffer, bufferSize);
	}

    serdebugF3("Render title, fontMag: ", buffer, gfxConfig->titleFontMagnification);

    graphics->setFont(gfxConfig->titleFont);
	graphics->setTextSize(gfxConfig->titleFontMagnification);

	int fontYStart = gfxConfig->titlePadding.top;
	Coord extents = textExtents(graphics, buffer, 0, gfxConfig->titleFont ? graphics->height() : 0);
	titleHeight = extents.y + gfxConfig->titlePadding.top + gfxConfig->titlePadding.bottom;
	if (gfxConfig->titleFont) {
	 	fontYStart = titleHeight - (gfxConfig->titlePadding.bottom);
	}

    serdebugF3("titleHeight, fontYStart: ",  titleHeight, fontYStart);

	graphics->fillRect(0, 0, graphics->width(), titleHeight, gfxConfig->bgTitleColor);
	graphics->setTextColor(gfxConfig->fgTitleColor);
	graphics->setCursor(gfxConfig->titlePadding.left, fontYStart);
	graphics->print(buffer);
	titleHeight += gfxConfig->titleBottomMargin;
}

bool AdaFruitGfxMenuRenderer::renderWidgets(bool forceDraw) {
	TitleWidget* widget = firstWidget;
	int xPos = graphics->width() - gfxConfig->widgetPadding.right;
    bool redrawNeeded = forceDraw;
	while(widget) {
		xPos -= widget->getWidth();

		if(widget->isChanged() || forceDraw) {
            redrawNeeded = true;

            serdebugF3("Drawing widget pos,icon: ", xPos, widget->getCurrentState());

			graphics->drawBitmap(xPos, gfxConfig->widgetPadding.top, widget->getCurrentIcon(), widget->getWidth(), widget->getHeight(),
								 gfxConfig->widgetColor, gfxConfig->bgTitleColor);
		}

		widget = widget->getNext();
		xPos -= gfxConfig->widgetPadding.left;
	}
    return redrawNeeded;
}

void AdaFruitGfxMenuRenderer::renderListMenu(int titleHeight) {
    ListRuntimeMenuItem* runList = reinterpret_cast<ListRuntimeMenuItem*>(currentRoot);
	
    uint8_t maxY = uint8_t((graphics->height() - titleHeight) / itemHeight);
	maxY = min(maxY, runList->getNumberOfParts());
	uint8_t currentActive = runList->getActiveIndex();
	
	uint8_t offset = 0;
	if (currentActive >= maxY) {
		offset = (currentActive+1) - maxY;
	}

    int yPos = titleHeight;
	for (int i = 0; i < maxY; i++) {
		uint8_t current = offset + i;
		RuntimeMenuItem* toDraw = (current==0) ? runList->asBackMenu() : runList->getChildItem(current - 1);
		renderMenuItem(yPos, itemHeight, toDraw);
        yPos += itemHeight;
	}

	// reset the list item to a normal list again.
	runList->asParent();
}

void AdaFruitGfxMenuRenderer::render() {
 	if (graphics == NULL) return;

	uint8_t locRedrawMode = redrawMode;
	redrawMode = MENUDRAW_NO_CHANGE;
    bool requiresUpdate = false;

	countdownToDefaulting();

	if (locRedrawMode == MENUDRAW_COMPLETE_REDRAW) {
	    // pre-populate the font.
        graphics->setFont(gfxConfig->itemFont);
        graphics->setTextSize(gfxConfig->itemFontMagnification);
        int yLocation = gfxConfig->itemFont ? graphics->height() : 0;
        Coord itemExtents = textExtents(graphics, "Aaygj", gfxConfig->itemPadding.left, yLocation);

       	itemHeight = itemExtents.y + gfxConfig->itemPadding.top + gfxConfig->itemPadding.bottom;
        serdebugF2("Redraw all, new item height ", itemHeight);

		graphics->fillScreen(gfxConfig->bgItemColor);
		renderTitleArea();

        taskManager.yieldForMicros(0);

        renderWidgets(true);

        taskManager.yieldForMicros(0);
        requiresUpdate = true;
	}
	else {
		requiresUpdate = renderWidgets(false);
	}

	graphics->setFont(gfxConfig->itemFont);
	graphics->setTextSize(gfxConfig->itemFontMagnification);
	int maxItemsY = ((graphics->height()-titleHeight) / itemHeight);

    if(currentRoot->getMenuType() == MENUTYPE_RUNTIME_LIST) {
        if(currentRoot->isChanged() || locRedrawMode != MENUDRAW_NO_CHANGE) {
            requiresUpdate = true;
            renderListMenu(titleHeight);
        }
    }
    else {
        MenuItem* item = currentRoot;
        // first we find the first currently active item in our single linked list
        if (offsetOfCurrentActive() >= maxItemsY) {
            uint8_t toOffsetBy = (offsetOfCurrentActive() - maxItemsY) + 1;

            if(lastOffset != toOffsetBy) locRedrawMode = MENUDRAW_COMPLETE_REDRAW;
            lastOffset = toOffsetBy;

            while (item != NULL && toOffsetBy--) {
                item = item->getNext();
            }
        }
        else {
            if(lastOffset != 0xff) locRedrawMode = MENUDRAW_COMPLETE_REDRAW;
            lastOffset = 0xff;
        }

        // and then we start drawing items until we run out of screen or items
        int ypos = titleHeight;
        while (item && (ypos + itemHeight) < graphics->height() ) {
            if (locRedrawMode != MENUDRAW_NO_CHANGE || item->isChanged()) {
                requiresUpdate = true;

                taskManager.yieldForMicros(0);

                renderMenuItem(ypos, itemHeight, item);
            }
            ypos += itemHeight;
            item = item->getNext();
        }
    }

    refreshDisplayIfNeeded(graphics, requiresUpdate);
}

void AdaFruitGfxMenuRenderer::renderMenuItem(int yPos, int menuHeight, MenuItem* item) {
	int icoWid = gfxConfig->editIconWidth;
	int icoHei = gfxConfig->editIconHeight;

	item->setChanged(false); // we are drawing the item so it's no longer changed.

    int imgMiddleY = yPos + ((menuHeight - icoHei) / 2);
	if(item->isEditing()) {
		graphics->fillRect(0, yPos, graphics->width(), menuHeight, gfxConfig->bgSelectColor);
		graphics->drawBitmap(gfxConfig->itemPadding.left, imgMiddleY, gfxConfig->editIcon, icoWid, icoHei, gfxConfig->fgSelectColor);
		graphics->setTextColor(gfxConfig->fgSelectColor);
        serdebugF("Item Editing");
	}
	else if(item->isActive()) {
		graphics->setTextColor(gfxConfig->fgSelectColor);
		graphics->fillRect(0, yPos, graphics->width(), menuHeight, gfxConfig->bgSelectColor);
		graphics->drawBitmap(gfxConfig->itemPadding.left, imgMiddleY, gfxConfig->activeIcon, icoWid, icoHei, gfxConfig->fgSelectColor);
        serdebugF("Item Active");
	}
	else {
		graphics->fillRect(0, yPos, graphics->width(), menuHeight, gfxConfig->bgItemColor);
		graphics->setTextColor(gfxConfig->fgItemColor);
        serdebugF("Item Normal");
	}

    taskManager.yieldForMicros(0);

	int textPos = gfxConfig->itemPadding.left + icoWid + gfxConfig->itemPadding.left;
	
	int drawingPositionY = yPos + gfxConfig->itemPadding.top;
	if (gfxConfig->itemFont) {
		drawingPositionY = yPos + (menuHeight - (gfxConfig->itemPadding.bottom));
	}
	graphics->setCursor(textPos, drawingPositionY);
	item->copyNameToBuffer(buffer, bufferSize);

    serdebugF4("Printing menu item (name, ypos, drawingPositionY)", buffer, yPos, drawingPositionY);

	graphics->print(buffer);

    if(isItemActionable(item)) {
        int rightOffset = graphics->width() - (gfxConfig->itemPadding.right + icoWid);
		graphics->drawBitmap(rightOffset, imgMiddleY, gfxConfig->activeIcon, icoWid, icoHei, gfxConfig->fgSelectColor);        
        buffer[0] = 0;
    }
    else if(item->getMenuType() == MENUTYPE_BACK_VALUE) {
        safeProgCpy(buffer, MENU_BACK_TEXT, bufferSize);
    }
    else {
	    menuValueToText(item, JUSTIFY_TEXT_LEFT);
    }
	Coord coord = textExtents(graphics, buffer, textPos, yPos);
	int16_t right = graphics->width() - (coord.x + gfxConfig->itemPadding.right);
	graphics->setCursor(right, drawingPositionY);
 	graphics->print(buffer);
    serdebugF2("Value ", buffer);

    taskManager.yieldForMicros(0);
}

void prepareAdaColorDefaultGfxConfig(AdaColorGfxMenuConfig* config) { 
    prepareDefaultGfxConfig((ColorGfxMenuConfig<void*>*)config);
}

void prepareAdaMonoGfxConfigLoRes(AdaColorGfxMenuConfig* config) {
	makePadding(config->titlePadding, 2, 1, 1, 1);
	makePadding(config->itemPadding, 1, 1, 1, 1);
	makePadding(config->widgetPadding, 2, 2, 0, 2);

	config->bgTitleColor = BLACK;
	config->fgTitleColor = WHITE;
	config->titleFont = NULL;
	config->titleBottomMargin = 2;
	config->widgetColor = WHITE;
	config->titleFontMagnification = 1;

	config->bgItemColor = WHITE;
	config->fgItemColor = BLACK;
	config->bgSelectColor = BLACK;
	config->fgSelectColor = WHITE;
	config->itemFont = NULL;
	config->itemFontMagnification = 1;

    config->editIcon = loResEditingIcon;
    config->activeIcon = loResActiveIcon;
    config->editIconHeight = 6;
    config->editIconWidth = 8;
}

BaseDialog* AdaFruitGfxMenuRenderer::getDialog() {
    if(dialog == NULL) {
        dialog = new AdaGfxDialog();
    }
    return dialog;
}

void AdaGfxDialog::internalRender(int currentValue) {
    AdaFruitGfxMenuRenderer* adaRenderer = reinterpret_cast<AdaFruitGfxMenuRenderer*>(MenuRenderer::getInstance());
    AdaColorGfxMenuConfig* gfxConfig = adaRenderer->getGfxConfig();
    Adafruit_GFX* graphics = adaRenderer->getGraphics();

    if(needsDrawing == MENUDRAW_COMPLETE_REDRAW) {
        graphics->fillScreen(gfxConfig->bgItemColor);
    }

    graphics->setFont(gfxConfig->itemFont);
	graphics->setTextSize(gfxConfig->itemFontMagnification);

    char data[20];
    safeProgCpy(data, headerPgm, sizeof(data));

	int fontYStart = gfxConfig->itemPadding.top;
	Coord extents = textExtents(graphics, data, 0, gfxConfig->itemFont ? graphics->height() : 0);
	int dlgNextDraw = extents.y + gfxConfig->titlePadding.top + gfxConfig->titlePadding.bottom;
	if (gfxConfig->itemFont) {
	 	fontYStart = dlgNextDraw - (gfxConfig->titlePadding.bottom);
	}

	graphics->fillRect(0, 0, graphics->width(), dlgNextDraw, gfxConfig->bgTitleColor);
	graphics->setTextColor(gfxConfig->fgTitleColor);
	graphics->setCursor(gfxConfig->titlePadding.left, fontYStart);
	graphics->print(data);

	dlgNextDraw += gfxConfig->titleBottomMargin;

    int startingPosition = dlgNextDraw;
    fontYStart = dlgNextDraw + gfxConfig->itemPadding.top;
	dlgNextDraw = dlgNextDraw + extents.y + gfxConfig->titlePadding.top + gfxConfig->titlePadding.bottom;
	if (gfxConfig->itemFont) {
	 	fontYStart = dlgNextDraw - (gfxConfig->titlePadding.bottom);
	}
    graphics->fillRect(0, startingPosition, graphics->width(), dlgNextDraw, gfxConfig->bgItemColor);
	graphics->setTextColor(gfxConfig->fgItemColor);
	graphics->setCursor(gfxConfig->titlePadding.left, fontYStart);

	graphics->print(MenuRenderer::getInstance()->getBuffer());
    
    bool active;
    if(button1 != BTNTYPE_NONE) {
        active = copyButtonText(data, 0, currentValue);
        drawButton(graphics, gfxConfig, data, 0, active);
    }
    if(button2 != BTNTYPE_NONE) {
        active = copyButtonText(data, 1, currentValue);
        drawButton(graphics, gfxConfig, data, 1, active);
    }

    refreshDisplayIfNeeded(graphics, true);
}
void AdaGfxDialog::drawButton(Adafruit_GFX* gfx, AdaColorGfxMenuConfig* config, const char* title, uint8_t num, bool active) {
	Coord extents = textExtents(gfx, title, 0, config->itemFont ? gfx->height() : 0);
    int itemHeight = ( extents.y + config->itemPadding.top + config->itemPadding.bottom);
    int start = gfx->height() - itemHeight;
    int fontYStart = start + config->itemPadding.top;
	if (config->itemFont) {
        fontYStart += extents.y;
	}
    int buttonWidth = gfx->width() / 2;
    int xOffset = (num == 0) ? 0 : buttonWidth;
    gfx->fillRect(xOffset, start, buttonWidth, itemHeight, active ? config->bgSelectColor : config->bgItemColor);
	gfx->setTextColor(active ? config->fgSelectColor : config->fgItemColor);
    gfx->setCursor(xOffset + ((buttonWidth - extents.x) / 2), fontYStart);
    gfx->print(title);
}

