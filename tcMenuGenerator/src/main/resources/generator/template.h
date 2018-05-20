
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

#define CALLBACK_FUNCTION

// Forward reference all the callback functions to make life easier for the compiler.
[# th:each="func : ${callbacks}"]
void CALLBACK_FUNCTION [(${func})](int menuId);
[/]

// Export all the menu items, makes like easier for the compiler.
[# th:each="item : ${menuItems}"]
[(${item.headerText})]
[/]

#endif /* header include check */
