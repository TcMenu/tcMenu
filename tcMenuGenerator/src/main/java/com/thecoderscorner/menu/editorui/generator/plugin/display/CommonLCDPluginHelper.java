package com.thecoderscorner.menu.editorui.generator.plugin.display;

import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.validation.CannedPropertyValidators;
import com.thecoderscorner.menu.editorui.generator.validation.ChoiceDescription;

import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.plugin.JavaPluginItem.ALWAYS_APPLICABLE;

public class CommonLCDPluginHelper {
    public static CreatorProperty unoOrFullProperty() {
        return new CreatorProperty("UNO_OR_FULL", "LCD Type", "The version of the LCD driver to use",
                "unoLcd", SubSystem.DISPLAY, CreatorProperty.PropType.TEXTUAL, CannedPropertyValidators.choicesValidator(List.of(
                new ChoiceDescription("unoLcd", "Uno - Low Memory"),
                new ChoiceDescription("fullLcd", "Full - More Configurable")
        ), "unoLcd"), ALWAYS_APPLICABLE);
    }

    public static String getSourceCode(boolean isUno) {
        return isUno ? LCD_UNO_CPP_CODE : FULL_LCD_CPP_CODE;
    }

    public static String getHeaderCode(boolean isUno) {
        return isUno ? LCD_UNO_H_CODE : FULL_LCD_H_CODE;
    }

    private static final String LCD_UNO_H_CODE = """
            /*
             * Copyright (c) 2018 https://www.thecoderscorner.com (Dave Cherry).
             * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
             */

            /**
             * @file tcMenuLiquidCrystal.h
             *\s
             * LiquidCrystalIO renderer that renders menus onto this type of display. This file is a plugin file and should not
             * be directly edited, it will be replaced each time the project is built. If you want to edit this file in place,
             * make sure to rename it first.
             *\s
             * LIBRARY REQUIREMENT
             * This renderer is designed for use with this library: https://github.com/TcMenu/LiquidCrystalIO
             */

            #ifndef _TCMENU_LIQUID_CRYSTAL_H
            #define _TCMENU_LIQUID_CRYSTAL_H

            #include "tcMenu.h"
            #include "BaseRenderers.h"
            #include <LiquidCrystalIO.h>
            #include <BaseDialog.h>

            /**
             * A renderer that can renderer onto a LiquidCrystal display and supports the concept of single level
             * sub menus, active items and editing.
             */
            class LiquidCrystalRenderer : public BaseMenuRenderer {
            private:
                LiquidCrystal* lcd;
                uint8_t dimY;
                uint8_t backChar;
                uint8_t forwardChar;
                uint8_t editChar;
                bool drewTitleThisTime;
                bool titleRequired;
                uint8_t lcdEditorCursorX = 0xFF;
                uint8_t lcdEditorCursorY = 0xFF;
            public:

                LiquidCrystalRenderer(LiquidCrystal& lcd, uint8_t dimX, uint8_t dimY);
                virtual ~LiquidCrystalRenderer();
                void render() override;
                void initialise() override;
                void setTitleRequired(bool titleRequired) { this->titleRequired = titleRequired; }

                void setEditorChars(char back, char forward, char edit);

                uint8_t getRows() {return dimY;}
                LiquidCrystal* getLCD() {return lcd;}
                BaseDialog* getDialog() override;
            private:
                void renderTitle(bool forceDraw);
                void renderMenuItem(uint8_t row, MenuItem* item);
                void renderActionItem(uint8_t row, MenuItem* item);
                void renderBackItem(uint8_t row, MenuItem* item);
                void renderList();
                void setupEditorPlacement(int32_t x, int32_t y);
            };

            class LiquidCrystalDialog : public BaseDialog {
            public:
                LiquidCrystalDialog(LiquidCrystalRenderer* renderer) {
                    bitWrite(flags, DLG_FLAG_SMALLDISPLAY, (renderer->getRows() <= 2));
                }
            protected:
                void internalRender(int currentValue) override;
            };

            /**
             * This method constructs an instance of a liquid crystal renderer.
             */
            inline MenuRenderer* liquidCrystalRenderer(LiquidCrystal& lcd, uint8_t dimX, uint8_t dimY) {
                return new LiquidCrystalRenderer(lcd, dimX, dimY);
            }

            #endif // _TCMENU_LIQUID_CRYSTAL_H
    
    """;

