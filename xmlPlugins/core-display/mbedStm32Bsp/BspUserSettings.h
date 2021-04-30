#ifndef TCMENU_BSP_INCLUDES
#define TCMENU_BSP_INCLUDES

//
// In this file you provide all the includes needed for your BSP driver. It may even be that you need to provide
// mappings between any incompatible functions and the ones you have available. In most cases this should be minimal.
//

// These are the includes needed to access the display and touch BSP functions
// This path works for platformio, change as needed for your arrangements.

#include "Drivers/BSP/STM32F429I-Discovery/stm32f429i_discovery_ts.h"
#include "Drivers/BSP/STM32F429I-Discovery/stm32f429i_discovery_lcd.h"

#include "Utilities/Fonts/fonts.h"

// Here's the functions we expect to be present, if you don't have these in your setup, you will need to provide them.

// Touch screen

// LCD
// structure TS_StateTypeDef must be defined
// BSP_TS_Init(xSize, ySize)
// BSP_TS_GetState(TS_StateTypeDef*)

// structure sFONT must be defined
// BSP_LCD_Init()
// BSP_LCD_LayerDefaultInit(layer, address)
// BSP_LCD_DrawBitmap(x, y, data)
// BSP_LCD_DrawPixel(x, y, color)
// BSP_LCD_FillRect(x, y, w, h)
// BSP_LCD_DrawRect(x, y, w, h)
// BSP_LCD_FillCircle(x, y, r)
// BSP_LCD_DrawCircle(x, y, r)
// BSP_LCD_DrawLine(x, y, x1, y1)
// BSP_LCD_FillTriangle(x, y, x1, y1, x2, y2)
// BSP_LCD_SetFont(sFONT*)
// BSP_LCD_SetTextColor(uint32_t)
// BSP_LCD_SetBackColor(uint32_t)
// BSP_LCD_DisplayStringAt(x, y, text, justification)

#endif