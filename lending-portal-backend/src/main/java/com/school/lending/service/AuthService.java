package com.school.lending.service;

import com.school.lending.model.UserAccount;
import com.school.lending.model.UserRole;
import com.school.lending.repo.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final UserAccountRepository userRepository;
    private final Map<String, Long> tokenBank = new ConcurrentHashMap<>();
    private final Map<Long, String> reverseLookup = new ConcurrentHashMap<>();

    public AuthService(UserAccountRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<UserAccount> login(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return Optional.empty();
        }
        return userRepository.findByUsername(username)
                .filter(u -> password.equals(u.getPassword()));
    }

    public Optional<UserAccount> createStudentUser(String username, String password, String nameTag) {
        if (userRepository.findByUsername(username).isPresent()) {
            return Optional.empty();
        }
        UserAccount fresh = new UserAccount(username, password, UserRole.STUDENT,
                StringUtils.hasText(nameTag) ? nameTag : username);
        return Optional.of(userRepository.save(fresh));
    }

    public String issueToken(UserAccount acct) {
        if (acct == null || acct.getId() == null) {
            return null;
        }
        String existing = reverseLookup.get(acct.getId());
        if (existing != null) {
            return existing;
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenBank.put(token, acct.getId());
        reverseLookup.put(acct.getId(), token);
        return token;
    }

    public Optional<UserAccount> findUserByToken(String token) {
        if (!StringUtils.hasText(token)) {
            return Optional.empty();
        }
        Long userId = tokenBank.get(token);
        if (userId == null) {
            return Optional.empty();
        }
        return userRepository.findById(userId);
    }
}
