package com.example.starter.rbac.role.repository;

import com.example.starter.rbac.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByCode(String code);

    @Modifying
    @Query(
            value = "DELETE FROM roles WHERE id = :id",
            nativeQuery = true
    )
    void hardDeleteById(@Param("id") UUID id);
}
