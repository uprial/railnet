package com.gmail.uprial.secretrooms.common;

import java.util.List;

public final class Utils {
    // A number of server ticks in one second
    public static final int SERVER_TICKS_IN_SECOND = 20;

    public static int seconds2ticks(int seconds) {
        return seconds * SERVER_TICKS_IN_SECOND;
    }

    public static <T> String joinStrings(String delimiter, List<T> contents) {
        if (contents.size() < 1) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(contents.get(0));
        int contentsSize = contents.size();
        for (int i = 1; i < contentsSize; i++) {
            stringBuilder.append(delimiter);
            stringBuilder.append(contents.get(i));
        }

        return stringBuilder.toString();
    }
}
