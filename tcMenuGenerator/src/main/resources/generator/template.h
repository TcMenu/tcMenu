
/*
 * The code in this file uses open source libraries provided by thecoderscorner
 */

#ifndef MENU_GENERATED_CODE_H
#define MENU_GENERATED_CODE_H

#include<IoAbstraction.h>
#include<Wire.h>
#include<tcMenu.h>
[# th:each="inc : ${allGeneratorIncludes}"]
[(${inc})]
[/]

[(${allGeneratorExports})]

// all callback functions must have this define on them, it is what the menu designer looks for.
#define CALLBACK_FUNCTION

// Forward reference all the callback functions.
[# th:each="func : ${callbacks}"]
void CALLBACK_FUNCTION [(${func})](int menuId);
[/]

// Export all the menu items so they are visible in sketches.
[# th:each="item : ${menuItems}"]
[(${item.headerText})]
[/]

void setupMenu();

#endif /* header include check */
