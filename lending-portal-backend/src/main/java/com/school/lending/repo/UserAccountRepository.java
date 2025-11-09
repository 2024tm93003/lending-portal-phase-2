package com.school.lending.repo;

import com.school.lending.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for {@link UserAccount} entities.
 */
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    /**
     * Find a user account by username.
     *
     * @param username the unique username
     * @return optional UserAccount
     */
    Optional<UserAccount> findByUsername(String username);
}
