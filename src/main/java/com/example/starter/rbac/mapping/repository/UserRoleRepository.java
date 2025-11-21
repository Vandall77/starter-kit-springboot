package com.example.starter.rbac.mapping.repository;

import com.example.starter.rbac.mapping.entity.UserRole;
import com.example.starter.rbac.user.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    List<UserRole> findByUser(UserAccount user);
}
