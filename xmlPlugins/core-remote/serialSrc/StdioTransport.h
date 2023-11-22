#ifndef STDIO_TRANSPORT_H
#define STDIO_TRANSPORT_H

#include <PlatformDetermination.h>
#include <RemoteConnector.h>
#include <remote/BaseRemoteComponents.h>
#include "SCCircularBuffer.h"

namespace tcremote {

    class StdioTransport : public TagValueTransport {
    private:
        SCCircularBuffer inputBuffer;
    public:
        explicit StdioTransport(int readBufferSize) : inputBuffer(readBufferSize), TagValueTransport(TVAL_UNBUFFERED) {}

        void flush() override { stdio_flush(); }

        int writeChar(char data) override;

        int writeStr(const char *data) override;

        uint8_t readByte() override;

        bool readAvailable() override;

        bool available() override { return true; }

        bool connected() override { return true; }

        void close() override;
    };
}

#ifndef TC_MANUAL_NAMESPACING
using namespace tcremote;
#endif // TC_MANUAL_NAMESPACING

#endif //STDIO_TRANSPORT_H
