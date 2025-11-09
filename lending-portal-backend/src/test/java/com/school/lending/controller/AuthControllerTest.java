package com.school.lending.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.lending.dto.LoginRequest;
import com.school.lending.dto.SignupRequest;
import com.school.lending.model.UserAccount;
import com.school.lending.model.UserRole;
import com.school.lending.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    AuthService authService;

    @Autowired
    ObjectMapper mapper;

    @Test
    void login_success() throws Exception {
        UserAccount u = new UserAccount("alice", "pw", UserRole.STUDENT, "Alice");
        u.setId(1L);
        when(authService.login("alice", "secret")).thenReturn(Optional.of(u));
        when(authService.issueToken(u)).thenReturn("tok-abc");

        LoginRequest req = new LoginRequest();
        req.username = "alice";
        req.password = "secret";

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("tok-abc"))
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void login_badCredentials_returns401() throws Exception {
        when(authService.login(any(), any())).thenReturn(Optional.empty());

        LoginRequest req = new LoginRequest();
        req.username = "noone";
        req.password = "bad";

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void signup_conflict_returns409() throws Exception {
        when(authService.createStudentUser("bob", "pw123", "Bob")).thenReturn(Optional.empty());

        SignupRequest req = new SignupRequest();
        req.username = "bob";
        req.password = "pw123";
        req.displayName = "Bob";

        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void whoAmI_success() throws Exception {
        UserAccount u = new UserAccount("carol", "x", UserRole.STAFF, "Carol");
        u.setId(2L);
        when(authService.findUserByToken("t1")).thenReturn(Optional.of(u));

        mvc.perform(get("/api/auth/me").header("X-Auth-Token", "t1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("carol"))
                .andExpect(jsonPath("$.role").value("STAFF"));
    }
}
