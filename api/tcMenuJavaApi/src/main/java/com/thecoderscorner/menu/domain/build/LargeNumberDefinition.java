package com.thecoderscorner.menu.domain.build;

public class LargeNumberDefinition {
    private final int decPlaces;
    private final int digitsAllowed;
    private final boolean neg;

    private LargeNumberDefinition(int decPlaces, int digitsAllowed, boolean neg) {
        this.decPlaces = decPlaces;
        this.digitsAllowed = digitsAllowed;
        this.neg = neg;
    }

    public int getDecPlaces() {
        return decPlaces;
    }

    public int getDigitsAllowed() {
        return digitsAllowed;
    }

    public boolean isNeg() {
        return neg;
    }

    public static LargeNumberDefinition allowingNegative(int digitsAllowed, int decPlaces) {
        return new LargeNumberDefinition(decPlaces, digitsAllowed, true);
    }
    public static LargeNumberDefinition positiveOnly(int digitsAllowed, int decPlaces) {
        return new LargeNumberDefinition(decPlaces, digitsAllowed, false);
    }
    public static LargeNumberDefinition intAllowingNegative(int digitsAllowed) {
        return new LargeNumberDefinition(0, digitsAllowed, true);
    }
    public static LargeNumberDefinition intPositiveOnly(int digitsAllowed) {
        return new LargeNumberDefinition(0, digitsAllowed, false);
    }
}