    private static final String LCD_UNO_CPP_CODE = """
            /*
             * Copyright (c) 2018 https://www.thecoderscorner.com (Dave Cherry).
             * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
             */
            
            /**
             * LiquidCrystalIO renderer that renders menus onto this type of display. This file is a plugin file and should not
             * be directly edited, it will be replaced each time the project is built. If you want to edit this file in place,
             * make sure to rename it first.
             *\s
             * LIBRARY REQUIREMENT
             * This renderer is designed for use with this library: https://github.com/TcMenu/LiquidCrystalIO
             */
            
            #include "tcMenuLiquidCrystal.h"
            #include "tcUtil.h"
            
            extern const ConnectorLocalInfo applicationInfo;
            
            LiquidCrystalRenderer::LiquidCrystalRenderer(LiquidCrystal& lcd, uint8_t dimX, uint8_t dimY) : BaseMenuRenderer(dimX) {
                this->dimY = dimY;
                this->lcd = &lcd;
                this->backChar = '<';
                this->forwardChar = '>';
                this->editChar = '=';
                this->drewTitleThisTime = false;
                this->titleRequired = true;
            }
            
            void LiquidCrystalRenderer::initialise() {
                // first we create the custom characters for any title widget.
                // we iterate over each widget then over each each icon.
                TitleWidget* wid = firstWidget;
                int charNo = 0;
                while(wid != NULL) {
                    for(int i = 0; i < wid->getMaxValue(); i++) {
                        lcd->createCharPgm((uint8_t)charNo, wid->getIcon(i));
                        charNo++;
                    }
                    wid = wid->getNext();
                }
                lcd->clear();
                BaseMenuRenderer::initialise();
            }
            
            LiquidCrystalRenderer::~LiquidCrystalRenderer() {
                delete this->buffer;
                if(dialog) delete dialog;
            }
            
            void LiquidCrystalRenderer::setEditorChars(char back, char forward, char edit) {
                backChar = back;
                forwardChar = forward;
                editChar = edit;
            }
            
            void LiquidCrystalRenderer::renderList() {
                ListRuntimeMenuItem* runList = reinterpret_cast<ListRuntimeMenuItem*>(menuMgr.getCurrentMenu());
            
                uint8_t maxY = min(dimY, runList->getNumberOfParts());
                uint8_t currentActive = runList->getActiveIndex();
            
                uint8_t offset = 0;
                if (currentActive >= maxY) {
                    offset = (currentActive+1) - maxY;
                }
            
                for (int i = 0; i < maxY; i++) {
                    uint8_t current = offset + i;
                    RuntimeMenuItem* toDraw = (current==0) ? runList->asBackMenu() : runList->getChildItem(current - 1);
                    renderMenuItem(i, toDraw);
                }
            
                // reset the list item to a normal list again.
                runList->asParent();
            }
            
            void LiquidCrystalRenderer::renderTitle(bool forceDraw) {
                if(!drewTitleThisTime || forceDraw) {
                    strcpy_P(buffer, applicationInfo.name);
                    uint8_t bufSz = bufferSize;
                    uint8_t last = min(bufSz, (uint8_t)strlen(buffer));
                    for(uint8_t i = last; i < bufSz; i++) {
                        buffer[i] = ' ';
                    }
                    buffer[bufSz] = 0;
                    lcd->setCursor(0,0);
                    lcd->print(buffer);
                }
            
                uint8_t widCount = 0;
                uint8_t charOffset = 0;
                TitleWidget* widget = firstWidget;
                while(widget != NULL) {
                    if(widget->isChanged() || forceDraw) {
                        lcd->setCursor(bufferSize - (widCount + 1), 0);
                        widget->setChanged(false);
                        lcd->write(charOffset + widget->getCurrentState());
                    }
                    charOffset += widget->getMaxValue();
                    widget = widget->getNext();
                    widCount++;
                }
            
            }
            
            void LiquidCrystalRenderer::render() {
                uint8_t locRedrawMode = redrawMode;
                redrawMode = MENUDRAW_NO_CHANGE;
                if (locRedrawMode == MENUDRAW_COMPLETE_REDRAW) {
                    lcd->clear();
                }
            
                countdownToDefaulting();
            
                if (menuMgr.getCurrentMenu()->getMenuType() == MENUTYPE_RUNTIME_LIST ) {
                    if (menuMgr.getCurrentMenu()->isChanged() || locRedrawMode != MENUDRAW_NO_CHANGE) {
                        renderList();
                    }
                }
                else {
                    MenuItem* item = menuMgr.getCurrentMenu();
            
                    if(lcdEditorCursorX != 0xFF) {
                        if(menuMgr.getCurrentEditor() == nullptr) {
                            lcdEditorCursorX = 0xFF; // edit has ended, clear our status
                            lcdEditorCursorY = 0xFF;
                        }
                        lcd->noCursor(); // always turn off the cursor while we draw
                    }
            
                    bool titleNeeded = titleRequired && menuMgr.getCurrentMenu() == menuMgr.getRoot();
            
                    // first we find the first currently active item in our single linked list
                    int activeOffs = offsetOfCurrentActive(item);
            
                    uint8_t cnt = 0;
                    uint8_t numLines = dimY;
                    if(titleNeeded && activeOffs <= (dimY - 2)) {
                        renderTitle(locRedrawMode != MENUDRAW_NO_CHANGE);
                        cnt++;
                        if(!drewTitleThisTime) locRedrawMode = MENUDRAW_COMPLETE_REDRAW;
                        drewTitleThisTime = true;
                        numLines--;
                    }
                    else {
                        if(drewTitleThisTime) locRedrawMode = MENUDRAW_COMPLETE_REDRAW;
                        drewTitleThisTime = false;
                    }
            
                    if (activeOffs >= numLines) {
                        uint8_t toOffsetBy = (activeOffs - dimY) + 1;
            
                        if (lastOffset != toOffsetBy) locRedrawMode = MENUDRAW_COMPLETE_REDRAW;
                        lastOffset = toOffsetBy;
            
                        while (item != NULL && toOffsetBy) {
                            if(item->isVisible()) toOffsetBy = toOffsetBy - 1;
                            item = item->getNext();
                        }
                    }
                    else {
                        if (lastOffset != 0xff) locRedrawMode = MENUDRAW_COMPLETE_REDRAW;
                        lastOffset = 0xff;
                    }
            
                    // and then we start drawing items until we run out of screen or items
                    while (item && cnt < dimY) {
                        if(item->isVisible())
                        {
                            if (locRedrawMode != MENUDRAW_NO_CHANGE || item->isChanged()) {
                                renderMenuItem(cnt, item);
                            }
                            ++cnt;
                        }
                        item = item->getNext();
                    }
            
                    if(lcdEditorCursorX != 0xFF) {
                        lcd->setCursor(lcdEditorCursorX, lcdEditorCursorY);
                        lcd->cursor(); // re-enable the cursor after drawing.
                    }
                }
            }
            
            void LiquidCrystalRenderer::renderMenuItem(uint8_t row, MenuItem* item) {
                if (item == NULL || row > dimY) return;
            
                item->setChanged(false);
                lcd->setCursor(0, row);
            
                int offs;
                if (item->getMenuType() == MENUTYPE_BACK_VALUE) {
                    buffer[0] = getActiveItem() == item ? backChar : ' ';
                    buffer[1] = backChar;
                    offs = 2;
                }
                else {
                    buffer[0] = menuMgr.getCurrentEditor() == item ? editChar : (activeItem == item ? forwardChar : ' ');
                    offs = 1;
                }
                uint8_t finalPos = item->copyNameToBuffer(buffer, offs, bufferSize);
                for(uint8_t i = finalPos; i < bufferSize; ++i)  buffer[i] = 32;
                buffer[bufferSize] = 0;
            
                if (isItemActionable(item)) {
                    buffer[bufferSize - 1] = forwardChar;
                }
                else {
                    char sz[20];
                    copyMenuItemValue(item, sz, sizeof sz);
                    uint8_t count = strlen(sz);
                    int cpy = bufferSize - count;
                    strcpy(buffer + cpy, sz);
                    if(item == menuMgr.getCurrentEditor() && menuMgr.getEditorHints().getEditorRenderingType() != CurrentEditorRenderingHints::EDITOR_REGULAR) {
                        setupEditorPlacement(cpy + menuMgr.getEditorHints().getStartIndex(), row);
                    }
                }
                lcd->print(buffer);
            }
            
            BaseDialog* LiquidCrystalRenderer::getDialog() {
                if(dialog == NULL) {
                    dialog = new LiquidCrystalDialog(this);
                }
                return dialog;
            }
            
            // dialog
            
            void LiquidCrystalDialog::internalRender(int currentValue) {
                LiquidCrystalRenderer* lcdRender = ((LiquidCrystalRenderer*)MenuRenderer::getInstance());
                LiquidCrystal* lcd = lcdRender->getLCD();
                if(needsDrawing == MENUDRAW_COMPLETE_REDRAW) {
                    lcd->clear();
                }
            
                char data[20];
                strncpy_P(data, headerPgm, sizeof(data));
                data[sizeof(data)-1]=0;
                lcd->setCursor(0,0);
                lcd->print(data);
            
                // we can only print the buffer on a newline when there's enough rows.
                // so on 16x2 we have to show the buffer over the header. It's all we
                // can do.
                int nextY = 3;
                if(isCompressedMode()) {
                    int len = strlen(lcdRender->getBuffer());
                    int startX = lcdRender->getBufferSize() - len;
                    lcd->setCursor(startX,0);
                    lcd->print(lcdRender->getBuffer());
                    nextY = 1;
                }
                else {
                    lcd->setCursor(0,1);
                    lcd->print(lcdRender->getBuffer());
                }
            
                if(button1 != BTNTYPE_NONE) {
                    copyButtonText(data, 0, currentValue);
                    lcd->setCursor(0, nextY);
                    lcd->print(data);
                }
                if(button2 != BTNTYPE_NONE) {
                    copyButtonText(data, 1, currentValue);
                    int startX = lcdRender->getBufferSize() - strlen(data);
                    lcd->setCursor(startX, nextY);
                    lcd->print(data);
                }
            }
            
            void LiquidCrystalRenderer::setupEditorPlacement(int32_t x, int32_t y) {
                lcdEditorCursorX = min((bufferSize - 1), x);
                lcdEditorCursorY = y;
            }
            """;

