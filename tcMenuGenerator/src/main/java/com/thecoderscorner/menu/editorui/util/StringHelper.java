/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.util;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public class StringHelper {

    public static boolean isStringEmptyOrNull(String str) {
        return str == null || str.isEmpty();
    }

    public static String escapeRex(String rex) {
        var sb = new StringBuilder(64);
        for(int i = 0; i < rex.length(); i++) {
            if(rex.charAt(i) == '{' || rex.charAt(i) == '}' || rex.charAt(i) == '$') {
                sb.append("\\\\").append(rex.charAt(i));
            }
            else sb.append(rex.charAt(i));
        }
        return sb.toString();
    }

    public static String escapeForString(String text) {
        var sb = new StringBuilder(64);
        for(int i = 0; i < text.length(); i++) {
            if(text.charAt(i) == '\"') {
                sb.append("\\").append(text.charAt(i));
            }
            else sb.append(text.charAt(i));
        }
        return sb.toString();

    }

    public static String capitaliseFirst(String s) {
        if (s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static String capitaliseWords(Object o) {
        if (o == null || o.toString().isEmpty()) return "";
        String[] parts = o.toString().split("[_\s-]");
        return Arrays.stream(parts)
                .filter(s -> !s.isBlank())
                .map(p -> Character.toUpperCase(p.charAt(0)) + p.substring(1).toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(" "));
    }

    public static String repeat(String a, int max) {
        StringBuilder sb = new StringBuilder(max + 16);
        for (int i=0; i<max; i++) {
            sb.append(a);
        }
        return sb.toString();
    }

    public static void printArrayToStream(PrintStream ps, byte[] dataBytes, int numberOfHexPoints) {
        for(int i = 0; i< dataBytes.length; i++) {
            ps.append(String.format("0x%02x", dataBytes[i]));
            if(i != (dataBytes.length -1)) {
                ps.append(",");
            }
            else {
                ps.append("\n");
            }

            if((i%20)==19) {
                ps.append("\n");
            }
        }
    }

}
