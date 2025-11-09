package com.school.lending.service;

import com.school.lending.model.UserAccount;
import com.school.lending.model.UserRole;
import com.school.lending.repo.UserAccountRepository;
import com.school.lending.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserAccountRepository repo;

    @Mock
    PasswordEncoder encoder;

    @Mock
    JwtUtil jwtUtil;

    @InjectMocks
    AuthService authService;

    UserAccount u;

    @BeforeEach
    void setup() {
        u = new UserAccount("k","enc", UserRole.STUDENT, "K");
        u.setId(7L);
    }

    @Test
    void login_success() {
        when(repo.findByUsername("k")).thenReturn(Optional.of(u));
        when(encoder.matches("plain", "enc")).thenReturn(true);

        Optional<UserAccount> out = authService.login("k", "plain");
        assertTrue(out.isPresent());
        assertEquals("k", out.get().getUsername());
    }

    @Test
    void login_missing_returnsEmpty() {
        assertTrue(authService.login(null, null).isEmpty());
    }

    @Test
    void createStudent_conflict() {
        when(repo.findByUsername("x")).thenReturn(Optional.of(u));
        assertTrue(authService.createStudentUser("x", "p", "X").isEmpty());
    }

    @Test
    void issueToken_and_findUserByToken() {
        when(jwtUtil.generateToken(u)).thenReturn("T1");
        when(jwtUtil.validateToken("T1")).thenReturn(true);
        when(jwtUtil.getUsername("T1")).thenReturn("k");
        when(repo.findByUsername("k")).thenReturn(Optional.of(u));

        String t = authService.issueToken(u);
        assertEquals("T1", t);

        Optional<UserAccount> found = authService.findUserByToken("T1");
        assertTrue(found.isPresent());
        assertEquals(u.getUsername(), found.get().getUsername());
    }
}
