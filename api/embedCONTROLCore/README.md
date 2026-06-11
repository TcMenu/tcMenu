## embedCONTROL core library

This library contains most of the logic to do with managing embedCONTROL connections, and the core code to do with arranging the items onto the display. However, this does not contain any platform specific logic.

This library is kept on Java 11 so that it can be shared with Android and used with the Objective-C cross-compiler. If need be, we will de-sugar this source code to keep it compatible with the cross-compiler.
