package com.thecoderscorner.menu.domain.state;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ListResponseTest {

    @Test
    void testListResponse() {
        var lr = new ListResponse(100, ListResponse.ResponseType.SELECT_ITEM);
        assertEquals(ListResponse.ResponseType.SELECT_ITEM, lr.getResponseType());
        assertEquals(100, lr.getRow());

        var lr2 = ListResponse.fromString("202:1").orElseThrow();
        assertEquals(ListResponse.ResponseType.INVOKE_ITEM, lr2.getResponseType());
        assertEquals(202, lr2.getRow());

        assertTrue(ListResponse.fromString("sldkfghkjd:2").isEmpty());
        assertTrue(ListResponse.fromString("sldkfghkjd").isEmpty());
    }
}