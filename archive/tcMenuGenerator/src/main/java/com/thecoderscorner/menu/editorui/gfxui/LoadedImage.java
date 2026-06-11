package com.thecoderscorner.menu.editorui.gfxui;

import com.thecoderscorner.menu.domain.util.PortablePalette;
import com.thecoderscorner.menu.editorui.gfxui.pixmgr.BmpDataManager;
import com.thecoderscorner.menu.editorui.gfxui.pixmgr.NativePixelFormat;

public record LoadedImage(
            BmpDataManager bmpData,
            NativePixelFormat pixelFormat,
            int width, int height,
            PortablePalette palette) {
    }
