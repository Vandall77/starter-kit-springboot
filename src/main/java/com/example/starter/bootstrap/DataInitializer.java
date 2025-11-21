package com.example.starter.bootstrap;

import com.example.starter.feature.item.entity.Item;
import com.example.starter.feature.item.repository.ItemRepository;
import com.example.starter.rbac.mapping.entity.RolePermission;
import com.example.starter.rbac.mapping.entity.UserRole;
import com.example.starter.rbac.mapping.repository.RolePermissionRepository;
import com.example.starter.rbac.mapping.repository.UserRoleRepository;
import com.example.starter.rbac.permission.entity.Permission;
import com.example.starter.rbac.permission.repository.PermissionRepository;
import com.example.starter.rbac.role.entity.Role;
import com.example.starter.rbac.role.repository.RoleRepository;
import com.example.starter.rbac.user.entity.UserAccount;
import com.example.starter.rbac.user.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final ItemRepository itemRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";

    // USER permissions
    private static final String[] USER_PERMISSIONS = {
            "USER_CREATE",
            "USER_READ",
            "USER_UPDATE",
            "USER_DELETE",
            "USER_HARD_DELETE"
    };

    // ROLE permissions
    private static final String[] ROLE_PERMISSIONS = {
            "ROLE_CREATE",
            "ROLE_READ",
            "ROLE_UPDATE",
            "ROLE_DELETE",
            "ROLE_HARD_DELETE"
    };

    // PERMISSION permissions
    private static final String[] PERMISSION_PERMISSIONS = {
            "PERMISSION_CREATE",
            "PERMISSION_READ",
            "PERMISSION_UPDATE",
            "PERMISSION_DELETE",
            "PERMISSION_HARD_DELETE"
    };

    // ITEM permissions
    private static final String[] ITEM_PERMISSIONS = {
            "ITEM_CREATE",
            "ITEM_READ",
            "ITEM_UPDATE",
            "ITEM_DELETE",
            "ITEM_HARD_DELETE"
    };

    // AUDIT LOG permissions (biasanya read-only)
    private static final String[] AUDIT_PERMISSIONS = {
            "AUDIT_LOG_READ"
    };

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        Map<String, Permission> permissionMap = seedPermissions();
        seedRolePermissions(permissionMap);
        seedAdminUser();
        seedSampleItems();
    }

    private void seedRoles() {
        Optional<Role> adminOpt = roleRepository.findByCode(ROLE_ADMIN);
        if (!adminOpt.isPresent()) {
            Role admin = new Role();
            admin.setCode(ROLE_ADMIN);
            admin.setName("Administrator");
            admin.setDescription("Full access administrator");
            roleRepository.save(admin);
        }

        Optional<Role> userOpt = roleRepository.findByCode(ROLE_USER);
        if (!userOpt.isPresent()) {
            Role user = new Role();
            user.setCode(ROLE_USER);
            user.setName("Standard User");
            user.setDescription("Read-only / limited access user");
            roleRepository.save(user);
        }
    }

    /**
     * Seed semua Permission dan kembalikan map code -> entity (sudah tersimpan).
     */
    private Map<String, Permission> seedPermissions() {
        List<String> codes = new ArrayList<String>();
        codes.addAll(Arrays.asList(USER_PERMISSIONS));
        codes.addAll(Arrays.asList(ROLE_PERMISSIONS));
        codes.addAll(Arrays.asList(PERMISSION_PERMISSIONS));
        codes.addAll(Arrays.asList(ITEM_PERMISSIONS));
        codes.addAll(Arrays.asList(AUDIT_PERMISSIONS));

        Map<String, Permission> result = new HashMap<String, Permission>();

        for (String code : codes) {
            Optional<Permission> existing = permissionRepository.findByCode(code);
            Permission perm;
            if (existing.isPresent()) {
                perm = existing.get();
            } else {
                perm = new Permission();
                perm.setCode(code);
                perm.setDescription(code.replace("_", " "));
                permissionRepository.save(perm);
            }
            result.put(code, perm);
        }

        return result;
    }

    /**
     * ADMIN → semua permission
     * USER  → hanya permission dengan suffix "_READ"
     */
    private void seedRolePermissions(Map<String, Permission> permissionMap) {
        Optional<Role> adminOpt = roleRepository.findByCode(ROLE_ADMIN);
        Optional<Role> userOpt = roleRepository.findByCode(ROLE_USER);

        if (!adminOpt.isPresent() || !userOpt.isPresent()) {
            return; // safety guard
        }

        Role admin = adminOpt.get();
        Role user = userOpt.get();

        // Assign semua permission ke ADMIN
        for (Permission perm : permissionMap.values()) {
            RolePermission rp = new RolePermission();
            rp.setRole(admin);
            rp.setPermission(perm);
            rolePermissionRepository.save(rp);
        }

        // Assign hanya *_READ ke USER
        for (Map.Entry<String, Permission> entry : permissionMap.entrySet()) {
            String code = entry.getKey();
            if (code.endsWith("_READ")) {
                RolePermission rp = new RolePermission();
                rp.setRole(user);
                rp.setPermission(entry.getValue());
                rolePermissionRepository.save(rp);
            }
        }
    }

    private void seedAdminUser() {
        // 🔧 FIX: pakai existsByUsername, bukan findByUsername
        if (userAccountRepository.existsByUsername("admin")) {
            return;
        }

        UserAccount admin = new UserAccount();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setEnabled(true);
        admin.setLocked(false);

        userAccountRepository.save(admin);

        Optional<Role> adminRoleOpt = roleRepository.findByCode(ROLE_ADMIN);
        if (!adminRoleOpt.isPresent()) {
            return;
        }

        UserRole userRole = new UserRole();
        userRole.setUser(admin);
        userRole.setRole(adminRoleOpt.get());
        userRoleRepository.save(userRole);
    }

    private void seedSampleItems() {
        // hanya contoh data dummy, tidak wajib
        if (itemRepository.count() > 0) {
            return;
        }

        Item item1 = new Item();
        item1.setCode("ITEM-001");
        item1.setName("Sample Item 1");
        item1.setDescription("Sample item 1 description");
        item1.setStockQty(10);

        Item item2 = new Item();
        item2.setCode("ITEM-002");
        item2.setName("Sample Item 2");
        item2.setDescription("Sample item 2 description");
        item2.setStockQty(20);

        Item item3 = new Item();
        item3.setCode("ITEM-003");
        item3.setName("Sample Item 3");
        item3.setDescription("Sample item 3 description");
        item3.setStockQty(30);

        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);
    }
}
