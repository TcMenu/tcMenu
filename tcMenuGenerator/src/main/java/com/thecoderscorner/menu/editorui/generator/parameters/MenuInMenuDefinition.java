package com.thecoderscorner.menu.editorui.generator.parameters;

public record MenuInMenuDefinition(String variableName, String portOrIpAddress, int portOrBaud,
                                   MenuInMenuDefinition.ConnectionType connectionType,
                                   int subMenuId, int idOffset, int maximumRange) {
    public enum ConnectionType {SOCKET, SERIAL}

    public String printableConnection() {
        return switch (connectionType) {
            case SOCKET ->  String.format("%s:%d offset %d", portOrIpAddress, portOrBaud, idOffset);
            case SERIAL ->  String.format("%s@%d offset %d", portOrIpAddress, portOrBaud, idOffset);
        };
    }
}
