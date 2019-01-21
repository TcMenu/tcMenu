package com.thecoderscorner.menu.editorui.util;

public class StringHelper {

    public static boolean isStringEmptyOrNull(String str) {
        return str == null || str.isEmpty();
    }


    public static String repeat(String a, int max) {
        StringBuilder sb = new StringBuilder(max + 16);
        for (int i=0; i<max; i++) {
            sb.append(a);
        }
        return sb.toString();
    }
}
