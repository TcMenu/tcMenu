package com.thecoderscorner.menu.domain.state;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class CurrentScrollPosition {
    private final int position;
    private final AtomicReference<String> value = new AtomicReference<>();

    public CurrentScrollPosition(int position, String value) {
        this.position = position;
        this.value.set(value);
    }

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

    public int getPosition() {
        return position;
    }

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

    public void setTextValue(Object txt) {
        value.set((txt != null) ? txt.toString() : "");
    }
}
