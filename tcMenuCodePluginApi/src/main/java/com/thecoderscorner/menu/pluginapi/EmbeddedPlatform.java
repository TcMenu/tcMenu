/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi;

import java.util.Objects;

import static com.thecoderscorner.menu.pluginapi.EmbeddedPlatform.EmbeddedLanguage.CPP_32;
import static com.thecoderscorner.menu.pluginapi.EmbeddedPlatform.EmbeddedLanguage.CPP_AVR;

/**
 * Defines an embedded platform. For example boardId of ARDUINO defines the 8 bit AVR based Arduino. Whereas
 * ARDUINO32 defines 32 bit SAMD variants. These embedded platforms can be loaded up at runtime and the list
 * below should not be relied upon to be the full set.
 */
public class EmbeddedPlatform {


    public enum EmbeddedLanguage {CPP_32, CPP_AVR;}

    /** Defines the AVR based 8 bit platform */
    public final static EmbeddedPlatform ARDUINO_AVR = new EmbeddedPlatform("Arduino AVR/Uno/Mega", "ARDUINO", CPP_AVR);
    /** Defines the 32 bit arduino platform */
    public final static EmbeddedPlatform ARDUINO32 = new EmbeddedPlatform("Arduino SAMD/ESP", "ARDUINO32", CPP_32);


    private final String friendlyName;
    private final String boardId;
    private final EmbeddedLanguage language;

    public EmbeddedPlatform(String friendlyName, String boardId, EmbeddedLanguage language) {
        this.friendlyName = friendlyName;
        this.boardId = boardId;
        this.language = language;
    }

    @Override
    public String toString() {
        return friendlyName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmbeddedPlatform that = (EmbeddedPlatform) o;
        return Objects.equals(boardId, that.boardId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(boardId);
    }

    public String getBoardId() {
        return boardId;
    }

    public EmbeddedLanguage getLanguage() {
        return language;
    }
}
