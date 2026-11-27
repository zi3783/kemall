package com.kemall.iam.api.dto.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 权限字典项。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String permCode;

    private String permName;

    private Integer permType;
}
