-- =============================================================================
-- IAM 权限中心测试数据（iam_db）
-- =============================================================================
-- 适用接口：
--   GET    /api/iam/v1/workspaces
--   GET    /api/iam/v1/workspaces/current
--   POST   /api/iam/v1/workspaces/switch
--   GET    /api/iam/v1/roles
--   GET    /api/iam/v1/roles/{roleId}
--   PATCH  /api/iam/v1/roles/{roleId}/permissions
--   GET    /api/iam/v1/permissions
--   GET    /api/iam/v1/permissions/me
--   POST   /api/iam/v1/permissions/refresh
--   PUT    /api/iam/v1/tenant-members/{memberId}/roles
--   PUT    /api/iam/v1/platform-members/{platformMemberId}/roles
--
-- 统一密码：Test@123456
-- 统一 BCrypt：$2a$10$ep3Wl2n0osDyac9JhKx.AOhFQPUzugh9MPM9Y8gdJw22UGiDgVbHu
--
-- 登录示例：
--   POST /api/iam/v1/auth/login
--   {"account":"alpha_tenant_admin","accountType":"USERNAME",
--    "password":"Test@123456","clientType":"PC_WEB","deviceId":"dev-1"}
--
-- 重要：登录后会话没有工作空间，必须先调用 /workspaces/switch 选择空间，
--       再测角色/权限接口；切换会返回新的 accessToken + refreshToken。
--
-- 测试矩阵（预期结果）：
--   1. platform_admin      ├ 平台空间：GET /roles 只看得到 2 个平台角色
--                          ├ 平台空间：可分配平台角色
--                          └ 读租户角色 9511 → 40321
--   2. platform_viewer     ├ 平台空间：GET /roles 返回 2 个
--                          └ PATCH 角色权限 → 403（无 iam:role:permission:update）
--   3. platform_norole     ├ GET /workspaces 不含平台空间
--                          └ switch PLATFORM → 40314
--   4. alpha_tenant_admin  ├ switch TENANT 9101 成功
--                          ├ GET /roles 返回 9511/9512/9521/9522（层级边界修复点）
--                          ├ 读组员角色 9531（租户 B）→ 40321
--                          ├ 分配平台角色 9501 → 40321
--                          ├ 分配租户角色 9511 给 member 9402 → 成功
--                          └ 读租户角色详情 → 成功
--   5. alpha_shop_mgr      ├ switch SHOP 9101/9201 成功
--                          ├ GET /roles 只返回 9521/9522
--                          ├ 读租户角色 9511 → 40321
--                          └ PUT 租户成员角色 → 40320（必须租户空间）
--   6. alpha_shop_reader   └ GET /permissions/me 只有 iam:role:list
--   7. alpha_tenant_operator └ GET /permissions/me 有 3 个编码（无分配权限）
--   8. alpha_disabled      └ switch SHOP 9101/9202 → 40311（成员停用）
--   9. beta_tenant_admin   ├ switch TENANT 9102 成功
--                          └ switch TENANT 9101 → 40310（跨租户）
--  10. gamma_tenant_admin  └ switch TENANT 9103 → 40312（租户冻结）
--  11. alpha_shop_mgr_a3   └ switch SHOP 9101/9203 → 40313（店铺停用）
-- =============================================================================

USE iam_db;

-- ============================ 0. 清理旧测试数据 ============================
-- 若曾通过注册接口创建过同名账号，一并清理，避免 uk_username 冲突
DELETE FROM user_identity
WHERE user_id IN (
    SELECT id FROM sys_user WHERE username IN (
        'platform_admin', 'platform_viewer', 'platform_norole',
        'alpha_tenant_admin', 'alpha_shop_mgr', 'alpha_shop_reader',
        'alpha_tenant_operator', 'alpha_disabled',
        'beta_tenant_admin', 'gamma_tenant_admin', 'alpha_shop_mgr_a3',
        'customer_only', 'customer_dual'
    )
);
DELETE FROM sys_user
WHERE username IN (
    'platform_admin', 'platform_viewer', 'platform_norole',
    'alpha_tenant_admin', 'alpha_shop_mgr', 'alpha_shop_reader',
    'alpha_tenant_operator', 'alpha_disabled',
    'beta_tenant_admin', 'gamma_tenant_admin', 'alpha_shop_mgr_a3',
    'customer_only', 'customer_dual'
);

