package com.thecoderscorner.menu.editorui.generator.parameters;

import com.thecoderscorner.menu.mgr.MenuInMenu;

import java.util.Objects;

public final class MenuInMenuDefinition {
    private final String variableName;
    private final String portOrIpAddress;
    private final int portOrBaud;
    private final ConnectionType connectionType;
    private final MenuInMenu.ReplicationMode replicationMode;
    private final int subMenuId;
    private final int idOffset;
    private final int maximumRange;

    public MenuInMenuDefinition(String variableName, String portOrIpAddress, int portOrBaud,
                                ConnectionType connectionType,
                                MenuInMenu.ReplicationMode replicationMode,
                                int subMenuId, int idOffset, int maximumRange) {
        this.variableName = variableName;
        this.portOrIpAddress = portOrIpAddress;
        this.portOrBaud = portOrBaud;
        this.connectionType = connectionType;
        this.replicationMode = replicationMode;
        this.subMenuId = subMenuId;
        this.idOffset = idOffset;
        this.maximumRange = maximumRange;
    }

    public enum ConnectionType {SOCKET, SERIAL}

    public String printableConnection() {
        return switch (connectionType) {
            case SOCKET -> String.format("%s:%d offset %d", portOrIpAddress, portOrBaud, idOffset);
            case SERIAL -> String.format("%s@%d offset %d", portOrIpAddress, portOrBaud, idOffset);
        };
    }

    public String getVariableName() {
        return variableName;
    }

    public String getPortOrIpAddress() {
        return portOrIpAddress;
    }

    public int getPortOrBaud() {
        return portOrBaud;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public MenuInMenu.ReplicationMode getReplicationMode() {
        return replicationMode;
    }

    public int getSubMenuId() {
        return subMenuId;
    }

    public int getIdOffset() {
        return idOffset;
    }

    public int getMaximumRange() {
        return maximumRange;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MenuInMenuDefinition) obj;
        return Objects.equals(this.variableName, that.variableName) &&
                Objects.equals(this.portOrIpAddress, that.portOrIpAddress) &&
                this.portOrBaud == that.portOrBaud &&
                Objects.equals(this.connectionType, that.connectionType) &&
                Objects.equals(this.replicationMode, that.replicationMode) &&
                this.subMenuId == that.subMenuId &&
                this.idOffset == that.idOffset &&
                this.maximumRange == that.maximumRange;
    }

    @Override
    public int hashCode() {
        return Objects.hash(variableName, portOrIpAddress, portOrBaud, connectionType, replicationMode, subMenuId, idOffset, maximumRange);
    }

    @Override
    public String toString() {
        return "MenuInMenuDefinition[" +
                "variableName=" + variableName + ", " +
                "portOrIpAddress=" + portOrIpAddress + ", " +
                "portOrBaud=" + portOrBaud + ", " +
                "connectionType=" + connectionType + ", " +
                "replicationMode=" + replicationMode + ", " +
                "subMenuId=" + subMenuId + ", " +
                "idOffset=" + idOffset + ", " +
                "maximumRange=" + maximumRange + ']';
    }

}
