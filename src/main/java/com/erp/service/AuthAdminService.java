package com.erp.service;

import com.erp.model.audit.AuditLog;
import com.erp.model.auth.Role;
import com.erp.model.auth.User;
import com.erp.model.hr.Department;
import com.erp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthAdminService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final DepartmentRepository departmentRepo;
    private final AuditLogRepository auditRepo;

    // In-memory session store (token -> User ID)
    private final Map<String, Long> activeSessions = new ConcurrentHashMap<>();

    public AuthAdminService(UserRepository userRepo,
                            RoleRepository roleRepo,
                            DepartmentRepository departmentRepo,
                            AuditLogRepository auditRepo) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.departmentRepo = departmentRepo;
        this.auditRepo = auditRepo;
    }

    @Transactional
    public Map<String, Object> login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }

        User user = userRepo.findByUsername(username.trim())
                .orElseThrow(() -> {
                    auditRepo.save(new AuditLog(AuditLog.ModuleName.AUTH, "LOGIN_FAILED",
                            username, "ANONYMOUS", "Failed login attempt: Username '" + username + "' not found"));
                    return new IllegalArgumentException("Invalid username or password.");
                });

        if (user.getStatus() == User.UserStatus.SUSPENDED) {
            auditRepo.save(new AuditLog(AuditLog.ModuleName.AUTH, "LOGIN_BLOCKED",
                    user.getUsername(), user.getFullName(), "Account access attempted on SUSPENDED user account"));
            throw new IllegalStateException("Your account has been suspended by the System Administrator.");
        }

        // Direct password match (for demo convenience, e.g. admin123, hr123)
        if (!password.trim().equals(user.getPassword())) {
            auditRepo.save(new AuditLog(AuditLog.ModuleName.AUTH, "LOGIN_FAILED",
                    user.getUsername(), "ANONYMOUS", "Invalid credentials submitted for user: " + user.getUsername()));
            throw new IllegalArgumentException("Invalid username or password.");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepo.save(user);

        String token = "omni_sec_" + UUID.randomUUID().toString().replace("-", "");
        activeSessions.put(token, user.getId());

        auditRepo.save(new AuditLog(AuditLog.ModuleName.AUTH, "LOGIN_SUCCESS",
                user.getUsername(), user.getFullName(),
                "User successfully authenticated as " + user.getRole().getName() + " (" + user.getRole().getRoleCode() + ")"));

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", formatUserDto(user));
        return response;
    }

    public void logout(String token) {
        if (token != null && activeSessions.containsKey(token)) {
            Long userId = activeSessions.remove(token);
            userRepo.findById(userId).ifPresent(user ->
                    auditRepo.save(new AuditLog(AuditLog.ModuleName.AUTH, "LOGOUT",
                            user.getUsername(), user.getFullName(), "User logged out of PHANTOM-ERP session"))
            );
        }
    }

    public Map<String, Object> getCurrentUser(String token) {
        if (token == null || !activeSessions.containsKey(token)) {
            return null;
        }
        Long userId = activeSessions.get(token);
        return userRepo.findById(userId).map(this::formatUserDto).orElse(null);
    }

    public List<Map<String, Object>> getDemoAccounts() {
        List<Map<String, Object>> list = new ArrayList<>();
        userRepo.findAll().forEach(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("username", u.getUsername());
            m.put("password", u.getPassword());
            m.put("fullName", u.getFullName());
            m.put("avatar", u.getAvatar());
            m.put("jobTitle", u.getJobTitle());
            m.put("roleCode", u.getRole().getRoleCode());
            m.put("roleName", u.getRole().getName());
            m.put("department", u.getDepartment() != null ? u.getDepartment().getName() : "Global Headquarters");
            list.add(m);
        });
        return list;
    }

    // ==========================================
    // USER ADMINISTRATION
    // ==========================================
    public List<User> getAllUsers() {
        return userRepo.findAllByOrderByCreatedAtDesc();
    }

    public User getUserById(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    @Transactional
    public User createUser(Map<String, Object> data) {
        String username = (String) data.get("username");
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (userRepo.existsByUsername(username.trim())) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }

        String password = data.get("password") != null ? (String) data.get("password") : "PhantomERP2026!";
        String fullName = (String) data.getOrDefault("fullName", username);
        String email = (String) data.getOrDefault("email", username + "@phantom-erp.cloud");
        String avatar = (String) data.getOrDefault("avatar", "👤");
        String jobTitle = (String) data.getOrDefault("jobTitle", "Enterprise Associate");

        Role role = null;
        if (data.containsKey("roleId") && data.get("roleId") != null) {
            Long roleId = ((Number) data.get("roleId")).longValue();
            role = roleRepo.findById(roleId).orElse(null);
        } else if (data.containsKey("roleCode") && data.get("roleCode") != null) {
            role = roleRepo.findByRoleCode((String) data.get("roleCode")).orElse(null);
        }
        if (role == null) {
            role = roleRepo.findByRoleCode("ROLE_EMPLOYEE")
                    .orElseGet(() -> roleRepo.findAll().stream().findFirst().orElseThrow());
        }

        Department dept = null;
        if (data.containsKey("departmentId") && data.get("departmentId") != null) {
            Long deptId = ((Number) data.get("departmentId")).longValue();
            dept = departmentRepo.findById(deptId).orElse(null);
        }

        User user = new User(username.trim(), password, fullName, email, avatar, jobTitle, role, dept, User.UserStatus.ACTIVE);
        User saved = userRepo.save(user);

        auditRepo.save(new AuditLog(AuditLog.ModuleName.ADMIN, "USER_CREATED",
                saved.getUsername(), "Administrator",
                "New user account created: " + saved.getFullName() + " with role " + saved.getRole().getName()));

        return saved;
    }

    @Transactional
    public User updateUser(Long id, Map<String, Object> data) {
        User user = getUserById(id);

        if (data.containsKey("fullName")) user.setFullName((String) data.get("fullName"));
        if (data.containsKey("email")) user.setEmail((String) data.get("email"));
        if (data.containsKey("jobTitle")) user.setJobTitle((String) data.get("jobTitle"));
        if (data.containsKey("avatar")) user.setAvatar((String) data.get("avatar"));

        if (data.containsKey("password") && data.get("password") != null && !((String) data.get("password")).isBlank()) {
            user.setPassword((String) data.get("password"));
        }

        if (data.containsKey("roleId") && data.get("roleId") != null) {
            Long roleId = ((Number) data.get("roleId")).longValue();
            roleRepo.findById(roleId).ifPresent(user::setRole);
        }

        if (data.containsKey("departmentId")) {
            if (data.get("departmentId") != null) {
                Long deptId = ((Number) data.get("departmentId")).longValue();
                departmentRepo.findById(deptId).ifPresent(user::setDepartment);
            } else {
                user.setDepartment(null);
            }
        }

        if (data.containsKey("status") && data.get("status") != null) {
            user.setStatus(User.UserStatus.valueOf((String) data.get("status")));
        }

        User saved = userRepo.save(user);

        auditRepo.save(new AuditLog(AuditLog.ModuleName.ADMIN, "USER_UPDATED",
                saved.getUsername(), "Administrator",
                "User account '" + saved.getUsername() + "' modified. Role: " + saved.getRole().getName() + ", Status: " + saved.getStatus()));

        return saved;
    }

    @Transactional
    public User toggleUserStatus(Long id) {
        User user = getUserById(id);
        if ("admin".equalsIgnoreCase(user.getUsername())) {
            throw new IllegalStateException("Primary System Administrator ('admin') account status cannot be toggled.");
        }

        User.UserStatus next = user.getStatus() == User.UserStatus.ACTIVE ? User.UserStatus.SUSPENDED : User.UserStatus.ACTIVE;
        user.setStatus(next);
        User saved = userRepo.save(user);

        auditRepo.save(new AuditLog(AuditLog.ModuleName.ADMIN, "USER_STATUS_TOGGLED",
                saved.getUsername(), "Administrator",
                "User '" + saved.getUsername() + "' status changed to " + next));

        return saved;
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        if ("admin".equalsIgnoreCase(user.getUsername())) {
            throw new IllegalStateException("Primary System Administrator ('admin') account cannot be deleted.");
        }
        userRepo.delete(user);
        auditRepo.save(new AuditLog(AuditLog.ModuleName.ADMIN, "USER_DELETED",
                user.getUsername(), "Administrator",
                "User account '" + user.getUsername() + "' permanently removed from system."));
    }

    // ==========================================
    // ROLES & PERMISSIONS ADMINISTRATION
    // ==========================================
    public List<Role> getAllRoles() {
        return roleRepo.findAll();
    }

    public Role getRoleById(Long id) {
        return roleRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + id));
    }

    @Transactional
    public Role createRole(Role role) {
        if (roleRepo.existsByRoleCode(role.getRoleCode())) {
            throw new IllegalArgumentException("Role with code '" + role.getRoleCode() + "' already exists.");
        }
        Role saved = roleRepo.save(role);
        auditRepo.save(new AuditLog(AuditLog.ModuleName.ADMIN, "ROLE_CREATED",
                saved.getRoleCode(), "Administrator",
                "New role created: " + saved.getName() + " with " + saved.getPermissions().size() + " permissions"));
        return saved;
    }

    @Transactional
    public Role updateRole(Long id, Role updated) {
        Role role = getRoleById(id);
        role.setName(updated.getName());
        role.setDescription(updated.getDescription());
        if (updated.getPermissions() != null) {
            role.setPermissions(new HashSet<>(updated.getPermissions()));
        }
        Role saved = roleRepo.save(role);
        auditRepo.save(new AuditLog(AuditLog.ModuleName.ADMIN, "ROLE_UPDATED",
                saved.getRoleCode(), "Administrator",
                "Role '" + saved.getName() + "' updated with permissions: " + saved.getPermissions()));
        return saved;
    }

    // ==========================================
    // SECURITY & SYSTEM STATS
    // ==========================================
    public Map<String, Object> getSecurityOverview() {
        List<User> users = userRepo.findAll();
        long activeCount = users.stream().filter(u -> u.getStatus() == User.UserStatus.ACTIVE).count();
        long suspendedCount = users.stream().filter(u -> u.getStatus() == User.UserStatus.SUSPENDED).count();
        long adminCount = users.stream().filter(u -> u.getRole().getRoleCode().contains("ADMIN")).count();
        List<Role> roles = roleRepo.findAll();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", users.size());
        stats.put("activeUsers", activeCount);
        stats.put("suspendedUsers", suspendedCount);
        stats.put("privilegedAdmins", adminCount);
        stats.put("totalRoles", roles.size());
        stats.put("securityScore", 98);
        stats.put("activeSessionCount", Math.max(activeSessions.size(), 1));
        stats.put("auditLogsCount", auditRepo.count());
        return stats;
    }

    public List<AuditLog> getAdminAuditLogs() {
        return auditRepo.findTop15ByOrderByTimestampDesc();
    }

    private Map<String, Object> formatUserDto(User user) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", user.getId());
        dto.put("username", user.getUsername());
        dto.put("fullName", user.getFullName());
        dto.put("email", user.getEmail());
        dto.put("avatar", user.getAvatar());
        dto.put("jobTitle", user.getJobTitle());
        dto.put("status", user.getStatus().name());
        dto.put("lastLogin", user.getLastLogin());
        dto.put("createdAt", user.getCreatedAt());

        if (user.getRole() != null) {
            dto.put("roleId", user.getRole().getId());
            dto.put("roleCode", user.getRole().getRoleCode());
            dto.put("roleName", user.getRole().getName());
            dto.put("permissions", user.getRole().getPermissions());
        }

        if (user.getDepartment() != null) {
            dto.put("departmentId", user.getDepartment().getId());
            dto.put("departmentName", user.getDepartment().getName());
        }

        return dto;
    }
}
