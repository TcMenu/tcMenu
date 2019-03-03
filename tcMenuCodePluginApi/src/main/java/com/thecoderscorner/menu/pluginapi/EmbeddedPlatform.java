/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi;

import java.util.Objects;

public class EmbeddedPlatform {
    private final String friendlyName;
    private final String boardId;

    public EmbeddedPlatform(String friendlyName, String boardId) {
        this.friendlyName = friendlyName;
        this.boardId = boardId;
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
}
