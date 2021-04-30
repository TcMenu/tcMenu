#ifndef CUSTOM_DISPLAY_DRAWABLE
#define CUSTOM_DISPLAY_DRAWABLE

/**
 * Here you should implement your own drawable that will be constructed by setup to draw onto the screen
 */
class CustomScreenDrawable : DeviceDrawable {
    /**
     * @return the dimensions of the display (x, y)
     */
    Coord getDisplayDimensions() override {
        return Coord();
    }

    /**
     * If the display supports double buffereing, you can provide a sub display here with a given colour palette and size.
     * If used, the menu rendering will try and use this to do all drawing, it is your responsibility to draw this sub
     * display upon a transaction end. Return nullptr when you dont support.
     * @param where the position of the buffer on the screen
     * @param size the size of the buffer on the screen
     * @param palette the palette colors to use
     * @param paletteSize the size of the palette
     * @return either nullptr or a drawable.
     */
    DeviceDrawable * getSubDeviceFor(const Coord &where, const Coord &size, const color_t *palette, int paletteSize) override {
        return nullptr;
    }

    /**
     * Draws text on to the display in the current foreground (draw) color, the background should be transparent.
     * @param where the position on screen
     * @param font the font to use for drawing
     * @param mag the magnification or font number.
     * @param text text to be drawn
     */
    void drawText(const Coord &where, const void *font, int mag, const char *text) override {

    }

    /**
     * Draws an icon bitmap on to the display at the position specified. It will be drawn in draw color and background
     * color.
     * @param where the position to draw
     * @param icon the icon to draw
     * @param selected if the item is to use the selected image
     */
    void drawBitmap(const Coord &where, const DrawableIcon *icon, bool selected) override {

    }

    /**
     * Draws an XBM onto the display at the position specified. It will be drawn in draw color and background color.
     * @param where the position to draw
     * @param size the size of the XBM
     * @param data the actual data
     */
    void drawXBitmap(const Coord &where, const Coord &size, const uint8_t *data) override {

    }

    /**
     * Draws a box either filled or outline at the specified position, in draw color.
     * @param where the position to draw
     * @param size the size of the rectangle
     * @param filled if it is filled.
     */
    void drawBox(const Coord &where, const Coord &size, bool filled) override {

    }

    /**
     * Draws a circle that is either filled or unfilled (if supported) at the specified position
     * @param where the position to draw
     * @param radius the circle radius
     * @param filled if it is to be filled (some displays only support filled)
     */
    void drawCircle(const Coord &where, int radius, bool filled) override {

    }

    /**
     * Draws a polygon outline of either two or three points, more than this may not be supported.
     * @param points most drawables only support either 2 points(line) or three points (triangle)
     * @param numPoints the number of points
     * @param filled if it is to be filled
     */
    void drawPolygon(const Coord *points, int numPoints, bool filled) override {

    }

    /**
     * Called at the beginning and end of drawing, during start any initialisation before drawing should be done, and
     * when called at the end, anything needed to actually display the buffer should be done.
     * @param isStarting if the event is a start or end.
     * @param redrawNeeded true if a full redraw is required.
     */
    void transaction(bool isStarting, bool redrawNeeded) override {

    }

    /**
     * Use this method to get the text size in a given font, and baseline if needed.
     * @param font the font to be used for sizing
     * @param mag the magnification
     * @param text the text to be used for sizing.
     * @param baseline the baseline below the starting point
     * @return the text extents
     */
    Coord textExtents(const void *font, int mag, const char *text, int *baseline) override {
        return Coord();
    }
};

/**
 * Here you must create an instance of your drawable and it must be called custom drawable
 */
CustomScreenDrawable customDrawable;

#endif //CUSTOM_DISPLAY_DRAWABLE