    private static String FULL_LCD_H_CODE = """
            /*
             * Copyright (c) 2018 https://www.thecoderscorner.com (Dave Cherry).
             * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
             */
            
            /**
             * @file tcMenuLiquidCrystal.h
             *\s
             * LiquidCrystalIO renderer that renders menus onto this type of display. This file is a plugin file and should not
             * be directly edited, it will be replaced each time the project is built. If you want to edit this file in place,
             * make sure to rename it first.
             *\s
             * LIBRARY REQUIREMENT
             * This renderer is designed for use with this library: https://github.com/TcMenu/LiquidCrystalIO
             */
            
            #ifndef _TCMENU_LIQUID_CRYSTAL_H
            #define _TCMENU_LIQUID_CRYSTAL_H
            
            #include "tcMenu.h"
            #include <LiquidCrystalIO.h>
            #include <BaseDialog.h>
            #include <graphics/BaseGraphicalRenderer.h>
            
            using namespace tcgfx;
            
            /**
             * A renderer that can renderer onto a LiquidCrystal display and supports the concept of single level
             * sub menus, active items and editing.
             */
            class LiquidCrystalRenderer : public BaseGraphicalRenderer {
            private:
                LiquidCrystal* lcd;
                NullItemDisplayPropertiesFactory propertiesFactory;
                char backChar;
                char forwardChar;
                char editChar;
                uint8_t lcdEditorCursorX = 0xFF;
                uint8_t lcdEditorCursorY = 0xFF;
            public:
                LiquidCrystalRenderer(LiquidCrystal& lcd, int dimX, int dimY);
                ~LiquidCrystalRenderer() override;
                void initialise() override;
                void setTitleRequired(bool titleRequired) { titleMode = (titleRequired) ? TITLE_FIRST_ROW : NO_TITLE; }
                void setEditorChars(char back, char forward, char edit);
            
                uint8_t getRows() {return height;}
                LiquidCrystal* getLCD() {return lcd;}
                BaseDialog* getDialog() override;
            
                void drawingCommand(RenderDrawingCommand command) override;
                void drawWidget(Coord where, TitleWidget* widget, color_t colorFg, color_t colorBg) override;
                void drawMenuItem(GridPositionRowCacheEntry* entry, Coord where, Coord areaSize, const DrawingFlags& drawAll) override;
                void fillWithBackgroundTo(int endPoint) override;
            
                ItemDisplayPropertiesFactory &getDisplayPropertiesFactory() override { return propertiesFactory; }
                NullItemDisplayPropertiesFactory &getLcdDisplayPropertiesFactory() { return propertiesFactory; }
            
                void setupEditorPlacement(int32_t x, int32_t y);
            };
            
            #endif // _TCMENU_LIQUID_CRYSTAL_H
            
            """;

