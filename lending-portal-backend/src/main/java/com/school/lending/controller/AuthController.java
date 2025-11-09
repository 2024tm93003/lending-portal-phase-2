package com.school.lending.controller;

import com.school.lending.dto.LoginRequest;
import com.school.lending.dto.LoginResponse;
import com.school.lending.dto.SignupRequest;
import com.school.lending.model.UserAccount;
import com.school.lending.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * REST endpoints for authentication: login, signup and retrieving current user.
     *
     * <p>Uses {@link com.school.lending.service.AuthService} to authenticate
     * users, create student users and issue tokens.
     */

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    /**
     * Authenticate a user with username and password.
     *
     * @param payload login request containing username and password
     * @return LoginResponse containing issued token and user info
     * @throws org.springframework.web.server.ResponseStatusException with
     *         HttpStatus.UNAUTHORIZED when credentials are invalid
     */
    public LoginResponse login(@RequestBody LoginRequest payload) {
        Optional<UserAccount> user = authService.login(
                payload == null ? null : payload.username,
                payload == null ? null : payload.password);
        UserAccount account = user.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "bad credentials"));
        String token = authService.issueToken(account);
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "token issue failed");
        }
        LoginResponse resp = new LoginResponse();
        resp.token = token;
        resp.username = account.getUsername();
        resp.displayName = account.getDisplayName();
        resp.role = account.getRole().name();
        return resp;
    }

    @PostMapping("/signup")
    /**
     * Create a new student account and return the resulting login response.
     *
     * @param request signup details (username, password, displayName)
     * @return LoginResponse for the newly created user
     * @throws org.springframework.web.server.ResponseStatusException with
     *         HttpStatus.CONFLICT when username already exists
     */
    public LoginResponse signup(@RequestBody SignupRequest request) {
        Optional<UserAccount> maybeUser = authService.createStudentUser(
                request == null ? null : request.username,
                request == null ? null : request.password,
                request == null ? null : request.displayName);
        UserAccount created = maybeUser.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.CONFLICT, "username already in play"));
        LoginResponse resp = new LoginResponse();
        resp.username = created.getUsername();
        resp.displayName = created.getDisplayName();
        resp.role = created.getRole().name();
        resp.token = authService.issueToken(created);
        return resp;
    }

    @GetMapping("/me")
    /**
     * Return information about the currently authenticated user based on token.
     *
     * @param token the X-Auth-Token header value
     * @return LoginResponse for the user represented by the token
     * @throws org.springframework.web.server.ResponseStatusException with
     *         HttpStatus.UNAUTHORIZED when token is missing or invalid
     */
    public LoginResponse whoAmI(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        UserAccount found = authService.findUserByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid or missing token"));
        LoginResponse resp = new LoginResponse();
        resp.username = found.getUsername();
        resp.displayName = found.getDisplayName();
        resp.role = found.getRole().name();
        resp.token = token;
        return resp;
    }
}
