package com.thecoderscorner.menu.editorui.generator.parameters;

import com.thecoderscorner.menu.mgr.MenuInMenu;
import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.editorui.generator.parameters.MenuInMenuDefinition.ConnectionType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class MenuInMenuCollectionTest {
    @Test
    void testCollectionAddRemove() {
        MenuInMenuCollection collection = new MenuInMenuCollection();
        assertThat(collection.getAllDefinitions()).isEmpty();
        MenuInMenuDefinition test1 = newDefinitionWith("test1", "COM2", 9600);
        MenuInMenuDefinition test2 = newDefinitionWith("test2", "COM1", 9600);
        collection.addDefinition(test1);
        collection.addDefinition(test2);
        assertThat(collection.getAllDefinitions()).containsExactlyInAnyOrder(test1, test2);
        MenuInMenuDefinition test2a = newDefinitionWith("test2", "COM5", 9600);
        collection.replaceDefinition(test2, test2a);
        assertThat(collection.getAllDefinitions()).containsExactlyInAnyOrder(test1, test2a);
        collection.removeDefinition(test2a);
        assertThat(collection.getAllDefinitions()).containsExactlyInAnyOrder(test1);
    }

    @Test
    void testObjectStructure() {
        MenuInMenuDefinition test2a = newDefinitionWith("test2", "COM5", 9600);
        MenuInMenuDefinition test2 = newDefinitionWith("test2", "COM1", 9600);
        MenuInMenuDefinition test2Copy = newDefinitionWith("test2", "COM1", 9600);
        assertEquals(-1, test2a.getSubMenuId());
        assertEquals(ConnectionType.SERIAL, test2a.getConnectionType());
        assertEquals("test2", test2a.getVariableName());
        assertEquals(9600, test2a.getPortOrBaud());
        assertEquals("COM5", test2a.getPortOrIpAddress());
        assertEquals(200, test2a.getMaximumRange());
        assertEquals(1000, test2a.getIdOffset());
        assertEquals(MenuInMenu.ReplicationMode.REPLICATE_ADD_STATUS_ITEM, test2a.getReplicationMode());
        assertEquals("COM5@9600 offset 1000", test2a.printableConnection());
        assertEquals(test2, test2Copy);
        assertNotEquals(test2a, test2);
        assertEquals("MenuInMenuDefinition[variableName=test2, portOrIpAddress=COM1, portOrBaud=9600, connectionType=SERIAL, replicationMode=REPLICATE_ADD_STATUS_ITEM, subMenuId=-1, idOffset=1000, maximumRange=200]", test2.toString());
    }

    private MenuInMenuDefinition newDefinitionWith(String name, String port, int baud) {
        return new MenuInMenuDefinition(name, port, baud, ConnectionType.SERIAL,
                MenuInMenu.ReplicationMode.REPLICATE_ADD_STATUS_ITEM, -1, 1000, 200);
    }
}