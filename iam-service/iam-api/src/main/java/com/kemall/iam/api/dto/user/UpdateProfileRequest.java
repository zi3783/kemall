package com.kemall.iam.api.dto.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(min = 1, max = 64, message = "昵称长度需在 1~64 之间")
    private String nickname;

    @Size(max = 512, message = "头像地址不能超过 512 个字符")
    @Pattern(regexp = "^https?://.+$", message = "头像地址必须是 http/https 链接")
    private String avatarUrl;
}
