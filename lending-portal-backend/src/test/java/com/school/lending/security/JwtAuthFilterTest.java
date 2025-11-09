package com.school.lending.security;

import com.school.lending.model.UserAccount;
import com.school.lending.model.UserRole;
import com.school.lending.repo.UserAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_setsAuthentication_whenTokenValid() throws Exception {
        JwtUtil jwt = mock(JwtUtil.class);
        UserAccountRepository repo = mock(UserAccountRepository.class);
        JwtAuthFilter filter = new JwtAuthFilter(jwt, repo);

        when(jwt.validateToken("T1")).thenReturn(true);
        when(jwt.getUsername("T1")).thenReturn("alice");

        UserAccount u = new UserAccount("alice","x", UserRole.STAFF, "Alice");
        when(repo.findByUsername("alice")).thenReturn(Optional.of(u));

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Auth-Token", "T1");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(req, resp, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("alice", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(chain).doFilter(req, resp);
    }
}
