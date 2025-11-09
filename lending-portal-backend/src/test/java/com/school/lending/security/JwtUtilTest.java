package com.school.lending.security;

import com.school.lending.model.UserAccount;
import com.school.lending.model.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    @Test
    void token_generation_and_validation() {
        JwtUtil util = new JwtUtil("tests-secret-123", 3600000);
        UserAccount u = new UserAccount("joe","p", UserRole.STUDENT, "Joe");
        u.setId(42L);

        String token = util.generateToken(u);
        assertNotNull(token);
        assertTrue(util.validateToken(token));
        assertEquals("joe", util.getUsername(token));
        assertEquals(42L, util.getUserId(token).longValue());
    }
}
