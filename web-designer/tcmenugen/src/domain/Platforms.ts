
export class EmbeddedPlatform {
    constructor(public friendlyName: string, public boardId: string, public usesProgmem: boolean) {}
}

export const ARDUINO_AVR = new EmbeddedPlatform("Arduino AVR/Mega2560", "ARDUINO", true);
export const ARDUINO32 = new EmbeddedPlatform("Arduino 32bit ARM", "ARDUINO32", false);
export const ARDUINO_ESP8266 = new EmbeddedPlatform("Arduino ESP8266", "ARDUINO_ESP8266", true);
export const ARDUINO_ESP32 = new EmbeddedPlatform("Arduino ESP32", "ARDUINO_ESP32", true);
export const STM32DUINO = new EmbeddedPlatform("Arduino STM32Duino", "STM32DUINO", false);
export const MBED_RTOS = new EmbeddedPlatform("mbed RTOS", "MBED_RTOS", false);
export const PICO_SDK_CMAKE = new EmbeddedPlatform("PicoSDK CMake", "PICO_SDK", false);
export const ESP32_IDF_CMAKE = new EmbeddedPlatform("ESP32-IDF CMake", "ESP32_IDF_CMAKE", false);
export const STM_CUBE_CMAKE = new EmbeddedPlatform("STM32 Cube CMake", "STM_CUBE_CMAKE", false);
export const RASPBERRY_PIJ = new EmbeddedPlatform("RaspberryPI-Java", "RASPBERRY_PIJ", false);

export const ALL_PLATFORMS = [
    ARDUINO_AVR,
    ARDUINO32,
    ARDUINO_ESP8266,
    ARDUINO_ESP32,
    STM32DUINO,
    PICO_SDK_CMAKE,
    ESP32_IDF_CMAKE,
    STM_CUBE_CMAKE,
    MBED_RTOS,
    RASPBERRY_PIJ
];

export function embeddedPlatformFromId(id: string): EmbeddedPlatform {
    return ALL_PLATFORMS.find(p => p.boardId === id)!;
}