DELETE FROM customer WHERE id BETWEEN 9351 AND 9399;

DELETE FROM platform_member_role WHERE id BETWEEN 9951 AND 9999;
DELETE FROM member_role WHERE id BETWEEN 9901 AND 9999;
DELETE FROM role_permission WHERE id BETWEEN 9801 AND 9999;
DELETE FROM platform_member WHERE id BETWEEN 9701 AND 9799;
DELETE FROM sys_permission WHERE id BETWEEN 9601 AND 9699;
DELETE FROM sys_role WHERE id BETWEEN 9501 AND 9599;
DELETE FROM tenant_member WHERE id BETWEEN 9401 AND 9499;
DELETE FROM user_identity WHERE user_id BETWEEN 9301 AND 9399;
DELETE FROM sys_user WHERE id BETWEEN 9301 AND 9399;
DELETE FROM shop WHERE id BETWEEN 9201 AND 9299;
DELETE FROM tenant WHERE id BETWEEN 9101 AND 9199;

-- ============================ 1. 租户 / 店铺 ============================
-- 9101 正常租户（两个可用店铺 + 一个停用店铺）
-- 9102 正常租户（用于跨租户隔离）
-- 9103 冻结租户（用于 40312）
INSERT INTO tenant (id, tenant_code, tenant_name, plan_code, status, expires_at) VALUES
(9101, 't-alpha', '阿尔法科技', 'PRO', 1, '2029-12-31 23:59:59.000'),
(9102, 't-beta',  '贝塔科技',   'BASIC', 1, '2029-12-31 23:59:59.000'),
(9103, 't-gamma', '伽马科技',   'BASIC', 2, '2029-12-31 23:59:59.000');

INSERT INTO shop (id, tenant_id, shop_code, shop_name, status) VALUES
(9201, 9101, 'shop-a1', '阿尔法旗舰店', 1),
(9202, 9101, 'shop-a2', '阿尔法二店',   1),
(9203, 9101, 'shop-a3', '阿尔法停用店', 2),
(9204, 9102, 'shop-b1', '贝塔店',       1);

-- ============================ 2. 账号 ============================
-- 密码统一 Test@123456（BCrypt, strength=10）
INSERT INTO sys_user (id, username, password_hash, nickname, status) VALUES
(9301, 'platform_admin',       '$2a$10$ep3Wl2n0osDyac9JhKx.AOhFQPUzugh9MPM9Y8gdJw22UGiDgVbHu', '平台超管',     1),
(9302, 'platform_viewer',      '$2a$10$ep3Wl2n0osDyac9JhKx.AOhFQPUzugh9MPM9Y8gdJw22UGiDgVbHu', '平台只读',     1),
(9303, 'platform_norole',      '$2a$10$ep3Wl2n0osDyac9JhKx.AOhFQPUzugh9MPM9Y8gdJw22UGiDgVbHu', '平台无角色',   1),
(9304, 'alpha_tenant_admin',   '$2a$10$ep3Wl2n0osDyac9JhKx.AOhFQPUzugh9MPM9Y8gdJw22UGiDgVbHu', '阿尔法租户管理员', 1),
(9305, 'alpha_shop_mgr',       '$2a$10$ep3Wl2n0osDyac9JhKx.AOhFQPUzugh9MPM9Y8gdJw22UGiDgVbHu', '阿尔法店铺管理员', 1),
(9306, 'alpha_shop_reader',    '$2a$10$ep3Wl2n0osDyac9JhKx.AOhFQPUzugh9MPM9Y8gdJw22UGiDgVbHu', '阿尔法店铺只读',   1),
(9307, 'alpha_tenant_operator','$2a$10$ep3Wl2n0osDyac9JhKx.AOhFQPUzugh9MPM9Y8gdJw22UGiDgVbHu', '阿尔法租户运营',   1),
(9308, 'alpha_disabled',       '$2a$10$ep3Wl2n0osDyac9JhKx.AOhFQPUzugh9MPM9Y8gdJw22UGiDgVbHu', '阿尔法停用成员',   1),
(9309, 'beta_tenant_admin',    '$2a$10$ep3Wl2n0osDyac9JhKx.AOhFQPUzugh9MPM9Y8gdJw22UGiDgVbHu', '贝塔租户管理员',   1),
(9310, 'gamma_tenant_admin',   '$2a$10$ep3Wl2n0osDyac9JhKx.AOhFQPUzugh9MPM9Y8gdJw22UGiDgVbHu', '伽马租户管理员',   1),
(9311, 'alpha_shop_mgr_a3',    '$2a$10$ep3Wl2n0osDyac9JhKx.AOhFQPUzugh9MPM9Y8gdJw22UGiDgVbHu', '停用店铺管理员',   1),
(9312, 'customer_only',        '$2a$10$ep3Wl2n0osDyac9JhKx.AOhFQPUzugh9MPM9Y8gdJw22UGiDgVbHu', '纯消费者',       1),
(9313, 'customer_dual',        '$2a$10$ep3Wl2n0osDyac9JhKx.AOhFQPUzugh9MPM9Y8gdJw22UGiDgVbHu', '消费者兼租户成员', 1);

