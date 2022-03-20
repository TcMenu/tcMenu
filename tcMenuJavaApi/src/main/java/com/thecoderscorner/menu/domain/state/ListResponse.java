package com.thecoderscorner.menu.domain.state;

import java.util.Optional;
import java.util.regex.Pattern;

public class ListResponse {
    public static final ListResponse EMPTY = new ListResponse(0, ResponseType.SELECT_ITEM);

    public enum ResponseType { SELECT_ITEM, INVOKE_ITEM }
    private static final Pattern LIST_RESPONSE_PATTERN = Pattern.compile("^(\\d+):(\\d)$");
    private final int row;
    private final ResponseType responseType;

    public ListResponse(int row, ResponseType responseType) {
        this.row = row;
        this.responseType = responseType;
    }

    public int getRow() {
        return row;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    @Override
    public String toString() {
        return row + ":" + (responseType == ResponseType.INVOKE_ITEM ? "1" : "0");
    }

    public static Optional<ListResponse> fromString(String value) {
        var matcher = LIST_RESPONSE_PATTERN.matcher(value);
        if(matcher.matches()) {
            int index = Integer.parseInt(matcher.group(1));
            ResponseType respType = matcher.group(2).equals("1") ? ResponseType.INVOKE_ITEM : ResponseType.SELECT_ITEM;
            return Optional.of(new ListResponse(index, respType));
        }
        return Optional.empty();
    }

}
