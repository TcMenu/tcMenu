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


#include "__ACTUAL_GENERATED_HDR__"
#include "stm32f4xx_hal.h"
#include <algorithm>

// Tell the compiler that we have a DMA2d handle available to us
extern DMA2D_HandleTypeDef hdma2d;

void StmDMA2dAdafruitFrameBuffer16::writeFillRect(int16_t x, int16_t y, int16_t w, int16_t h, color_t color) {
    if (w <= 0 || h <= 0) return;

    Coord start, end;
    if (!this->correctDimensions(x, y, start) ||
        !this->correctDimensions(x + w - 1, y + h - 1, end)) {
        return;
    }

    // Use DMA2d to fill the rectangle, wait for it to be available if needed
    HAL_DMA2D_PollForTransfer(&hdma2d, 100);

    int16_t x_min = std::min(start.x, end.x);
    int16_t x_max = std::max(start.x, end.x);
    int16_t y_min = std::min(start.y, end.y);
    int16_t y_max = std::max(start.y, end.y);

    uint32_t width = (x_max - x_min) + 1;
    uint32_t height = (y_max - y_min) + 1;
    auto dstAddress = reinterpret_cast<uint32_t>(&this->buffer[x_min + (y_min * this->WIDTH)]);
    // Prepare 16-bit RGB565 color for DMA2D R2M mode
    uint32_t dmaColor = static_cast<uint32_t>((color & 0xF800) << 8) |
        static_cast<uint32_t>((color & 0x07E0) << 5) |
        static_cast<uint32_t>((color & 0x001F) << 3);

    hdma2d.Init.Mode = DMA2D_R2M;
    hdma2d.Init.ColorMode = DMA2D_OUTPUT_RGB565;
    hdma2d.Init.OutputOffset = WIDTH - width;

    if (HAL_DMA2D_Init(&hdma2d) == HAL_OK) {
        if (HAL_DMA2D_Start(&hdma2d, dmaColor, dstAddress, width, height) == HAL_OK) {
            HAL_DMA2D_PollForTransfer(&hdma2d, 100);
        }
    }
}

void StmDMA2dAdafruitFrameBuffer16::writeFastVLine(int16_t x, int16_t y, int16_t h, color_t color) {
    writeFillRect(x, y, 1, h, color);
}

void StmDMA2dAdafruitFrameBuffer16::writeFastHLine(int16_t x, int16_t y, int16_t w, color_t color) {
    writeFillRect(x, y, w, 1, color);
}

__STANDARD_DRAWABLE_CODE__

__TEXT_HANDLING_CODE__
