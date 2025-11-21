package com.example.starter.rbac.mapping.repository;

import com.example.starter.rbac.mapping.entity.RolePermission;
import com.example.starter.rbac.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {

    List<RolePermission> findByRoleIn(Set<Role> roles);
}
