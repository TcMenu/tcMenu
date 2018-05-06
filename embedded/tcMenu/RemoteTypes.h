/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 * RemoteConnector.h - contains the base functionality for communication between the menu library
 * and remote APIs.
 */

#ifndef _TCMENU_REMOTETYPES_H_
#define _TCMENU_REMOTETYPES_H_

#define msgFieldToWord(a,b)  ( (((uint16_t)a)<<8) | ((uint16_t)b) )

#define MAX_VALUE_LEN 24

/*
 * A list of errors returned by the error callback
 */
#define REMOTE_ERR_WRITE_NOT_CONNECTED 1
#define REMOTE_ERR_PROTOCOL_WRONG 2

// an unknown message or field
#define UNKNOWN_FIELD_PART 0x00
#define UNKNOWN_MSG_TYPE 0x0000

/*
 * Message Types are defined below, each type is a two digit ID represented as a word.
 */

#define MSG_JOIN msgFieldToWord('N','J')
#define MSG_HEARTBEAT msgFieldToWord('H','B')
#define MSG_BOOTSTRAP msgFieldToWord('B','S')
#define MSG_BOOT_ANALOG msgFieldToWord('B','A')

/*
 * Fields names that can be used in messages. Again each type is a two digit ID represented as a word
 */

#define FIELD_MSG_TYPE    msgFieldToWord('M', 'T')
#define FIELD_MSG_NAME    msgFieldToWord('N', 'M')
#define FIELD_VERSION     msgFieldToWord('C', 'V')
#define FIELD_BOOT_TYPE   msgFieldToWord('B', 'T')
#define FIELD_ID          msgFieldToWord('I', 'D')
#define FIELD_PARENT      msgFieldToWord('P', 'I')
#define FIELD_ANALOG_MAX  msgFieldToWord('A', 'M')
#define FIELD_ANALOG_OFF  msgFieldToWord('A', 'O')
#define FIELD_ANALOG_DIV  msgFieldToWord('A', 'D')
#define FIELD_ANALOG_UNIT msgFieldToWord('A', 'U')

#endif /* _TCMENU_REMOTETYPES_H_ */
