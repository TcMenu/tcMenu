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

__STANDARD_DRAWABLE_CODE__

__TEXT_HANDLING_CODE__

