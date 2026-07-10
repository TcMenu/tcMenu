package com.thecoderscorner.menu.domain.build;

public class CallbackDefinition {
    private final String callback;

    private CallbackDefinition(String callback) {
        this.callback = callback;
    }

    public static CallbackDefinition noCallback() {
        return new CallbackDefinition("");
    }

    public static CallbackDefinition functionCb(String callback) {
        if(callback == null) throw new IllegalArgumentException("Callback cannot be null");
        if(callback.endsWith("RtCall")) throw new IllegalArgumentException("Not function callback as ends with RtCall");
        return new CallbackDefinition(callback);
    }

    public static CallbackDefinition rtCall(String callback) {
        if(callback == null) throw new IllegalArgumentException("Callback cannot be null");
        if(callback.endsWith("RtCall")) throw new IllegalArgumentException("We already add RtCall");
        return new CallbackDefinition(callback + "RtCall");
    }

    public String cbText() {
        return callback;
    }
}
