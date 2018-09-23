/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 * RemoteTypes.h - contains the definitions of each message and field.
 */

#ifndef _TCMENU_REMOTETYPES_H_
#define _TCMENU_REMOTETYPES_H_

/**
 * This defines the maximum size of any field value that can be received by this library.
 * If you need longer fields, change this value to a higher one.
 */
#define MAX_VALUE_LEN 24

/**
 * A helper to generate the major minor version numbers used in the protocol
 */
#define majorminor(maj, min) ((maj * 100) + min)

/**
 * Definition of the current API version
 */
#define API_VERSION majorminor(1, 0)

/**
 * Converts a message field as two separate entities into a single word.
 */
#define msgFieldToWord(a,b)  ( (((uint16_t)a)<<8) | ((uint16_t)b) )

/*
 * Definitions for an unknown field key or part thereof.
 */
#define UNKNOWN_FIELD_PART 0x00

/**
 * Definition for an unknown message key
 */
#define UNKNOWN_MSG_TYPE 0x0000

/*
 * Message Types are defined below, each type is a two digit ID represented as a word.
 */

#define MSG_JOIN msgFieldToWord('N','J')
#define MSG_HEARTBEAT msgFieldToWord('H','B')
#define MSG_BOOTSTRAP msgFieldToWord('B','S')
#define MSG_BOOT_ANALOG msgFieldToWord('B','A')
#define MSG_BOOT_SUBMENU msgFieldToWord('B', 'M')
#define MSG_BOOT_ENUM msgFieldToWord('B', 'E')
#define MSG_BOOT_BOOL msgFieldToWord('B', 'B')
#define MSG_BOOT_TEXT msgFieldToWord('B','T')
#define MSG_CHANGE_INT msgFieldToWord('V', 'C')

/*
 * Fields names that can be used in messages. Again each type is a two digit ID represented as a word
 */

#define FIELD_MSG_TYPE    msgFieldToWord('M', 'T')
#define FIELD_MSG_NAME    msgFieldToWord('N', 'M')
#define FIELD_VERSION     msgFieldToWord('V', 'E')
#define FIELD_PLATFORM    msgFieldToWord('P', 'F')
#define FIELD_BOOT_TYPE   msgFieldToWord('B', 'T')
#define FIELD_ID          msgFieldToWord('I', 'D')
#define FIELD_READONLY    msgFieldToWord('R', 'O')
#define FIELD_PARENT      msgFieldToWord('P', 'I')
#define FIELD_ANALOG_MAX  msgFieldToWord('A', 'M')
#define FIELD_ANALOG_OFF  msgFieldToWord('A', 'O')
#define FIELD_ANALOG_DIV  msgFieldToWord('A', 'D')
#define FIELD_ANALOG_UNIT msgFieldToWord('A', 'U')
#define FIELD_CURRENT_VAL msgFieldToWord('V', 'C')
#define FIELD_BOOL_NAMING msgFieldToWord('B', 'N')
#define FIELD_NO_CHOICES  msgFieldToWord('N', 'C')
#define FIELD_CHANGE_TYPE msgFieldToWord('T', 'C')
#define FIELD_MAX_LEN     msgFieldToWord('M', 'L')

#define FIELD_PREPEND_CHOICE 'C'

/**
 * Defines the types of change that can be received / sent in changes messages, either
 * delta or incremental (for example menuVolume + 3) or absolulte (channel is now 2)
 */
enum ChangeType: byte {
	CHANGE_DELTA = 0, CHANGE_ABSOLUTE = 1
};

/**
 * Defines the API platforms that are supported at the moment
 */
enum ApiPlatform : byte {
	PLATFORM_ARDUINO_8BIT = 0,
	PLATFORM_JAVA_API = 1
};

#endif /* _TCMENU_REMOTETYPES_H_ */