-- ============================ 3. 租户 / 店铺成员 ============================
-- shop_id = 0 表示租户级成员；停用成员 status = 2
INSERT INTO tenant_member (id, tenant_id, shop_id, user_id, member_type, status) VALUES
(9401, 9101, 0,    9304, 3, 1),  -- alpha_tenant_admin：租户管理员
(9402, 9101, 9201, 9305, 2, 1),  -- alpha_shop_mgr：店铺 A1 员工
(9403, 9101, 9201, 9306, 2, 1),  -- alpha_shop_reader：店铺 A1 只读
(9404, 9101, 0,    9307, 2, 1),  -- alpha_tenant_operator：租户级运营
(9405, 9101, 9202, 9308, 2, 2),  -- alpha_disabled：店铺 A2 已停用
(9406, 9102, 0,    9309, 3, 1),  -- beta_tenant_admin
(9407, 9103, 0,    9310, 3, 1),  -- gamma_tenant_admin（租户本身冻结）
(9408, 9101, 9203, 9311, 2, 1),  -- alpha_shop_mgr_a3（店铺本身停用）
(9409, 9101, 0,    9313, 2, 1);  -- customer_dual：兼具租户成员身份

-- ============================ 4. 平台成员 ============================
-- 消费者身份（依赖 customer_table.sql 先执行）
INSERT INTO customer (id, user_id, status) VALUES
(9351, 9312, 1),  -- customer_only：纯消费者，登录后 workspaceRequired=false
(9352, 9313, 1);  -- customer_dual：消费者 + 租户成员，workspaceRequired=true

INSERT INTO platform_member (id, user_id, status) VALUES
(9701, 9301, 1),  -- platform_admin：有合规平台角色
(9702, 9302, 1),  -- platform_viewer：有平台只读角色
(9703, 9303, 1);  -- platform_norole：无平台角色 → 40314

-- ============================ 5. 角色 ============================
-- scope_type：0 平台 / 1 租户 / 2 店铺；平台角色 tenant_id = 0
INSERT INTO sys_role (id, tenant_id, role_code, role_name, scope_type, status) VALUES
-- 平台
(9501, 0,    'platform_super_admin', '平台超级管理员', 0, 1),
(9502, 0,    'platform_viewer',      '平台只读',       0, 1),
-- 租户 9101
(9511, 9101, 't_tenant_admin',       '租户管理员',     1, 1),
(9512, 9101, 't_tenant_operator',    '租户运营',       1, 1),
(9521, 9101, 't_shop_manager',       '店铺管理员',     2, 1),
(9522, 9101, 't_shop_viewer',        '店铺只读',       2, 1),
-- 租户 9102
(9531, 9102, 'b_tenant_admin',       '贝塔租户管理员', 1, 1),
(9541, 9102, 'b_shop_manager',       '贝塔店铺管理员', 2, 1),
-- 租户 9103（冻结）
(9551, 9103, 'g_tenant_admin',       '伽马租户管理员', 1, 1);

