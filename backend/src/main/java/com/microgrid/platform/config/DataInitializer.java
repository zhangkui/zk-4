package com.microgrid.platform.config;

import com.microgrid.platform.entity.SysPermission;
import com.microgrid.platform.entity.SysRole;
import com.microgrid.platform.entity.SysRolePermission;
import com.microgrid.platform.entity.SysUser;
import com.microgrid.platform.entity.SysUserRole;
import com.microgrid.platform.repository.SysPermissionRepository;
import com.microgrid.platform.repository.SysRolePermissionRepository;
import com.microgrid.platform.repository.SysRoleRepository;
import com.microgrid.platform.repository.SysUserRepository;
import com.microgrid.platform.repository.SysUserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final SysPermissionRepository permissionRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final SysRolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PASSWORD = "admin123";
    private static final List<String> DEFAULT_USERS = Arrays.asList("admin", "operator", "viewer");

    private record PermDef(String code, String name, Integer type, String path, String icon, String apiMethod, String apiPath) {}

    @Override
    public void run(String... args) {
        log.info("开始初始化系统数据...");

        initPermissions();
        initRoles();
        initRolePermissions();
        initUserPasswords();
        initUserRoles();

        log.info("系统数据初始化完成");
    }

    private void initPermissions() {
        List<PermDef> permDefs = new ArrayList<>();

        permDefs.add(new PermDef("admin-home", "管理中心首页", 1, "/admin-home", "HomeFilled", null, null));

        permDefs.add(new PermDef("user", "用户管理", 1, "/user", "User", null, null));
        permDefs.add(new PermDef("role", "角色管理", 1, "/role", "UserFilled", null, null));
        permDefs.add(new PermDef("permission", "权限管理", 1, "/permission", "Lock", null, null));
        permDefs.add(new PermDef("log", "日志审计", 1, "/log", "Document", null, null));
        permDefs.add(new PermDef("profile", "个人中心", 1, "/profile", "Avatar", null, null));

        permDefs.add(new PermDef("park", "园区管理", 1, "/park", "OfficeBuilding", null, null));
        permDefs.add(new PermDef("device", "设备台账", 1, "/device", "Cpu", null, null));
        permDefs.add(new PermDef("device-detail", "设备详情", 1, null, "Cpu", null, null));

        permDefs.add(new PermDef("dashboard", "监测大屏", 1, "/dashboard", "Monitor", null, null));
        permDefs.add(new PermDef("history", "历史数据", 1, "/history", "Histogram", null, null));
        permDefs.add(new PermDef("simulator", "数据模拟器", 1, "/simulator", "Setting", null, null));
        permDefs.add(new PermDef("alert-rule", "告警规则", 1, "/alert-rule", "Warning", null, null));
        permDefs.add(new PermDef("alert-center", "告警中心", 1, "/alert-center", "Bell", null, null));

        int count = 0;
        Map<String, SysPermission> existingMap = new HashMap<>();
        for (SysPermission p : permissionRepository.findAll()) {
            existingMap.put(p.getPermissionCode(), p);
        }

        int sortOrder = 0;
        for (PermDef def : permDefs) {
            SysPermission perm = existingMap.get(def.code());
            if (perm == null) {
                perm = new SysPermission();
                perm.setPermissionCode(def.code());
                perm.setPermissionName(def.name());
                perm.setPermissionType(def.type());
                perm.setParentId(0L);
                perm.setPath(def.path());
                perm.setIcon(def.icon());
                perm.setApiMethod(def.apiMethod());
                perm.setApiPath(def.apiPath());
            }
            perm.setSortOrder(sortOrder++);
            perm.setPermissionName(def.name());
            perm.setPath(def.path());
            perm.setIcon(def.icon());
            permissionRepository.save(perm);
            count++;
        }

        log.info("初始化权限完成，共处理 {} 条", count);
    }

    private void initRoles() {
        List<SysRole> roleDefs = new ArrayList<>();

        SysRole admin = new SysRole();
        admin.setRoleCode("ADMIN");
        admin.setRoleName("系统管理员");
        admin.setDescription("拥有所有系统权限");
        admin.setStatus(1);
        roleDefs.add(admin);

        SysRole operator = new SysRole();
        operator.setRoleCode("OPERATOR");
        operator.setRoleName("运营人员");
        operator.setDescription("运营操作权限");
        operator.setStatus(1);
        roleDefs.add(operator);

        SysRole viewer = new SysRole();
        viewer.setRoleCode("VIEWER");
        viewer.setRoleName("只读用户");
        viewer.setDescription("仅查看权限");
        viewer.setStatus(1);
        roleDefs.add(viewer);

        for (SysRole def : roleDefs) {
            SysRole existing = roleRepository.findByRoleCode(def.getRoleCode()).orElse(null);
            if (existing == null) {
                roleRepository.save(def);
                log.info("创建角色: {}", def.getRoleCode());
            } else {
                existing.setRoleName(def.getRoleName());
                existing.setDescription(def.getDescription());
                roleRepository.save(existing);
            }
        }
    }

    private void initRolePermissions() {
        List<SysPermission> allPerms = permissionRepository.findAll();
        Map<String, SysPermission> permMap = new HashMap<>();
        for (SysPermission p : allPerms) {
            permMap.put(p.getPermissionCode(), p);
        }

        assignRolePerms("ADMIN", permMap, Arrays.asList(
                "admin-home", "user", "role", "permission", "log", "profile",
                "park", "device", "device-detail",
                "dashboard", "history", "simulator",
                "alert-rule", "alert-center"
        ));
        assignRolePerms("OPERATOR", permMap, Arrays.asList(
                "admin-home", "profile",
                "park", "device", "device-detail",
                "dashboard", "history", "simulator", "log",
                "alert-rule", "alert-center"
        ));
        assignRolePerms("VIEWER", permMap, Arrays.asList(
                "admin-home", "profile",
                "park", "device", "device-detail",
                "dashboard", "history",
                "alert-center"
        ));
    }

    private void assignRolePerms(String roleCode, Map<String, SysPermission> permMap, List<String> permCodes) {
        SysRole role = roleRepository.findByRoleCode(roleCode).orElse(null);
        if (role == null) {
            log.warn("角色 {} 不存在，跳过权限分配", roleCode);
            return;
        }

        rolePermissionRepository.deleteByRoleId(role.getId());

        for (String code : permCodes) {
            SysPermission perm = permMap.get(code);
            if (perm == null) {
                log.warn("权限 {} 不存在，跳过", code);
                continue;
            }
            if (!rolePermissionRepository.existsByRoleIdAndPermissionId(role.getId(), perm.getId())) {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(role.getId());
                rp.setPermissionId(perm.getId());
                rolePermissionRepository.save(rp);
            }
        }
        log.info("角色 {} 权限分配完成，共 {} 条", roleCode, permCodes.size());
    }

    private void initUserPasswords() {
        log.info("开始初始化默认用户密码...");
        String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);
        int count = 0;

        for (String username : DEFAULT_USERS) {
            SysUser user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                if (!passwordEncoder.matches(DEFAULT_PASSWORD, user.getPassword())) {
                    user.setPassword(encodedPassword);
                    userRepository.save(user);
                    log.info("用户 [{}] 密码已重置为默认密码", username);
                    count++;
                }
            } else {
                log.warn("用户 [{}] 不存在", username);
            }
        }

        if (count > 0) {
            log.info("共重置了 {} 个用户的密码", count);
        } else {
            log.info("默认用户密码均已正确，无需修改");
        }
    }

    private void initUserRoles() {
        assignUserRole("admin", "ADMIN");
        assignUserRole("operator", "OPERATOR");
        assignUserRole("viewer", "VIEWER");
    }

    private void assignUserRole(String username, String roleCode) {
        SysUser user = userRepository.findByUsername(username).orElse(null);
        SysRole role = roleRepository.findByRoleCode(roleCode).orElse(null);
        if (user == null || role == null) {
            log.warn("用户 {} 或角色 {} 不存在，跳过关联", username, roleCode);
            return;
        }
        if (!userRoleRepository.existsByUserIdAndRoleId(user.getId(), role.getId())) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(role.getId());
            userRoleRepository.save(ur);
            log.info("关联用户 {} -> 角色 {}", username, roleCode);
        }
    }
}
