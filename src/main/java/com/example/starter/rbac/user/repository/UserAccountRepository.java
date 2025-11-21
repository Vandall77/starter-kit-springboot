package com.example.starter.rbac.user.repository;

import com.example.starter.rbac.user.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

    Optional<UserAccount> findByUsernameAndDeletedAtIsNull(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // --- HARD DELETE (bypass @SQLDelete) ---
    @Modifying
    @Query(
            value = "DELETE FROM user_accounts WHERE id = :id",
            nativeQuery = true
    )
    void hardDeleteById(@Param("id") UUID id);
}
