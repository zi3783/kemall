package com.kemall.iam.api.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {

    private Long userId;

    private String username;
}
