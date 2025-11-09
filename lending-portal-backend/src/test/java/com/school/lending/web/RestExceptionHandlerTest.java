package com.school.lending.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.http.HttpStatus;

class RestExceptionHandlerTest {

    RestExceptionHandler handler = new RestExceptionHandler();

    @Test
    void handleResponseStatus_returnsBodyWithMessage() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "not found");
        ResponseEntity<Object> resp = handler.handleResponseStatus(ex);
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertTrue(resp.getBody() instanceof java.util.Map);
        var body = (java.util.Map<?,?>) resp.getBody();
        assertEquals(404, body.get("status"));
        assertEquals("not found", body.get("message"));
    }

    @Test
    void handleAll_returnsInternalError() {
        Exception ex = new RuntimeException("boom");
        ResponseEntity<Object> resp = handler.handleAll(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        var body = (java.util.Map<?,?>) resp.getBody();
        assertEquals(500, body.get("status"));
        assertEquals("internal error", body.get("message"));
    }
}
