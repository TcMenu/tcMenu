/**
 * Adafruit based FrameBuffer support class. This class provides the minimal wrappers for high performance
 * drawing extending from our frame buffer class. The base class implements all operations using either
 * draw pixel or slightly optimised line drawing. This version accelerates the most common methods using
 * DMA2D techniques.
 * 
 * It is provided to you as a starting point, and is based on the StmCube BSP packages for the board. So,
 * unless you have the exact same hardware configuration it is likely to require modification. For example,
 * if you use the same processor but a different configuration of the hardware, you may need to modify this
 * class or the BSP package to suit your needs.
 *
 * Words of caution, this is a high performance driver that is intended for senior developers with enough
 * experience to understand the limitations of the hardware and the trade-offs of the acceleration techniques.
 * 
 * Round-trip/Local filesystem notes: It will not be overwritten by designer in local files. If you want to go
 * back to the original then delete this file and regenerate it from the designer.
 */

#ifndef TESTLTDC_ADAFRUITFRAMEBUFFER_H
#define TESTLTDC_ADAFRUITFRAMEBUFFER_H

#include <Adafruit_GFX.h>
#include <PlatformDetermination.h>
#include <frame/AdafruitFrameBuffer.h>

#include "graphics/DeviceDrawable.h"
#include "graphics/DrawingPrimitives.h"
#include <graphics/GraphicsDeviceRenderer.h>

// some colour displays don't create this value
#ifndef BLACK
#define BLACK 0
#endif

// some colour displays don't create this value
#ifndef WHITE
#define WHITE 0xffff
#endif

using namespace tcgfx;

class StmDMA2dAdafruitFrameBuffer16 : public AdafruitFrameBuffer<uint16_t> {
public:
    StmDMA2dAdafruitFrameBuffer16(uint16_t* buffer, int16_t rawWidth, int16_t rawHeight) :
        AdafruitFrameBuffer(buffer, rawWidth, rawHeight) {
    }

    ~StmDMA2dAdafruitFrameBuffer16() override = default;

    void writeFillRect(int16_t x, int16_t y, int16_t w, int16_t h, color_t color) override;
    void writeFastVLine(int16_t x, int16_t y, int16_t h, color_t color) override;
    void writeFastHLine(int16_t x, int16_t y, int16_t w, color_t color) override;
};

__DEFAULT_FRAME_DRAWABLE_HDR__

#endif //TESTLTDC_ADAFRUITFRAMEBUFFER_H
