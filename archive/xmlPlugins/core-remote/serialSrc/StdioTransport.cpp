
#include "StdioTransport.h"

int StdioTransport::writeStr(const char *data) {
    auto dl = strlen(data);
    for(size_t i = 0; i<dl; ++i) {
        putchar_raw(data[i]);
    }
    return (int)dl;
}

uint8_t StdioTransport::readByte() {
    if (inputBuffer.available()) {
        return inputBuffer.get();
    }
    return -1;
}

void StdioTransport::close() {
    currentField.msgType = UNKNOWN_MSG_TYPE;
    currentField.fieldType = FVAL_PROCESSING_AWAITINGMSG;
}

int StdioTransport::writeChar(char data) {
    putchar_raw(data);
    return 1;
}


bool StdioTransport::readAvailable() {
    int ch;
    while ((ch=getchar_timeout_us(0)) != PICO_ERROR_TIMEOUT) {
        inputBuffer.put(ch);
    }
    return inputBuffer.available();
}