    private static String FULL_LCD_CPP_CODE = """
            /*
             * Copyright (c) 2018 https://www.thecoderscorner.com (Dave Cherry).
             * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
             */
            
            /**
             * LiquidCrystalIO renderer that renders menus onto this type of display. This file is a plugin file and should not
             * be directly edited, it will be replaced each time the project is built. If you want to edit this file in place,
             * make sure to rename it first.
             *\s
             * LIBRARY REQUIREMENT
             * This renderer is designed for use with this library: https://github.com/TcMenu/LiquidCrystalIO
             */
            
            #include "tcMenuLiquidCrystal.h"
            #include "tcUtil.h"
            
            extern const ConnectorLocalInfo applicationInfo;
            
            LiquidCrystalRenderer::LiquidCrystalRenderer(LiquidCrystal& lcd, int dimX, int dimY) : BaseGraphicalRenderer(dimX, dimX, dimY, true, applicationInfo.name) {
                this->lcd = &lcd;
                this->backChar = '<';
                this->forwardChar = '>';
                this->editChar = '=';
            }
            
            void LiquidCrystalRenderer::initialise() {
                // first we create the custom characters for any title widget.
                // we iterate over each widget then over each each icon.
                TitleWidget* wid = firstWidget;
                int charNo = 0;
                while(wid != nullptr) {
                    serlogF2(SER_TCMENU_INFO, "Title widget present max=", wid->getMaxValue());
                    for(int i = 0; i < wid->getMaxValue(); i++) {
                        serlogF2(SER_TCMENU_DEBUG, "Creating char ", charNo);
                        lcd->createCharPgm((uint8_t)charNo, wid->getIcon(i));
                        charNo++;
                    }
                    wid = wid->getNext();
                }
                lcd->clear();
            
                BaseGraphicalRenderer::initialise();
            }
            
            LiquidCrystalRenderer::~LiquidCrystalRenderer() {
                delete this->buffer;
                delete dialog;
            }
            
            void LiquidCrystalRenderer::setEditorChars(char back, char forward, char edit) {
                backChar = back;
                forwardChar = forward;
                editChar = edit;
            }
            
            void LiquidCrystalRenderer::drawWidget(Coord where, TitleWidget *widget, color_t, color_t) {
                char ch = char(widget->getHeight() + widget->getCurrentState());
                serlogF4(SER_TCMENU_DEBUG, "draw widget", where.x, where.y, (int)ch);
                lcd->setCursor(where.x, where.y);
                widget->setChanged(false);
                lcd->write(ch);
            }
            
            int calculateOffset(GridPosition::GridJustification just, int totalLen, const char* sz) {
                int len = strlen(sz);
                auto actualJust = coreJustification(just);
                if(len > totalLen || actualJust == GridPosition::CORE_JUSTIFY_LEFT) return 0;
            
                if(actualJust == tcgfx::GridPosition::CORE_JUSTIFY_RIGHT) {
                    return (totalLen - len) - 1;
                }
                else {
                    // must be centered in this case.
                    return (totalLen - len) / 2;
                }
            }
            
            void copyIntoBuffer(char* buffer, const char* source, int offset, int bufferLen) {
                auto len = strlen(source);
                for(size_t i=0; i<len; i++) {
                    size_t pos = offset+i;
                    if(pos >= (size_t)bufferLen) return;
                    buffer[pos] = source[i];
                }
            }
            
            void LiquidCrystalRenderer::drawMenuItem(GridPositionRowCacheEntry* entry, Coord where, Coord areaSize, const DrawingFlags& drawingFlags) {
                auto* theItem = entry->getMenuItem();
                theItem->setChanged(displayNumber, false);
                char sz[21];
            
                if(entry->getPosition().getJustification() == GridPosition::JUSTIFY_TITLE_LEFT_VALUE_RIGHT) {
                    buffer[0] = drawingFlags.isEditing() ? editChar : (drawingFlags.isActive() ? forwardChar : ' ');
                    lcd->setCursor(where.x, where.y);
                    int offs = 1;
                    uint8_t finalPos = theItem->copyNameToBuffer(buffer, offs, bufferSize);
                    for(uint8_t i = finalPos; i < uint8_t(areaSize.x); ++i)  buffer[i] = 32;
                    buffer[bufferSize] = 0;
                    copyMenuItemValue(theItem, sz, sizeof sz, drawingFlags.isActive());
                    uint8_t count = strlen(sz);
                    int cpy = bufferSize - count;
                    strcpy(buffer + cpy, sz);
                    if(theItem == menuMgr.getCurrentEditor() && menuMgr.getEditorHints().getEditorRenderingType() != CurrentEditorRenderingHints::EDITOR_REGULAR) {
                        setupEditorPlacement(where.x + cpy + menuMgr.getEditorHints().getStartIndex(), where.y);
                    }
                }
                else {
                    for(size_t i = 1; i < (sizeof(sz) - 1); ++i)  buffer[i] = 32;
                    buffer[sizeof(sz)-1] = 0;
                    uint8_t valueStart = 0;
                    if(itemNeedsName(entry->getPosition().getJustification())) {
                        theItem->copyNameToBuffer(sz, sizeof sz);
                        valueStart += strlen(sz);
                    }
                    if(itemNeedsValue(entry->getPosition().getJustification())) {
                        sz[valueStart] = 32;
                        valueStart++;
                        copyMenuItemValue(entry->getMenuItem(), sz + valueStart, sizeof(sz) - valueStart, drawingFlags.isActive());
                        serlogF2(SER_TCMENU_DEBUG, "Value ", sz);
                    }
                    int position = calculateOffset(entry->getPosition().getJustification(), int(areaSize.x) + 1, sz);
                    copyIntoBuffer(&buffer[1], sz, position, bufferSize);
                    buffer[0] = drawingFlags.isEditing() ? editChar : (drawingFlags.isActive() ? forwardChar : ' ');
                    buffer[min(uint8_t(areaSize.x + 1), bufferSize)] = 0;
                    lcd->setCursor(where.x, where.y);
                    if(theItem == menuMgr.getCurrentEditor() && menuMgr.getEditorHints().getEditorRenderingType() != CurrentEditorRenderingHints::EDITOR_REGULAR) {
                        setupEditorPlacement(where.x + valueStart + menuMgr.getEditorHints().getStartIndex(), where.y);
                    }
                }
                serlogF4(SER_TCMENU_DEBUG, "Buffer: ", where.x,where.y, buffer);
                lcd->print(buffer);
            }
            
            void LiquidCrystalRenderer::setupEditorPlacement(int32_t x, int32_t y) {
                lcdEditorCursorX = min(int32_t(width - 1), x);
                lcdEditorCursorY = y;
            }
            
            void LiquidCrystalRenderer::drawingCommand(RenderDrawingCommand command) {
                switch (command) {
                    case DRAW_COMMAND_CLEAR:
                        lcd->clear();
                        break;
                    case DRAW_COMMAND_START:
                        if(lcdEditorCursorX != 0xFF) {
                            if(menuMgr.getCurrentEditor() == nullptr) {
                                lcdEditorCursorX = 0xFF; // edit has ended, clear our status
                                lcdEditorCursorY = 0xFF;
                            }
                            lcd->noCursor(); // always turn off the cursor while we draw
                        }
                    case DRAW_COMMAND_ENDED:
                        if(lcdEditorCursorX != 0xFF) {
                            lcd->setCursor(lcdEditorCursorX, lcdEditorCursorY);
                            serlogF3(SER_TCMENU_DEBUG, "Editor cursor: ", lcdEditorCursorX, lcdEditorCursorY);
                            lcd->cursor(); // re-enable the cursor after drawing.
                        }
                        break;
                    default:
                        break;
                }
            }
            
            void LiquidCrystalRenderer::fillWithBackgroundTo(int endPoint) {
                for(uint16_t y=endPoint;y<height;++y) {
                    lcd->setCursor(0, y);
                    for(uint16_t x=0;x<width;x++) {
                        lcd->print(' ');
                    }
                }
            }
            
            BaseDialog* LiquidCrystalRenderer::getDialog() {
                if(dialog == nullptr) {
                    dialog = new MenuBasedDialog();
                }
                return dialog;
            }
            """;
}
