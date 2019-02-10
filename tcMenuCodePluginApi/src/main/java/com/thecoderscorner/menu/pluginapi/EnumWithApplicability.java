/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi;


import java.util.Objects;
import java.util.Set;

public class EnumWithApplicability {
    private final Set<EmbeddedPlatform> platformApplicability;
    private String description;
    private final Class<? extends EmbeddedCodeCreator> creator;
    private final int key;

    public EnumWithApplicability(Set<EmbeddedPlatform> platformApplicability, String description,
                                 Class<? extends EmbeddedCodeCreator> creator, int key) {
        this.platformApplicability = platformApplicability;
        this.description = description;
        this.creator = creator;
        this.key = key;
    }

    public boolean isApplicableFor(EmbeddedPlatform platform) {
        return platformApplicability.contains(platform);
    }

    public int getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnumWithApplicability that = (EnumWithApplicability) o;
        return key == that.key;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return getDescription();
    }

    public EmbeddedCodeCreator makeCreator() {
        try {
            return creator.getConstructor().newInstance();
        } catch (Exception e) {
            throw new UnsupportedOperationException("Code generators must have no args constructor: " + creator, e);
        }
    }

}
