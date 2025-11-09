package com.school.lending.service;

import com.school.lending.model.UserAccount;
import com.school.lending.model.UserRole;
import com.school.lending.repo.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.school.lending.security.JwtUtil;

@Service
public class AuthService {

    /**
     * Lightweight authentication service used by controllers.
     *
     * <p>This service provides username/password checks, student user
     * creation and an in-memory token issuance mechanism used for demo/test
     * purposes. Tokens are stored in-process and are not persisted.
     */

    private final UserAccountRepository userRepository;
    private final Map<String, Long> tokenBank = new ConcurrentHashMap<>();
    private final Map<Long, String> reverseLookup = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserAccountRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Authenticate a user by username and password. This method performs a
     * simple equality check against the stored password.
     *
     * @param username username to authenticate
     * @param password plaintext password to verify
     * @return Optional containing the UserAccount on success, otherwise empty
     */
    public Optional<UserAccount> login(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return Optional.empty();
        }
    return userRepository.findByUsername(username)
        .filter(u -> passwordEncoder.matches(password, u.getPassword()));
    }

    /**
     * Create a new student user if the username is not already taken.
     *
     * @param username desired username
     * @param password desired password
     * @param nameTag  display name or null
     * @return Optional containing the created UserAccount, or empty on conflict
     */
    public Optional<UserAccount> createStudentUser(String username, String password, String nameTag) {
        if (userRepository.findByUsername(username).isPresent()) {
            return Optional.empty();
        }
    // store passwords securely using BCrypt
    String encoded = passwordEncoder.encode(password == null ? "" : password);
    UserAccount fresh = new UserAccount(username, encoded, UserRole.STUDENT,
        StringUtils.hasText(nameTag) ? nameTag : username);
        return Optional.of(userRepository.save(fresh));
    }

    /**
     * Issue (or return existing) an in-memory token for the supplied account.
     *
     * @param acct the user account to issue a token for
     * @return a token string, or null when acct is invalid
     */
    public String issueToken(UserAccount acct) {
        if (acct == null || acct.getId() == null) {
            return null;
        }
        // generate signed JWT
        return jwtUtil.generateToken(acct);
    }

    /**
     * Resolve a user account from an issued token.
     *
     * @param token token previously issued by {@link #issueToken(UserAccount)}
     * @return Optional containing the UserAccount if token is valid
     */
    public Optional<UserAccount> findUserByToken(String token) {
        if (!StringUtils.hasText(token)) {
            return Optional.empty();
        }
        // Validate and parse JWT token
        if (!jwtUtil.validateToken(token)) {
            return Optional.empty();
        }
        String username = jwtUtil.getUsername(token);
        return userRepository.findByUsername(username);
    }
}
