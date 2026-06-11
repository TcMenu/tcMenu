package com.thecoderscorner.menu.domain.state;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a scroll position as used by ScrollChoiceMenuItems, it holds the position and the current string value.
 */
public class CurrentScrollPosition {
    private final int position;
    private final AtomicReference<String> value = new AtomicReference<>();

    /**
     * Create from the position and value
     * @param position the current position
     * @param value the current value
     */
    public CurrentScrollPosition(int position, String value) {
        this.position = position;
        this.value.set(value);
    }

    /**
     * Create from a textual representation in the form, position-value, EG 1-Pizza
     * @param text the text form of the object to parse
     */
    public CurrentScrollPosition(String text) {
        var splitPoint = text.indexOf('-');
        if(splitPoint != -1)
        {
            var numStr = text.substring(0, splitPoint);
            int pos = 0;
            try {
                pos = Integer.parseInt(numStr);
            }
            catch(NumberFormatException ex) {
                // ignored, there's nothing we can do if we get a bad value
            }
            position = pos;
            value.set(text.substring(splitPoint + 1));
            return;
        }
        position = 0;
        value.set("Unknown");
    }

    /**
     * @return the current position
     */
    public int getPosition() {
        return position;
    }

    /**
     * @return the current value
     */
    public String getValue() {
        return value.get();
    }

    @Override
    public String toString() {
        return position + "-" + value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CurrentScrollPosition that = (CurrentScrollPosition) o;
        return position == that.position && Objects.equals(value.get(), that.value.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, value.get());
    }

    /**
     * Allows the value to be changed just after creation, this is for local UIs where the setting happens just after
     * creation of the state.
     * @param txt the new value
     */
    public void setTextValue(Object txt) {
        value.set(Objects.toString(txt));
    }
}
