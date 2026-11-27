package com.kemall.iam.api.dto.platform;

import com.kemall.iam.api.dto.permission.RoleInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 平台管理员成员信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformMemberInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String username;

    private String nickname;

    private Integer status;

    private LocalDateTime createdAt;

    /** 已分配的平台角色（仅详情接口返回） */
    private List<RoleInfo> roles;
}
