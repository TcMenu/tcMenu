package com.thecoderscorner.bmped.gfxui;

import com.thecoderscorner.menu.domain.util.PortablePalette;
import com.thecoderscorner.bmped.gfxui.pixmgr.BmpDataManager;
import com.thecoderscorner.bmped.gfxui.pixmgr.NativePixelFormat;

public record LoadedImage(
            BmpDataManager bmpData,
            NativePixelFormat pixelFormat,
            int width, int height,
            PortablePalette palette) {
    }
