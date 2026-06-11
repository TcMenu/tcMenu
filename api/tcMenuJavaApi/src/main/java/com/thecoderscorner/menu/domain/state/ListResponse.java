package com.thecoderscorner.menu.domain.state;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * This represents an action that was performed on a list, and not really the state of the list. For example, when
 * the user clicks on the list, or double-clicks. It holds the row that was selected and the action type.
 */
public class ListResponse {
    public static final ListResponse EMPTY = new ListResponse(0, ResponseType.SELECT_ITEM);

    /**
     * The types of response that are supported, current select - single click, invoke - double click.
     */
    public enum ResponseType { SELECT_ITEM, INVOKE_ITEM }
    private static final Pattern LIST_RESPONSE_PATTERN = Pattern.compile("^(\\d+):(\\d)$");
    private final int row;
    private final ResponseType responseType;

    public ListResponse(int row, ResponseType responseType) {
        this.row = row;
        this.responseType = responseType;
    }

    /**
     * @return the row that was selected
     */
    public int getRow() {
        return row;
    }

    /**
     * @return the action that was performed
     */
    public ResponseType getResponseType() {
        return responseType;
    }

    @Override
    public String toString() {
        return row + ":" + (responseType == ResponseType.INVOKE_ITEM ? "1" : "0");
    }

    /**
     * Deserialize a ListResponse from a string if possible or return empty
     * @param value the string to decode
     * @return either a ListResponse or empty.
     */
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
