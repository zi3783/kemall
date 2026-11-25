package com.kemall.iam.api.dto.auth;

import com.kemall.iam.api.enums.LogoutType;
import lombok.Data;

@Data
public class LogoutRequest {

    /**
     * CURRENT_DEVICE：仅当前设备（默认）
     * ALL_DEVICES：所有设备
     */
    private LogoutType logoutType = LogoutType.CURRENT_DEVICE;
}
