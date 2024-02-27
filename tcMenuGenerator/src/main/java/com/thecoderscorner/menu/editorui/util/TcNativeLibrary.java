package com.thecoderscorner.menu.editorui.util;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TcNative wraps the tcNativeLibrary functions to make it easier to use in more than one place in the code, and remove
 * native specific code from the main code area.
 */
public class TcNativeLibrary {
    private final static System.Logger logger = System.getLogger(TcNativeLibrary.class.getSimpleName());
    private final static AtomicReference<TcNativeLibrary> theInstance = new AtomicReference<>(null);

    private final MethodHandle fontLibInit;
    private final MethodHandle fontLibDestroy;
    private final MethodHandle fontClose;
    private final MethodHandle fontLibCreateFont;
    private final MethodHandle fontGetGlyph;
    private final MethodHandle canDisplayFn;
    private final MethodHandle setPixelsPerInch;

    private TcNativeLibrary() {
        logger.log(System.Logger.Level.INFO, "Loading TcNative Library");
        System.loadLibrary("tcMenuNative");
        logger.log(System.Logger.Level.INFO, "Creating native linker");
        Linker linker = Linker.nativeLinker();
        SymbolLookup fontLib = SymbolLookup.loaderLookup();
        logger.log(System.Logger.Level.INFO, "Creating initialiseLibrary");
        fontLibInit = linker.downcallHandle(
                fontLib.find("initialiseLibrary").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT)
        );
        logger.log(System.Logger.Level.INFO, "Creating closeLibrary");
        fontLibDestroy = linker.downcallHandle(
                fontLib.find("closeLibrary").orElseThrow(),
                FunctionDescriptor.ofVoid()
        );
        logger.log(System.Logger.Level.INFO, "Creating closeFont");
        fontClose = linker.downcallHandle(
                fontLib.find("closeFont").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT)
        );
        logger.log(System.Logger.Level.INFO, "Creating createFont");
        fontLibCreateFont = linker.downcallHandle(
                fontLib.find("createFont").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
        );
        logger.log(System.Logger.Level.INFO, "Creating getFontGlyph");
        fontGetGlyph = linker.downcallHandle(
                fontLib.find("getFontGlyph").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
        );
        logger.log(System.Logger.Level.INFO, "Creating canDisplay");
        canDisplayFn = linker.downcallHandle(
                fontLib.find("canDisplay").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
        );
        logger.log(System.Logger.Level.INFO, "Creating setPixelsPerInch");
        setPixelsPerInch = linker.downcallHandle(
                fontLib.find("setPixelsPerInch").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT)
        );
    }

    public static TcNativeLibrary getInstance() {
        var ret = theInstance.get();
        if(ret == null) {
            ret = new TcNativeLibrary();
            theInstance.set(ret);
        }
        return ret;
    }

    public MethodHandle getFontLibInit() {
        return fontLibInit;
    }

    public MethodHandle getFontLibDestroy() {
        return fontLibDestroy;
    }

    public MethodHandle getFontClose() {
        return fontClose;
    }

    public MethodHandle getFontLibCreateFont() {
        return fontLibCreateFont;
    }

    public MethodHandle getFontGetGlyph() {
        return fontGetGlyph;
    }

    public MethodHandle getCanDisplayFn() {
        return canDisplayFn;
    }

    public MethodHandle getSetPixelsPerInch() {
        return setPixelsPerInch;
    }
}