-- ============================ 6. 权限字典 ============================
INSERT INTO sys_permission (id, perm_code, perm_name, perm_type) VALUES
(9601, 'iam:role:list',              '角色列表',     1),
(9602, 'iam:role:read',              '角色详情',     1),
(9603, 'iam:role:permission:update', '角色权限维护', 1),
(9604, 'iam:permission:list',        '权限字典查询', 1),
(9605, 'iam:member:role:assign',     '租户成员角色分配', 1),
(9606, 'iam:platform:role:assign',   '平台成员角色分配', 1),
-- 租户与店铺管理（tenancy）
(9607, 'iam:tenant:list',            '租户列表',     1),
(9608, 'iam:tenant:read',            '租户详情',     1),
(9609, 'iam:tenant:create',          '创建租户',     1),
(9610, 'iam:tenant:status',          '租户状态管理', 1),
(9611, 'iam:shop:list',              '店铺列表',     1),
(9612, 'iam:shop:read',              '店铺详情',     1),
(9613, 'iam:shop:create',            '创建店铺',     1),
(9614, 'iam:shop:status',            '店铺状态管理', 1),
(9615, 'iam:member:list',            '成员列表',     1),
(9616, 'iam:member:add',             '成员加入',     1),
(9617, 'iam:member:status',          '成员禁用启用', 1),
(9618, 'iam:member:remove',          '成员移除',     1),
-- 平台管理员管理（platform）
(9619, 'iam:platform:member:list',   '平台成员列表',   1),
(9620, 'iam:platform:member:read',   '平台成员详情',   1),
(9621, 'iam:platform:member:create', '创建平台成员',   1),
(9622, 'iam:platform:member:status', '平台成员禁用启用', 1);

-- ============================ 7. 角色-权限 ============================
INSERT INTO role_permission (id, role_id, permission_id) VALUES
-- 平台超管：全部
(9801, 9501, 9601),
(9802, 9501, 9602),
(9803, 9501, 9603),
(9804, 9501, 9604),
(9805, 9501, 9605),
(9806, 9501, 9606),
-- 平台只读
(9807, 9502, 9601),
(9808, 9502, 9602),
(9809, 9502, 9604),
-- 阿尔法租户管理员
(9810, 9511, 9601),
(9811, 9511, 9602),
(9812, 9511, 9603),
(9813, 9511, 9604),
(9814, 9511, 9605),
-- 阿尔法租户运营
(9815, 9512, 9601),
(9816, 9512, 9602),
(9817, 9512, 9604),
-- 阿尔法店铺管理员
(9818, 9521, 9601),
(9819, 9521, 9602),
(9820, 9521, 9604),
-- 阿尔法店铺只读
(9821, 9522, 9601),
-- 贝塔租户管理员
(9822, 9531, 9601),
(9823, 9531, 9602),
(9824, 9531, 9604),
(9825, 9531, 9605),
-- 贝塔店铺管理员
(9826, 9541, 9601),
(9827, 9541, 9602),
-- 伽马租户管理员
(9828, 9551, 9601),
-- ===== 租户与店铺管理权限 =====
-- 平台超管：tenancy 全部
(9830, 9501, 9607),
(9831, 9501, 9608),
(9832, 9501, 9609),
(9833, 9501, 9610),
(9834, 9501, 9611),
(9835, 9501, 9612),
(9836, 9501, 9613),
(9837, 9501, 9614),
(9838, 9501, 9615),
(9839, 9501, 9616),
(9840, 9501, 9617),
(9841, 9501, 9618),
-- 平台只读
(9842, 9502, 9607),
(9843, 9502, 9608),
(9844, 9502, 9611),
(9845, 9502, 9612),
(9846, 9502, 9615),
-- 阿尔法租户管理员：本租户店铺 + 成员全部
(9847, 9511, 9608),
(9848, 9511, 9611),
(9849, 9511, 9612),
(9850, 9511, 9613),
(9851, 9511, 9614),
(9852, 9511, 9615),
(9853, 9511, 9616),
(9854, 9511, 9617),
(9855, 9511, 9618),
-- 阿尔法租户运营：只读
(9856, 9512, 9608),
(9857, 9512, 9611),
(9858, 9512, 9612),
(9859, 9512, 9615),
-- 阿尔法店铺管理员
(9860, 9521, 9612),
(9861, 9521, 9615),
-- 阿尔法店铺只读
(9862, 9522, 9612),
-- 贝塔租户管理员
(9863, 9531, 9608),
(9864, 9531, 9611),
(9865, 9531, 9612),
(9866, 9531, 9613),
(9867, 9531, 9614),
(9868, 9531, 9615),
(9869, 9531, 9616),
(9870, 9531, 9617),
(9871, 9531, 9618),
-- 贝塔店铺管理员
(9872, 9541, 9612),
(9873, 9541, 9615),
-- 伽马租户管理员
(9874, 9551, 9608),
-- ===== 平台管理员管理权限 =====
(9875, 9501, 9619),
(9876, 9501, 9620),
(9877, 9501, 9621),
(9878, 9501, 9622),
(9879, 9502, 9619),
(9880, 9502, 9620);

