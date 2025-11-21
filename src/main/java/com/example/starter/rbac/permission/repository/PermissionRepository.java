package com.example.starter.rbac.permission.repository;

import com.example.starter.rbac.permission.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByCode(String code);

    @Modifying
    @Query(
            value = "DELETE FROM permissions WHERE id = :id",
            nativeQuery = true
    )
    void hardDeleteById(@Param("id") UUID id);
}