-- ============================ 8. 成员-角色 ============================
INSERT INTO member_role (id, tenant_id, member_id, role_id) VALUES
(9901, 9101, 9401, 9511),  -- alpha_tenant_admin  → 租户管理员
(9902, 9101, 9402, 9521),  -- alpha_shop_mgr      → 店铺管理员
(9903, 9101, 9403, 9522),  -- alpha_shop_reader   → 店铺只读
(9904, 9101, 9404, 9512),  -- alpha_tenant_operator → 租户运营
(9905, 9101, 9405, 9521),  -- alpha_disabled      → 店铺管理员（成员已停用）
(9906, 9102, 9406, 9531),  -- beta_tenant_admin   → 贝塔租户管理员
(9907, 9103, 9407, 9551),  -- gamma_tenant_admin  → 伽马租户管理员
(9908, 9101, 9408, 9521);  -- alpha_shop_mgr_a3   → 店铺管理员（店铺已停用）

-- ============================ 9. 平台成员-角色 ============================
INSERT INTO platform_member_role (id, platform_member_id, role_id) VALUES
(9951, 9701, 9501),  -- platform_admin  → 平台超管
(9952, 9702, 9502);  -- platform_viewer → 平台只读
-- 9703 platform_norole 不分配任何角色

-- ============================ 10. 数据自检 ============================
-- 应返回 3 条：平台 / alpha 租户级 / alpha 店铺级 均按预期
SELECT 'tenant' AS tbl, COUNT(*) AS cnt FROM tenant WHERE id BETWEEN 9101 AND 9199
UNION ALL SELECT 'shop', COUNT(*) FROM shop WHERE id BETWEEN 9201 AND 9299
UNION ALL SELECT 'sys_user', COUNT(*) FROM sys_user WHERE id BETWEEN 9301 AND 9399
UNION ALL SELECT 'tenant_member', COUNT(*) FROM tenant_member WHERE id BETWEEN 9401 AND 9499
UNION ALL SELECT 'platform_member', COUNT(*) FROM platform_member WHERE id BETWEEN 9701 AND 9799
UNION ALL SELECT 'sys_role', COUNT(*) FROM sys_role WHERE id BETWEEN 9501 AND 9599
UNION ALL SELECT 'sys_permission', COUNT(*) FROM sys_permission WHERE id BETWEEN 9601 AND 9699
UNION ALL SELECT 'role_permission', COUNT(*) FROM role_permission WHERE id BETWEEN 9801 AND 9899
UNION ALL SELECT 'member_role', COUNT(*) FROM member_role WHERE id BETWEEN 9901 AND 9999
UNION ALL SELECT 'platform_member_role', COUNT(*) FROM platform_member_role WHERE id BETWEEN 9951 AND 9999;

-- 预期角色可见范围（层级边界）：
--   平台空间（tenant_id=0, scope_type=0）        → 9501, 9502
--   alpha 租户空间（tenant_id=9101, scope_type>=1）→ 9511, 9512, 9521, 9522
--   alpha 店铺空间（tenant_id=9101, scope_type=2） → 9521, 9522
SELECT id, tenant_id, role_code, scope_type, status
FROM sys_role
WHERE id BETWEEN 9501 AND 9599
ORDER BY tenant_id, scope_type, role_code;
